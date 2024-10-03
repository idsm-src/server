package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class TargetComponent
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:targetcomponent", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/targetcomponent/CHEMBL_TC_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            Table table = new Table(schema, "component_sequences");
            NodeMapping subject = config.createIriMapping("chembl:targetcomponent", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:TargetComponent"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:resource", Ontology.unitTaxonomy, "tax_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("reference:ncbi-taxonomy", "tax_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("purl:uniprot", "accession"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:organismName"),
                    config.createLiteralMapping(xsdString, "organism"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:componentType"),
                    config.createLiteralMapping(xsdString, "component_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                    config.createLiteralMapping(xsdString, "description"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:proteinSequence"),
                    config.createLiteralMapping(xsdString, "sequence"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:resource", Ontology.unitNCBITaxon, "tax_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("chembl/TargetComponent.vm"));
        }

        {
            Table table = new Table(schema, "component_synonyms");
            NodeMapping subject = config.createIriMapping("chembl:targetcomponent", "component_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "component_synonym"));
        }
    }
}
