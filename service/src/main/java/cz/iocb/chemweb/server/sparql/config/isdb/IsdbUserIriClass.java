package cz.iocb.chemweb.server.sparql.config.isdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ConstantColumn;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.SQLRuntimeException;
import cz.iocb.sparql.engine.mapping.classes.ResultTag;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.parser.model.VariableOrBlankNode;
import cz.iocb.sparql.engine.parser.model.triple.Node;



public class IsdbUserIriClass extends UserIriClass
{
    private final Pattern pattern;
    private final String regexp;
    private final String prefix;
    private final String suffix;
    private final String sqlQuery;
    private final int prefixLen;
    private final int suffixLen;


    public IsdbUserIriClass(String name, String prefix, String suffix)
    {
        super(name, List.of("integer", "char"), List.of(ResultTag.IRI));

        this.prefix = prefix;
        this.suffix = suffix;
        this.prefixLen = prefix.length();
        this.suffixLen = suffix != null ? suffix.length() : 0;
        this.sqlQuery = "select id::varchar from isdb.compound_bases where accession = ?";

        //FIXME: check whether the pattern is valid also in pcre2
        this.regexp = Pattern.quote(prefix) + "([A-Z]{14}-[NP])" + (suffix != null ? Pattern.quote(suffix) : "");
        this.pattern = Pattern.compile(regexp);
    }


    public IsdbUserIriClass(String name, String prefix)
    {
        this(name, prefix, null);
    }


    @Override
    public List<Column> toColumns(Statement statement, Node node)
    {
        IRI iri = (IRI) node;
        assert match(statement, iri);

        try
        {
            String sql = sqlQuery.replaceAll("\\?",
                    sanitizeString(iri.getValue().substring(prefixLen, prefixLen + 14)));

            try(ResultSet result = statement.executeQuery(sql))
            {
                if(result.next())
                {
                    return List.of(new ConstantColumn(result.getString(1), "integer"),
                            new ConstantColumn(iri.getValue().substring(prefix.length() + 15), "char"));
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
    public boolean match(Statement statement, IRI iri)
    {
        Matcher matcher = pattern.matcher(iri.getValue());

        if(!matcher.matches())
            return false;

        try
        {
            String sql = sqlQuery.replaceAll("\\?",
                    sanitizeString(iri.getValue().substring(prefixLen, prefixLen + 14)));

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
    public int getCheckCost()
    {
        return 1;
    }


    protected Column generateFunction(List<Column> columns)
    {
        String access = "(SELECT id as \"@from\", accession as \"@to\" FROM isdb.compound_bases) as \"@rctab\"";

        String code = String.format("'%s' || \"@to\" || '-' || %s", prefix.replaceAll("'", "''"), columns.get(1),
                columns.get(1).toString());

        if(suffix != null)
            code = String.format("%s || '%s'", code, suffix.replaceAll("'", "''"));

        code = String.format("(SELECT (%s)::varchar FROM %s WHERE \"@from\" = %s)", code, access, columns.get(0));

        return new ExpressionColumn(code);
    }


    protected List<Column> generateInverseFunctions(Column parameter, boolean check)
    {
        String access = "(SELECT id as \"@from\", accession as \"@to\" FROM isdb.compound_bases) as \"@rctab\"";
        String col1 = String.format(
                "(SELECT \"@from\"::integer FROM %s WHERE \"@to\" = substring(%s, %d, 14)::varchar)", access, parameter,
                prefixLen + 1);
        String col2 = String.format("right(%s, %d)::char", parameter.toString(), suffixLen + 1);

        if(check)
        {
            StringBuilder builder = new StringBuilder();

            builder.append("CASE WHEN sparql.regex_string(");
            builder.append(parameter);
            builder.append(", '^(");
            builder.append(regexp.replaceAll("'", "''"));
            builder.append(")$', '') THEN ");

            col1 = builder.toString() + col1 + " END";
            col2 = builder.toString() + col2 + " END";
        }

        List<Column> result = new ArrayList<Column>(getColumnCount());

        result.add(new ExpressionColumn(col1));
        result.add(new ExpressionColumn(col2));

        return result;
    }


    @Override
    public List<Column> fromGeneralClass(List<Column> columns)
    {
        return generateInverseFunctions(columns.get(0), true);
    }


    @Override
    public List<Column> toGeneralClass(List<Column> columns, boolean check)
    {
        return List.of(generateFunction(columns));
    }


    @Override
    public List<Column> fromExpression(Column column)
    {
        return generateInverseFunctions(column, true);
    }


    @Override
    public Column toExpression(List<Column> columns)
    {
        return generateFunction(columns);
    }


    @Override
    public List<Column> fromBoxedExpression(Column column, boolean check)
    {
        return generateInverseFunctions(new ExpressionColumn("sparql.rdfbox_get_iri(" + column + ")"), check);
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
    public String getPrefix(List<Column> columns)
    {
        return prefix;
    }


    @Override
    public boolean match(Statement statement, Node node)
    {
        if(node instanceof VariableOrBlankNode)
            return true;

        if(!(node instanceof IRI))
            return false;

        return match(statement, (IRI) node);
    }


    @Override
    public boolean equals(Object object)
    {
        if(object == this)
            return true;

        if(!super.equals(object))
            return false;

        IsdbUserIriClass other = (IsdbUserIriClass) object;

        if(!prefix.equals(other.prefix))
            return false;

        return true;
    }
}
