package cz.iocb.chemweb.server.velocity;

import java.util.ArrayList;
import cz.iocb.sparql.engine.rdf.RdfTerm;



@SuppressWarnings("serial")
public class SparqlResult extends ArrayList<SparqlRow>
{
    public int getCount()
    {
        return size();
    }


    public RdfTerm get(String name)
    {
        if(size() == 0)
            return null;

        return get(0).get(name);
    }
}
