package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Target
{
    private static String targetRelationshipType = schema + ".target_relationship_type";


    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("chembl:target", INT4, "http://rdf.ebi.ac.uk/resource/chembl/target/CHEMBL"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            DatabaseTable table = new DatabaseTable(schema, "target_bases");
            TermMapping subject = config.createIriMapping("chembl:target", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:SingleProtein"),
                    config.createAreEqualCondition("type", "'SINGLE PROTEIN'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Organism"),
                    config.createAreEqualCondition("type", "'ORGANISM'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:CellLineTarget"),
                    config.createAreEqualCondition("type", "'CELL-LINE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinComplex"),
                    config.createAreEqualCondition("type", "'PROTEIN COMPLEX'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinFamily"),
                    config.createAreEqualCondition("type", "'PROTEIN FAMILY'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Tissue"), config.createAreEqualCondition("type", "'TISSUE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinSelectivityGroup"),
                    config.createAreEqualCondition("type", "'SELECTIVITY GROUP'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinProteinInteraction"),
                    config.createAreEqualCondition("type", "'PROTEIN-PROTEIN INTERACTION'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinComplexGroup"),
                    config.createAreEqualCondition("type", "'PROTEIN COMPLEX GROUP'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:NucleicAcid"),
                    config.createAreEqualCondition("type", "'NUCLEIC-ACID'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:SmallMoleculeTarget"),
                    config.createAreEqualCondition("type", "'SMALL MOLECULE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnknownTarget"),
                    config.createAreEqualCondition("type", "'UNKNOWN'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ChimericProtein"),
                    config.createAreEqualCondition("type", "'CHIMERIC PROTEIN'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Macromolecule"),
                    config.createAreEqualCondition("type", "'MACROMOLECULE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:SubCellular"),
                    config.createAreEqualCondition("type", "'SUBCELLULAR'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:OligosaccharideTarget"),
                    config.createAreEqualCondition("type", "'OLIGOSACCHARIDE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Metal"), config.createAreEqualCondition("type", "'METAL'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinNucleicAcidComplex"),
                    config.createAreEqualCondition("type", "'PROTEIN NUCLEIC-ACID COMPLEX'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Phenotype"),
                    config.createAreEqualCondition("type", "'PHENOTYPE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:NonMolecular"),
                    config.createAreEqualCondition("type", "'NON-MOLECULAR'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ADMET"), config.createAreEqualCondition("type", "'ADMET'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnclassifiedTarget"),
                    config.createAreEqualCondition("type", "'LIPID'::varchar", "'3D CELL CULTURE'::varchar",
                            "'UNCHECKED'::varchar", "'NO TARGET'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:taxonomy", "taxonomy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("reference:ncbi-taxonomy", "taxonomy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:isTargetForCellLine"),
                    config.createIriMapping("chembl:cell_line", "cell_line"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:isSpeciesGroup"),
                    config.createLiteralMapping(xsdBoolean, "species_group"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetType"),
                    config.createLiteralMapping(xsdString, "type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:organismName"),
                    config.createLiteralMapping(xsdString, "organism"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:cell_line", "cell_line"),
                    config.createIriMapping("cco:isCellLineForTarget"), subject);

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Target"), config.createIsNotNullCondition(table, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:ncbitaxon", "taxonomy"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "target_relations");
            TermMapping subject = config.createIriMapping("chembl:target", "target");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:relOverlapsWith"),
                    config.createIriMapping("chembl:target", "related"),
                    config.createAreEqualCondition("relationship", "'OVERLAPS WITH'::" + targetRelationshipType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:relSubsetOf"),
                    config.createIriMapping("chembl:target", "related"),
                    config.createAreEqualCondition("relationship", "'SUBSET OF'::" + targetRelationshipType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:relHasSubset"),
                    config.createIriMapping("chembl:target", "related"),
                    config.createAreEqualCondition("relationship", "'SUPERSET OF'::" + targetRelationshipType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:relEquivalentTo"),
                    config.createIriMapping("chembl:target", "related"),
                    config.createAreEqualCondition("relationship", "'EQUIVALENT TO'::" + targetRelationshipType));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "target_components");
            TermMapping subject = config.createIriMapping("chembl:target", "target");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTargetComponent"),
                    config.createIriMapping("chembl:targetcomponent", "component"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:targetcomponent", "component"),
                    config.createIriMapping("cco:hasTarget"), subject);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "target_exact_matches");

            config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target"),
                    config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("chembl:targetcomponent", "component"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "target_related_matches");

            config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target"),
                    config.createIriMapping("skos:relatedMatch"),
                    config.createIriMapping("chembl:targetcomponent", "component"));
        }
    }
}
