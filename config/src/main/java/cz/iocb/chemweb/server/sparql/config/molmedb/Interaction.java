package cz.iocb.chemweb.server.sparql.config.molmedb;

import static cz.iocb.chemweb.server.sparql.config.molmedb.MolmedbConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Interaction
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("molmedb:interaction", "integer",
                "https://rdf.molmedb.upol.cz/interaction/int"));

        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_interaction", "integer",
                "https://rdf.molmedb.upol.cz/interaction/fluo"));

        config.addIriClass(new IntegerUserIriClass("molmedb:membrane", "integer",
                "https://rdf.molmedb.upol.cz/interaction/membrane"));

        config.addIriClass(new IntegerUserIriClass("molmedb:measure_group", "integer",
                "https://rdf.molmedb.upol.cz/interaction/measure_group_int"));

        config.addIriClass(
                new IntegerUserIriClass("molmedb:method", "integer", "https://rdf.molmedb.upol.cz/interaction/method"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_logk", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_LogK_int"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_logperm", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_LogPerm_int"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_position", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_position_int"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_penetration", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_penetration_int"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_water", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_water_int"));

        config.addIriClass(new IntegerUserIriClass("molmedb:measure_group_temperature", "integer",
                "https://rdf.molmedb.upol.cz/interaction/measure_group_temperature_int"));

        config.addIriClass(new IntegerUserIriClass("molmedb:measure_group_mol_charge", "integer",
                "https://rdf.molmedb.upol.cz/interaction/measure_group_mol_charge_int"));

        config.addIriClass(new IntegerUserIriClass("molmedb:measure_group_ph", "integer",
                "https://rdf.molmedb.upol.cz/interaction/measure_group_pH_int"));


        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_measure_group", "integer",
                "https://rdf.molmedb.upol.cz/interaction/measure_group_fluo"));

        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_endpoint_theta", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_theta_fluo"));

        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_endpoint_abs_wl", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_abs_wl_fluo"));

        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_endpoint_fluo_wl", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_fluo_wl_fluo"));

        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_endpoint_qy", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_qy_fluo"));

        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_endpoint_lt", "integer",
                "https://rdf.molmedb.upol.cz/interaction/endpoint_lt_fluo"));

        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_measure_group_temperature", "integer",
                "https://rdf.molmedb.upol.cz/interaction/measure_group_temperature_fluo"));

        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_measure_group_mol_charge", "integer",
                "https://rdf.molmedb.upol.cz/interaction/measure_group_mol_charge_fluo"));

        config.addIriClass(new IntegerUserIriClass("molmedb:fluorescent_measure_group_ph", "integer",
                "https://rdf.molmedb.upol.cz/interaction/measure_group_pH_fluo"));

    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("<https://rdf.molmedb.upol.cz>");

        /*
         * interaction central node
         */

        // triples map #1
        {
            Table table = new Table(schema, "interaction_bases");
            NodeMapping subject = config.createIriMapping("molmedb:interaction", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002182")); // pharmacokinetic assay

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:substance", "substance_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:membrane", "membrane_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000209"), // has measure group
                    config.createIriMapping("molmedb:measure_group", "id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("molmedb:reference", "model_publication_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:references"),
                    config.createIriMapping("molmedb:reference", "publication_id"));
        }

        // triples map #2
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_interaction", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002182")); // pharmacokinetic assay

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:substance", "substance_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:membrane", "membrane_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000209"), // has measure group
                    config.createIriMapping("molmedb:fluorescent_measure_group", "id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("molmedb:reference", "model_publication_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:references"),
                    config.createIriMapping("molmedb:reference", "publication_id"));
        }


        /*
         * interaction measure group
         */

        // triples map #3
        {
            Table table = new Table(schema, "interaction_bases");
            NodeMapping subject = config.createIriMapping("molmedb:measure_group", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000040")); // measure group

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000212"), // has assay method
                    config.createIriMapping("molmedb:method", "method_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:endpoint_logk", "id"), config.createIsNotNullCondition("logk"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:endpoint_logperm", "id"),
                    config.createIsNotNullCondition("logperm"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:endpoint_position", "id"),
                    config.createIsNotNullCondition("x_min"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:endpoint_penetration", "id"),
                    config.createIsNotNullCondition("gpen"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:endpoint_water", "id"), config.createIsNotNullCondition("gwat"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:substance", "substance_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:membrane", "membrane_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000426"), // is measure group of
                    config.createIriMapping("molmedb:interaction", "id"));

            //TODO: is there other possibility?
            config.addQuadMapping(table, graph, subject, config.createIriMapping("repr:hasExperimentalCondition"),
                    config.createIriMapping("molmedb:measure_group_temperature", "id"),
                    config.createIsNotNullCondition("temperature"));

            //TODO: is there other possibility?
            config.addQuadMapping(table, graph, subject, config.createIriMapping("repr:hasExperimentalCondition"),
                    config.createIriMapping("molmedb:measure_group_mol_charge", "id"),
                    config.createIsNotNullCondition("charge"));

            //TODO: is there other possibility?
            config.addQuadMapping(table, graph, subject, config.createIriMapping("repr:hasExperimentalCondition"),
                    config.createIriMapping("molmedb:measure_group_ph", "id"), config.createIsNotNullCondition("ph"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:comment"),
                    config.createLiteralMapping(xsdString, "comment"));
        }

        // triples map #4
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_measure_group", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000040")); // measure group

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000212"), // has assay method
                    config.createIriMapping("molmedb:method", "method_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:fluorescent_endpoint_theta", "id"),
                    config.createIsNotNullCondition("theta"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:fluorescent_endpoint_abs_wl", "id"),
                    config.createIsNotNullCondition("abs_wl"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:fluorescent_endpoint_fluo_wl", "id"),
                    config.createIsNotNullCondition("fluo_wl"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:fluorescent_endpoint_qy", "id"),
                    config.createIsNotNullCondition("qy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:fluorescent_endpoint_lt", "id"),
                    config.createIsNotNullCondition("lt"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:substance", "substance_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:membrane", "membrane_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000426"), // is measure group of
                    config.createIriMapping("molmedb:fluorescent_interaction", "id"));

            //TODO: is there other possibility?
            config.addQuadMapping(table, graph, subject, config.createIriMapping("repr:hasExperimentalCondition"),
                    config.createIriMapping("molmedb:fluorescent_measure_group_temperature", "id"),
                    config.createIsNotNullCondition("temperature"));

            //TODO: is there other possibility?
            config.addQuadMapping(table, graph, subject, config.createIriMapping("repr:hasExperimentalCondition"),
                    config.createIriMapping("molmedb:fluorescent_measure_group_mol_charge", "id"),
                    config.createIsNotNullCondition("charge"));

            //TODO: is there other possibility?
            config.addQuadMapping(table, graph, subject, config.createIriMapping("repr:hasExperimentalCondition"),
                    config.createIriMapping("molmedb:fluorescent_measure_group_ph", "id"),
                    config.createIsNotNullCondition("ph"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:comment"),
                    config.createLiteralMapping(xsdString, "comment"));
        }


        /*
         * membrane
         */

        // triples map #5
        {
            Table table = new Table(schema, "membrane_bases");
            NodeMapping subject = config.createIriMapping("molmedb:membrane", "id");

            //FIXME:
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:MembraneModel"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "name"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "abbreviation"));

            //config.addQuadMapping(table, graph, subject, config.createIriMapping("dc:description"),
            //        config.createLiteralMapping(rdfHTML, "description"));
        }

        // triples map #6
        {
            Table table = new Table(schema, "membrane_bases");
            NodeMapping subject = config.createIriMapping("molmedb:membrane", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:SubstanceBasedMembraneModel"),
                    config.createAreEqualCondition("category", "'62'::integer", "'65'::integer", "'67'::integer",
                            "'69'::integer", "'72'::integer", "'153'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:BiologyBasedMembraneModel"),
                    config.createAreEqualCondition("category", "'63'::integer", "'64'::integer", "'66'::integer",
                            "'68'::integer", "'70'::integer", "'154'::integer", "'155'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:GenericMembraneModel"),
                    config.createAreEqualCondition("parent_category", "'58'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:IntestineMembraneModel"),
                    config.createAreEqualCondition("parent_category", "'59'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:OralMembraneModel"),
                    config.createAreEqualCondition("parent_category", "'60'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:SkinMembraneModel"),
                    config.createAreEqualCondition("parent_category", "'61'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:BrainMembraneModel"),
                    config.createAreEqualCondition("parent_category", "'71'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:EyeMembraneModel"),
                    config.createAreEqualCondition("parent_category", "'73'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:CellMembraneModel"),
                    config.createAreEqualCondition("parent_category", "'152'::integer"));
        }


        /*
         * method
         */

        // triples map #7
        {
            Table table = new Table(schema, "method_bases");
            NodeMapping subject = config.createIriMapping("molmedb:method", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002753")); // assay method component

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "name"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "abbreviation"));

            //config.addQuadMapping(table, graph, subject, config.createIriMapping("dc:description"),
            //        config.createLiteralMapping(rdfHTML, "description"));
        }


        /*
         * method type
        */

        // triples map #8
        {
            Table table = new Table(schema, "method_bases");
            NodeMapping subject = config.createIriMapping("molmedb:method", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002202"),
                    config.createAreEqualCondition("parent_category", "'4'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002094"),
                    config.createAreEqualCondition("parent_category", "'5'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002301"),
                    config.createAreEqualCondition("parent_category", "'6'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002094"),
                    config.createAreEqualCondition("category", "'5'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002317"),
                    config.createAreEqualCondition("category", "'8'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002248"),
                    config.createAreEqualCondition("category", "'9'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002305"),
                    config.createAreEqualCondition("category", "'10'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:WaterMembranePartitioningMethod"),
                    config.createAreEqualCondition("category", "'7'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:AtomisticSimulation"),
                    config.createAreEqualCondition("category", "'11'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:UnitedAtomsSimulation"),
                    config.createAreEqualCondition("category", "'12'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:CoarseGrainedSimulation"),
                    config.createAreEqualCondition("category", "'13'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:HybridResolutionSimulation"),
                    config.createAreEqualCondition("category", "'14'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:MembranePositionMethod"),
                    config.createAreEqualCondition("category", "'167'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:MembranePermeabilityMethod"),
                    config.createAreEqualCondition("category", "'168'::integer"));
        }


        /*
         * experimental conditions
         */

        // triples map #9
        {
            Table table = new Table(schema, "interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("temperature");
            NodeMapping subject = config.createIriMapping("molmedb:measure_group_temperature", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("efo:EFO_0001702"), cnd); // temperature

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "temperature"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000027"), cnd); // degree celsius
        }

        // triples map #10
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("temperature");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_measure_group_temperature", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("efo:EFO_0001702"), cnd); // temperature

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "temperature"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000027"), cnd); // degree celsius
        }

        // triples map #11
        {
            Table table = new Table(schema, "interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("charge");
            NodeMapping subject = config.createIriMapping("molmedb:measure_group_mol_charge", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000120"), cnd); // charge

            //FIXME: use string?
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "charge"), cnd);
        }

        // triples map #12
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("charge");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_measure_group_mol_charge", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000120"), cnd); // charge

            //FIXME: use string?
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "charge"), cnd);
        }

        // triples map #13
        {
            Table table = new Table(schema, "interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("ph");
            NodeMapping subject = config.createIriMapping("molmedb:measure_group_ph", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001089"), cnd); // pH

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "ph"));
        }

        // triples map #14
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("ph");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_measure_group_ph", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001089"), cnd); // pH

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "ph"));
        }


        /*
         * interaction endpoints
         */

        // triples map #15
        {
            Table table = new Table(schema, "interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("logk");
            NodeMapping subject = config.createIriMapping("molmedb:endpoint_logk", "id");

            //FIXME:
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:LogK"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "logk"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "logk_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:measure_group", "id"), cnd);
        }

        // triples map #16
        {
            Table table = new Table(schema, "interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("logperm");
            NodeMapping subject = config.createIriMapping("molmedb:endpoint_logperm", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:LogPerm"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "logperm"));

            //TODO: change
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("mmdbvoc:CmS"), cnd); // Centimeter per Second

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "logperm_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:measure_group", "id"), cnd);
        }

        // triples map #17
        {
            Table table = new Table(schema, "interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("x_min");
            NodeMapping subject = config.createIriMapping("molmedb:endpoint_position", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:PositionOfMinima"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "x_min"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000018"), cnd); // nanometer

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "x_min_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:measure_group", "id"), cnd);
        }

        // triples map #18
        {
            Table table = new Table(schema, "interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("gpen");
            NodeMapping subject = config.createIriMapping("molmedb:endpoint_penetration", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:PenetrationBarrier"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "gpen"));

            //TODO: change
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("mmdbvoc:KcalMol"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "gpen_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:measure_group", "id"), cnd);
        }

        // triples map #19
        {
            Table table = new Table(schema, "interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("gwat");
            NodeMapping subject = config.createIriMapping("molmedb:endpoint_water", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:DepthOfMinima"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "gwat"));

            //TODO: change
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("mmdbvoc:KcalMol"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "gwat_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:measure_group", "id"), cnd);
        }

        // triples map #20
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("theta");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_endpoint_theta", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:ContactAngle"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "theta"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000185"), cnd); // degree

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "theta_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:fluorescent_measure_group", "id"), cnd);
        }

        // triples map #21
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("abs_wl");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_endpoint_abs_wl", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:AbsorptionWavelength"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "abs_wl"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000018"), cnd); // nanometer

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "abs_wl_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:fluorescent_measure_group", "id"), cnd);
        }

        // triples map #22
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("fluo_wl");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_endpoint_fluo_wl", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:FluorescenceWavelength"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "fluo_wl"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000018"), cnd); // nanometer

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "fluo_wl_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:fluorescent_measure_group", "id"), cnd);
        }

        // triples map #23
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("qy");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_endpoint_qy", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:QuantumYield"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "qy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "qy_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:fluorescent_measure_group", "id"), cnd);
        }

        // triples map #24
        {
            Table table = new Table(schema, "fluorescent_interaction_bases");
            Conditions cnd = config.createIsNotNullCondition("lt");
            NodeMapping subject = config.createIriMapping("molmedb:fluorescent_endpoint_lt", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:FluorescenceLifetime"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "lt"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000150"), cnd); // nanosecond

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "lt_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:fluorescent_measure_group", "id"), cnd);
        }


        /*
         * membranes to chebi
         */
        {
            Table table = new Table(schema, "membrane_parts");
            NodeMapping subject = config.createIriMapping("molmedb:membrane", "membrane_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090004"), // has part
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi_id"));
        }
    }
}
