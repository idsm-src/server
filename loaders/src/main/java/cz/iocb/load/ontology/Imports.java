package cz.iocb.load.ontology;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import cz.iocb.load.common.VoidStreamRDF;



public class Imports
{
    public static void main(String[] args) throws IOException
    {
        Node predicate = NodeFactory.createURI("http://www.w3.org/2002/07/owl#imports");

        for(int i = 0; i < args.length; i++)
        {
            String file = args[i];
            try(InputStream stream = new FileInputStream(file))
            {
                RDFParser.source(stream).lang(file.endsWith(".ttl") ? Lang.TTL : Lang.RDFXML).parse(new VoidStreamRDF()
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
                });
            }
        }
    }
}
