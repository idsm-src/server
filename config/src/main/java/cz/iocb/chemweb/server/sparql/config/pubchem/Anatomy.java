package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitBTO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitChEMBLTissue;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitEFO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersBTO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersEFO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersNCIT;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersUberon;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitThesaurus;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUberon;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



/**
 * Mapping of the PubChem anatomies (graph {@code pubchem:anatomy}).
 */
public class Anatomy
{
    /**
     * Ontology units of the matches of the anatomies ({@code rdfs:seeAlso}).
     */
    public static final short[] matchUnits = { unitBTO, unitUberon, unitIdentifiersBTO, unitIdentifiersEFO,
            unitIdentifiersNCIT, unitIdentifiersUberon, unitEFO, unitChEMBLTissue, unitThesaurus };


    /**
     * Not instantiable.
     */
    private Anatomy()
    {
    }


    /**
     * Adds the resource classes of the PubChem anatomies to the configuration.
     *
     * @param config the configuration
     */
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:anatomy", INT4,
                "http://rdf.ncbi.nlm.nih.gov/pubchem/anatomy/ANATOMYID"));
    }


    /**
     * Adds the quad mappings of the PubChem anatomies to the configuration.
     *
     * @param config the configuration
     */
    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:anatomy");

        {
            DatabaseTable table = new DatabaseTable(schema, "anatomies");
            TermMapping subject = config.createIriMapping("pubchem:anatomy", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Anatomy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001262"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "anatomy_alternatives");
            TermMapping subject = config.createIriMapping("pubchem:anatomy", "anatomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "anatomy_matches");
            TermMapping subject = config.createIriMapping("pubchem:anatomy", "anatomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    Ontology.createResourceMapping(config, "match_unit", "match_id", matchUnits));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "anatomy_mesh_matches");
            TermMapping subject = config.createIriMapping("pubchem:anatomy", "anatomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("mesh:resource", "match"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:mesh", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "anatomy_patents");
            TermMapping subject = config.createIriMapping("pubchem:anatomy", "anatomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:patent", "patent"));
        }
    }
}
