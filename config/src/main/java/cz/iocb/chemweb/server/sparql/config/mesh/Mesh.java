package cz.iocb.chemweb.server.sparql.config.mesh;

import static cz.iocb.chemweb.server.sparql.config.mesh.MeshConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.mesh.MeshConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDate;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;



public class Mesh
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new StringUserIriClass("mesh:resource", "http://id.nlm.nih.gov/mesh/",
                "[A-Z][0-9]+(\\.[0-9]+|[A-Z][0-9]+)*"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("mesh:heading");

        {
            DatabaseTable table = new DatabaseTable(schema, "resources");
            TermMapping subject = config.createIriMapping("mesh:resource", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:uncategorized", "type_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_alt_labels");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:altLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_previous_indexing_values");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:previousIndexing"),
                    config.createLiteralMapping(rdfLangStringEn, "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_sources");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:source"),
                    config.createLiteralMapping(rdfLangStringEn, "source"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_thesauruses");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:thesaurusID"),
                    config.createLiteralMapping(rdfLangStringEn, "thesaurus"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_labels");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_abbreviations");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:abbreviation"),
                    config.createLiteralMapping(rdfLangStringEn, "abbreviation"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_annotations");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:annotation"),
                    config.createLiteralMapping(rdfLangStringEn, "annotation"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_casn1_labels");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:casn1_label"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_consider_also_values");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:considerAlso"),
                    config.createLiteralMapping(rdfLangStringEn, "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_entry_versions");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:entryVersion"),
                    config.createLiteralMapping(rdfLangStringEn, "version"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_history_notes");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:historyNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_last_active_years");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:lastActiveYear"),
                    config.createLiteralMapping(rdfLangStringEn, "year"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_lexical_tags");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:lexicalTag"),
                    config.createLiteralMapping(rdfLangStringEn, "tag"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_notes");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:note"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_online_notes");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:onlineNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_pref_labels");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:prefLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_public_mesh_notes");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:publicMeSHNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_scope_notes");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:scopeNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_sort_versions");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:sortVersion"),
                    config.createLiteralMapping(rdfLangStringEn, "version"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_related_registry_numbers");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:relatedRegistryNumber"),
                    config.createLiteralMapping(xsdString, "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_identifiers");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:identifier"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_nlm_classification_numbers");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:nlmClassificationNumber"),
                    config.createLiteralMapping(xsdString, "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_registry_numbers");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:registryNumber"),
                    config.createLiteralMapping(xsdString, "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_frequencies");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:frequency"),
                    config.createLiteralMapping(xsdInt, "frequency"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_active_flags");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:active"),
                    config.createLiteralMapping(xsdBoolean, "flag"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_created_dates");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:dateCreated"),
                    config.createLiteralMapping(xsdDate, "date", "timezone"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_revised_dates");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:dateRevised"),
                    config.createLiteralMapping(xsdDate, "date", "timezone"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_established_dates");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:dateEstablished"),
                    config.createLiteralMapping(xsdDate, "date", "timezone"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_allowable_qualifiers");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:allowableQualifier"),
                    config.createIriMapping("mesh:resource", "qualifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_broader_concepts");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:broaderConcept"),
                    config.createIriMapping("mesh:resource", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_broader_descriptors");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:broaderDescriptor"),
                    config.createIriMapping("mesh:resource", "descriptor"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_broader_qualifiers");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:broaderQualifier"),
                    config.createIriMapping("mesh:resource", "qualifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_concepts");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:concept"),
                    config.createIriMapping("mesh:resource", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_indexer_consider_also_relations");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:indexerConsiderAlso"),
                    config.createIriMapping("mesh:resource", "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_mapped_to_relations");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:mappedTo"),
                    config.createIriMapping("mesh:resource", "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_narrower_concepts");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:narrowerConcept"),
                    config.createIriMapping("mesh:resource", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_pharmacological_actions");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:pharmacologicalAction"),
                    config.createIriMapping("mesh:resource", "action"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_preferred_mapped_to_relations");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:preferredMappedTo"),
                    config.createIriMapping("mesh:resource", "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_related_concepts");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:relatedConcept"),
                    config.createIriMapping("mesh:resource", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_see_also_relations");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:seeAlso"),
                    config.createIriMapping("mesh:resource", "reference"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_terms");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:term"),
                    config.createIriMapping("mesh:resource", "term"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_tree_numbers");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:treeNumber"),
                    config.createIriMapping("mesh:resource", "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_descriptors");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:hasDescriptor"),
                    config.createIriMapping("mesh:resource", "descriptor"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_qualifiers");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:hasQualifier"),
                    config.createIriMapping("mesh:resource", "qualifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_parent_tree_numbers");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:parentTreeNumber"),
                    config.createIriMapping("mesh:resource", "number"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_preferred_concepts");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:preferredConcept"),
                    config.createIriMapping("mesh:resource", "concept"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_preferred_terms");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:preferredTerm"),
                    config.createIriMapping("mesh:resource", "term"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "resource_use_instead_relations");
            TermMapping subject = config.createIriMapping("mesh:resource", "resource");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:useInstead"),
                    config.createIriMapping("mesh:resource", "value"));
        }
    }
}
