package cz.iocb.load.chembl;

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



class Source extends Updater
{
    static final String prefix = ChEMBL.chembl + "source/CHEMBL_SRC_";

    private static final EntityTable<Integer> sources = new EntityTable<>("chembl.sources", intKey("id"), "chembl_id",
            uniqueVarchar("chembl_id"), varchar("label"), varchar("description"));


    static void load() throws IOException, SQLException
    {
        System.out.println("load sources ...");

        try(InputStream stream = getTtlStream(ChEMBL.file("source")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> checkType(subject, object, cco + "Source");
                        case chemblId -> sources.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL_SRC_" + id));
                        case rdfsLabel -> sources.set(id, "label", getString(object));
                        case dcterms + "description" -> sources.set(id, "description", getString(object));
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish sources ...");

        sources.store("source");

        System.out.println();
    }


    static int getSourceID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        sources.reference(id);
        return id;
    }
}
