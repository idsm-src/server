package cz.iocb.chemweb.server.velocity;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.error.TranslateExceptions;
import cz.iocb.sparql.engine.request.Engine;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.request.Result;



public class SparqlDirective extends Directive
{
    public static final String SPARQL_CONFIG = "SPARQL_CONFIG";

    private Engine engine;


    @Override
    public String getName()
    {
        return "sparql";
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

        SparqlDatabaseConfiguration dbConfig = (SparqlDatabaseConfiguration) rs.getApplicationAttribute(SPARQL_CONFIG);
        engine = new Engine(dbConfig);
    }


    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        if(node.jjtGetNumChildren() != 2)
        {
            log.error("sparql directive: wrong number of arguments");
            return false;
        }


        SimpleNode sn = (SimpleNode) node.jjtGetChild(0);
        String varName = ((ASTReference) sn).getRootString();

        StringWriter blockContent = new StringWriter();
        node.jjtGetChild(node.jjtGetNumChildren() - 1).render(context, blockContent);
        String query = blockContent.toString();



        try(Request request = engine.getRequest())
        {
            try(Result result = request.execute(query))
            {
                SparqlResult rows = new SparqlResult();

                while(result.next())
                    rows.add(new SparqlRow(result.getVariableIndexes(), result.getRow()));

                context.put(varName, rows);
                return true;
            }
        }
        catch(TranslateExceptions | SQLException e)
        {
            log.error("sparql directive: " + e.getMessage());

            context.put(varName, null);
            return false;
        }
    }
}
