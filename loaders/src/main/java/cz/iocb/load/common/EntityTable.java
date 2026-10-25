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



/**
 * A table of the single-valued properties of entities, one row per entity. The rows are assembled from the data and
 * stored as a whole: an unchanged row is kept, a changed or new row is upserted, and a stored row without an assembled
 * one is deleted. The rows can be assembled by several threads at once.
 *
 * <p>
 * Once the described entities are assembled, the rows can be flushed to free their memory: they are stored at once
 * and only their keys are kept. An entity referenced after the flush gets a new row, and the final store adds these
 * rows and deletes the stored rows of the entities that are neither described nor referenced. A stored row without an
 * assembled one is remembered by its key and by whether it is a stub, which has no value except those determined by
 * the key, so that the final store can keep it unchanged if the entity is referenced again.
 *
 * @param <K> the type of the keys of the entities
 */
public class EntityTable<K> extends Updater
{
    /**
     * Type of the values of a column and their transfer from and to the database.
     */
    public static enum Type
    {
        /**
         * SQL {@code integer}, held as {@link Integer}.
         */
        INTEGER(Types.INTEGER),

        /**
         * SQL {@code real}, held as {@link Float}.
         */
        REAL(Types.REAL),

        /**
         * SQL {@code double precision}, held as {@link Double}.
         */
        DOUBLE(Types.DOUBLE),

        /**
         * SQL {@code boolean}, held as {@link Boolean}.
         */
        BOOLEAN(Types.BOOLEAN),

        /**
         * SQL {@code varchar}, or the text form of a value of another SQL type, held as {@link String}.
         */
        VARCHAR(Types.VARCHAR);

        /**
         * JDBC type code of the values.
         */
        private final int sqlType;

        /**
         * Creates the type.
         *
         * @param sqlType the JDBC type code of the values
         */
        Type(int sqlType)
        {
            this.sqlType = sqlType;
        }


        /**
         * Reads a value of the type from a column of a result.
         *
         * @param result the result
         * @param index the index of the column
         * @return the value, or null for an SQL null
         * @throws SQLException if a database error occurs
         */
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


        /**
         * Sets a value of the type to a parameter of a statement.
         *
         * @param statement the statement
         * @param index the index of the parameter
         * @param value the value, or null
         * @throws SQLException if a database error occurs
         */
        void write(PreparedStatement statement, int index, Object value) throws SQLException
        {
            statement.setObject(index, value, sqlType);
        }


        /**
         * Checks that a value is held by the Java class of the type; fails with a {@link ClassCastException}
         * otherwise.
         *
         * @param value the value
         * @return the value
         */
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


    /**
     * A value column of a table. The strings of a pooled column are shared by the rows; a column of (nearly) all
     * distinct values is not pooled. A column with a cast holds the text form of a value of another SQL type, such as
     * a date or an enumeration. The values of a column with an equality test are compared by it instead of their
     * equals method, so that a value that differs from the stored one only in its form does not count as changed. A
     * column determined by the key holds the same value in every row of the key, such as the IRI of an entity
     * identified by a surrogate id; a stub row of a referenced entity carries it too.
     *
     * @param name the name of the column
     * @param type the type of the values
     * @param pooled whether the strings of the column are shared by the rows
     * @param cast the SQL type whose values the column holds in their text form, or null
     * @param equality the equality test of the values, or null for their equals method
     * @param keyDetermined whether the column is determined by the key
     */
    public static record Column(String name, Type type, boolean pooled, String cast,
            BiPredicate<Object, Object> equality, boolean keyDetermined)
    {
        /**
         * Returns the column without the pooling of its strings.
         *
         * @return the column that is not pooled
         */
        public Column unique()
        {
            return new Column(name, type, false, cast, equality, keyDetermined);
        }


        /**
         * Returns the column with an equality test of its values.
         *
         * @param test the equality test
         * @return the column compared by the test
         */
        public Column comparedBy(BiPredicate<Object, Object> test)
        {
            return new Column(name, type, pooled, cast, test, keyDetermined);
        }


        /**
         * Returns the column as determined by the key.
         *
         * @return the column determined by the key
         */
        public Column determinedByKey()
        {
            return new Column(name, type, pooled, cast, equality, true);
        }


        /**
         * Tests whether two values of the column are the same.
         *
         * @param a the first value, or null
         * @param b the second value, or null
         * @return true if the values are the same
         */
        private boolean same(Object a, Object b)
        {
            return equality == null ? Objects.equals(a, b) : equality.test(a, b);
        }


        /**
         * Returns the expression that reads the column.
         *
         * @return the name of the column, cast to {@code varchar} for a column with a cast
         */
        private String select()
        {
            return cast == null ? name : name + "::varchar";
        }


        /**
         * Returns the parameter that writes the column.
         *
         * @return the parameter placeholder, cast to the SQL type for a column with a cast
         */
        private String parameter()
        {
            return cast == null ? "?" : "?::" + cast;
        }
    }


    /**
     * The key columns of a table and the conversion of their values.
     *
     * @param <K> the type of the keys
     */
    public static abstract class Key<K>
    {
        /**
         * Names of the key columns.
         */
        private final String[] names;


        /**
         * Creates the key of the columns.
         *
         * @param names the names of the key columns
         */
        protected Key(String... names)
        {
            this.names = names;
        }


        /**
         * Reads the key from the first columns of the result.
         *
         * @param result the result
         * @return the key
         * @throws SQLException if a database error occurs
         */
        protected abstract K read(ResultSet result) throws SQLException;


        /**
         * Sets the key to the first parameters of the statement.
         *
         * @param statement the statement
         * @param key the key
         * @throws SQLException if a database error occurs
         */
        protected abstract void write(PreparedStatement statement, K key) throws SQLException;


        /**
         * Creates an empty set for the keys of the flushed rows.
         *
         * @return the empty set
         */
        protected Set<K> createSet()
        {
            return new HashSet<>();
        }
    }


    /**
     * A set of integer keys that holds the non-negative ones as bits.
     */
    private static class IntKeySet extends AbstractSet<Integer>
    {
        /**
         * Non-negative keys.
         */
        private final BitSet bits = new BitSet();

        /**
         * Negative keys.
         */
        private final HashSet<Integer> negatives = new HashSet<>();

        /**
         * Creates an empty set.
         */
        private IntKeySet()
        {
        }

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


    /**
     * Rows by their keys, read from and written to the key columns followed by the value columns.
     */
    @SuppressWarnings("serial")
    private class RowMap extends SqlMap<K, Object[]>
    {
        /**
         * Creates an empty map.
         */
        private RowMap()
        {
        }

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


    /**
     * Keys of rows, read from and written to the key columns.
     */
    @SuppressWarnings("serial")
    private class KeySet extends SqlSet<K>
    {
        /**
         * Creates an empty set.
         */
        private KeySet()
        {
        }

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


    /**
     * Qualified name of the table.
     */
    private final String table;

    /**
     * Key columns of the table.
     */
    private final Key<K> key;

    /**
     * Value columns of the table.
     */
    private final Column[] columns;

    /**
     * Index of each value column by its name.
     */
    private final HashMap<String, Integer> indexes = new HashMap<>();

    /**
     * Index of the column set for the entities described by the data, or null if there is none.
     */
    private final Integer described;

    /**
     * Assembled rows by their keys.
     */
    private final RowMap rows = new RowMap();

    /**
     * Pool of the strings of the pooled columns.
     */
    private final HashMap<String, String> strings = new HashMap<>();

    /**
     * Keys of the flushed rows, or null if the table has not been flushed.
     */
    private Set<K> flushed;

    /**
     * Keys of the stored rows that had no assembled row at the flush, or null if the table has not been flushed.
     */
    private Set<K> absent;

    /**
     * Keys of the stubs among {@link #absent}, or null if the table has not been flushed.
     */
    private Set<K> absentStubs;

    /**
     * Whether the table has been stored and can no longer be used.
     */
    private boolean stored;

    /**
     * Number of the entities of the flushed rows that {@link #size()} counts.
     */
    private int storedCount;

    /**
     * Counter of the entities that have got their rows only because they are referenced, or null until it is needed.
     */
    private MissingEntities<K> missing;


    /**
     * Creates the table. The column {@code described} (if any) is set for the entities described by the data, rows of
     * entities that are only referenced have it null.
     *
     * @param table the qualified name of the table
     * @param key the key columns
     * @param described the name of the column set for the described entities, or null
     * @param columns the value columns
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


    /**
     * Returns the key of one integer column; the keys of the flushed rows are held as bits.
     *
     * @param name the name of the key column
     * @return the key
     */
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


    /**
     * Returns the key of one {@code varchar} column.
     *
     * @param name the name of the key column
     * @return the key
     */
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


    /**
     * Returns the key of two integer columns.
     *
     * @param first the name of the first key column
     * @param second the name of the second key column
     * @return the key
     */
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


    /**
     * Returns an {@code integer} column.
     *
     * @param name the name of the column
     * @return the column
     */
    public static Column integer(String name)
    {
        return new Column(name, Type.INTEGER, true, null, null, false);
    }


    /**
     * Returns a {@code real} column.
     *
     * @param name the name of the column
     * @return the column
     */
    public static Column real(String name)
    {
        return new Column(name, Type.REAL, true, null, null, false);
    }


    /**
     * Returns a {@code double precision} column.
     *
     * @param name the name of the column
     * @return the column
     */
    public static Column float8(String name)
    {
        return new Column(name, Type.DOUBLE, true, null, null, false);
    }


    /**
     * Returns a {@code boolean} column.
     *
     * @param name the name of the column
     * @return the column
     */
    public static Column bool(String name)
    {
        return new Column(name, Type.BOOLEAN, true, null, null, false);
    }


    /**
     * Returns a {@code varchar} column whose strings are pooled.
     *
     * @param name the name of the column
     * @return the column
     */
    public static Column varchar(String name)
    {
        return new Column(name, Type.VARCHAR, true, null, null, false);
    }


    /**
     * Returns a {@code varchar} column of (nearly) all distinct values, which are not pooled.
     *
     * @param name the name of the column
     * @return the column
     */
    public static Column uniqueVarchar(String name)
    {
        return new Column(name, Type.VARCHAR, false, null, null, false);
    }


    /**
     * A column of an SQL type whose values are kept in their text form, such as an enumeration.
     *
     * @param name the name of the column
     * @param type the SQL type of the column
     * @return the column
     */
    public static Column typed(String name, String type)
    {
        return new Column(name, Type.VARCHAR, true, type, null, false);
    }


    /**
     * Returns a {@code date} column, whose values are kept in their text form.
     *
     * @param name the name of the column
     * @return the column
     */
    public static Column date(String name)
    {
        return typed(name, "date");
    }


    /**
     * Returns the index of a value column; fails for an unknown column.
     *
     * @param column the name of the column
     * @return the index of the column
     */
    private int index(String column)
    {
        Integer index = indexes.get(column);

        if(index == null)
            throw new IllegalArgumentException("unknown column " + column + " of " + table);

        return index;
    }


    /**
     * Returns the assembled row of an entity, which is created if it does not exist.
     *
     * @param id the key of the entity
     * @return the row
     */
    private Object[] row(K id)
    {
        Object[] row = rows.get(id);

        if(row == null)
            rows.put(id, row = new Object[columns.length]);

        return row;
    }


    /**
     * Fails if the table has already been stored.
     */
    private void checkUnstored()
    {
        if(stored)
            throw new IllegalStateException(table + " has already been stored");
    }


    /**
     * Fails if the row of an entity has already been flushed: a flushed row can no longer be read or changed.
     *
     * @param id the key of the entity
     */
    private void checkUnflushed(K id)
    {
        if(flushed != null && flushed.contains(id))
            throw new IllegalStateException("the row of " + id + " in " + table + " has already been flushed");
    }


    /**
     * Ensures that a referenced entity has a row, which stays empty unless the entity is described; returns whether
     * the row has been created now.
     *
     * @param id the key of the entity
     * @return true if the row has been created now
     */
    public synchronized boolean reference(K id)
    {
        checkUnstored();

        if(rows.containsKey(id) || flushed != null && flushed.contains(id))
            return false;

        rows.put(id, new Object[columns.length]);
        return true;
    }


    /**
     * Sets a single-valued property; repeating the same value is allowed, a different second value is an error.
     *
     * @param id the key of the entity
     * @param column the name of the column
     * @param value the value
     * @throws IOException if the property already has a different value (a {@link DataException})
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


    /**
     * Returns a value of the assembled row of an entity.
     *
     * @param id the key of the entity
     * @param column the name of the column
     * @return the value, or null if the entity has no row or the row no value
     */
    public synchronized Object get(K id, String column)
    {
        checkUnstored();
        checkUnflushed(id);

        Object[] row = rows.get(id);
        return row == null ? null : row[index(column)];
    }


    /**
     * Tests whether an entity has a row, assembled or flushed.
     *
     * @param id the key of the entity
     * @return true if the entity has a row
     */
    public synchronized boolean contains(K id)
    {
        checkUnstored();

        return rows.containsKey(id) || flushed != null && flushed.contains(id);
    }


    /**
     * Returns the assembled rows, which are no longer available once flushed.
     *
     * @return the assembled rows by their keys
     */
    public Iterable<Entry<K, Object[]>> rows()
    {
        checkUnstored();

        if(flushed != null)
            throw new IllegalStateException(table + " has already been flushed");

        return rows.entrySet();
    }


    /**
     * Returns the index of a value column in the rows.
     *
     * @param column the name of the column
     * @return the index of the column
     */
    public int columnIndex(String column)
    {
        return index(column);
    }


    /**
     * Returns the number of the entities described by the data; without the column {@code described}, the number of
     * all entities.
     *
     * @return the number of the entities
     */
    public int size()
    {
        if(described == null)
            return storedCount + rows.size();

        return storedCount + (int) rows.values().stream().filter(r -> r[described] != null).count();
    }


    /**
     * Tests whether two rows hold the same values.
     *
     * @param a the first row
     * @param b the second row
     * @return true if the rows hold the same values
     */
    private boolean same(Object[] a, Object[] b)
    {
        for(int i = 0; i < columns.length; i++)
            if(!columns[i].same(a[i], b[i]))
                return false;

        return true;
    }


    /**
     * Tests whether a row is a stub, which has no value except those determined by the key.
     *
     * @param values the values of the row
     * @return true if the row is a stub
     */
    private boolean isStub(Object[] values)
    {
        for(int i = 0; i < columns.length; i++)
            if(values[i] != null && !columns[i].keyDetermined)
                return false;

        return true;
    }


    /**
     * Counts the entities that have got their rows only because they are referenced; the summary shows them under the
     * name of the entity.
     *
     * @param entity the name of the entity
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


    /**
     * Compares a stored row with the assembled one, which is dropped if it is unchanged; returns whether the stored
     * row has no assembled one.
     *
     * @param id the key of the row
     * @param values the values of the stored row
     * @return true if the stored row has no assembled one
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


    /**
     * Compares the stored rows with the assembled ones; the stored rows without an assembled one are passed on.
     *
     * @param unmatched the receiver of the keys and the values of the stored rows without an assembled one
     * @throws SQLException if a database error occurs
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


    /**
     * Inserts the assembled rows, or updates the stored rows of their keys.
     *
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Flushes the rows of a table without the column {@code described}; see {@link #flush(String)}.
     *
     * @throws SQLException if a database error occurs
     */
    public void flush() throws SQLException
    {
        flush(null);
    }


    /**
     * Stores the assembled rows and keeps only their keys. A flushed row can no longer be read or changed, an entity
     * referenced later gets a new row that {@link #store(String)} adds. The stored rows without an assembled one are
     * kept until the store, which deletes those of the entities that have not been referenced meanwhile and keeps a
     * stub that is referenced again by a stub.
     *
     * @param entity the name under which the summary counts the entities that are only referenced, or null for a table
     *            without the column {@code described}
     * @throws SQLException if a database error occurs
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


    /**
     * Stores the rows of a table without the column {@code described}; see {@link #store(String)}.
     *
     * @throws SQLException if a database error occurs
     */
    public void store() throws SQLException
    {
        store(null);
    }


    /**
     * Stores the rows and deletes the stored rows of the entities that are neither described nor referenced; the
     * table cannot be used afterwards.
     *
     * @param entity the name under which the summary counts the entities that are only referenced, or null for a table
     *            without the column {@code described}
     * @throws SQLException if a database error occurs
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
