package cz.iocb.chemweb.server.velocity;

import java.util.Map;
import cz.iocb.sparql.engine.rdf.RdfTerm;
import cz.iocb.sparql.engine.rdf.Variable;



/**
 * Row of a {@link SparqlResult}, giving the values of the variables by their names.
 */
public class SparqlRow
{
    /**
     * Positions of the variables in the row.
     */
    private final Map<Variable, Integer> varNames;

    /**
     * Values of the row.
     */
    private final RdfTerm[] rowData;


    /**
     * Creates the row.
     *
     * @param varNames the positions of the variables in the row
     * @param rowData the values of the row
     */
    public SparqlRow(Map<Variable, Integer> varNames, RdfTerm[] rowData)
    {
        this.varNames = varNames;
        this.rowData = rowData;
    }


    /**
     * Returns the value of a variable.
     *
     * @param name the name of the variable
     * @return the value, null if the variable is unbound or unknown
     */
    public RdfTerm get(String name)
    {
        Integer idx = varNames.get(new Variable(name));

        if(idx == null)
            return null;

        return rowData[idx];
    }
}
