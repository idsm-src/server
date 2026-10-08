package cz.iocb.load.chembl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import cz.iocb.load.common.Updater;



/*
 * A table of a multi-valued property, one row per value. The rows assembled from the RDF data are compared with the
 * stored ones: missing rows are inserted and stored rows without an assembled one are deleted.
 */
class ValueTable extends Updater
{
    static record Column(String name, String enumType)
    {
    }


    static final class Value
    {
        private final Object[] values;

        Value(Object[] values)
        {
            this.values = values;
        }


        Object get(int index)
        {
            return values[index];
        }


        @Override
        public boolean equals(Object object)
        {
            return object instanceof Value value && Arrays.equals(values, value.values);
        }


        @Override
        public int hashCode()
        {
            return Arrays.hashCode(values);
        }
    }


    @SuppressWarnings("serial")
    private class ValueSet extends SqlSet<Value>
    {
        @Override
        public Value get(ResultSet result) throws SQLException
        {
            Object[] values = new Object[columns.length];

            for(int i = 0; i < columns.length; i++)
                values[i] = result.getObject(i + 1);

            return new Value(values);
        }

        @Override
        public void set(PreparedStatement statement, Value value) throws SQLException
        {
            for(int i = 0; i < columns.length; i++)
                statement.setObject(i + 1, value.values[i],
                        value.values[i] instanceof Integer ? Types.INTEGER : Types.VARCHAR);
        }
    }


    private final String table;
    private final Column[] columns;
    private final ValueSet values = new ValueSet();


    ValueTable(String table, Column... columns)
    {
        this.table = table;
        this.columns = columns;
    }


    static Column column(String name)
    {
        return new Column(name, null);
    }


    static Column enumeration(String name, String enumType)
    {
        return new Column(name, enumType);
    }


    /*
     * Adds a value (integers and strings only); adding the same value again is allowed.
     */
    void add(Object... value)
    {
        values.add(new Value(value));
    }


    boolean contains(Object... value)
    {
        return values.contains(new Value(value));
    }


    Iterable<Value> values()
    {
        return values;
    }


    int size()
    {
        return values.size();
    }


    void store() throws SQLException
    {
        String names = Stream.of(columns).map(c -> c.enumType == null ? c.name : c.name + "::varchar")
                .collect(Collectors.joining(","));
        ValueSet oldValues = new ValueSet();

        try(PreparedStatement statement = connection.prepareStatement("select " + names + " from " + table))
        {
            statement.setFetchSize(100000);

            try(ResultSet result = statement.executeQuery())
            {
                while(result.next())
                {
                    Value value = oldValues.get(result);

                    if(!values.remove(value))
                        oldValues.add(value);
                }
            }
        }

        String conditions = Stream.of(columns).map(c -> c.name + "=" + parameter(c))
                .collect(Collectors.joining(" and "));
        String parameters = Stream.of(columns).map(c -> parameter(c)).collect(Collectors.joining(","));

        store("delete from " + table + " where " + conditions, oldValues);
        store("insert into " + table + "(" + Stream.of(columns).map(c -> c.name).collect(Collectors.joining(","))
                + ") values(" + parameters + ")", values);

        values.clear();
    }


    private static String parameter(Column column)
    {
        return column.enumType == null ? "?" : "?::" + column.enumType;
    }
}
