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
            DatabaseTable table = new DatabaseTable(schema, "assay_bases");
            TermMapping subject = config.createIriMapping("chembl:assay", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Assay"), config.createIsNotNullCondition(table, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:taxonomy", "taxonomy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("reference:ncbi-taxonomy", "taxonomy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000205"),
                    config.createIriMapping("ontology:bao", "format_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasSource"),
                    config.createIriMapping("chembl:chembl_source", "source"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayXref"),
                    config.createIriMapping("reference:pubchem-assay", "pubchem_assay"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTarget"),
                    config.createIriMapping("chembl:target", "target"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasDocument"),
                    config.createIriMapping("chembl:document", "document"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasCellLine"),
                    config.createIriMapping("chembl:cell_line", "cell_line"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetConfScore"),
                    config.createLiteralMapping(xsdInt, "confidence_score"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetConfDesc"),
                    config.createLiteralMapping(xsdString, "confidence_desc"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetRelType"),
                    config.createLiteralMapping(xsdString, "relationship_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetRelDesc"),
                    config.createLiteralMapping(xsdString, "relationship_desc"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayType"),
                    config.createLiteralMapping(xsdString, "type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                    config.createLiteralMapping(xsdString, "description"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:organismName"),
                    config.createLiteralMapping(xsdString, "organism"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayCellType"),
                    config.createLiteralMapping(xsdString, "cell_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayStrain"),
                    config.createLiteralMapping(xsdString, "strain"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayTissue"),
                    config.createLiteralMapping(xsdString, "tissue"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayTestType"),
                    config.createLiteralMapping(xsdString, "test_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assaySubCellFrac"),
                    config.createLiteralMapping(xsdString, "subcellular_fraction"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:assayCategory"),
                    config.createLiteralMapping(xsdString, "category"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:cell_line", "cell_line"),
                    config.createIriMapping("cco:isCellLineForAssay"), subject);
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:chembl_source", "source"),
                    config.createIriMapping("cco:hasAssay"), subject);
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target"),
                    config.createIriMapping("cco:hasAssay"), subject);
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:document", "document"),
                    config.createIriMapping("cco:hasAssay"), subject);

            // an AID may be referenced by several assays
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pubchem-assay", "pubchem_assay"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PubchemBioassayRef"),
                    config.createIsNotNullCondition(table, "pubchem_assay"), true);

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("pubchem:bioassay", "pubchem_bioassay"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:ncbitaxon", "taxonomy"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "assay_reference_labels");

            config.addQuadMapping(table, graph, config.createIriMapping("reference:pubchem-assay", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "activity_bases");
            TermMapping subject = config.createIriMapping("chembl:assay", "assay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasActivity"),
                    config.createIriMapping("chembl:activity", "id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:activity", "id"),
                    config.createIriMapping("cco:hasAssay"), subject);
        }
    }
}
