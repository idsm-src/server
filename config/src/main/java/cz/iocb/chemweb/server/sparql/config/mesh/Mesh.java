package cz.iocb.chemweb.server.sparql.config.mesh;

import static cz.iocb.chemweb.server.sparql.config.mesh.MeshConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.mesh.MeshConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDate;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;



public class Mesh
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new StringUserIriClass("mesh:heading", "http://id.nlm.nih.gov/mesh/",
                "[A-Z][0-9]+(\\.[0-9]+|[A-Z][0-9]+)*"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("mesh:heading");

        {
            DatabaseTable table = new DatabaseTable(schema, "mesh_bases");
            TermMapping subject = config.createIriMapping("mesh:heading", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitUncategorized, "type_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "alt_labels");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:altLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "previous_indexing_values");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:previousIndexing"),
                    config.createLiteralMapping(rdfLangStringEn, "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "sources");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:source"),
                    config.createLiteralMapping(rdfLangStringEn, "source"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "thesauruses");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:thesaurusID"),
                    config.createLiteralMapping(rdfLangStringEn, "thesaurus"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "labels");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "abbreviations");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:abbreviation"),
                    config.createLiteralMapping(rdfLangStringEn, "abbreviation"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "annotations");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:annotation"),
                    config.createLiteralMapping(rdfLangStringEn, "annotation"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "casn1_labels");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:casn1_label"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "consider_also_values");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:considerAlso"),
                    config.createLiteralMapping(rdfLangStringEn, "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "entry_versions");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:entryVersion"),
                    config.createLiteralMapping(rdfLangStringEn, "version"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "history_notes");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:historyNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "last_active_years");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:lastActiveYear"),
                    config.createLiteralMapping(rdfLangStringEn, "year"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "lexical_tags");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:lexicalTag"),
                    config.createLiteralMapping(rdfLangStringEn, "tag"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "notese_notes");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:note"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "online_notes");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:onlineNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "pref_labels");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:prefLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "public_mesh_notes");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:publicMeSHNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "scope_notes");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:scopeNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "sort_versions");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:sortVersion"),
                    config.createLiteralMapping(rdfLangStringEn, "version"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "related_registry_numbers");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:relatedRegistryNumber"),
                    config.createLiteralMapping(xsdString, "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "identifiers");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:identifier"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "nlm_cassification_numbers");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:nlmClassificationNumber"),
                    config.createLiteralMapping(xsdString, "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "registry_numbers");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:registryNumber"),
                    config.createLiteralMapping(xsdString, "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "frequencies");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:frequency"),
                    config.createLiteralMapping(xsdInt, "frequency"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "active_property");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:active"),
                    config.createLiteralMapping(xsdBoolean, "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "created_dates");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:dateCreated"),
                    config.createLiteralMapping(xsdDate, "date", "timezone"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "revised_dates");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:dateRevised"),
                    config.createLiteralMapping(xsdDate, "date", "timezone"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "established_dates");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:dateEstablished"),
                    config.createLiteralMapping(xsdDate, "date", "timezone"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "allowable_qualifiers");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:allowableQualifier"),
                    config.createIriMapping("mesh:heading", "qualifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "broader_concepts");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:broaderConcept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "broader_descriptors");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:broaderDescriptor"),
                    config.createIriMapping("mesh:heading", "descriptor"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "broader_qualifiers");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:broaderQualifier"),
                    config.createIriMapping("mesh:heading", "qualifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "concepts");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:concept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "indexer_consider_also_relations");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:indexerConsiderAlso"),
                    config.createIriMapping("mesh:heading", "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "mapped_to_relations");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:mappedTo"),
                    config.createIriMapping("mesh:heading", "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "narrower_concepts");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:narrowerConcept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "pharmacological_actions");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:pharmacologicalAction"),
                    config.createIriMapping("mesh:heading", "action"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "preferred_mapped_to_relations");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:preferredMappedTo"),
                    config.createIriMapping("mesh:heading", "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "related_concepts");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:relatedConcept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "see_also_relations");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:seeAlso"),
                    config.createIriMapping("mesh:heading", "reference"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "terms");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:term"),
                    config.createIriMapping("mesh:heading", "term"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "tree_numbers");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:treeNumber"),
                    config.createIriMapping("mesh:heading", "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptors");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:hasDescriptor"),
                    config.createIriMapping("mesh:heading", "descriptor"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "qualifiers");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:hasQualifier"),
                    config.createIriMapping("mesh:heading", "qualifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "parent_tree_numbers");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:parentTreeNumber"),
                    config.createIriMapping("mesh:heading", "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "preferred_concept");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:preferredConcept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "preferred_term");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:preferredTerm"),
                    config.createIriMapping("mesh:heading", "term"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "use_instead_relations");
            TermMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:useInstead"),
                    config.createIriMapping("mesh:heading", "value"));
        }
    }
}
