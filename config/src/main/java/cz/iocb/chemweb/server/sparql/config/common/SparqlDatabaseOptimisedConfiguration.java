package cz.iocb.chemweb.server.sparql.config.common;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinDataTypes.xsdStringType;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.mapping.ConstantLiteralMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedLiteralMapping;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.parser.model.expression.Literal;



public abstract class SparqlDatabaseOptimisedConfiguration extends SparqlDatabaseConfiguration
{
    public static final LiteralClass xsdOtherString = new StringSubsetLiteralClass("string-others");


    protected SparqlDatabaseOptimisedConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema);
    }


    public SparqlDatabaseOptimisedConfiguration(String service, String descriptionGraph, DataSource connectionPool,
            DatabaseSchema schema) throws SQLException
    {
        super(service, descriptionGraph, connectionPool, schema, true);
    }


    @Override
    public NodeMapping createLiteralMapping(LiteralClass literalClass, String... columns)
    {
        if(literalClass == xsdString)
            literalClass = xsdOtherString;

        return new ParametrisedLiteralMapping(literalClass, getColumns(columns));
    }


    @Override
    public NodeMapping createLiteralMapping(LiteralClass literalClass, Literal literal)
    {
        if(literalClass == xsdString)
            literalClass = xsdOtherString;

        return new ConstantLiteralMapping(literalClass, literal);
    }


    @Override
    public NodeMapping createLiteralMapping(String value)
    {
        return new ConstantLiteralMapping(xsdOtherString, new Literal(value, xsdStringType));
    }
}
