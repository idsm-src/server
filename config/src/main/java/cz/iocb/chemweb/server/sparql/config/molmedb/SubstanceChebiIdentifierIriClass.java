package cz.iocb.chemweb.server.sparql.config.molmedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ConstantColumn;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.SQLRuntimeException;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.parser.model.triple.Node;



public class SubstanceChebiIdentifierIriClass extends SubstanceIdentifierIriClass
{
    protected SubstanceChebiIdentifierIriClass(String name)
    {
        super(name, "_CHEBI", "[0-9]+");
    }


    @Override
    public Column generateFunction(List<Column> cols)
    {
        String pref = prefix.replaceAll("'", "''");
        String delim = delimiter.replaceAll("'", "''");

        String access = String.format("(SELECT %s as \"@from\", %s as \"@to\" FROM %s) as \"@rctab\"", from, to, table);
        String code = String.format("'%s' || \"@to\" || '%s' || right(%s, -6)", pref, delim, cols.get(1));
        String expr = String.format("(SELECT (%s)::varchar FROM %s WHERE \"@from\" = %s)", code, access, cols.get(0));

        return new ExpressionColumn(expr);
    }


    @Override
    public Column generateInverseFunction2(Column parameter, boolean check)
    {
        StringBuilder builder = new StringBuilder();

        if(check)
        {
            builder.append("CASE WHEN sparql.regex_string(");
            builder.append(parameter);
            builder.append(", '^(");
            builder.append(regexp.replaceAll("'", "''"));
            builder.append(")$', '') AND ( ");

            String access = String.format("(SELECT %s as \"@from\", %s as \"@to\" FROM %s) as \"@rctab\"", from, to,
                    table);
            builder.append(String.format("SELECT \"@from\"::%s FROM %s WHERE \"@to\" = ", sqlTypes.get(0), access));

            String delim = delimiter.replaceAll("'", "''");
            builder.append(String.format("right(split_part(%s, '%s', 1), -%d)", parameter, delim, prefix.length()));

            builder.append(") IS NOT NULL THEN ");
        }

        String delim = delimiter.replaceAll("'", "''");
        builder.append(String.format("('CHEBI:' || split_part(%s, '%s', 2))", parameter, delim));

        if(check)
            builder.append(" END");

        return new ExpressionColumn(builder.toString());
    }




    @Override
    public List<Column> toColumns(Statement statement, Node node)
    {
        IRI iri = (IRI) node;
        assert match(statement, iri);

        try
        {
            String sql = sqlQuery.replaceAll("\\?", sanitizeString(iri.getValue()));

            try(ResultSet result = statement.executeQuery(sql))
            {
                if(result.next())
                {
                    String col0 = result.getString(1);
                    String col1 = "CHEBI:"
                            + iri.getValue().substring(iri.getValue().indexOf(delimiter) + delimiter.length());

                    return List.of(new ConstantColumn(col0, sqlTypes.get(0)),
                            new ConstantColumn(col1, sqlTypes.get(1)));
                }
                else
                {
                    throw new RuntimeException();
                }
            }
        }
        catch(SQLException e)
        {
            throw new SQLRuntimeException(e);
        }
    }
}
