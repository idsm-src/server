package cz.iocb.chemweb.server.servlets.hints;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cz.iocb.chemweb.server.servlets.hints.NormalizeIRI.PrefixedName;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.error.TranslateExceptions;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.request.Engine;
import cz.iocb.sparql.engine.request.RdfNode;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.request.Result;
import cz.iocb.sparql.engine.translator.ServiceException;



public class GenerateHints extends HttpServlet
{
    private static class Item
    {
        String type;
        String name;
        String info;
    }


    private static final long serialVersionUID = 1L;
    private static HashMap<String, String> hintsMap = new HashMap<String, String>();

    private String hintsJS;


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String resourceName = config.getInitParameter("resource");

        if(resourceName == null || resourceName.isEmpty())
            throw new ServletException("Resource name is not set");


        synchronized(hintsMap)
        {
            hintsJS = hintsMap.get(resourceName);

            if(hintsJS == null)
            {
                try
                {
                    Context context = (Context) (new InitialContext()).lookup("java:comp/env");
                    SparqlDatabaseConfiguration dbConfig = (SparqlDatabaseConfiguration) context.lookup(resourceName);

                    hintsJS = generateHints(dbConfig);
                    hintsMap.put(resourceName, hintsJS);
                }
                catch(NamingException | TranslateExceptions | ServiceException | SQLException e)
                {
                    throw new ServletException(e);
                }
            }
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        processRequest(req, res);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        processRequest(req, res);
    }


    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        PrintWriter out = res.getWriter();
        out.print(hintsJS);
        out.close();
    }


    private static String generateHints(SparqlDatabaseConfiguration sparqlConfig)
            throws TranslateExceptions, SQLException, ServiceException
    {
        Set<String> iris = new HashSet<String>();

        for(QuadMapping mapping : sparqlConfig.getMappings(sparqlConfig.getServiceIri()))
        {
            if(mapping.getGraph() instanceof ConstantIriMapping)
                iris.add(((IRI) (mapping.getGraph().getValue())).getValue());

            if(mapping.getSubject() instanceof ConstantIriMapping)
                iris.add(((IRI) (((ConstantIriMapping) mapping.getSubject()).getValue())).getValue());

            if(mapping.getPredicate() instanceof ConstantIriMapping)
                iris.add(((IRI) (((ConstantIriMapping) mapping.getPredicate()).getValue())).getValue());

            if(mapping.getObject() instanceof ConstantIriMapping)
                iris.add(((IRI) (((ConstantIriMapping) mapping.getObject()).getValue())).getValue());
        }


        StringBuilder builder = new StringBuilder();

        builder.append("select distinct ?H ?T ?L where");
        builder.append("{");
        builder.append("?H rdf:type ?T.");
        builder.append("optional {?H rdfs:label ?L}");
        builder.append("filter (?T in (owl:Class, owl:NamedIndividual, rdf:Property))");
        builder.append("}");
        builder.append("values ?H {");

        for(String iri : iris)
        {
            builder.append("<");
            builder.append(iri);
            builder.append(">");
        }

        builder.append("}");


        StringWriter stringWriter = new StringWriter();
        PrintWriter out = new PrintWriter(stringWriter);

        LinkedHashMap<String, ArrayList<Item>> hints = new LinkedHashMap<String, ArrayList<Item>>();
        Engine engine = new Engine(sparqlConfig);

        try(Request request = engine.getRequest())
        {
            try(Result result = request.execute(builder.toString()))
            {
                while(result.next())
                {
                    RdfNode text = result.get("H");
                    RdfNode type = result.get("T");
                    RdfNode label = result.get("L");

                    PrefixedName iri = NormalizeIRI.decompose(sparqlConfig, text.getValue());

                    if(iri == null)
                        continue;

                    ArrayList<Item> list = hints.get(iri.prefix.toLowerCase());

                    if(list == null)
                    {
                        list = new ArrayList<Item>();
                        hints.put(iri.prefix.toLowerCase(), list);
                    }


                    String typeCode = null;

                    switch(type.getValue())
                    {
                        case "http://www.w3.org/2002/07/owl#Class":
                            typeCode = "C";
                            break;

                        case "http://www.w3.org/2002/07/owl#NamedIndividual":
                            typeCode = "I";
                            break;

                        case "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property":
                            typeCode = "P";
                            break;

                        default:
                            continue;
                    }

                    Item item = new Item();
                    item.type = typeCode;
                    item.name = iri.name;

                    if(label != null)
                        item.info = label.getValue().replaceAll("\"", "\\\"");

                    list.add(item);
                }
            }
        }


        for(ArrayList<Item> list : hints.values())
            Collections.sort(list,
                    (Item item1, Item item2) -> item1.name.toLowerCase().compareTo(item2.name.toLowerCase()));


        out.println("var hints = {");

        int i = 0;
        for(Entry<String, ArrayList<Item>> entry : hints.entrySet())
        {
            if(i++ > 0)
                out.println(",");

            out.print(entry.getKey());
            out.println(": {");
            out.println("\tprefix: \"" + entry.getKey() + "\",");
            out.println("\thints: [");

            int j = 0;
            for(Item hint : entry.getValue())
            {
                if(j++ > 0)
                    out.println(",");

                out.print("\t\t{");
                out.print(" type: \"" + hint.type + "\",");
                out.print(" name: \"" + hint.name + "\"");

                if(hint.info != null)
                    out.print(", info: \"" + hint.info + "\"");

                out.print(" }");
            }

            out.print("]");
            out.print("}");
        }

        out.println("};");


        out.close();
        return stringWriter.toString();
    }


    @Override
    public String getServletInfo()
    {
        return "Hints Servlet";
    }
}
