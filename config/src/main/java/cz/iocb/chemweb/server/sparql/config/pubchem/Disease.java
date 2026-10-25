package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitDOIDColon;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitDOIDColon0;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitGARD;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitGlycosmosDOID;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitGlycosmosDOID0;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitGuideToPharmacologyDisease;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitHP;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersDOID;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersDOID0;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersHP;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersKEGGDisease;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersMIM;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersMedGen;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersMedGenCN;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersNANDO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersNCIT;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersOrphanet;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersPharmGKBDisease;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIdentifiersUMLS;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitMONDO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitThesaurus;
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
 * Mapping of the PubChem diseases (graph {@code pubchem:disease}).
 */
public class Disease
{
    /**
     * Ontology units of the close and related matches of the diseases ({@code skos:closeMatch} and
     * {@code skos:relatedMatch}).
     */
    public static final short[] matchUnits = { unitDOIDColon, unitDOIDColon0, unitHP, unitMONDO, unitIdentifiersDOID,
            unitIdentifiersDOID0, unitIdentifiersHP, unitIdentifiersKEGGDisease, unitIdentifiersMedGen,
            unitIdentifiersMedGenCN, unitIdentifiersMIM, unitIdentifiersNANDO, unitIdentifiersNCIT,
            unitIdentifiersOrphanet, unitIdentifiersPharmGKBDisease, unitIdentifiersUMLS, unitThesaurus, unitGARD,
            unitGlycosmosDOID, unitGlycosmosDOID0, unitGuideToPharmacologyDisease };


    /**
     * Not instantiable.
     */
    private Disease()
    {
    }


    /**
     * Adds the resource classes of the PubChem diseases to the configuration.
     *
     * @param config the configuration
     */
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("pubchem:disease", INT4, "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"));
    }


    /**
     * Adds the quad mappings of the PubChem diseases to the configuration.
     *
     * @param config the configuration
     */
    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:disease");

        {
            DatabaseTable table = new DatabaseTable(schema, "diseases");
            TermMapping subject = config.createIriMapping("pubchem:disease", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Disease"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_010299"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "disease_alternatives");
            TermMapping subject = config.createIriMapping("pubchem:disease", "disease");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "disease_matches");
            TermMapping subject = config.createIriMapping("pubchem:disease", "disease");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    Ontology.createResourceMapping(config, "match_unit", "match_id", matchUnits));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "disease_mesh_matches");
            TermMapping subject = config.createIriMapping("pubchem:disease", "disease");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("mesh:resource", "match"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("identifiers:mesh", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "disease_related_matches");
            TermMapping subject = config.createIriMapping("pubchem:disease", "disease");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:relatedMatch"),
                    Ontology.createResourceMapping(config, "match_unit", "match_id", matchUnits));
        }
    }
}
