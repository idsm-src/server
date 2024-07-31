package cz.iocb.load.ontology;

import java.io.InputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import cz.iocb.load.common.VoidStreamRDF;



public class Imports
{
    public static void main(String[] args)
    {
        Node predicate = NodeFactory.createURI("http://www.w3.org/2002/07/owl#imports");

        for(int i = 0; i < args.length; i++)
        {
            String file = args[i];
            InputStream stream = FileManager.get().open(file);

            RDFDataMgr.parse(new VoidStreamRDF()
            {
                @Override
                public void triple(Triple triple)
                {
                    if(triple.predicateMatches(predicate))
                    {
                        Node object = triple.getObject();

                        if(object.isURI())
                            System.out.println(object.getURI());
                    }
                }
            }, stream, file.endsWith(".ttl") ? Lang.TTL : Lang.RDFXML);
        }
    }
}
