package cz.iocb.chemweb.server.sparql.config.ontology;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import java.sql.SQLException;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;



public class Ontology
{
    public static final String unitUncategorized = "'0'::smallint";
    public static final String unitBlank = "'1'::smallint";
    public static final String unitSIO = "'2'::smallint";
    public static final String unitCHEMINF = "'3'::smallint";
    public static final String unitBAO = "'4'::smallint";
    public static final String unitGO = "'5'::smallint";
    public static final String unitPR = "'6'::smallint";
    public static final String unitCHEBI = "'7'::smallint";
    public static final String unitThesaurus = "'10'::smallint";
    public static final String unitTaxonomy = "'11'::smallint";
    public static final String unitClassyFire = "'12'::smallint";
    public static final String unitNCBITaxon = "'64'::smallint";
    public static final String unitUberon = "'65'::smallint";
    public static final String unitCL = "'71'::smallint";
    public static final String unitUO = "'74'::smallint";
    public static final String unitIAO = "'75'::smallint";
    public static final String unitCLO = "'77'::smallint";
    public static final String unitEFO = "'92'::smallint";
    public static final String unitStar = "'95'::smallint";
    public static final String unitNCIT = "'100'::smallint";


    public static void addResourceClasses(SparqlDatabaseConfiguration config) throws SQLException
    {
        config.addIriClass(OntologyResource.get(config));
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
            TermMapping subject = config.createIriMapping("ontology:resource", unitBlank, "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:someValuesFrom"),
                    config.createIriMapping("ontology:resource", "class_unit", "class_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "allvaluesfrom_restrictions");
            TermMapping subject = config.createIriMapping("ontology:resource", unitBlank, "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:allValuesFrom"),
                    config.createIriMapping("ontology:resource", "class_unit", "class_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:resource", unitBlank, "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:cardinality"),
                    config.createLiteralMapping(xsdInt, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "mincardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:resource", unitBlank, "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:minCardinality"),
                    config.createLiteralMapping(xsdInt, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "maxcardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:resource", unitBlank, "restriction_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:maxCardinality"),
                    config.createLiteralMapping(xsdInt, "cardinality"));
        }
    }
}
