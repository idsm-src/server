package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ValueTable.column;
import static cz.iocb.load.chembl.ValueTable.enumeration;
import java.io.IOException;
import java.sql.SQLException;
import java.util.BitSet;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;



/*
 * The taxonomies are not entities of their own: both IRIs of a taxonomy are produced by the mapping from the taxonomy
 * columns of the entities, which this class checks, and their labels, which the target component and biocomponent
 * files give, are stored as they are.
 */
class Taxonomy
{
    static final class Forms
    {
        private final String entity;
        private final BitSet identifiers = new BitSet();
        private final BitSet ncbi = new BitSet();


        Forms(String entity)
        {
            this.entity = entity;
        }


        int getTaxonomy(int id, Node object) throws IOException
        {
            if(object.getURI().startsWith(identifiersPrefix))
            {
                identifiers.set(id);
                return TripleStreamProcessor.getIntID(object, identifiersPrefix);
            }
            else
            {
                ncbi.set(id);
                return TripleStreamProcessor.getIntID(object, ncbiPrefix);
            }
        }


        void check()
        {
            BitSet single = (BitSet) identifiers.clone();
            single.xor(ncbi);

            for(int id = single.nextSetBit(0); id >= 0; id = single.nextSetBit(id + 1))
                Problems.error("cco:taxonomy of one form only", entity + " " + id);
        }
    }


    private static final String identifiersPrefix = "http://identifiers.org/taxonomy/";
    private static final String ncbiPrefix = "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=";

    private static final ValueTable labels = new ValueTable("chembl.taxonomy_labels", column("taxonomy"),
            enumeration("type", "chembl.taxonomy_reference_type"), column("label"));


    static boolean isTaxonomy(Node node)
    {
        return node.getURI().startsWith(identifiersPrefix) || node.getURI().startsWith(ncbiPrefix);
    }


    static void addLabel(Node subject, Node predicate, Node object) throws IOException
    {
        if(!predicate.getURI().equals(ChEMBL.rdfsLabel))
            ChEMBL.unexpected(subject, predicate, object);
        else if(subject.getURI().startsWith(identifiersPrefix))
            labels.add(TripleStreamProcessor.getIntID(subject, identifiersPrefix), "IDENTIFIERS.ORG",
                    TripleStreamProcessor.getString(object));
        else
            labels.add(TripleStreamProcessor.getIntID(subject, ncbiPrefix), "NCBI TAXONOMY",
                    TripleStreamProcessor.getString(object));
    }


    static void finish() throws SQLException
    {
        System.out.println("finish taxonomies ...");

        labels.store();

        System.out.println();
    }
}
