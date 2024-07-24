package cz.iocb.chemweb.server.sparql.config.mesh;

import static cz.iocb.chemweb.server.sparql.config.mesh.MeshConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.mesh.MeshConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDate;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
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
            Table table = new Table(schema, "mesh_bases");
            NodeMapping subject = config.createIriMapping("mesh:heading", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitUncategorized, "type_id"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("mesh/Mesh.vm"));
        }

        {
            Table table = new Table(schema, "alt_labels");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:altLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            Table table = new Table(schema, "previous_indexing_values");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:previousIndexing"),
                    config.createLiteralMapping(rdfLangStringEn, "value"));
        }

        {
            Table table = new Table(schema, "sources");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:source"),
                    config.createLiteralMapping(rdfLangStringEn, "source"));
        }

        {
            Table table = new Table(schema, "thesauruses");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:thesaurusID"),
                    config.createLiteralMapping(rdfLangStringEn, "thesaurus"));
        }

        {
            Table table = new Table(schema, "labels");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            Table table = new Table(schema, "abbreviations");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:abbreviation"),
                    config.createLiteralMapping(rdfLangStringEn, "abbreviation"));
        }

        {
            Table table = new Table(schema, "annotations");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:annotation"),
                    config.createLiteralMapping(rdfLangStringEn, "annotation"));
        }

        {
            Table table = new Table(schema, "casn1_labels");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:casn1_label"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            Table table = new Table(schema, "consider_also_values");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:considerAlso"),
                    config.createLiteralMapping(rdfLangStringEn, "value"));
        }

        {
            Table table = new Table(schema, "entry_versions");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:entryVersion"),
                    config.createLiteralMapping(rdfLangStringEn, "version"));
        }

        {
            Table table = new Table(schema, "history_notes");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:historyNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            Table table = new Table(schema, "last_active_years");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:lastActiveYear"),
                    config.createLiteralMapping(rdfLangStringEn, "year"));
        }

        {
            Table table = new Table(schema, "lexical_tags");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:lexicalTag"),
                    config.createLiteralMapping(rdfLangStringEn, "tag"));
        }

        {
            Table table = new Table(schema, "notese_notes");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:note"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            Table table = new Table(schema, "online_notes");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:onlineNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            Table table = new Table(schema, "pref_labels");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:prefLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
        }

        {
            Table table = new Table(schema, "public_mesh_notes");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:publicMeSHNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            Table table = new Table(schema, "scope_notes");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:scopeNote"),
                    config.createLiteralMapping(rdfLangStringEn, "note"));
        }

        {
            Table table = new Table(schema, "sort_versions");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:sortVersion"),
                    config.createLiteralMapping(rdfLangStringEn, "version"));
        }

        {
            Table table = new Table(schema, "related_registry_numbers");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:relatedRegistryNumber"),
                    config.createLiteralMapping(xsdString, "number"));
        }

        {
            Table table = new Table(schema, "identifiers");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:identifier"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        {
            Table table = new Table(schema, "nlm_cassification_numbers");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:nlmClassificationNumber"),
                    config.createLiteralMapping(xsdString, "number"));
        }

        {
            Table table = new Table(schema, "registry_numbers");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:registryNumber"),
                    config.createLiteralMapping(xsdString, "number"));
        }

        {
            Table table = new Table(schema, "frequencies");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:frequency"),
                    config.createLiteralMapping(xsdInt, "frequency"));
        }

        {
            Table table = new Table(schema, "active_property");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:active"),
                    config.createLiteralMapping(xsdBoolean, "value"));
        }

        {
            Table table = new Table(schema, "created_dates");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:dateCreated"),
                    config.createLiteralMapping(xsdDate, "date", "timezone"));
        }

        {
            Table table = new Table(schema, "revised_dates");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:dateRevised"),
                    config.createLiteralMapping(xsdDate, "date", "timezone"));
        }

        {
            Table table = new Table(schema, "established_dates");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:dateEstablished"),
                    config.createLiteralMapping(xsdDate, "date", "timezone"));
        }

        {
            Table table = new Table(schema, "allowable_qualifiers");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:allowableQualifier"),
                    config.createIriMapping("mesh:heading", "qualifier"));
        }

        {
            Table table = new Table(schema, "broader_concepts");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:broaderConcept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            Table table = new Table(schema, "broader_descriptors");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:broaderDescriptor"),
                    config.createIriMapping("mesh:heading", "descriptor"));
        }

        {
            Table table = new Table(schema, "broader_qualifiers");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:broaderQualifier"),
                    config.createIriMapping("mesh:heading", "qualifier"));
        }

        {
            Table table = new Table(schema, "concepts");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:concept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            Table table = new Table(schema, "indexer_consider_also_relations");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:indexerConsiderAlso"),
                    config.createIriMapping("mesh:heading", "value"));
        }

        {
            Table table = new Table(schema, "mapped_to_relations");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:mappedTo"),
                    config.createIriMapping("mesh:heading", "value"));
        }

        {
            Table table = new Table(schema, "narrower_concepts");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:narrowerConcept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            Table table = new Table(schema, "pharmacological_actions");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:pharmacologicalAction"),
                    config.createIriMapping("mesh:heading", "action"));
        }

        {
            Table table = new Table(schema, "preferred_mapped_to_relations");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:preferredMappedTo"),
                    config.createIriMapping("mesh:heading", "value"));
        }

        {
            Table table = new Table(schema, "related_concepts");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:relatedConcept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            Table table = new Table(schema, "see_also_relations");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:seeAlso"),
                    config.createIriMapping("mesh:heading", "reference"));
        }

        {
            Table table = new Table(schema, "terms");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:term"),
                    config.createIriMapping("mesh:heading", "term"));
        }

        {
            Table table = new Table(schema, "tree_numbers");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:treeNumber"),
                    config.createIriMapping("mesh:heading", "number"));
        }

        {
            Table table = new Table(schema, "descriptors");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:hasDescriptor"),
                    config.createIriMapping("mesh:heading", "descriptor"));
        }

        {
            Table table = new Table(schema, "qualifiers");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:hasQualifier"),
                    config.createIriMapping("mesh:heading", "qualifier"));
        }

        {
            Table table = new Table(schema, "parent_tree_numbers");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:parentTreeNumber"),
                    config.createIriMapping("mesh:heading", "number"));
        }

        {
            Table table = new Table(schema, "preferred_concept");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:preferredConcept"),
                    config.createIriMapping("mesh:heading", "concept"));
        }

        {
            Table table = new Table(schema, "preferred_term");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:preferredTerm"),
                    config.createIriMapping("mesh:heading", "term"));
        }

        {
            Table table = new Table(schema, "use_instead_relations");
            NodeMapping subject = config.createIriMapping("mesh:heading", "mesh");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("meshv:useInstead"),
                    config.createIriMapping("mesh:heading", "value"));
        }
    }
}
