package cz.iocb.chemweb.server.velocity;

import java.util.ArrayList;
import cz.iocb.sparql.engine.request.RdfNode;



@SuppressWarnings("serial")
public class SparqlResult extends ArrayList<SparqlRow>
{
    public int getCount()
    {
        return size();
    }


    public RdfNode get(String name)
    {
        if(size() == 0)
            return null;

        return get(0).get(name);
    }
}
