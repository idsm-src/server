package cz.iocb.chemweb.server.velocity;

import java.io.IOException;
import java.io.Writer;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import cz.iocb.sparql.engine.request.LanguageTaggedLiteral;
import cz.iocb.sparql.engine.request.TypedLiteral;



public class EscapeHtmlDirective extends Directive
{
    @Override
    public String getName()
    {
        return "escapeHTML";
    }


    @Override
    public int getType()
    {
        return LINE;
    }


    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException
    {
        super.init(rs, context, node);
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
            case TypedLiteral literal -> writer.write(escape(literal.getValue()));
            case LanguageTaggedLiteral literal -> writer.write(escape(literal.getValue()));
            case Object obj -> writer.write(escape(obj.toString()));
        }

        return true;
    }


    private static String escape(String value)
    {
        return StringEscapeUtils.escapeHtml4(value).replaceAll("\uFFFD",
                "<span style=\"color: #a0a0a0\">\uFFFD</span>");
    }
}
