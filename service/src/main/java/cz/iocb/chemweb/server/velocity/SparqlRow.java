package cz.iocb.chemweb.server.velocity;

import java.util.HashMap;
import cz.iocb.sparql.engine.request.RdfNode;



public class SparqlRow
{
    private final HashMap<String, Integer> varNames;
    private final RdfNode[] rowData;


    public SparqlRow(HashMap<String, Integer> varNames, RdfNode[] rowData)
    {
        this.varNames = varNames;
        this.rowData = rowData;
    }


    public RdfNode get(String name)
    {
        Integer idx = varNames.get(name);

        if(idx == null)
            return null;

        return rowData[idx];
    }
}
