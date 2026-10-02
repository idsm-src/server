package cz.iocb.chemweb.server.sparql.config.stats;

import static cz.iocb.sparql.engine.database.SqlType.INT2;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.RDFBOX;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.box;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.iri;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.constant;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.expression;
import static cz.iocb.sparql.engine.mapping.classes.DerivedClass.subtract;
import static cz.iocb.sparql.engine.mapping.classes.DerivedClass.unionize;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.SqlType;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.request.Request;



public class VoidResource extends UserIriClass
{
    private final Pattern pattern;
    private final String regexp;
    private final String prefix;
    private final List<Integer> offsets;
    private final List<Integer> lengths;


    public VoidResource(String name, String prefix, List<SqlType> types)
    {
        super(name, types, Set.of(iri, box));

        int offset = prefix.length() + 1;

        this.prefix = prefix;
        this.offsets = new ArrayList<>(types.size());
        this.lengths = new ArrayList<>(types.size());

        StringBuilder builder = new StringBuilder();
        builder.append(Pattern.quote(prefix));

        for(SqlType type : types)
        {
            if(type == INT2)
            {
                builder.append("[0-7][0-9a-f]{3}");
                offsets.add(offset);
                lengths.add(4);
                offset += 4;
            }
            else if(type == INT4)
            {
                builder.append("[0-7][0-9a-f]{7}");
                offsets.add(offset);
                lengths.add(8);
                offset += 8;
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }

        //FIXME: check whether the pattern is valid also in pcre2
        this.regexp = builder.toString();
        this.pattern = Pattern.compile(regexp);
    }


    @Override
    public List<Column> toColumns(Request request, Iri iri)
    {
        assert match(request, iri);

        List<Column> columns = new ArrayList<>();

        String value = iri.getValue();

        for(int i = 0; i < getColumnCount(); i++)
        {
            int part = Integer.parseInt(value.substring(offsets.get(i) - 1, offsets.get(i) + lengths.get(i) - 1), 16);
            columns.add(constant(part, sqlTypes.get(i)));
        }

        return columns;
    }


    @Override
    public boolean match(Request request, Iri iri)
    {
        return pattern.matcher(iri.getValue()).matches();
    }


    @Override
    public int getCheckCost()
    {
        return 0;
    }


    protected Column generateFunction(List<Column> columns)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("'");
        builder.append(prefix.replaceAll("'", "''"));
        builder.append("'");

        for(int i = 0; i < getColumnCount(); i++)
        {
            if(sqlTypes.get(i) == INT2)
                builder.append(" || lpad(to_hex(" + columns.get(i) + "::int), 4, '0')");
            else if(sqlTypes.get(i) == INT4)
                builder.append(" || lpad(to_hex(" + columns.get(i) + "), 8, '0')");
        }

        return new ExpressionColumn(builder.toString(), VARCHAR);
    }


    protected List<Column> generateInverseFunctions(Column parameter, boolean check)
    {
        List<Column> result = new ArrayList<>(getColumnCount());

        for(int i = 0; i < getColumnCount(); i++)
        {
            String cast = sqlTypes.get(i) == INT2 ? "::bit(16)::integer" : sqlTypes.get(i) == INT4 ? "::bit(32)" : null;

            String code = "('x' || substring(" + parameter + ", " + offsets.get(i) + ", " + lengths.get(i) + "))" + cast
                    + "::" + sqlTypes.get(i);

            if(check)
            {
                StringBuilder builder = new StringBuilder();

                builder.append("CASE WHEN sparql.regex_string(");
                builder.append(parameter);
                builder.append(", '^(");
                builder.append(regexp.replaceAll("'", "''"));
                builder.append(")$', '') THEN ");

                code = builder.toString() + code + " END";
            }

            result.add(new ExpressionColumn(code, sqlTypes.get(i)));
        }

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
        return columns;
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

        VoidResource other = (VoidResource) object;

        if(!prefix.equals(other.prefix))
            return false;

        return true;
    }
}
