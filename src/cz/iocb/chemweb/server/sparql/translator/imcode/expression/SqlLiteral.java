package cz.iocb.chemweb.server.sparql.translator.imcode.expression;

import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.unsupportedLiteral;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinDataTypes.xsdBooleanType;
import java.util.List;
import cz.iocb.chemweb.server.sparql.database.Column;
import cz.iocb.chemweb.server.sparql.engine.Request;
import cz.iocb.chemweb.server.sparql.mapping.classes.DataType;
import cz.iocb.chemweb.server.sparql.mapping.classes.LiteralClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.ResourceClass;
import cz.iocb.chemweb.server.sparql.parser.model.expression.Literal;
import cz.iocb.chemweb.server.sparql.translator.UsedVariables;



public class SqlLiteral extends SqlNodeValue
{
    public static final Literal trueLiteral = new Literal("true", xsdBooleanType);
    public static final Literal falseLiteral = new Literal("false", xsdBooleanType);

    public static final SqlLiteral trueValue = new SqlLiteral(trueLiteral, xsdBoolean);
    public static final SqlLiteral falseValue = new SqlLiteral(falseLiteral, xsdBoolean);

    private final Literal literal;
    private final LiteralClass literalClass;


    protected SqlLiteral(Literal literal, LiteralClass resourceClass)
    {
        super(asSet(resourceClass), false);
        this.literalClass = resourceClass;
        this.literal = literal;
    }


    public static SqlExpressionIntercode create(Literal literal)
    {
        if(literal.equals(trueLiteral))
            return trueValue;

        if(literal.equals(falseLiteral))
            return falseValue;

        DataType datatype = Request.currentRequest().getConfiguration().getDataType(literal.getTypeIri());
        LiteralClass resourceClass = datatype == null ? unsupportedLiteral : datatype.getResourceClass(literal);

        return new SqlLiteral(literal, resourceClass);
    }


    @Override
    public SqlExpressionIntercode optimize(UsedVariables variables)
    {
        return this;
    }


    @Override
    public String translate()
    {
        LiteralClass resourceClass = (LiteralClass) getResourceClasses().iterator().next();

        return resourceClass.toExpression(literal).toString();
    }


    public Literal getLiteral()
    {
        return literal;
    }


    public LiteralClass getLiteralClass()
    {
        return literalClass;
    }


    @Override
    public List<Column> asResource(ResourceClass resourceClass)
    {
        return resourceClass.toColumns(literal);
    }
}
