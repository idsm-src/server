package cz.iocb.load.common;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;



/**
 * Stream of parsed RDF that ignores everything; a subclass overrides the methods of the events it handles.
 */
public class VoidStreamRDF implements StreamRDF
{
    /**
     * Creates the stream.
     */
    public VoidStreamRDF()
    {
    }


    @Override
    public void start()
    {
    }

    @Override
    public void triple(Triple triple)
    {
    }

    @Override
    public void quad(Quad quad)
    {
    }

    @Override
    public void base(String base)
    {
    }

    @Override
    public void prefix(String prefix, String iri)
    {
    }

    @Override
    public void version(String version)
    {
    }

    @Override
    public void finish()
    {
    }
}
