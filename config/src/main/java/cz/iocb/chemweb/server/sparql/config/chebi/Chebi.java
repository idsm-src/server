package cz.iocb.chemweb.server.sparql.config.chebi;

import static cz.iocb.chemweb.server.sparql.config.chebi.ChebiConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;



public class Chebi
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chebi:restriction", INT4, "http://blank/ID_R"));
        config.addIriClass(new IntegerUserIriClass("chebi:axiom", INT4, "http://blank/ID_A"));
        config.addIriClass(
                new IntegerUserIriClass("chebi:molfile", INT4, "http://purl.obolibrary.org/obo/CHEBI_", "_Molfile"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chebi");

        {
            DatabaseTable table = new DatabaseTable(schema, "classes");
            TermMapping subject = config.createIriMapping("ontology:chebi", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Class"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "restrictions");
            TermMapping subject = config.createIriMapping("chebi:restriction", "id");

            config.addQuadMapping(table, graph, config.createIriMapping("ontology:chebi", "chebi"),
                    config.createIriMapping("rdfs:subClassOf"), subject);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:someValuesFrom"),
                    config.createIriMapping("ontology:chebi", "value_restriction"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "axioms");
            TermMapping subject = config.createIriMapping("chebi:axiom", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Axiom"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:annotatedProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:annotatedSource"),
                    config.createIriMapping("ontology:chebi", "chebi"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:annotatedTarget"),
                    config.createLiteralMapping(xsdString, "target"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasSynonymType"),
                    config.createIriMapping("ontology:uncategorized", "type_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasDbXref"),
                    config.createLiteralMapping(xsdString, "reference"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:source"),
                    config.createLiteralMapping(xsdString, "source"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "parents");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subClassOf"),
                    config.createIriMapping("ontology:chebi", "parent"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "stars");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:inSubset"),
                    config.createIriMapping("ontology:star", "star"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "replacements");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0100001"),
                    config.createIriMapping("ontology:chebi", "replacement"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "obsolescence_reasons");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0000231"),
                    config.createIriMapping("ontology:iao", "reason"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "references");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasDbXref"),
                    config.createLiteralMapping(xsdString, "reference"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "related_synonyms");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasRelatedSynonym"),
                    config.createLiteralMapping(xsdString, "synonym"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "exact_synonyms");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasExactSynonym"),
                    config.createLiteralMapping(xsdString, "synonym"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "formulas");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("chemrof:generalized_empirical_formula"),
                    config.createLiteralMapping(xsdString, "formula"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "masses");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chemrof:mass"),
                    config.createLiteralMapping(xsdString, "mass"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "monoisotopic_masses");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chemrof:monoisotopic_mass"),
                    config.createLiteralMapping(xsdString, "mass"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "alternative_identifiers");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasAlternativeId"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "labels");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "identifiers");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:id"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "namespaces");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasOBONamespace"),
                    config.createLiteralMapping(xsdString, "namespace"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "charges");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chemrof:charge"),
                    config.createLiteralMapping(xsdString, "charge"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "smiles_codes");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chemrof:smiles_string"),
                    config.createLiteralMapping(xsdString, "smiles"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "inchikeys");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chemrof:inchi_key_string"),
                    config.createLiteralMapping(xsdString, "inchikey"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "inchies");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chemrof:inchi_string"),
                    config.createLiteralMapping(xsdString, "inchi"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "wurcs_representations");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chemrof:wurcs_representation"),
                    config.createLiteralMapping(xsdString, "wurcs"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "definitions");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0000115"),
                    config.createLiteralMapping(xsdString, "definition"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "deprecated_flags");
            TermMapping subject = config.createIriMapping("ontology:chebi", "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:deprecated"),
                    config.createLiteralMapping(xsdBoolean, "flag"));
        }

        // extension
        {
            DatabaseTable table = new DatabaseTable("molecules", "chebi");
            TermMapping subject = config.createIriMapping("chebi:molfile", "id");
            LiteralClass molfileLiteral = new StringSubsetLiteralClass("chebi-molfile");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011120"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("ontology:chebi", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("ontology:chebi", "id"),
                    config.createIriMapping("sio:SIO_000008"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("ontology:chebi", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));
        }
    }
}
