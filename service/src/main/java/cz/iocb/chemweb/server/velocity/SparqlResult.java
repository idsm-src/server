package cz.iocb.chemweb.server.velocity;

import java.util.ArrayList;
import cz.iocb.sparql.engine.rdf.RdfTerm;



/**
 * Result of a query evaluated by the {@link SparqlDirective}, given to the templates as the list of its rows.
 */
@SuppressWarnings("serial")
public class SparqlResult extends ArrayList<SparqlRow>
{
    /**
     * Creates an empty result.
     */
    public SparqlResult()
    {
    }


    /**
     * Returns the number of the rows.
     *
     * @return the number of the rows
     */
    public int getCount()
    {
        return size();
    }


    /**
     * Returns the value of a variable in the first row.
     *
     * @param name the name of the variable
     * @return the value, null if the result is empty or the variable is unbound or unknown
     */
    public RdfTerm get(String name)
    {
        if(size() == 0)
            return null;

        return get(0).get(name);
    }
}
