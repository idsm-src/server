package cz.iocb.chemweb.server.sparql.config.molmedb;

import static cz.iocb.chemweb.server.sparql.config.molmedb.MolmedbConfiguration.schema;
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
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.SQLRuntimeException;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.database.ValueColumn;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.request.Request;



public class SubstanceIdentifierIriClass extends UserIriClass
{
    protected final DatabaseTable table = new DatabaseTable(schema, "substance_bases");
    protected final TableColumn from = new TableColumn("id", INT4);
    protected final TableColumn to = new TableColumn("identifier", VARCHAR);

    protected final String prefix = "https://rdf.molmedb.upol.cz/substance/";
    protected final String delimiter;
    protected final String sqlQuery;
    protected final String regexp;
    protected final Pattern pattern;


    protected SubstanceIdentifierIriClass(String name, String delimiter, String pattern)
    {
        super(name, List.of(INT4, VARCHAR), Set.of(iri, box));

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

        return new ExpressionColumn(expr, VARCHAR);
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

        return new ExpressionColumn(builder.toString(), sqlTypes.get(0));
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

        return new ExpressionColumn(builder.toString(), VARCHAR);
    }


    @Override
    public boolean match(Request request, Iri iri)
    {
        Matcher matcher = pattern.matcher(iri.getValue());

        if(!matcher.matches())
            return false;

        try
        {
            String sql = sqlQuery.replaceAll("\\?", string(iri.getValue()));

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
    public List<Column> toColumns(Request request, Iri iri)
    {
        assert match(request, iri);

        try
        {
            String sql = sqlQuery.replaceAll("\\?", string(iri.getValue()));

            try(ResultSet result = request.getStatement().executeQuery(sql))
            {
                if(result.next())
                {
                    String col0 = result.getString(1);
                    String col1 = iri.getValue().substring(iri.getValue().indexOf(delimiter) + delimiter.length());

                    return List.of(new ValueColumn(col0, sqlTypes.get(0)), new ValueColumn(col1, sqlTypes.get(1)));
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
        Column value = columns.get(0);

        if(sourceClass.equals(box))
            value = expression(VARCHAR, "sparql.rdfbox_get_iri(%s)", value);
        else if(!sourceClass.equals(iri))
            throw new IllegalArgumentException();

        return List.of(generateInverseFunction1(value, check), generateInverseFunction2(value, check));
    }
}
