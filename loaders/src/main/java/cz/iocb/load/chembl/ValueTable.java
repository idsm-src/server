package cz.iocb.load.chembl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import cz.iocb.load.common.Updater;



/**
 * A table of a multi-valued property, one row per value. The rows assembled from the RDF data are compared with the
 * stored ones: missing rows are inserted and stored rows without an assembled one are deleted.
 */
class ValueTable extends Updater
{
    /**
     * A column of the table.
     *
     * @param name the name of the column
     * @param enumType the enumeration type of the column, or null for an integer or varchar column
     */
    static record Column(String name, String enumType)
    {
    }


    /**
     * A row of the table, compared by the values of its columns.
     */
    static final class Value
    {
        /**
         * The values of the columns, in the order of the columns of the table.
         */
        private final Object[] values;

        /**
         * Creates the row.
         *
         * @param values the values of the columns
         */
        Value(Object[] values)
        {
            this.values = values;
        }


        /**
         * Returns the value of a column.
         *
         * @param index the index of the column
         * @return the value of the column
         */
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


    /**
     * A set of rows of the table, read from and written to the columns of the table.
     */
    @SuppressWarnings("serial")
    private class ValueSet extends SqlSet<Value>
    {
        /**
         * Creates an empty set.
         */
        private ValueSet()
        {
        }


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


    /**
     * The name of the table.
     */
    private final String table;

    /**
     * The columns of the table.
     */
    private final Column[] columns;

    /**
     * The rows assembled from the RDF data.
     */
    private final ValueSet values = new ValueSet();


    /**
     * Creates the table.
     *
     * @param table the name of the table
     * @param columns the columns of the table
     */
    ValueTable(String table, Column... columns)
    {
        this.table = table;
        this.columns = columns;
    }


    /**
     * Creates an integer or varchar column.
     *
     * @param name the name of the column
     * @return the column
     */
    static Column column(String name)
    {
        return new Column(name, null);
    }


    /**
     * Creates a column of an enumeration type.
     *
     * @param name the name of the column
     * @param enumType the enumeration type of the column
     * @return the column
     */
    static Column enumeration(String name, String enumType)
    {
        return new Column(name, enumType);
    }


    /**
     * Adds a row (integers and strings only); adding the same row again is allowed.
     *
     * @param value the values of the columns
     */
    void add(Object... value)
    {
        values.add(new Value(value));
    }


    /**
     * Returns whether a row has been added.
     *
     * @param value the values of the columns
     * @return true if the row has been added
     */
    boolean contains(Object... value)
    {
        return values.contains(new Value(value));
    }


    /**
     * Returns the added rows.
     *
     * @return the added rows
     */
    Iterable<Value> values()
    {
        return values;
    }


    /**
     * Returns the number of the added rows.
     *
     * @return the number of the added rows
     */
    int size()
    {
        return values.size();
    }


    /**
     * Brings the table in line with the added rows and forgets them.
     *
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Returns the statement parameter of a column, cast to the enumeration type of the column if it has one.
     *
     * @param column the column
     * @return the statement parameter of the column
     */
    private static String parameter(Column column)
    {
        return column.enumType == null ? "?" : "?::" + column.enumType;
    }
}
