package cz.iocb.chemweb.server.sparql.config.ontology;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitBAO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitBlank;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitCHEBI;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitCHEMINF;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitCL;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitCLO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitClassyFire;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitEFO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitGO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitIAO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitNCBITaxon;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitNCIT;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitPR;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitSIO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitStar;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitTaxonomy;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitThesaurus;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUberon;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUncategorized;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import java.sql.SQLException;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;



public class Ontology
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config) throws SQLException
    {
        OntologyResource resource = OntologyResource.get(config);

        // registered before the whole class, so that an uncategorized resource is detected by a single lookup
        config.addIriClass(new OntologyUnitResource("ontology:uncategorized", resource, unitUncategorized));
        config.addIriClass(new OntologyUnitResource("ontology:blank", resource, unitBlank));
        config.addIriClass(new OntologyUnitResource("ontology:sio", resource, unitSIO));
        config.addIriClass(new OntologyUnitResource("ontology:cheminf", resource, unitCHEMINF));
        config.addIriClass(new OntologyUnitResource("ontology:bao", resource, unitBAO));
        config.addIriClass(new OntologyUnitResource("ontology:go", resource, unitGO));
        config.addIriClass(new OntologyUnitResource("ontology:pr", resource, unitPR));
        config.addIriClass(new OntologyUnitResource("ontology:chebi", resource, unitCHEBI));
        config.addIriClass(new OntologyUnitResource("ontology:thesaurus", resource, unitThesaurus));
        config.addIriClass(new OntologyUnitResource("ontology:taxonomy", resource, unitTaxonomy));
        config.addIriClass(new OntologyUnitResource("ontology:classyfire", resource, unitClassyFire));
        config.addIriClass(new OntologyUnitResource("ontology:ncbitaxon", resource, unitNCBITaxon));
        config.addIriClass(new OntologyUnitResource("ontology:uberon", resource, unitUberon));
        config.addIriClass(new OntologyUnitResource("ontology:cl", resource, unitCL));
        config.addIriClass(new OntologyUnitResource("ontology:uo", resource, unitUO));
        config.addIriClass(new OntologyUnitResource("ontology:iao", resource, unitIAO));
        config.addIriClass(new OntologyUnitResource("ontology:clo", resource, unitCLO));
        config.addIriClass(new OntologyUnitResource("ontology:efo", resource, unitEFO));
        config.addIriClass(new OntologyUnitResource("ontology:star", resource, unitStar));
        config.addIriClass(new OntologyUnitResource("ontology:ncit", resource, unitNCIT));
        config.addIriClass(resource);
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("dataset:ontology");

        {
            DatabaseTable table = new DatabaseTable(schema, "classes");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Class"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "properties");
            TermMapping subject = config.createIriMapping("ontology:resource", "property_unit", "property_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("rdf:Property"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "individuals");
            TermMapping subject = config.createIriMapping("ontology:resource", "individual_unit", "individual_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:NamedIndividual"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_labels");
            TermMapping subject = config.createIriMapping("ontology:resource", "resource_unit", "resource_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "superclasses");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subClassOf"),
                    config.createIriMapping("ontology:resource", "superclass_unit", "superclass_id"));
        }


        {
            DatabaseTable table = new DatabaseTable(schema, "superproperties");
            TermMapping subject = config.createIriMapping("ontology:resource", "property_unit", "property_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subPropertyOf"),
                    config.createIriMapping("ontology:resource", "superproperty_unit", "superproperty_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "property_domains");
            TermMapping subject = config.createIriMapping("ontology:resource", "property_unit", "property_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:domain"),
                    config.createIriMapping("ontology:resource", "domain_unit", "domain_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "property_ranges");
            TermMapping subject = config.createIriMapping("ontology:resource", "property_unit", "property_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:range"),
                    config.createIriMapping("ontology:resource", "range_unit", "range_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "somevaluesfrom_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:someValuesFrom"),
                    config.createIriMapping("ontology:resource", "class_unit", "class_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "allvaluesfrom_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:allValuesFrom"),
                    config.createIriMapping("ontology:resource", "class_unit", "class_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:cardinality"),
                    config.createLiteralMapping(xsdInt, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "mincardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:minCardinality"),
                    config.createLiteralMapping(xsdInt, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "maxcardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:maxCardinality"),
                    config.createLiteralMapping(xsdInt, "cardinality"));
        }
    }
}
