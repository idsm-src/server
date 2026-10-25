package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ValueTable.column;
import static cz.iocb.load.chembl.ValueTable.enumeration;
import static cz.iocb.load.common.TripleStreamProcessor.unexpected;
import java.io.IOException;
import java.sql.SQLException;
import java.util.BitSet;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;



/**
 * The taxonomies are not entities of their own: both IRIs of a taxonomy are produced by the mapping from the taxonomy
 * columns of the entities, which this class checks, and their labels, which the target component and biocomponent
 * files give, are stored as they are in the table {@code chembl.taxonomy_labels}.
 */
class Taxonomy
{
    /**
     * The forms of the {@code cco:taxonomy} IRIs given for the entities of one kind, which must give each taxonomy in
     * both forms, the identifiers.org one and the NCBI one.
     */
    static final class Forms
    {
        /**
         * The name of the kind of the entities, used in the reported errors.
         */
        private final String entity;

        /**
         * The ids of the entities with a taxonomy IRI of identifiers.org.
         */
        private final BitSet identifiers = new BitSet();

        /**
         * The ids of the entities with a taxonomy IRI of NCBI.
         */
        private final BitSet ncbi = new BitSet();


        /**
         * Creates the forms of the entities of one kind.
         *
         * @param entity the name of the kind of the entities, used in the reported errors
         */
        Forms(String entity)
        {
            this.entity = entity;
        }


        /**
         * Returns the taxonomy id of a {@code cco:taxonomy} IRI and records its form for the entity.
         *
         * @param id the id of the entity
         * @param object the taxonomy IRI
         * @return the taxonomy id
         * @throws IOException if the node is not a taxonomy IRI
         */
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


        /**
         * Reports an error for every entity whose taxonomy is given in one form only.
         */
        void check()
        {
            BitSet single = (BitSet) identifiers.clone();
            single.xor(ncbi);

            for(int id = single.nextSetBit(0); id >= 0; id = single.nextSetBit(id + 1))
                Problems.error("cco:taxonomy of one form only", entity + " " + id);
        }
    }


    /**
     * IRI prefix of the taxonomies of identifiers.org.
     */
    private static final String identifiersPrefix = "http://identifiers.org/taxonomy/";

    /**
     * IRI prefix of the taxonomies of NCBI.
     */
    private static final String ncbiPrefix = "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=";

    /**
     * Table of the labels of the taxonomies, each with the form of the IRI it belongs to.
     */
    private static final ValueTable labels = new ValueTable("chembl.taxonomy_labels", column("taxonomy"),
            enumeration("type", "chembl.taxonomy_reference_type"), column("label"));


    /**
     * Not instantiable.
     */
    private Taxonomy()
    {
    }


    /**
     * Returns whether a node is a taxonomy IRI of either form.
     *
     * @param node the node
     * @return true if the node is a taxonomy IRI
     */
    static boolean isTaxonomy(Node node)
    {
        return node.getURI().startsWith(identifiersPrefix) || node.getURI().startsWith(ncbiPrefix);
    }


    /**
     * Adds the label of a taxonomy given by an {@code rdfs:label} triple; a triple with another predicate is reported
     * as an error.
     *
     * @param subject the subject of the triple, a taxonomy IRI
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @throws IOException if the subject is not a taxonomy IRI or the object is not a string literal
     */
    static void addLabel(Node subject, Node predicate, Node object) throws IOException
    {
        if(!predicate.getURI().equals(ChEMBL.rdfsLabel))
            unexpected(subject, predicate, object);
        else if(subject.getURI().startsWith(identifiersPrefix))
            labels.add(TripleStreamProcessor.getIntID(subject, identifiersPrefix), "IDENTIFIERS.ORG",
                    TripleStreamProcessor.getString(object));
        else
            labels.add(TripleStreamProcessor.getIntID(subject, ncbiPrefix), "NCBI TAXONOMY",
                    TripleStreamProcessor.getString(object));
    }


    /**
     * Stores the collected labels of the taxonomies.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        System.out.println("finish taxonomies ...");

        labels.store();

        System.out.println();
    }
}
