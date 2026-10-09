package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ChEMBL.bibo;
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
import java.util.BitSet;
import java.util.Map.Entry;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Document extends Updater
{
    static final String prefix = ChEMBL.chembl + "document/CHEMBL";
    static final String pubmedPrefix = "http://identifiers.org/pubmed/";

    private static final EntityTable<Integer> documents = new EntityTable<>("chembl.documents", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), integer("journal"), varchar("type"), varchar("title"),
            integer("year"), varchar("volume"), varchar("issue"), varchar("first_page"), varchar("last_page"),
            varchar("doi"), integer("pubmed"));


    static void load() throws IOException, SQLException
    {
        System.out.println("load documents ...");

        InverseCheck journals = new InverseCheck("cco:hasJournal");
        BitSet withoutJournal = new BitSet();

        try(InputStream stream = getTtlStream(ChEMBL.file("document")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(subject.getURI().startsWith(Journal.prefix))
                    {
                        if(!predicate.getURI().equals(cco + "hasDocument"))
                        {
                            ChEMBL.unexpected(subject, predicate, object);
                            return;
                        }

                        Integer journalID = Journal.getJournalID(subject);
                        journals.inverse(getDocumentID(object), journalID == null ? -1 : journalID);
                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> ChEMBL.checkType(subject, object, cco + "Document");
                        case chemblId -> documents.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL" + id));
                        case rdfsLabel -> ChEMBL.checkValue(subject, predicate, object, "CHEMBL" + id);
                        case cco + "documentType" -> documents.set(id, "type", getString(object));
                        case dcterms + "title" -> documents.set(id, "title", getString(object));
                        case dcterms + "date" -> documents.set(id, "year", getInt(object));
                        case bibo + "volume" -> documents.set(id, "volume", getString(object));
                        case bibo + "issue" -> documents.set(id, "issue", getString(object));
                        case bibo + "pageStart" -> documents.set(id, "first_page", getString(object));
                        case bibo + "pageEnd" -> documents.set(id, "last_page", getString(object));
                        case bibo + "doi" -> documents.set(id, "doi", getString(object));
                        case bibo + "pmid" -> documents.set(id, "pubmed", getIntID(object, pubmedPrefix));
                        case cco + "hasJournal" ->
                        {
                            Integer journalID = Journal.getJournalID(object);

                            if(journalID == null)
                                withoutJournal.set(id);
                            else
                                documents.set(id, "journal", journalID);

                            journals.forward(id, journalID == null ? -1 : journalID);
                        }
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        journals.check();

        int chemblIdIndex = documents.columnIndex("chembl_id");
        int journalIndex = documents.columnIndex("journal");

        for(Entry<Integer, Object[]> entry : documents.rows())
        {
            boolean hasJournal = entry.getValue()[journalIndex] != null;

            if(hasJournal && withoutJournal.get(entry.getKey()))
                Problems.error("multiple journals of document", "CHEMBL" + entry.getKey());

            // the mapping produces cco:hasJournal CHEMBL_JRN_null for described documents without a journal
            if(entry.getValue()[chemblIdIndex] != null && !hasJournal && !withoutJournal.get(entry.getKey()))
                Problems.error("document without cco:hasJournal", "CHEMBL" + entry.getKey());
        }

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish documents ...");

        documents.store("document");

        System.out.println();
    }


    static int getDocumentID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        documents.reference(id);
        return id;
    }
}
