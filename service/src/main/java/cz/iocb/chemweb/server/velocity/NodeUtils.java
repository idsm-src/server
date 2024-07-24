package cz.iocb.chemweb.server.velocity;

import java.util.HashMap;
import org.apache.commons.text.StringEscapeUtils;
import cz.iocb.chemweb.shared.utils.Encode;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.request.LiteralNode;
import cz.iocb.sparql.engine.request.ReferenceNode;



public class NodeUtils
{
    private HashMap<String, String> prefixes;


    public NodeUtils(HashMap<String, String> prefixes)
    {
        this.prefixes = prefixes;
    }


    public String escapeHtml(LiteralNode node)
    {
        if(node == null)
            return null;

        return StringEscapeUtils.escapeHtml4(node.getValue()).replaceAll("\uFFFD",
                "<span style=\"color: #a0a0a0\">\uFFFD</span>");
    }


    public String prefixedIRI(ReferenceNode node)
    {
        String iri = IRI.toPrefixedIRI(node.getValue(), prefixes);
        return iri != null ? iri : node.getValue();
    }


    public String nodeId(ReferenceNode node)
    {
        return "NODE_" + Encode.base32m(node.getValue());
    }
}
