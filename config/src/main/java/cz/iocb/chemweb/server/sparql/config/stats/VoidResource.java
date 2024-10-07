package cz.iocb.chemweb.server.sparql.config.stats;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ConstantColumn;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.mapping.classes.ResultTag;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.parser.model.VariableOrBlankNode;
import cz.iocb.sparql.engine.parser.model.triple.Node;



public class VoidResource extends UserIriClass
{
    private final Pattern pattern;
    private final String regexp;
    private final String prefix;
    private final List<Integer> offsets;
    private final List<Integer> lengths;


    public VoidResource(String name, String prefix, List<String> types)
    {
        super(name, types, List.of(ResultTag.IRI));

        int offset = prefix.length() + 1;

        this.prefix = prefix;
        this.offsets = new ArrayList<Integer>(types.size());
        this.lengths = new ArrayList<Integer>(types.size());

        StringBuilder builder = new StringBuilder();
        builder.append(Pattern.quote(prefix));

        for(String type : types)
        {
            switch(type)
            {
                case "smallint" ->
                {
                    builder.append("[0-7][0-9a-f]{3}");
                    offsets.add(offset);
                    lengths.add(4);
                    offset += 4;
                }

                case "integer" ->
                {
                    builder.append("[0-7][0-9a-f]{7}");
                    offsets.add(offset);
                    lengths.add(8);
                    offset += 8;
                }

                default ->
                {
                    throw new IllegalArgumentException();
                }
            }
        }

        //FIXME: check whether the pattern is valid also in pcre2
        this.regexp = builder.toString();
        this.pattern = Pattern.compile(regexp);
    }


    @Override
    public List<Column> toColumns(Statement statement, Node node)
    {
        IRI iri = (IRI) node;
        assert match(statement, iri);

        List<Column> columns = new ArrayList<Column>();

        String value = iri.getValue();

        for(int i = 0; i < getColumnCount(); i++)
        {
            int part = Integer.parseInt(value.substring(offsets.get(i) - 1, offsets.get(i) + lengths.get(i) - 1), 16);
            columns.add(new ConstantColumn(part, sqlTypes.get(i)));
        }

        return columns;
    }


    @Override
    public boolean match(Statement statement, IRI iri)
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
            switch(sqlTypes.get(i))
            {
                case "smallint" -> builder.append(" || lpad(to_hex(" + columns.get(i) + "::int), 4, '0')");
                case "integer" -> builder.append(" || lpad(to_hex(" + columns.get(i) + "), 8, '0')");
            }
        }

        return new ExpressionColumn(builder.toString());
    }


    protected List<Column> generateInverseFunctions(Column parameter, boolean check)
    {
        List<Column> result = new ArrayList<Column>(getColumnCount());

        for(int i = 0; i < getColumnCount(); i++)
        {
            String code = "('x' || substring(" + parameter + ", " + offsets.get(i) + ", " + lengths.get(i) + "))"
                    + switch(sqlTypes.get(i))
                    {
                        case "smallint" -> "::bit(16)::integer";
                        case "integer" -> "::bit(32)";
                        default -> null;
                    } + "::" + sqlTypes.get(i);

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

            result.add(new ExpressionColumn(code));
        }

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

        VoidResource other = (VoidResource) object;

        if(!prefix.equals(other.prefix))
            return false;

        return true;
    }
}
