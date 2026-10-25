package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitEOL;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitGlyConnectTaxonomy;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitGlycoInfo;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitGlycosmosOrganism;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIPNI1;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIPNI2;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIPNI3;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIPNI4;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitITIS;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersNCIT;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitPOWO1;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitPOWO2;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitPOWO3;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitPOWO4;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitPOWO5;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitThesaurus;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUniProtTaxonomy;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO0;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO1;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO2;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO3;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO4;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO5;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO6;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO7;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO8;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitWFO9;
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
 * Mapping of the PubChem taxa (graph {@code pubchem:taxonomy}).
 */
public class Taxonomy
{
    /**
     * Ontology units of the matches of the taxa ({@code rdfs:seeAlso}).
     */
    public static final short[] matchUnits = { unitIdentifiersNCIT, unitThesaurus, unitWFO0, unitWFO1, unitWFO2,
            unitWFO3, unitWFO4, unitWFO5, unitWFO6, unitWFO7, unitWFO8, unitWFO9, unitPOWO1, unitPOWO2, unitPOWO3,
            unitPOWO4, unitPOWO5, unitGlycosmosOrganism, unitIPNI1, unitIPNI2, unitIPNI3, unitIPNI4,
            unitGlyConnectTaxonomy, unitUniProtTaxonomy, unitGlycoInfo, unitEOL, unitITIS };


    /**
     * Not instantiable.
     */
    private Taxonomy()
    {
    }


    /**
     * Adds the resource classes of the PubChem taxa to the configuration.
     *
     * @param config the configuration
     */
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:taxonomy", INT4,
                "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
    }


    /**
     * Adds the quad mappings of the PubChem taxa to the configuration.
     *
     * @param config the configuration
     */
    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:taxonomy");

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomies");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Taxonomy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_010000"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createLiteralMapping(xsdString, "(id)::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:taxonomy", "id"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("ncbi:taxonomy", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("ontology:ncbitaxon", "id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_alternatives");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_references");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_patents");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:patent", "patent"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_matches");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    Ontology.createResourceMapping(config, "match_unit", "match_id", matchUnits));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_mesh_matches");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("mesh:resource", "match"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:mesh", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_catalogueoflife_matches");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:col", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_wikidata_matches");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("wikidata:entity", "match"));
        }
    }
}
