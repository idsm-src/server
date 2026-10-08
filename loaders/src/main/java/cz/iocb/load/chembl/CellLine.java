package cz.iocb.load.chembl;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitCLO;
import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.dcterms;
import static cz.iocb.load.chembl.ChEMBL.rdfType;
import static cz.iocb.load.chembl.ChEMBL.rdfsLabel;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class CellLine extends Updater
{
    static final String prefix = ChEMBL.chembl + "cell_line/CHEMBL";

    private static final EntityTable<Integer> cellLines = new EntityTable<>("chembl.cell_line_bases", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), varchar("label"), varchar("description"), varchar("organism"),
            integer("taxonomy"), varchar("cellosaurus"), integer("clo_id"), integer("efo_unit"), integer("efo_id"));


    static void load() throws IOException, SQLException
    {
        System.out.println("load cell lines ...");

        Taxonomy.Forms taxonomies = new Taxonomy.Forms("cell line");

        try(InputStream stream = getTtlStream(ChEMBL.file("cellline")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> ChEMBL.checkType(subject, object, cco + "CellLine");
                        case chemblId -> cellLines.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL" + id));
                        case rdfsLabel -> cellLines.set(id, "label", getString(object));
                        case dcterms + "description" -> cellLines.set(id, "description", getString(object));
                        case cco + "organismName" -> cellLines.set(id, "organism", getString(object));
                        case cco + "taxonomy" -> cellLines.set(id, "taxonomy", taxonomies.getTaxonomy(id, object));
                        case cco + "cellosaurusId" -> cellLines.set(id, "cellosaurus", getString(object));
                        case cco + "hasCLO" ->
                        {
                            Pair<Integer, Integer> clo = ChEMBL.getOntologyId(subject, predicate, object, unitCLO);

                            if(clo != null)
                                cellLines.set(id, "clo_id", clo.getTwo());
                        }
                        case cco + "hasEFO" ->
                        {
                            Pair<Integer, Integer> efo = ChEMBL.getOntologyId(subject, predicate, object, null);

                            if(efo != null)
                            {
                                cellLines.set(id, "efo_unit", efo.getOne());
                                cellLines.set(id, "efo_id", efo.getTwo());
                            }
                        }
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        taxonomies.check();

        ChEMBL.finishLoad();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish cell lines ...");

        cellLines.store("cell line");

        ChEMBL.finishLoad();
    }


    static int getCellLineID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        cellLines.reference(id);
        return id;
    }
}
