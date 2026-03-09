package cz.iocb.chemweb.server.sparql.config.molmedb;

import static cz.iocb.chemweb.server.sparql.config.molmedb.MolmedbConfiguration.schema;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ConstantColumn;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.SQLRuntimeException;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.classes.ResultTag;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.parser.model.VariableOrBlankNode;
import cz.iocb.sparql.engine.parser.model.triple.Node;



public class SubstanceIdentifierIriClass extends UserIriClass
{
    protected final Table table = new Table(schema, "substance_bases");
    protected final TableColumn from = new TableColumn("id");
    protected final TableColumn to = new TableColumn("identifier");

    protected final String prefix = "https://rdf.molmedb.upol.cz/substance/";
    protected final String delimiter;
    protected final String sqlQuery;
    protected final String regexp;
    protected final Pattern pattern;


    protected SubstanceIdentifierIriClass(String name, String delimiter, String pattern)
    {
        super(name, List.of("integer", "varchar"), List.of(ResultTag.IRI));

        String delim = delimiter.replaceAll("'", "''");
        String code = String.format("right(split_part(?::varchar,'%s',1), -%d)", delim, prefix.length());

        this.sqlQuery = String.format("(SELECT %s::varchar FROM %s WHERE %s = %s)", from, table, to, code);
        this.delimiter = delimiter;
        this.regexp = String.format("%sMM[0-9.]+%s%s", Pattern.quote(prefix), Pattern.quote(delimiter), pattern);
        this.pattern = Pattern.compile(regexp);
    }


    public Column generateFunction(List<Column> cols)
    {
        String pref = prefix.replaceAll("'", "''");
        String delim = delimiter.replaceAll("'", "''");

        String access = String.format("(SELECT %s as \"@from\", %s as \"@to\" FROM %s) as \"@rctab\"", from, to, table);
        String code = String.format("'%s' || \"@to\" || '%s' || (%s)", pref, delim, cols.get(1));
        String expr = String.format("(SELECT (%s)::varchar FROM %s WHERE \"@from\" = %s)", code, access, cols.get(0));

        return new ExpressionColumn(expr);
    }


    public Column generateInverseFunction1(Column parameter, boolean check)
    {
        StringBuilder builder = new StringBuilder();

        if(check)
        {
            builder.append("CASE WHEN sparql.regex_string(");
            builder.append(parameter);
            builder.append(", '^(");
            builder.append(regexp.replaceAll("'", "''"));
            builder.append(")$', '') THEN ");
        }

        String access = String.format("(SELECT %s as \"@from\", %s as \"@to\" FROM %s) as \"@rctab\"", from, to, table);
        builder.append(String.format("(SELECT \"@from\"::%s FROM %s WHERE \"@to\" = ", sqlTypes.get(0), access));

        String delim = delimiter.replaceAll("'", "''");
        builder.append(String.format("right(split_part(%s, '%s', 1), -%d)", parameter, delim, prefix.length()));

        builder.append(")");

        if(check)
            builder.append(" END");

        return new ExpressionColumn(builder.toString());
    }


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
        builder.append(String.format("split_part(%s, '%s', 2)", parameter, delim));

        if(check)
            builder.append(" END");

        return new ExpressionColumn(builder.toString());
    }


    @Override
    public boolean match(Statement statement, IRI iri)
    {
        Matcher matcher = pattern.matcher(iri.getValue());

        if(!matcher.matches())
            return false;

        try
        {
            String sql = sqlQuery.replaceAll("\\?", sanitizeString(iri.getValue()));

            try(ResultSet result = statement.executeQuery(sql))
            {
                return result.next();
            }
        }
        catch(SQLException e)
        {
            throw new SQLRuntimeException(e);
        }
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
                    String col1 = iri.getValue().substring(iri.getValue().indexOf(delimiter) + delimiter.length());

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


    @Override
    public int getCheckCost()
    {
        return 1;
    }


    @Override
    public String getPrefix(List<Column> columns)
    {
        return prefix;
    }


    @Override
    public List<Column> fromGeneralClass(List<Column> columns)
    {
        return List.of(generateInverseFunction1(columns.get(0), true), generateInverseFunction2(columns.get(0), true));
    }


    @Override
    public List<Column> toGeneralClass(List<Column> columns, boolean check)
    {
        return List.of(generateFunction(columns));
    }


    @Override
    public List<Column> fromExpression(Column column)
    {
        return List.of(generateInverseFunction1(column, true), generateInverseFunction2(column, true));
    }


    @Override
    public Column toExpression(List<Column> columns)
    {
        return generateFunction(columns);
    }


    @Override
    public List<Column> fromBoxedExpression(Column column, boolean check)
    {
        return List.of(generateInverseFunction1(new ExpressionColumn("sparql.rdfbox_get_iri(" + column + ")"), check),
                generateInverseFunction2(new ExpressionColumn("sparql.rdfbox_get_iri(" + column + ")"), check));
    }


    @Override
    public Column toBoxedExpression(List<Column> columns)
    {
        return new ExpressionColumn("sparql.rdfbox_create_from_iri(" + generateFunction(columns) + ")");
    }


    @Override
    public List<Column> toResult(List<Column> columns)
    {
        return List.of(generateFunction(columns));
    }


    @Override
    public boolean match(Statement statement, Node node)
    {
        return switch(node)
        {
            case VariableOrBlankNode var -> true;
            case IRI iri -> match(statement, iri);
            default -> false;
        };
    }
}
