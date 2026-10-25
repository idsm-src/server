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
import cz.iocb.sparql.engine.model.IriNode;
import cz.iocb.sparql.engine.rdf.Iri;



/**
 * Velocity line directive {@code #escapeIRI(value)} writing an IRI as a prefixed name of the prefixes given by the
 * application attribute {@link #IRI_PREFIXES_CONFIG}, or unchanged if it has no prefixed name.
 */
public class EscapeIriDirective extends Directive
{
    /**
     * Name of the application attribute holding the prefixes.
     */
    public static final String IRI_PREFIXES_CONFIG = "IRI_PREFIXES_CONFIG";

    /**
     * IRIs of the prefixes by their names.
     */
    HashMap<String, String> prefixes;


    /**
     * Creates the directive.
     */
    public EscapeIriDirective()
    {
    }


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
            case Iri iri -> writer.write(escape(iri.getValue()));
            case Object obj -> writer.write(escape(obj.toString()));
        }

        return true;
    }


    /**
     * Converts an IRI to a prefixed name, if it has one.
     *
     * @param value the IRI
     * @return the prefixed name, or the IRI if it has no prefixed name
     */
    private String escape(String value)
    {
        String iri = IriNode.toPrefixedIri(value, prefixes);
        return iri != null ? iri : value;
    }
}
