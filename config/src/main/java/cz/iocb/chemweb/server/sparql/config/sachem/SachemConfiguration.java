package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;



/**
 * Configuration of a SACHEM endpoint over the compounds of one dataset: the IRI classes of the compounds and of their
 * molfiles, the quad mappings of the molfiles and the SACHEM search procedures over the index of the dataset. The
 * subclasses fill in the dataset.
 */
public class SachemConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Creates the configuration of a dataset.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @param index the name of the SACHEM index of the dataset, also the prefix of the names of its IRI classes
     * @param entity the name of the IRI class of the compounds within the index
     * @param iriPrefix the IRI prefix of the compounds, followed by their numeric ids
     * @param idLength the length the ids are zero-padded to in the IRIs, 0 for unpadded ids
     * @param table the table of the molfiles
     * @param idColumn the column of the compound ids in the table
     * @param molfileLiteralClass the literal class of the molfiles
     * @throws SQLException if a database error occurs
     */
    public SachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema, String index,
            String entity, String iriPrefix, int idLength, DatabaseTable table, String idColumn,
            LiteralClass molfileLiteralClass) throws SQLException
    {
        super(service, connectionPool, schema);

        String compound = index + ":" + entity;

        addPrefixes();
        addResourceClasses(index, compound, iriPrefix, idLength);
        addQuadMappings(index, compound, table, idColumn, molfileLiteralClass);
        addProcedures(index, compound);
    }


    /**
     * Adds the prefixes of the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);
        Sachem.addPrefixes(this);
        MolFiles.addPrefixes(this);
    }


    /**
     * Adds the resource classes of the configuration.
     *
     * @param index the name of the SACHEM index
     * @param compound the name of the IRI class of the compounds
     * @param iriPrefix the IRI prefix of the compounds
     * @param idLength the length the ids are zero-padded to
     */
    private void addResourceClasses(String index, String compound, String iriPrefix, int idLength)
    {
        Sachem.addResourceClasses(this);

        addIriClass(new IntegerUserIriClass(compound, INT4, iriPrefix, idLength));
        addIriClass(new IntegerUserIriClass(index + ":molfile", INT4, iriPrefix, idLength, "_Molfile"));
    }


    /**
     * Adds the quad mappings of the molfiles.
     *
     * @param index the name of the SACHEM index
     * @param compound the name of the IRI class of the compounds
     * @param table the table of the molfiles
     * @param idColumn the column of the compound ids in the table
     * @param molfileLiteralClass the literal class of the molfiles
     */
    private void addQuadMappings(String index, String compound, DatabaseTable table, String idColumn,
            LiteralClass molfileLiteralClass)
    {
        MolFiles.addQuadMappings(this, compound, index + ":molfile", table, getColumns(getIriClass(compound), idColumn),
                idColumn, "molfile", molfileLiteralClass);
    }


    /**
     * Adds the SACHEM search procedures over the index.
     *
     * @param index the name of the SACHEM index
     * @param compound the name of the IRI class of the compounds
     */
    private void addProcedures(String index, String compound)
    {
        Sachem.addProcedures(this, index, compound, getColumns(getIriClass(compound), "compound"));
    }
}
