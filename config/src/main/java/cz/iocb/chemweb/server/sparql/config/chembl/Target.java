package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Target
{
    private static String targetRelationshipType = schema + ".target_relationship_type";


    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:target", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/target/CHEMBL"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            Table table = new Table(schema, "target_dictionary");
            NodeMapping subject = config.createIriMapping("chembl:target", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:SingleProtein"),
                    config.createAreEqualCondition("target_type", "'SINGLE PROTEIN'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Organism"),
                    config.createAreEqualCondition("target_type", "'ORGANISM'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:CellLineTarget"),
                    config.createAreEqualCondition("target_type", "'CELL-LINE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinComplex"),
                    config.createAreEqualCondition("target_type", "'PROTEIN COMPLEX'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinFamily"),
                    config.createAreEqualCondition("target_type", "'PROTEIN FAMILY'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Tissue"),
                    config.createAreEqualCondition("target_type", "'TISSUE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinSelectivityGroup"),
                    config.createAreEqualCondition("target_type", "'SELECTIVITY GROUP'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinProteinInteraction"),
                    config.createAreEqualCondition("target_type", "'PROTEIN-PROTEIN INTERACTION'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinComplexGroup"),
                    config.createAreEqualCondition("target_type", "'PROTEIN COMPLEX GROUP'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:NucleicAcid"),
                    config.createAreEqualCondition("target_type", "'NUCLEIC-ACID'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:SmallMoleculeTarget"),
                    config.createAreEqualCondition("target_type", "'SMALL MOLECULE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnknownTarget"),
                    config.createAreEqualCondition("target_type", "'UNKNOWN'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ChimericProtein"),
                    config.createAreEqualCondition("target_type", "'CHIMERIC PROTEIN'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Macromolecule"),
                    config.createAreEqualCondition("target_type", "'MACROMOLECULE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:SubCellular"),
                    config.createAreEqualCondition("target_type", "'SUBCELLULAR'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:OligosaccharideTarget"),
                    config.createAreEqualCondition("target_type", "'OLIGOSACCHARIDE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Metal"),
                    config.createAreEqualCondition("target_type", "'METAL'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinNucleicAcidComplex"),
                    config.createAreEqualCondition("target_type", "'PROTEIN NUCLEIC-ACID COMPLEX'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Phenotype"),
                    config.createAreEqualCondition("target_type", "'PHENOTYPE'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:NonMolecular"),
                    config.createAreEqualCondition("target_type", "'NON-MOLECULAR'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ADMET"),
                    config.createAreEqualCondition("target_type", "'ADMET'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnclassifiedTarget"),
                    config.createAreEqualCondition("target_type", "'LIPID'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnclassifiedTarget"),
                    config.createAreEqualCondition("target_type", "'UNCHECKED'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnclassifiedTarget"),
                    config.createAreEqualCondition("target_type", "'NO TARGET'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:resource", Ontology.unitTaxonomy, "tax_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("reference:ncbi-taxonomy", "tax_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:isTargetForCellLine"),
                    config.createIriMapping("chembl:cell_line", "cell_line_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:isSpeciesGroup"),
                    config.createLiteralMapping(true),
                    config.createAreEqualCondition("species_group_flag", "'true'::boolean"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "pref_name"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "pref_name"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:targetType"),
                    config.createLiteralMapping(xsdString, "target_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:organismName"),
                    config.createLiteralMapping(xsdString, "organism"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:cell_line", "cell_line_id"),
                    config.createIriMapping("cco:isCellLineForTarget"), subject);

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Target"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:taxonomy"),
                    config.createIriMapping("ontology:resource", Ontology.unitNCBITaxon, "tax_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("chembl/Target.vm"));
        }

        {
            Table table = new Table(schema, "target_relations");
            NodeMapping subject = config.createIriMapping("chembl:target", "target_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:relOverlapsWith"),
                    config.createIriMapping("chembl:target", "related_target_id"),
                    config.createAreEqualCondition("relationship", "'OVERLAPS WITH'::" + targetRelationshipType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:relSubsetOf"),
                    config.createIriMapping("chembl:target", "related_target_id"),
                    config.createAreEqualCondition("relationship", "'SUBSET OF'::" + targetRelationshipType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:relHasSubset"),
                    config.createIriMapping("chembl:target", "related_target_id"),
                    config.createAreEqualCondition("relationship", "'SUPERSET OF'::" + targetRelationshipType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:relEquivalentTo"),
                    config.createIriMapping("chembl:target", "related_target_id"),
                    config.createAreEqualCondition("relationship", "'EQUIVALENT TO'::" + targetRelationshipType));
        }

        {
            Table table = new Table(schema, "target_components");
            NodeMapping subject = config.createIriMapping("chembl:target", "target_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTargetComponent"),
                    config.createIriMapping("chembl:targetcomponent", "component_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:targetcomponent", "component_id"),
                    config.createIriMapping("cco:hasTarget"), subject);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("chembl:targetcomponent", "component_id"),
                    config.createAreEqualCondition("is_exact", "'true'::boolean"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:relatedMatch"),
                    config.createIriMapping("chembl:targetcomponent", "component_id"),
                    config.createAreEqualCondition("is_related", "'true'::boolean"));
        }
    }
}
