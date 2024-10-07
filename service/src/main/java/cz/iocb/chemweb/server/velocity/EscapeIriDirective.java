package cz.iocb.chemweb.server.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.request.IriNode;



public class EscapeIriDirective extends Directive
{
    public static final String IRI_PREFIXES_CONFIG = "IRI_PREFIXES_CONFIG";

    HashMap<String, String> prefixes;


    @Override
    public String getName()
    {
        return "escapeIRI";
    }


    @Override
    public int getType()
    {
        return LINE;
    }


    @Override
    @SuppressWarnings("unchecked")
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException
    {
        super.init(rs, context, node);

        prefixes = (HashMap<String, String>) rs.getApplicationAttribute(IRI_PREFIXES_CONFIG);
    }


    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        if(node.jjtGetNumChildren() != 1)
        {
            log.error("url directive: wrong number of arguments");
            return false;
        }

        Object value = node.jjtGetChild(0).value(context);

        switch(value)
        {
            case IriNode iri -> writer.write(escape(iri.getValue()));
            case Object obj -> writer.write(escape(obj.toString()));
        }

        return true;
    }


    private String escape(String value)
    {
        String iri = IRI.toPrefixedIRI(value, prefixes);
        return iri != null ? iri : value;
    }
}
