package cz.iocb.chemweb.shared.services.query;

import java.io.Serializable;



/**
 * Cell of the result table: the HTML rendering of an RDF term and, for an IRI, the IRI the cell links to.
 */
public class DataGridNode implements Serializable
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * HTML rendering of the term.
     */
    public String html;

    /**
     * IRI of the term, null if the term is not an IRI.
     */
    public String ref;

    /**
     * Creates an empty cell.
     */
    public DataGridNode()
    {
    }
}
