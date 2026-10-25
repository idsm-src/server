package cz.iocb.chemweb.server.sparql.config.common;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import static cz.iocb.sparql.engine.mapping.datatypes.BuiltinDatatypes.xsdStringIri;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.mapping.ConstantLiteralMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedLiteralMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.rdf.Literal;
import cz.iocb.sparql.engine.rdf.TypedLiteral;



/**
 * Base of the dataset configurations that maps the {@code xsd:string} literals to the subset class
 * {@link #xsdOtherString}. The molfiles are mapped to subset classes of their own ({@link StringSubsetLiteralClass}),
 * so the engine knows that no other string of the database equals a molfile and leaves out the joins that cannot match.
 */
public abstract class SparqlDatabaseOptimisedConfiguration extends SparqlDatabaseConfiguration
{
    /**
     * Subset of {@code xsd:string} holding the string literals of the mappings that have no subset class of their own.
     */
    public static final LiteralClass xsdOtherString = new StringSubsetLiteralClass("string-others");


    /**
     * Creates a configuration with a union default graph and the default service description graph.
     *
     * @param service IRI of the endpoint's own service, or null
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    protected SparqlDatabaseOptimisedConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema);
    }


    /**
     * Creates a configuration with a union default graph.
     *
     * @param service IRI of the endpoint's own service, or null
     * @param descriptionGraph IRI of the graph holding the service description, or null for the default
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public SparqlDatabaseOptimisedConfiguration(String service, String descriptionGraph, DataSource connectionPool,
            DatabaseSchema schema) throws SQLException
    {
        super(service, descriptionGraph, connectionPool, schema, true);
    }


    @Override
    public TermMapping createLiteralMapping(LiteralClass literalClass, String... columns)
    {
        if(literalClass == xsdString)
            literalClass = xsdOtherString;

        return new ParametrisedLiteralMapping(literalClass, getColumns(literalClass, columns));
    }


    @Override
    public TermMapping createLiteralMapping(LiteralClass literalClass, Literal literal)
    {
        if(literalClass == xsdString)
            literalClass = xsdOtherString;

        return new ConstantLiteralMapping(literalClass, literal);
    }


    @Override
    public TermMapping createLiteralMapping(String value)
    {
        return new ConstantLiteralMapping(xsdOtherString, new TypedLiteral(value, xsdStringIri));
    }
}
