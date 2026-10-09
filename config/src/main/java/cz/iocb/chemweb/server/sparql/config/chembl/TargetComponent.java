package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class TargetComponent
{
    private static String componentReferenceType = schema + ".component_reference_type";


    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:component", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/targetcomponent/CHEMBL_TC_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            DatabaseTable table = new DatabaseTable(schema, "components");
            TermMapping subject = config.createIriMapping("chembl:component", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:TargetComponent"),
                    config.createIsNotNullCondition(table, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:taxonomy", "taxonomy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("reference:ncbi_taxonomy", "taxonomy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("purl:uniprot", "accession"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:organismName"),
                    config.createLiteralMapping(xsdString, "organism"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:componentType"),
                    config.createLiteralMapping(xsdString, "type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                    config.createLiteralMapping(xsdString, "description"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:proteinSequence"),
                    config.createLiteralMapping(xsdString, "sequence"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:ncbitaxon", "taxonomy"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "component_alternatives");
            TermMapping subject = config.createIriMapping("chembl:component", "component");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "component_references");
            TermMapping subject = config.createIriMapping("chembl:component", "component");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createAreEqualCondition("type", "'GO PROCESS'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createAreEqualCondition("type", "'GO FUNCTION'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createAreEqualCondition("type", "'GO COMPONENT'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("identifiers:pdb", "reference"),
                    config.createAreEqualCondition("type", "'PDB'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("identifiers:interpro", "reference"),
                    config.createAreEqualCondition("type", "'INTERPRO'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("identifiers:reactome_old", "reference"),
                    config.createAreEqualCondition("type", "'REACTOME'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("identifiers:pfam", "reference"),
                    config.createAreEqualCondition("type", "'PFAM'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("identifiers:ec_code", "reference"),
                    config.createAreEqualCondition("type", "'ENZYME CLASS'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("identifiers:intact", "reference"),
                    config.createAreEqualCondition("type", "'INTACT'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("purl:uniprot", "reference"),
                    config.createAreEqualCondition("type", "'UNIPROT'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("reference:pharmgkb_gene", "reference"),
                    config.createAreEqualCondition("type", "'PHARMGKB'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("reference:timbal", "reference"),
                    config.createAreEqualCondition("type", "'TIMBAL'::" + componentReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetCmptXref"),
                    config.createIriMapping("reference:cgd", "reference"),
                    config.createAreEqualCondition("type", "'CGD'::" + componentReferenceType));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("pdbo:link_to_pdb"),
                    config.createIriMapping("rdf:wwpdb", "reference"),
                    config.createAreEqualCondition("type", "'PDB'::" + componentReferenceType));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "component_references");

            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:GoProcessRef"),
                    config.createAreEqualCondition("type", "'GO PROCESS'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:GoFunctionRef"),
                    config.createAreEqualCondition("type", "'GO FUNCTION'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:GoComponentRef"),
                    config.createAreEqualCondition("type", "'GO COMPONENT'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:pdb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ProteinDataBankRef"),
                    config.createAreEqualCondition("type", "'PDB'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:interpro", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:InterproRef"),
                    config.createAreEqualCondition("type", "'INTERPRO'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:reactome_old", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ReactomeRef"),
                    config.createAreEqualCondition("type", "'REACTOME'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:pfam", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PfamRef"),
                    config.createAreEqualCondition("type", "'PFAM'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:ec_code", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:EnzymeClassRef"),
                    config.createAreEqualCondition("type", "'ENZYME CLASS'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:intact", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:IntactRef"),
                    config.createAreEqualCondition("type", "'INTACT'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("purl:uniprot", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:UniprotRef"),
                    config.createAreEqualCondition("type", "'UNIPROT'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pharmgkb_gene", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PharmgkbRef"),
                    config.createAreEqualCondition("type", "'PHARMGKB'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:timbal", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:TimbalRef"),
                    config.createAreEqualCondition("type", "'TIMBAL'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:cgd", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:CGDRef"),
                    config.createAreEqualCondition("type", "'CGD'::" + componentReferenceType), true);

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'GO PROCESS'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'GO FUNCTION'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'GO COMPONENT'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:pdb", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'PDB'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:interpro", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'INTERPRO'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:reactome_old", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'REACTOME'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:pfam", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'PFAM'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:ec_code", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'ENZYME CLASS'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:intact", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'INTACT'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("purl:uniprot", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'UNIPROT'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pharmgkb_gene", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'PHARMGKB'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:timbal", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'TIMBAL'::" + componentReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:cgd", "reference"),
                    config.createIriMapping("dc:identifier"), config.createLiteralMapping(xsdString, "reference"),
                    config.createAreEqualCondition("type", "'CGD'::" + componentReferenceType), true);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "component_reference_labels");

            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'GO PROCESS'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'GO FUNCTION'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:obo.go", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'GO COMPONENT'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:pdb", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'PDB'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:interpro", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'INTERPRO'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:reactome_old", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'REACTOME'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:pfam", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'PFAM'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:ec_code", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'ENZYME CLASS'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:intact", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'INTACT'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("purl:uniprot", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'UNIPROT'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pharmgkb_gene", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'PHARMGKB'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:timbal", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'TIMBAL'::" + componentReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:cgd", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'CGD'::" + componentReferenceType));
        }
    }
}
