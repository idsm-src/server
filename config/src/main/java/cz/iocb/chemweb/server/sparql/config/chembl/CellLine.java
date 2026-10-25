package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitEFO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitEFOShort;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



/**
 * Mapping of the ChEMBL cell lines (graph {@code ebi:chembl}).
 */
public class CellLine
{
    /**
     * Ontology units of the EFO terms of the cell lines ({@code cco:hasEFO}).
     */
    public static final short[] efoUnits = { unitEFOShort, unitEFO };


    /**
     * Not instantiable.
     */
    private CellLine()
    {
    }


    /**
     * Adds the resource classes of the ChEMBL cell lines to the configuration.
     *
     * @param config the configuration
     */
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:cell_line", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/cell_line/CHEMBL"));
    }


    /**
     * Adds the quad mappings of the ChEMBL cell lines to the configuration.
     *
     * @param config the configuration
     */
    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        DatabaseTable table = new DatabaseTable(schema, "cell_lines");
        TermMapping subject = config.createIriMapping("chembl:cell_line", "id");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:CellLine"), config.createIsNotNullCondition(table, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasCLO"),
                config.createIriMapping("ontology:clo", "clo_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasEFO"),
                Ontology.createResourceMapping(config, "efo_unit", "efo_id", efoUnits));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                config.createIriMapping("ontology:taxonomy", "taxonomy"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                config.createIriMapping("reference:ncbi_taxonomy", "taxonomy"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "label"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                config.createLiteralMapping(xsdString, "description"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:organismName"),
                config.createLiteralMapping(xsdString, "organism"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:cellosaurusId"),
                config.createLiteralMapping(xsdString, "cellosaurus"));

        // extension
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                config.createIriMapping("ontology:ncbitaxon", "taxonomy"));
    }
}
