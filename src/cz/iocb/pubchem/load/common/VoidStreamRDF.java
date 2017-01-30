package cz.iocb.pubchem.load.common;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;



public class VoidStreamRDF implements StreamRDF
{
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
    public void finish()
    {
    }
}
