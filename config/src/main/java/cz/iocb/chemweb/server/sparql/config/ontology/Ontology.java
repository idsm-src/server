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
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.rdfLangString;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDouble;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInteger;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdNonNegativeInteger;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
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
            TermMapping subject = config.createIriMapping("ontology:resource", "unit", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Class"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "properties");
            TermMapping subject = config.createIriMapping("ontology:resource", "unit", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("rdf:Property"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "individuals");
            TermMapping subject = config.createIriMapping("ontology:resource", "unit", "id");

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
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:someValuesFrom"),
                    config.createIriMapping("ontology:resource", "class_unit", "class_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "allvaluesfrom_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:allValuesFrom"),
                    config.createIriMapping("ontology:resource", "class_unit", "class_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:cardinality"),
                    config.createLiteralMapping(xsdNonNegativeInteger, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "mincardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:minCardinality"),
                    config.createLiteralMapping(xsdNonNegativeInteger, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "maxcardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:maxCardinality"),
                    config.createLiteralMapping(xsdNonNegativeInteger, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_types");
            TermMapping subject = config.createIriMapping("ontology:resource", "resource_unit", "resource_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", "type_unit", "type_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_equivalents");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:equivalentClass"),
                    config.createIriMapping("ontology:resource", "equivalent_unit", "equivalent_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_disjoints");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:disjointWith"),
                    config.createIriMapping("ontology:resource", "disjoint_unit", "disjoint_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_complements");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:complementOf"),
                    config.createIriMapping("ontology:resource", "complement_unit", "complement_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_intersections");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:intersectionOf"),
                    config.createIriMapping("ontology:resource", "list_unit", "list_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_unions");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:unionOf"),
                    config.createIriMapping("ontology:resource", "list_unit", "list_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_enumerations");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:oneOf"),
                    config.createIriMapping("ontology:resource", "list_unit", "list_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_disjoint_unions");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:disjointUnionOf"),
                    config.createIriMapping("ontology:resource", "list_unit", "list_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_keys");
            TermMapping subject = config.createIriMapping("ontology:resource", "class_unit", "class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:hasKey"),
                    config.createIriMapping("ontology:resource", "list_unit", "list_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "property_inverses");
            TermMapping subject = config.createIriMapping("ontology:resource", "property_unit", "property_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:inverseOf"),
                    config.createIriMapping("ontology:resource", "inverse_unit", "inverse_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "property_equivalents");
            TermMapping subject = config.createIriMapping("ontology:resource", "property_unit", "property_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:equivalentProperty"),
                    config.createIriMapping("ontology:resource", "equivalent_unit", "equivalent_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "property_disjoints");
            TermMapping subject = config.createIriMapping("ontology:resource", "property_unit", "property_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:propertyDisjointWith"),
                    config.createIriMapping("ontology:resource", "disjoint_unit", "disjoint_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "property_chains");
            TermMapping subject = config.createIriMapping("ontology:resource", "property_unit", "property_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:propertyChainAxiom"),
                    config.createIriMapping("ontology:resource", "list_unit", "list_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "same_individuals");
            TermMapping subject = config.createIriMapping("ontology:resource", "individual_unit", "individual_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("ontology:resource", "same_unit", "same_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createLiteralMapping(xsdString, "same_string"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "different_individuals");
            TermMapping subject = config.createIriMapping("ontology:resource", "individual_unit", "individual_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:differentFrom"),
                    config.createIriMapping("ontology:resource", "different_unit", "different_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_members");
            TermMapping subject = config.createIriMapping("ontology:resource", "resource_unit", "resource_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:members"),
                    config.createIriMapping("ontology:resource", "list_unit", "list_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_distinct_members");
            TermMapping subject = config.createIriMapping("ontology:resource", "resource_unit", "resource_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:distinctMembers"),
                    config.createIriMapping("ontology:resource", "list_unit", "list_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "lists");
            TermMapping subject = config.createIriMapping("ontology:resource", "unit", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:first"),
                    config.createIriMapping("ontology:resource", "first_unit", "first_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:first"),
                    config.createLiteralMapping(xsdString, "first_string"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:first"),
                    config.createLiteralMapping(xsdInteger, "first_integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:first"),
                    config.createLiteralMapping(xsdFloat, "first_float"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:rest"),
                    config.createIriMapping("ontology:resource", "rest_unit", "rest_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "datatype_bases");
            TermMapping subject = config.createIriMapping("ontology:resource", "datatype_unit", "datatype_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onDatatype"),
                    config.createIriMapping("ontology:resource", "base_unit", "base_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "datatype_restrictions");
            TermMapping subject = config.createIriMapping("ontology:resource", "datatype_unit", "datatype_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:withRestrictions"),
                    config.createIriMapping("ontology:resource", "list_unit", "list_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "facet_restrictions");
            TermMapping subject = config.createIriMapping("ontology:resource", "restriction_unit", "restriction_id");

            for(String[] facet : new String[][] { { "LENGTH", "xsd:length" }, { "MIN_LENGTH", "xsd:minLength" },
                    { "MAX_LENGTH", "xsd:maxLength" }, { "PATTERN", "xsd:pattern" }, { "LANG_RANGE", "rdf:langRange" },
                    { "MIN_INCLUSIVE", "xsd:minInclusive" }, { "MAX_INCLUSIVE", "xsd:maxInclusive" },
                    { "MIN_EXCLUSIVE", "xsd:minExclusive" }, { "MAX_EXCLUSIVE", "xsd:maxExclusive" },
                    { "TOTAL_DIGITS", "xsd:totalDigits" }, { "FRACTION_DIGITS", "xsd:fractionDigits" } })
            {
                Conditions condition = config.createAreEqualCondition("facet",
                        "'" + facet[0] + "'::" + schema + ".facet_restriction_facet_type");

                config.addQuadMapping(table, graph, subject, config.createIriMapping(facet[1]),
                        config.createLiteralMapping(xsdString, "value_string"), condition);
                config.addQuadMapping(table, graph, subject, config.createIriMapping(facet[1]),
                        config.createLiteralMapping(xsdInteger, "value_integer"), condition);
                config.addQuadMapping(table, graph, subject, config.createIriMapping(facet[1]),
                        config.createLiteralMapping(xsdDouble, "value_double"), condition);
            }
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "ontology_relations");
            TermMapping subject = config.createIriMapping("ontology:resource", "ontology_unit", "ontology_id");

            for(String[] relation : new String[][] { { "IMPORTS", "owl:imports" }, { "VERSION_IRI", "owl:versionIRI" },
                    { "VERSION_INFO", "owl:versionInfo" }, { "PRIOR_VERSION", "owl:priorVersion" },
                    { "BACKWARD_COMPATIBLE_WITH", "owl:backwardCompatibleWith" },
                    { "INCOMPATIBLE_WITH", "owl:incompatibleWith" } })
            {
                Conditions condition = config.createAreEqualCondition("property",
                        "'" + relation[0] + "'::" + schema + ".ontology_relation_property_type");

                config.addQuadMapping(table, graph, subject, config.createIriMapping(relation[1]),
                        config.createIriMapping("ontology:resource", "target_unit", "target_id"), condition);
                config.addQuadMapping(table, graph, subject, config.createIriMapping(relation[1]),
                        config.createLiteralMapping(xsdString, "target_string"),
                        Conditions.and(condition, config.createIsNullCondition(table, "target_language")));
                config.addQuadMapping(table, graph, subject, config.createIriMapping(relation[1]),
                        config.createLiteralMapping(rdfLangString, "target_string", "target_language"), condition);
            }
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "hasvalue_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:hasValue"),
                    config.createIriMapping("ontology:resource", "value_unit", "value_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:hasValue"),
                    config.createLiteralMapping(xsdString, "value_string"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:hasValue"),
                    config.createLiteralMapping(xsdInteger, "value_integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:hasValue"),
                    config.createLiteralMapping(xsdFloat, "value_float"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:hasValue"),
                    config.createLiteralMapping(xsdBoolean, "value_boolean"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "hasself_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:hasSelf"),
                    config.createLiteralMapping(xsdBoolean, "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "qualifiedcardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:qualifiedCardinality"),
                    config.createLiteralMapping(xsdNonNegativeInteger, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "minqualifiedcardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:minQualifiedCardinality"),
                    config.createLiteralMapping(xsdNonNegativeInteger, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "maxqualifiedcardinality_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:maxQualifiedCardinality"),
                    config.createLiteralMapping(xsdNonNegativeInteger, "cardinality"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "incomplete_restrictions");
            TermMapping subject = config.createIriMapping("ontology:blank", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "restriction_classes");
            TermMapping subject = config.createIriMapping("ontology:blank", "restriction");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onClass"),
                    config.createIriMapping("ontology:resource", "class_unit", "class_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "restriction_dataranges");
            TermMapping subject = config.createIriMapping("ontology:blank", "restriction");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onDataRange"),
                    config.createIriMapping("ontology:resource", "datarange_unit", "datarange_id"));
        }
    }
}
