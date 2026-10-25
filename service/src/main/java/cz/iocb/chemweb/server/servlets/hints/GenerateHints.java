package cz.iocb.chemweb.server.servlets.hints;

import static java.util.Locale.ROOT;
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
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import cz.iocb.chemweb.server.servlets.hints.NormalizeIRI.PrefixedName;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.error.TranslateExceptions;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.rdf.LangStringLiteral;
import cz.iocb.sparql.engine.rdf.Literal;
import cz.iocb.sparql.engine.rdf.RdfTerm;
import cz.iocb.sparql.engine.rdf.Variable;
import cz.iocb.sparql.engine.request.Engine;
import cz.iocb.sparql.engine.request.LimitExceedException;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.request.Result;
import cz.iocb.sparql.engine.translator.ServiceException;



/**
 * Servlet generating the script {@code chemweb/js/sparql-hint-data.js} with the hints of the query editor: the classes,
 * individuals and properties that occur as constant IRIs in the quad mappings of the configuration named by the
 * {@code resource} init parameter (a JNDI resource), as prefixed names grouped by their prefixes and with their labels.
 * The script is generated once per configuration.
 */
public class GenerateHints extends HttpServlet
{
    /**
     * Variable of the hinted resources in the query of the hints.
     */
    private static final Variable varH = new Variable("H");

    /**
     * Variable of the types of the resources in the query of the hints.
     */
    private static final Variable varT = new Variable("T");

    /**
     * Variable of the labels of the resources in the query of the hints.
     */
    private static final Variable varL = new Variable("L");


    /**
     * Hint of one resource of one type.
     */
    private static class Item
    {
        /**
         * Code of the type: {@code C} for a class, {@code I} for an individual and {@code P} for a property.
         */
        String type;

        /**
         * Local part of the prefixed name of the resource.
         */
        String name;

        /**
         * Label of the resource escaped for a JavaScript string literal, null if the resource has no label.
         */
        String info;

        /**
         * Preferred label of the resource, null if the resource has no label.
         */
        Literal label;

        /**
         * Creates an empty hint.
         */
        private Item()
        {
        }
    }


    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Generated scripts by the names of the configurations, shared by the servlets of a configuration.
     */
    private static HashMap<String, String> hintsMap = new HashMap<>();

    /**
     * Script served by the servlet.
     */
    private String hintsJS;


    /**
     * Creates the servlet.
     */
    public GenerateHints()
    {
    }


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
                catch(NamingException | TranslateExceptions | LimitExceedException | ServiceException | SQLException e)
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


    /**
     * Writes the script as the response.
     *
     * @param req the request
     * @param res the response
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        PrintWriter out = res.getWriter();
        out.print(hintsJS);
        out.close();
    }


    /**
     * Generates the script of the hints of a configuration by querying the types and the labels of the constant IRIs of
     * its quad mappings.
     *
     * @param sparqlConfig the configuration
     * @return the script
     * @throws TranslateExceptions if the query of the hints cannot be translated
     * @throws LimitExceedException if the query of the hints exceeds a limit
     * @throws SQLException if a database error occurs
     * @throws ServiceException if a SERVICE call of the query fails
     */
    private static String generateHints(SparqlDatabaseConfiguration sparqlConfig)
            throws TranslateExceptions, LimitExceedException, SQLException, ServiceException
    {
        Set<String> iris = new HashSet<>();

        for(QuadMapping mapping : sparqlConfig.getMappings(sparqlConfig.getServiceIri()))
        {
            if(mapping.getGraph() instanceof ConstantIriMapping graph)
                iris.add(graph.getIri().getValue());

            if(mapping.getSubject() instanceof ConstantIriMapping subject)
                iris.add(subject.getIri().getValue());

            if(mapping.getPredicate() instanceof ConstantIriMapping predicate)
                iris.add(predicate.getIri().getValue());

            if(mapping.getObject() instanceof ConstantIriMapping object)
                iris.add(object.getIri().getValue());
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

        LinkedHashMap<String, ArrayList<Item>> hints = new LinkedHashMap<>();
        HashMap<String, Item> items = new HashMap<>();
        Engine engine = new Engine(sparqlConfig);

        try(Request request = engine.getRequest())
        {
            try(Result result = request.execute(builder.toString()))
            {
                while(result.next())
                {
                    Iri text = (Iri) result.get(varH);
                    Iri type = (Iri) result.get(varT);
                    RdfTerm label = result.get(varL);

                    PrefixedName iri = NormalizeIRI.decompose(sparqlConfig, text.getValue());

                    if(iri == null)
                        continue;

                    ArrayList<Item> list = hints.get(iri.prefix.toLowerCase(ROOT));

                    if(list == null)
                    {
                        list = new ArrayList<>();
                        hints.put(iri.prefix.toLowerCase(ROOT), list);
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

                    // a resource with several labels gets the preferred one
                    String key = text.getValue() + " " + typeCode;
                    Item item = items.get(key);

                    if(item == null)
                    {
                        item = new Item();
                        item.type = typeCode;
                        item.name = iri.name;

                        items.put(key, item);
                        list.add(item);
                    }

                    if(label instanceof Literal literal && (item.label == null || isPreferred(literal, item.label)))
                    {
                        item.label = literal;
                        item.info = literal.getValue().replaceAll("\"", "\\\"");
                    }
                }
            }
        }


        for(ArrayList<Item> list : hints.values())
            Collections.sort(list,
                    (Item item1, Item item2) -> item1.name.toLowerCase(ROOT).compareTo(item2.name.toLowerCase(ROOT)));


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


    /**
     * Tests whether a label is preferred to another one: an English label to a label without a language and that one to
     * a label in another language, otherwise the label with the greater lexical form.
     *
     * @param label the label
     * @param other the other label
     * @return true if the label is preferred to the other one, false otherwise
     */
    private static boolean isPreferred(Literal label, Literal other)
    {
        int rank = getRank(label);
        int otherRank = getRank(other);

        if(rank != otherRank)
            return rank > otherRank;

        return label.getValue().compareTo(other.getValue()) > 0;
    }


    /**
     * Returns the rank of the language of a label: 2 for English, 1 for no language and 0 for another language.
     *
     * @param label the label
     * @return the rank of the language of the label
     */
    private static int getRank(Literal label)
    {
        String language = label instanceof LangStringLiteral literal ? literal.getTag() : "";

        return language.equals("en") || language.startsWith("en-") ? 2 : language.isEmpty() ? 1 : 0;
    }


    @Override
    public String getServletInfo()
    {
        return "Hints Servlet";
    }
}
