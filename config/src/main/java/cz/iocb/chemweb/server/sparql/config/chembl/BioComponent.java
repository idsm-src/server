package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class BioComponent
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:biocomponent", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/biocomponent/CHEMBL_BC_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        DatabaseTable table = new DatabaseTable(schema, "bio_component_sequences");
        TermMapping subject = config.createIriMapping("chembl:biocomponent", "id");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:BioComponent"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                config.createIriMapping("ontology:taxonomy", "tax_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                config.createIriMapping("reference:ncbi-taxonomy", "tax_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:componentType"),
                config.createLiteralMapping(xsdString, "component_type"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:proteinSequence"),
                config.createLiteralMapping(xsdString, "sequence"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                config.createLiteralMapping(xsdString, "description"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:organismName"),
                config.createLiteralMapping(xsdString, "organism"));

        // extension
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                config.createIriMapping("ontology:ncbitaxon", "tax_id"));
    }
}
