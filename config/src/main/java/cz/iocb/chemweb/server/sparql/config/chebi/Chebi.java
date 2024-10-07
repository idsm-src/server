package cz.iocb.chemweb.server.sparql.config.chebi;

import static cz.iocb.chemweb.server.sparql.config.chebi.ChebiConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Chebi
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chebi:restriction", "integer", "http://blank/ID_R"));
        config.addIriClass(new IntegerUserIriClass("chebi:axiom", "integer", "http://blank/ID_A"));
        config.addIriClass(new IntegerUserIriClass("chebi:molfile", "integer", "http://purl.obolibrary.org/obo/CHEBI_",
                "_Molfile"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chebi");

        {
            Table table = new Table(schema, "classes");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Class"));
        }

        {
            Table table = new Table(schema, "restrictions");
            NodeMapping subject = config.createIriMapping("chebi:restriction", "id");

            config.addQuadMapping(table, graph,
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi"),
                    config.createIriMapping("rdfs:subClassOf"), subject);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Restriction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:onProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:someValuesFrom"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "value_restriction"));
        }

        {
            Table table = new Table(schema, "axioms");
            NodeMapping subject = config.createIriMapping("chebi:axiom", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("owl:Axiom"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:annotatedProperty"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:annotatedSource"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:annotatedTarget"),
                    config.createLiteralMapping(xsdString, "target"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasSynonymType"),
                    config.createIriMapping("ontology:resource", Ontology.unitUncategorized, "type_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasDbXref"),
                    config.createLiteralMapping(xsdString, "reference"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:source"),
                    config.createLiteralMapping(xsdString, "source"));
        }

        {
            Table table = new Table(schema, "parents");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subClassOf"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "parent"));
        }

        {
            Table table = new Table(schema, "stars");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:inSubset"),
                    config.createIriMapping("ontology:resource", Ontology.unitStar, "star"));
        }

        {
            Table table = new Table(schema, "replacements");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0100001"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "replacement"));
        }

        {
            Table table = new Table(schema, "obsolescence_reasons");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0000231"),
                    config.createIriMapping("ontology:resource", Ontology.unitIAO, "reason"));
        }

        {
            Table table = new Table(schema, "references");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasDbXref"),
                    config.createLiteralMapping(xsdString, "reference"));
        }

        {
            Table table = new Table(schema, "related_synonyms");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasRelatedSynonym"),
                    config.createLiteralMapping(xsdString, "synonym"));
        }

        {
            Table table = new Table(schema, "exact_synonyms");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasExactSynonym"),
                    config.createLiteralMapping(xsdString, "synonym"));
        }

        {
            Table table = new Table(schema, "formulas");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chebi:formula"),
                    config.createLiteralMapping(xsdString, "formula"));
        }

        {
            Table table = new Table(schema, "masses");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chebi:mass"),
                    config.createLiteralMapping(xsdString, "mass"));
        }

        {
            Table table = new Table(schema, "monoisotopic_masses");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chebi:monoisotopicmass"),
                    config.createLiteralMapping(xsdString, "mass"));
        }

        {
            Table table = new Table(schema, "alternative_identifiers");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasAlternativeId"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        {
            Table table = new Table(schema, "labels");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "label"));
        }

        {
            Table table = new Table(schema, "identifiers");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:id"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        {
            Table table = new Table(schema, "namespaces");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("oboInOwl:hasOBONamespace"),
                    config.createLiteralMapping(xsdString, "namespace"));
        }

        {
            Table table = new Table(schema, "charges");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chebi:charge"),
                    config.createLiteralMapping(xsdString, "charge"));
        }

        {
            Table table = new Table(schema, "smiles_codes");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chebi:smiles"),
                    config.createLiteralMapping(xsdString, "smiles"));
        }

        {
            Table table = new Table(schema, "inchikeys");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chebi:inchikey"),
                    config.createLiteralMapping(xsdString, "inchikey"));
        }

        {
            Table table = new Table(schema, "inchies");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("chebi:inchi"),
                    config.createLiteralMapping(xsdString, "inchi"));
        }

        {
            Table table = new Table(schema, "definitions");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0000115"),
                    config.createLiteralMapping(xsdString, "definition"));
        }

        {
            Table table = new Table(schema, "deprecated_flags");
            NodeMapping subject = config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:deprecated"),
                    config.createLiteralMapping(xsdBoolean, "flag"));
        }

        // extension
        {
            Table table = new Table("molecules", "chebi");
            NodeMapping subject = config.createIriMapping("chebi:molfile", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011120"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "molfile"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "id"),
                    config.createIriMapping("sio:SIO_000008"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdString, "molfile"));
        }
    }
}
