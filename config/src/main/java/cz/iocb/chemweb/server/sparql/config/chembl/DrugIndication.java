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



public class DrugIndication
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:drug_indication", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/drug_indication/CHEMBL_IND_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        DatabaseTable table = new DatabaseTable(schema, "drug_indication");
        TermMapping subject = config.createIriMapping("chembl:drug_indication", "id");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:DrugIndication"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasMesh"),
                config.createIriMapping("identifiers:mesh_old", "mesh_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasEFO"),
                config.createIriMapping("ontology:resource", "efo_resource_unit", "efo_resource_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasMolecule"),
                config.createIriMapping("chembl:compound", "molecule_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:highestDevelopmentPhase"),
                config.createLiteralMapping(xsdInt, "max_phase_for_ind"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasMeshHeading"),
                config.createLiteralMapping(xsdString, "mesh_heading"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasEFOName"),
                config.createLiteralMapping(xsdString, "efo_term"));
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:compound", "molecule_id"),
                config.createIriMapping("cco:hasDrugIndication"), subject);

        // extension
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasMesh"),
                config.createIriMapping("mesh:heading", "mesh_id"));
    }
}
