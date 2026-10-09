package cz.iocb.load.common;

import static java.util.stream.Collectors.joining;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.AbstractSet;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Stream;



/*
 * A table of the single-valued properties of entities, one row per entity. The rows are assembled from the data and
 * stored as a whole: an unchanged row is kept, a changed or new row is upserted, and a stored row without an assembled
 * one is deleted. The rows can be assembled by several threads at once.
 *
 * Once the described entities are assembled, the rows can be flushed to free their memory: they are stored at once
 * and only their keys are kept. An entity referenced after the flush gets a new row, and the final store adds these
 * rows and deletes the stored rows of the entities that are neither described nor referenced. A stored row without an
 * assembled one is remembered by its key and by whether it is a stub, which has no value except those determined by
 * the key, so that the final store can keep it unchanged if the entity is referenced again.
 */
public class EntityTable<K> extends Updater
{
    public static enum Type
    {
        INTEGER(Types.INTEGER), REAL(Types.REAL), DOUBLE(Types.DOUBLE), BOOLEAN(Types.BOOLEAN), VARCHAR(Types.VARCHAR);

        private final int sqlType;

        Type(int sqlType)
        {
            this.sqlType = sqlType;
        }


        Object read(ResultSet result, int index) throws SQLException
        {
            Object value = switch(this)
            {
                case INTEGER -> result.getInt(index);
                case REAL -> result.getFloat(index);
                case DOUBLE -> result.getDouble(index);
                case BOOLEAN -> result.getBoolean(index);
                case VARCHAR -> result.getString(index);
            };

            return result.wasNull() ? null : value;
        }


        void write(PreparedStatement statement, int index, Object value) throws SQLException
        {
            statement.setObject(index, value, sqlType);
        }


        Object check(Object value)
        {
            Class<?> type = switch(this)
            {
                case INTEGER -> Integer.class;
                case REAL -> Float.class;
                case DOUBLE -> Double.class;
                case BOOLEAN -> Boolean.class;
                case VARCHAR -> String.class;
            };

            return type.cast(value);
        }
    }


    /*
     * The strings of a pooled column are shared by the rows; a column of (nearly) all distinct values is not pooled. A
     * column with a cast holds the text form of a value of another SQL type, such as a date or an enumeration. The
     * values of a column with an equality test are compared by it instead of their equals method, so that a value
     * that differs from the stored one only in its form does not count as changed. A column determined by the key
     * holds the same value in every row of the key, such as the IRI of an entity identified by a surrogate id; a stub
     * row of a referenced entity carries it too.
     */
    public static record Column(String name, Type type, boolean pooled, String cast,
            BiPredicate<Object, Object> equality, boolean keyDetermined)
    {
        public Column unique()
        {
            return new Column(name, type, false, cast, equality, keyDetermined);
        }


        public Column comparedBy(BiPredicate<Object, Object> test)
        {
            return new Column(name, type, pooled, cast, test, keyDetermined);
        }


        public Column determinedByKey()
        {
            return new Column(name, type, pooled, cast, equality, true);
        }


        private boolean same(Object a, Object b)
        {
            return equality == null ? Objects.equals(a, b) : equality.test(a, b);
        }


        private String select()
        {
            return cast == null ? name : name + "::varchar";
        }


        private String parameter()
        {
            return cast == null ? "?" : "?::" + cast;
        }
    }


    /*
     * The key columns of a table and the conversion of their values.
     */
    public static abstract class Key<K>
    {
        private final String[] names;


        protected Key(String... names)
        {
            this.names = names;
        }


        /*
         * Reads the key from the first columns of the result.
         */
        protected abstract K read(ResultSet result) throws SQLException;


        /*
         * Sets the key to the first parameters of the statement.
         */
        protected abstract void write(PreparedStatement statement, K key) throws SQLException;


        /*
         * Creates an empty set for the keys of the flushed rows.
         */
        protected Set<K> createSet()
        {
            return new HashSet<>();
        }
    }


    /*
     * A set of integer keys that holds the non-negative ones as bits.
     */
    private static class IntKeySet extends AbstractSet<Integer>
    {
        private final BitSet bits = new BitSet();
        private final HashSet<Integer> negatives = new HashSet<>();

        @Override
        public boolean add(Integer key)
        {
            if(key < 0)
                return negatives.add(key);

            if(bits.get(key))
                return false;

            bits.set(key);
            return true;
        }

        @Override
        public boolean contains(Object key)
        {
            if(!(key instanceof Integer value))
                return false;

            return value < 0 ? negatives.contains(value) : bits.get(value);
        }

        @Override
        public int size()
        {
            return bits.cardinality() + negatives.size();
        }

        @Override
        public Iterator<Integer> iterator()
        {
            return Stream.concat(bits.stream().boxed(), negatives.stream()).iterator();
        }
    }


    @SuppressWarnings("serial")
    private class RowMap extends SqlMap<K, Object[]>
    {
        @Override
        public K getKey(ResultSet result) throws SQLException
        {
            return key.read(result);
        }

        @Override
        public Object[] getValue(ResultSet result) throws SQLException
        {
            Object[] row = new Object[columns.length];

            for(int i = 0; i < columns.length; i++)
                row[i] = columns[i].type.read(result, key.names.length + i + 1);

            return row;
        }

        @Override
        public void set(PreparedStatement statement, K id, Object[] value) throws SQLException
        {
            key.write(statement, id);

            for(int i = 0; i < columns.length; i++)
                columns[i].type.write(statement, key.names.length + i + 1, value[i]);
        }
    }


    @SuppressWarnings("serial")
    private class KeySet extends SqlSet<K>
    {
        @Override
        public K get(ResultSet result) throws SQLException
        {
            return key.read(result);
        }

        @Override
        public void set(PreparedStatement statement, K id) throws SQLException
        {
            key.write(statement, id);
        }
    }


    private final String table;
    private final Key<K> key;
    private final Column[] columns;
    private final HashMap<String, Integer> indexes = new HashMap<>();
    private final Integer described;
    private final RowMap rows = new RowMap();
    private final HashMap<String, String> strings = new HashMap<>();
    private Set<K> flushed;
    private Set<K> absent;
    private Set<K> absentStubs;
    private boolean stored;
    private int storedCount;
    private MissingEntities<K> missing;


    /*
     * The column 'described' (if any) is set for the entities described by the data, rows of entities that are only
     * referenced have it null.
     */
    public EntityTable(String table, Key<K> key, String described, Column... columns)
    {
        this.table = table;
        this.key = key;
        this.columns = columns;

        for(int i = 0; i < columns.length; i++)
            indexes.put(columns[i].name(), i);

        this.described = described == null ? null : index(described);
    }


    public static Key<Integer> intKey(String name)
    {
        return new Key<>(name)
        {
            @Override
            protected Integer read(ResultSet result) throws SQLException
            {
                return result.getInt(1);
            }

            @Override
            protected void write(PreparedStatement statement, Integer key) throws SQLException
            {
                statement.setInt(1, key);
            }

            @Override
            protected Set<Integer> createSet()
            {
                return new IntKeySet();
            }
        };
    }


    public static Key<String> stringKey(String name)
    {
        return new Key<>(name)
        {
            @Override
            protected String read(ResultSet result) throws SQLException
            {
                return result.getString(1);
            }

            @Override
            protected void write(PreparedStatement statement, String key) throws SQLException
            {
                statement.setString(1, key);
            }
        };
    }


    public static Key<Pair<Integer, Integer>> intPairKey(String first, String second)
    {
        return new Key<>(first, second)
        {
            @Override
            protected Pair<Integer, Integer> read(ResultSet result) throws SQLException
            {
                return Pair.getPair(result.getInt(1), result.getInt(2));
            }

            @Override
            protected void write(PreparedStatement statement, Pair<Integer, Integer> key) throws SQLException
            {
                statement.setInt(1, key.getOne());
                statement.setInt(2, key.getTwo());
            }
        };
    }


    public static Column integer(String name)
    {
        return new Column(name, Type.INTEGER, true, null, null, false);
    }


    public static Column real(String name)
    {
        return new Column(name, Type.REAL, true, null, null, false);
    }


    public static Column float8(String name)
    {
        return new Column(name, Type.DOUBLE, true, null, null, false);
    }


    public static Column bool(String name)
    {
        return new Column(name, Type.BOOLEAN, true, null, null, false);
    }


    public static Column varchar(String name)
    {
        return new Column(name, Type.VARCHAR, true, null, null, false);
    }


    public static Column uniqueVarchar(String name)
    {
        return new Column(name, Type.VARCHAR, false, null, null, false);
    }


    /*
     * A column of an SQL type whose values are kept in their text form, such as an enumeration.
     */
    public static Column typed(String name, String type)
    {
        return new Column(name, Type.VARCHAR, true, type, null, false);
    }


    public static Column date(String name)
    {
        return typed(name, "date");
    }


    private int index(String column)
    {
        Integer index = indexes.get(column);

        if(index == null)
            throw new IllegalArgumentException("unknown column " + column + " of " + table);

        return index;
    }


    private Object[] row(K id)
    {
        Object[] row = rows.get(id);

        if(row == null)
            rows.put(id, row = new Object[columns.length]);

        return row;
    }


    private void checkUnstored()
    {
        if(stored)
            throw new IllegalStateException(table + " has already been stored");
    }


    /*
     * A flushed row can no longer be read or changed.
     */
    private void checkUnflushed(K id)
    {
        if(flushed != null && flushed.contains(id))
            throw new IllegalStateException("the row of " + id + " in " + table + " has already been flushed");
    }


    /*
     * Ensures that a referenced entity has a row, which stays empty unless the entity is described; returns whether
     * the row has been created now.
     */
    public synchronized boolean reference(K id)
    {
        checkUnstored();

        if(rows.containsKey(id) || flushed != null && flushed.contains(id))
            return false;

        rows.put(id, new Object[columns.length]);
        return true;
    }


    /*
     * Sets a single-valued property; repeating the same value is allowed, a different second value is an error.
     */
    public synchronized void set(K id, String column, Object value) throws IOException
    {
        checkUnstored();
        checkUnflushed(id);

        int index = index(column);
        Object[] row = row(id);

        if(value instanceof String string && columns[index].pooled)
        {
            String pooled = strings.putIfAbsent(string, string);

            if(pooled != null)
                value = pooled;
        }

        if(row[index] == null)
            row[index] = columns[index].type.check(value);
        else if(!columns[index].same(row[index], value))
            throw new DataException("multiple values of " + table + "." + column,
                    id + ": " + row[index] + ", " + value);
    }


    public synchronized Object get(K id, String column)
    {
        checkUnstored();
        checkUnflushed(id);

        Object[] row = rows.get(id);
        return row == null ? null : row[index(column)];
    }


    public synchronized boolean contains(K id)
    {
        checkUnstored();

        return rows.containsKey(id) || flushed != null && flushed.contains(id);
    }


    /*
     * Returns the assembled rows, which are no longer available once flushed.
     */
    public Iterable<Entry<K, Object[]>> rows()
    {
        checkUnstored();

        if(flushed != null)
            throw new IllegalStateException(table + " has already been flushed");

        return rows.entrySet();
    }


    public int columnIndex(String column)
    {
        return index(column);
    }


    /*
     * Returns the number of the entities described by the data; without the column 'described', the number of all
     * entities.
     */
    public int size()
    {
        if(described == null)
            return storedCount + rows.size();

        return storedCount + (int) rows.values().stream().filter(r -> r[described] != null).count();
    }


    private boolean same(Object[] a, Object[] b)
    {
        for(int i = 0; i < columns.length; i++)
            if(!columns[i].same(a[i], b[i]))
                return false;

        return true;
    }


    /*
     * Tests whether a row is a stub, which has no value except those determined by the key.
     */
    private boolean isStub(Object[] values)
    {
        for(int i = 0; i < columns.length; i++)
            if(values[i] != null && !columns[i].keyDetermined)
                return false;

        return true;
    }


    /*
     * Counts the entities that have got their rows only because they are referenced; the summary shows them under the
     * name of the entity.
     */
    private void countMissing(String entity)
    {
        if(described == null)
            return;

        if(missing == null)
            missing = new MissingEntities<>(entity, false);

        for(Entry<K, Object[]> entry : rows.entrySet())
            if(entry.getValue()[described] == null)
                missing.referenced(entry.getKey());
    }


    /*
     * Compares a stored row with the assembled one, which is dropped if it is unchanged; returns whether the stored
     * row has no assembled one.
     */
    private boolean compare(K id, Object[] values)
    {
        Object[] row = rows.get(id);

        if(row == null)
            return true;

        if(same(row, values))
            rows.remove(id);

        return false;
    }


    /*
     * Compares the stored rows with the assembled ones; the stored rows without an assembled one are passed on.
     */
    private void compareStored(BiConsumer<K, Object[]> unmatched) throws SQLException
    {
        String keys = String.join(",", key.names);
        String selects = Stream.of(columns).map(Column::select).collect(joining(","));

        try(PreparedStatement statement = connection
                .prepareStatement("select " + keys + "," + selects + " from " + table))
        {
            statement.setFetchSize(100000);

            try(ResultSet result = statement.executeQuery())
            {
                while(result.next())
                {
                    K id = rows.getKey(result);
                    Object[] values = rows.getValue(result);

                    if(compare(id, values))
                        unmatched.accept(id, values);
                }
            }
        }
    }


    private void upsert() throws SQLException
    {
        String keys = String.join(",", key.names);
        String names = Stream.of(columns).map(Column::name).collect(joining(","));
        String parameters = Stream.of(key.names).map(name -> "?").collect(joining(","));
        String values = Stream.of(columns).map(Column::parameter).collect(joining(","));
        String updates = Stream.of(columns).map(c -> c.name() + "=EXCLUDED." + c.name()).collect(joining(","));

        store("insert into " + table + "(" + keys + "," + names + ") values(" + parameters + "," + values + ") "
                + "on conflict(" + keys + ") do update set " + updates, rows);
    }


    public void flush() throws SQLException
    {
        flush(null);
    }


    /*
     * Stores the assembled rows and keeps only their keys. A flushed row can no longer be read or changed, an entity
     * referenced later gets a new row that store() adds. The stored rows without an assembled one are kept until
     * store(), which deletes those of the entities that have not been referenced meanwhile and keeps a stub that is
     * referenced again by a stub.
     */
    public synchronized void flush(String entity) throws SQLException
    {
        checkUnstored();

        if(flushed != null)
            throw new IllegalStateException(table + " has already been flushed");

        storedCount = size();
        countMissing(entity);

        flushed = key.createSet();
        flushed.addAll(rows.keySet());
        absent = key.createSet();
        absentStubs = key.createSet();

        compareStored((id, values) -> {
            absent.add(id);

            if(isStub(values))
                absentStubs.add(id);
        });

        upsert();

        rows.clear();
        strings.clear();
    }


    public void store() throws SQLException
    {
        store(null);
    }


    /*
     * Stores the rows and deletes the stored rows of the entities that are neither described nor referenced; the
     * table cannot be used afterwards.
     */
    public synchronized void store(String entity) throws SQLException
    {
        checkUnstored();

        storedCount = size();
        countMissing(entity);

        KeySet deleted = new KeySet();

        if(flushed == null)
        {
            compareStored((id, values) -> deleted.add(id));
        }
        else
        {
            for(K id : absent)
            {
                Object[] row = rows.get(id);

                if(row == null)
                    deleted.add(id);
                else if(absentStubs.contains(id) && isStub(row))
                    rows.remove(id);
            }
        }

        String conditions = Stream.of(key.names).map(name -> name + "=?").collect(joining(" and "));

        store("delete from " + table + " where " + conditions, deleted);
        upsert();

        rows.clear();
        strings.clear();
        flushed = null;
        absent = null;
        absentStubs = null;
        stored = true;
    }
}
