package cz.iocb.chemweb.server.velocity;

import java.io.IOException;
import java.io.Writer;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;



public class UrlDirective extends Directive
{
    public static enum Context
    {
        DETAILS, NODE, PAGE
    };

    private Log log;



    @Override
    public String getName()
    {
        return "url";
    }


    @Override
    public int getType()
    {
        return BLOCK;
    }


    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException
    {
        super.init(rs, context, node);
        log = rs.getLog();
    }


    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        Context urlContext = (Context) context.get("urlContext");

        if(node.jjtGetNumChildren() != 2)
        {
            log.error("url directive: wrong number of arguments");
            return false;
        }

        String value = node.jjtGetChild(0).value(context).toString();
        value = value.replaceFirst("^<(.*)>$", "$1");


        if(urlContext == Context.DETAILS)
            writer.write("<a href=\"\" onClick=\"window.postMessage('" + value + "','*');return false;\">");
        else if(urlContext == Context.PAGE)
            writer.write("<a href=\"page?iri=" + value + "\">");
        else if(urlContext == Context.NODE)
            writer.write("<a href=\"page?iri=" + value + "\" target=\"_blank\">");

        node.jjtGetChild(node.jjtGetNumChildren() - 1).render(context, writer);

        if(urlContext != null)
            writer.write("</a>");

        return true;
    }
}
