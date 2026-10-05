package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Assay
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("chembl:assay", INT4, "http://rdf.ebi.ac.uk/resource/chembl/assay/CHEMBL"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            DatabaseTable table = new DatabaseTable(schema, "assays");
            TermMapping subject = config.createIriMapping("chembl:assay", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Assay"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:taxonomy", "assay_tax_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("reference:ncbi-taxonomy", "assay_tax_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000205"),
                    config.createIriMapping("ontology:bao", "bao_format_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasSource"),
                    config.createIriMapping("chembl:chembl_source", "src_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayXref"),
                    config.createIriMapping("reference:pubchem-assay", "pubchem_assay_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTarget"),
                    config.createIriMapping("chembl:target", "target_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasDocument"),
                    config.createIriMapping("chembl:document", "document_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasCellLine"),
                    config.createIriMapping("chembl:cell_line", "cell_line_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetConfScore"),
                    config.createLiteralMapping(xsdInt, "confidence_score"),
                    config.createAreNotEqualCondition("confidence_score", "'0'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetRelType"),
                    config.createLiteralMapping(xsdString, "relationship_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                    config.createLiteralMapping(xsdString, "description"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:organismName"),
                    config.createLiteralMapping(xsdString, "assay_organism"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayCellType"),
                    config.createLiteralMapping(xsdString, "assay_cell_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayStrain"),
                    config.createLiteralMapping(xsdString, "assay_strain"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayTissue"),
                    config.createLiteralMapping(xsdString, "assay_tissue"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayTestType"),
                    config.createLiteralMapping(xsdString, "assay_test_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assaySubCellFrac"),
                    config.createLiteralMapping(xsdString, "assay_subcellular_fraction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayCategory"),
                    config.createLiteralMapping(xsdString, "assay_category"));
            config.addQuadMapping(table, new DatabaseTable(schema, "assay_type"), "assay_type", "assay_type", graph,
                    subject, config.createIriMapping("cco:assayType"),
                    config.createLiteralMapping(xsdString, "assay_desc"));
            config.addQuadMapping(table, new DatabaseTable(schema, "relationship_type"), "relationship_type",
                    "relationship_type", graph, subject, config.createIriMapping("cco:targetRelDesc"),
                    config.createLiteralMapping(xsdString, "relationship_desc"));
            config.addQuadMapping(table, new DatabaseTable(schema, "confidence_score_lookup"), "confidence_score",
                    "confidence_score", graph, subject, config.createIriMapping("cco:targetConfDesc"),
                    config.createLiteralMapping(xsdString, "description"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:cell_line", "cell_line_id"),
                    config.createIriMapping("cco:isCellLineForAssay"), subject);
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:chembl_source", "src_id"),
                    config.createIriMapping("cco:hasAssay"), subject);
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target_id"),
                    config.createIriMapping("cco:hasAssay"), subject);
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:document", "document_id"),
                    config.createIriMapping("cco:hasAssay"), subject);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pubchem-assay", "pubchem_assay_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "(chembl_id || ' PubChem BioAssay Reference: ' || pubchem_assay_id)::varchar"),
                    config.createIsNotNullCondition(table, "pubchem_assay_id"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("pubchem:bioassay", "pubchem_assay_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:ncbitaxon", "assay_tax_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "pubchem_assays");
            TermMapping subject = config.createIriMapping("reference:pubchem-assay", "pubchem_assay_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:PubchemBioassayRef"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "activities");
            TermMapping subject = config.createIriMapping("chembl:assay", "assay_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasActivity"),
                    config.createIriMapping("chembl:activity", "id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:activity", "id"),
                    config.createIriMapping("cco:hasAssay"), subject);
        }
    }
}
