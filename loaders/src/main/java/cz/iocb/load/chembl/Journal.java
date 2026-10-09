package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ChEMBL.bibo;
import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.dcterms;
import static cz.iocb.load.chembl.ChEMBL.rdfType;
import static cz.iocb.load.chembl.ChEMBL.rdfsLabel;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Journal extends Updater
{
    static final String prefix = ChEMBL.chembl + "journal/CHEMBL_JRN_";
    static final String nullJournal = prefix + "null";

    private static final EntityTable<Integer> journals = new EntityTable<>("chembl.journals", intKey("id"), "chembl_id",
            uniqueVarchar("chembl_id"), varchar("label"), varchar("title"), varchar("short_title"), varchar("issn"),
            varchar("eissn"));


    static void load() throws IOException, SQLException
    {
        System.out.println("load journals ...");

        try(InputStream stream = getTtlStream(ChEMBL.file("journal")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> ChEMBL.checkType(subject, object, cco + "Journal");
                        case chemblId -> journals.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL_JRN_" + id));
                        case rdfsLabel -> journals.set(id, "label", getString(object));
                        case dcterms + "title" -> journals.set(id, "title", getString(object));
                        case bibo + "shortTitle" -> journals.set(id, "short_title", getString(object));
                        case bibo + "issn" -> journals.set(id, "issn", getString(object));
                        case bibo + "eissn" -> journals.set(id, "eissn", getString(object));
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        ChEMBL.finishLoad();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish journals ...");

        journals.store("journal");

        ChEMBL.finishLoad();
    }


    /*
     * Returns null for CHEMBL_JRN_null, which the mapping produces for the documents without a journal.
     */
    static Integer getJournalID(Node node) throws IOException
    {
        if(node.getURI().equals(nullJournal))
            return null;

        int id = TripleStreamProcessor.getIntID(node, prefix);
        journals.reference(id);
        return id;
    }
}
