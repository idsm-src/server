package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import cz.iocb.pubchem.load.common.Updater;



public class ConstraintChecker extends Updater
{
    private static Object buildTableAccess(String table, String[] columns)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("select * from ");
        builder.append(table);
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


    private static String buildLabel(String table, String[] columns)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(table);
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


    public static void checkAditionalForeignKeys() throws SQLException, IOException
    {
        try(Statement statement = connection.createStatement())
        {
            try(ResultSet results = statement.executeQuery(
                    "select parent_table, parent_columns, foreign_table, foreign_columns from schema_foreign_keys"))
            {
                while(results.next())
                {
                    String leftTable = results.getString(1);
                    String[] leftColumns = (String[]) results.getArray(2).getArray();

                    String rightTable = results.getString(3);
                    String[] rightColumns = (String[]) results.getArray(4).getArray();

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
                                System.out.println("warning: " + buildLabel(rightTable, rightColumns)
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
                    "select left_table, left_columns, right_table, right_columns from schema_unjoinable_columns"))
            {
                while(results.next())
                {
                    String leftTable = results.getString(1);
                    String[] leftColumns = (String[]) results.getArray(2).getArray();

                    String rightTable = results.getString(3);
                    String[] rightColumns = (String[]) results.getArray(4).getArray();

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
                                System.out.println("warning: " + buildLabel(leftTable, leftColumns)
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
        checkAditionalForeignKeys();
    }
}
