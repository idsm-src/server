package cz.iocb.chemweb.server.sparql.config.isdb;

import static cz.iocb.sparql.engine.database.SqlType.CHAR;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.RDFBOX;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.box;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.iri;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.expression;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.string;
import static cz.iocb.sparql.engine.mapping.classes.DerivedClass.subtract;
import static cz.iocb.sparql.engine.mapping.classes.DerivedClass.unionize;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.SQLRuntimeException;
import cz.iocb.sparql.engine.database.ValueColumn;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.request.Request;



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
        super(name, List.of(INT4, CHAR), Set.of(iri, box));

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
    public List<Column> toColumns(Request request, Iri iri)
    {
        assert match(request, iri);

        try
        {
            String sql = sqlQuery.replaceAll("\\?", string(iri.getValue().substring(prefixLen, prefixLen + 14)));

            try(ResultSet result = request.getStatement().executeQuery(sql))
            {
                if(result.next())
                {
                    return List.of(new ValueColumn(result.getString(1), INT4),
                            new ValueColumn(iri.getValue().substring(prefixLen + 15, prefixLen + 16), CHAR));
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
    public boolean match(Request request, Iri iri)
    {
        Matcher matcher = pattern.matcher(iri.getValue());

        if(!matcher.matches())
            return false;

        try
        {
            String sql = sqlQuery.replaceAll("\\?", string(iri.getValue().substring(prefixLen, prefixLen + 14)));

            try(ResultSet result = request.getStatement().executeQuery(sql))
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

        String code = String.format("'%s' || \"@to\" || '-' || %s", prefix.replaceAll("'", "''"), columns.get(1));

        if(suffix != null)
            code = String.format("%s || '%s'", code, suffix.replaceAll("'", "''"));

        code = String.format("(SELECT (%s)::varchar FROM %s WHERE \"@from\" = %s)", code, access, columns.get(0));

        return new ExpressionColumn(code, VARCHAR);
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

        List<Column> result = new ArrayList<>(getColumnCount());

        result.add(new ExpressionColumn(col1, sqlTypes.get(0)));
        result.add(new ExpressionColumn(col2, sqlTypes.get(1)));

        return result;
    }


    @Override
    public List<Column> toGeneralClass(ResourceClass superClass, List<Column> columns, boolean canBeNull)
    {
        assert isSubclassOf(superClass);

        ResourceClass targetClass = superClass.getEffectiveClass();

        if(targetClass.equals(this))
            return columns;

        if(targetClass.equals(box))
            return List.of(expression(RDFBOX, "sparql.rdfbox_create_from_iri(%s)", generateFunction(columns)));

        if(targetClass.equals(iri))
            return List.of(generateFunction(columns));

        throw new IllegalArgumentException();
    }


    @Override
    public List<Column> fromGeneralClass(ResourceClass superClass, List<Column> columns, boolean checkOptional)
    {
        if(superClass.equals(this))
            return columns;

        ResourceClass sourceClass = superClass.getEffectiveClass();

        assert isSubclassOf(sourceClass);

        // the check is needless when every IRI of the superclass belongs to this class
        boolean check = !checkOptional && !superClass.isSubclassOf(unionize(this, subtract(box, iri)));

        if(sourceClass.equals(box))
            return generateInverseFunctions(expression(VARCHAR, "sparql.rdfbox_get_iri(%s)", columns.get(0)), check);

        if(sourceClass.equals(iri))
            return generateInverseFunctions(columns.get(0), check);

        throw new IllegalArgumentException();
    }


    @Override
    public List<Column> toOrderColumns(List<Column> columns)
    {
        String access = "(SELECT id as \"@from\", accession as \"@to\" FROM isdb.compound_bases) as \"@rctab\"";
        String code = String.format("(SELECT \"@to\" FROM %s WHERE \"@from\" = %s)", access, columns.get(0));

        return List.of(new ExpressionColumn(code, VARCHAR), columns.get(1));
    }


    @Override
    public String getPrefix(List<Column> columns)
    {
        return prefix;
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
