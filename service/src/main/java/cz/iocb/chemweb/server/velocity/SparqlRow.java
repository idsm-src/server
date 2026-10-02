package cz.iocb.chemweb.server.velocity;

import java.util.Map;
import cz.iocb.sparql.engine.rdf.RdfTerm;
import cz.iocb.sparql.engine.rdf.Variable;



public class SparqlRow
{
    private final Map<Variable, Integer> varNames;
    private final RdfTerm[] rowData;


    public SparqlRow(Map<Variable, Integer> varNames, RdfTerm[] rowData)
    {
        this.varNames = varNames;
        this.rowData = rowData;
    }


    public RdfTerm get(String name)
    {
        Integer idx = varNames.get(new Variable(name));

        if(idx == null)
            return null;

        return rowData[idx];
    }
}
