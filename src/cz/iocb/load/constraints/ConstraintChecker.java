package cz.iocb.load.constraints;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.load.common.Updater;



public class ConstraintChecker extends Updater
{
    private static Object buildTableAccess(Table table, String[] columns)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("select * from ");
        builder.append(table.getCode());
        builder.append(" where ");

        for(int c = 0; c < columns.length; c++)
        {
            if(c > 0)
                builder.append(" and ");

            builder.append(columns[c]);
            builder.append(" is not null");
        }

        return builder.toString();
    }


    private static String buildLabel(Table table, String[] columns)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(table.getSchema());
        builder.append(".");
        builder.append(table.getName());
        builder.append("(");

        for(int c = 0; c < columns.length; c++)
        {
            if(c > 0)
                builder.append(", ");

            builder.append(columns[c]);
        }

        builder.append(")");

        return builder.toString();
    }


    public static void checkAdditionalForeignKeys() throws SQLException, IOException
    {
        try(Statement statement = connection.createStatement())
        {
            try(ResultSet results = statement.executeQuery(
                    "select parent_schema, parent_table, parent_columns, foreign_schema, foreign_table, foreign_columns, __ from constraints.foreign_keys"))
            {
                while(results.next())
                {
                    Table leftTable = new Table(results.getString(1), results.getString(2));
                    String[] leftColumns = (String[]) results.getArray(3).getArray();

                    Table rightTable = new Table(results.getString(4), results.getString(5));
                    String[] rightColumns = (String[]) results.getArray(6).getArray();

                    if(leftColumns.length != rightColumns.length)
                        throw new RuntimeException();


                    StringBuilder builder = new StringBuilder();

                    builder.append("select distinct 1 from (");
                    builder.append(buildTableAccess(leftTable, leftColumns));
                    builder.append(") t1 right join (");
                    builder.append(buildTableAccess(rightTable, rightColumns));
                    builder.append(") t2 on (");

                    for(int c = 0; c < leftColumns.length; c++)
                    {
                        if(c > 0)
                            builder.append(" and ");

                        if(!leftColumns[c].startsWith("(") && !leftColumns[c].matches(".*::[_a-zA-Z0-9]+"))
                            builder.append("t1.");

                        builder.append(leftColumns[c]);

                        builder.append(" = ");

                        if(!rightColumns[c].startsWith("(") && !rightColumns[c].matches(".*::[_a-zA-Z0-9]+"))
                            builder.append("t2.");

                        builder.append(rightColumns[c]);
                    }

                    builder.append(") where ");

                    for(int c = 0; c < leftColumns.length; c++)
                    {
                        if(c > 0)
                            builder.append(" or ");

                        if(!leftColumns[c].startsWith("(") && !leftColumns[c].matches(".*::[_a-zA-Z0-9]+"))
                            builder.append("t1.");

                        builder.append(leftColumns[c]);

                        builder.append(" is null");
                    }

                    builder.append(" limit 1");

                    try(Statement s = connection.createStatement())
                    {
                        try(ResultSet r = s.executeQuery(builder.toString()))
                        {
                            if(r.next())
                                System.out.println(
                                        "warning: [" + results.getInt(5) + "] " + buildLabel(rightTable, rightColumns)
                                                + " not reference " + buildLabel(leftTable, leftColumns));
                        }
                    }
                }
            }

        }
    }


    public static void checkUnjoinableColumns() throws SQLException, IOException
    {
        try(Statement statement = connection.createStatement())
        {
            try(ResultSet results = statement.executeQuery(
                    "select left_schema, left_table, left_columns, right_schema, right_table, right_columns, __ from constraints.unjoinable_columns"))
            {
                while(results.next())
                {
                    Table leftTable = new Table(results.getString(1), results.getString(2));
                    String[] leftColumns = (String[]) results.getArray(3).getArray();

                    Table rightTable = new Table(results.getString(4), results.getString(5));
                    String[] rightColumns = (String[]) results.getArray(6).getArray();

                    if(leftColumns.length != rightColumns.length)
                        throw new RuntimeException();


                    StringBuilder builder = new StringBuilder();

                    builder.append("select distinct 1 from (");
                    builder.append(buildTableAccess(leftTable, leftColumns));
                    builder.append(") t1 inner join (");
                    builder.append(buildTableAccess(rightTable, rightColumns));
                    builder.append(") t2 on (");

                    for(int c = 0; c < leftColumns.length; c++)
                    {
                        if(c > 0)
                            builder.append(" and ");

                        if(!leftColumns[c].startsWith("(") && !leftColumns[c].matches(".*::[_a-zA-Z0-9]+"))
                            builder.append("t1.");

                        builder.append(leftColumns[c]);

                        builder.append(" = ");

                        if(!rightColumns[c].startsWith("(") && !rightColumns[c].matches(".*::[_a-zA-Z0-9]+"))
                            builder.append("t2.");

                        builder.append(rightColumns[c]);
                    }

                    builder.append(") limit 1");

                    try(Statement s = connection.createStatement())
                    {
                        try(ResultSet r = s.executeQuery(builder.toString()))
                        {
                            if(r.next())
                                System.out.println(
                                        "warning: [" + results.getInt(5) + "] " + buildLabel(leftTable, leftColumns)
                                                + " is joinable with " + buildLabel(rightTable, rightColumns));
                        }
                    }
                }
            }

        }
    }


    public static void check() throws SQLException, IOException
    {
        checkUnjoinableColumns();
        checkAdditionalForeignKeys();
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        init();
        check();
    }
}
