package cz.iocb.load.common;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;



/*
 * Reads RDF files as streams of triples and passes every triple to the handlers registered for its predicate, so that
 * all the data of the files are loaded in one pass and without holding the files in memory. A handler registered for
 * a type gets the triples whose predicate is rdf:type and whose object is the type. The handlers are called one at a
 * time, also when several files are read in parallel, and they must not depend on the order of the triples. The
 * actions registered by after() run, in their order, once all the files have been read.
 */
public class TripleDispatcher extends Updater
{
    @FunctionalInterface
    public static interface Handler
    {
        void handle(Node subject, Node object) throws IOException, SQLException;
    }


    @FunctionalInterface
    public static interface TripleHandler
    {
        void handle(Node subject, Node predicate, Node object) throws IOException, SQLException;
    }


    @FunctionalInterface
    public static interface Action
    {
        void run() throws IOException, SQLException;
    }


    private static final String typePredicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private final HashMap<String, List<Handler>> handlers = new HashMap<>();
    private final HashMap<String, List<Handler>> typeHandlers = new HashMap<>();
    private final List<TripleHandler> tripleHandlers = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();
    private final HashSet<String> reported = new HashSet<>();


    /*
     * Registers a handler of the triples with the given predicate.
     */
    public void on(String predicate, Handler handler)
    {
        handlers.computeIfAbsent(predicate, k -> new ArrayList<>()).add(handler);
    }


    /*
     * Registers a handler of the rdf:type triples with the given type.
     */
    public void onType(String type, Handler handler)
    {
        typeHandlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
    }


    /*
     * Registers a handler of all the triples.
     */
    public void onEvery(TripleHandler handler)
    {
        tripleHandlers.add(handler);
    }


    /*
     * Registers an action to run once all the files have been read.
     */
    public void after(Action action)
    {
        actions.add(action);
    }


    /*
     * Reports an unexpected value of the data once.
     */
    public synchronized void missing(String value)
    {
        if(reported.add(value))
            System.out.println("    missing " + value);
    }


    /*
     * Returns the IRI of an IRI or the lexical form of a literal, as the SPARQL function str() does, and null for a
     * blank node.
     */
    public static String str(Node node)
    {
        if(node.isURI())
            return node.getURI();

        if(node.isLiteral())
            return node.getLiteralLexicalForm();

        return null;
    }


    /*
     * Returns the IRI of an IRI node and the string form of any other node, as the messages show them.
     */
    public static String text(Node node)
    {
        return TripleStreamProcessor.text(node);
    }


    /*
     * Tests whether the IRI or literal value starts with any of the prefixes, as strstarts(str(...), ...) does.
     */
    public static boolean startsWith(Node node, String... prefixes)
    {
        String value = str(node);

        if(value != null)
            for(String prefix : prefixes)
                if(value.startsWith(prefix))
                    return true;

        return false;
    }


    /*
     * Tests whether the node is the given IRI.
     */
    public static boolean is(Node node, String iri)
    {
        return node.isURI() && node.getURI().equals(iri);
    }


    public static Predicate<Node> all()
    {
        return node -> true;
    }


    public static Predicate<Node> startingWith(String prefix)
    {
        return node -> node.isURI() && node.getURI().startsWith(prefix);
    }


    public static Predicate<Node> notStartingWith(String prefix)
    {
        return node -> node.isURI() && !node.getURI().startsWith(prefix);
    }


    /*
     * Selects all the nodes except the given IRIs.
     */
    public static Predicate<Node> except(String... iris)
    {
        Set<String> excluded = Set.of(iris);

        return node -> !node.isURI() || !excluded.contains(node.getURI());
    }


    /*
     * Reports the predicates of the selected subjects other than the allowed ones.
     */
    public void checkPredicates(Predicate<Node> subjects, String... allowed)
    {
        Set<String> predicates = Set.of(allowed);

        onEvery((subject, predicate, object) -> {
            if(subjects.test(subject) && !predicates.contains(predicate.getURI()))
                missing(predicate.getURI());
        });
    }


    /*
     * Reports the types of the selected subjects other than the allowed ones.
     */
    public void checkTypes(Predicate<Node> subjects, String... allowed)
    {
        Set<String> types = Set.of(allowed);

        on(typePredicate, (subject, object) -> {
            if(subjects.test(subject) && !(object.isURI() && types.contains(object.getURI())))
                missing(text(object));
        });
    }


    /*
     * Reports the values of the predicate other than the allowed IRIs.
     */
    public void checkValues(String predicate, String... allowed)
    {
        Set<String> values = Set.of(allowed);

        on(predicate, (subject, object) -> {
            if(!(object.isURI() && values.contains(object.getURI())))
                missing(text(object));
        });
    }


    /*
     * Reports the values of the predicate at the selected subjects that start with none of the prefixes.
     */
    public void checkPrefixes(Predicate<Node> subjects, String predicate, String... prefixes)
    {
        on(predicate, (subject, object) -> {
            String value = str(object);

            if(value == null || !subjects.test(subject))
                return;

            for(String prefix : prefixes)
                if(value.startsWith(prefix))
                    return;

            missing(value);
        });
    }


    /*
     * Reports the subjects whose IRI without the pattern differs from their value of the predicate.
     */
    public void checkIdentifier(String predicate, String pattern)
    {
        on(predicate, (subject, object) -> {
            if(subject.isURI() && !subject.getURI().replaceAll(pattern, "").equals(str(object)))
                missing(subject.getURI());
        });
    }


    /*
     * Reports the selected values of the predicate at the selected subjects whose rest without the value pattern
     * differs from the rest of the subject IRI without the subject pattern.
     */
    public void checkLink(Predicate<Node> subjects, String predicate, Predicate<Node> values, String subjectPattern,
            String valuePattern)
    {
        on(predicate, (subject, object) -> {
            String value = str(object);

            if(value == null || !subject.isURI() || !subjects.test(subject) || !values.test(object))
                return;

            if(!subject.getURI().replaceAll(subjectPattern, "").equals(value.replaceAll(valuePattern, "")))
                missing(value);
        });
    }


    /*
     * Reports the values of the predicate that start with the prefix and lack a value with the pair prefix and the
     * same rest at the same subject.
     */
    public void checkPaired(String predicate, String prefix, String pairPrefix)
    {
        HashMap<Pair<Node, String>, String> values = new HashMap<>();
        HashSet<Pair<Node, String>> pairs = new HashSet<>();

        on(predicate, (subject, object) -> {
            String value = str(object);

            if(value != null && value.startsWith(prefix))
                values.put(Pair.getPair(subject, value.replaceAll(prefix, "")), value);

            if(value != null && value.startsWith(pairPrefix))
                pairs.add(Pair.getPair(subject, value.replaceAll(pairPrefix, "")));
        });

        after(() -> {
            for(Entry<Pair<Node, String>, String> entry : values.entrySet())
                if(!pairs.contains(entry.getKey()))
                    missing(entry.getValue());
        });
    }


    /*
     * Reports the literal values of the predicate whose datatype is not the given one.
     */
    public void checkDatatype(String predicate, String datatype)
    {
        on(predicate, (subject, object) -> {
            if(object.isLiteral() && !object.getLiteralDatatypeURI().equals(datatype))
                missing(text(object));
        });
    }


    /*
     * Passes the triple to its handlers. A problem of the data that a handler finds is reported, and the other handlers
     * still get the triple.
     */
    private synchronized void dispatch(Node subject, Node predicate, Node object) throws IOException, SQLException
    {
        for(TripleHandler handler : tripleHandlers)
        {
            try
            {
                handler.handle(subject, predicate, object);
            }
            catch(DataException | NumberFormatException e)
            {
                TripleStreamProcessor.report(e, subject, predicate, object);
            }
        }

        List<Handler> list = handlers.get(predicate.getURI());

        if(list != null)
        {
            for(Handler handler : list)
            {
                try
                {
                    handler.handle(subject, object);
                }
                catch(DataException | NumberFormatException e)
                {
                    TripleStreamProcessor.report(e, subject, predicate, object);
                }
            }
        }

        if(object.isURI() && predicate.getURI().equals(typePredicate))
        {
            List<Handler> types = typeHandlers.get(object.getURI());

            if(types != null)
            {
                for(Handler handler : types)
                {
                    try
                    {
                        handler.handle(subject, object);
                    }
                    catch(DataException | NumberFormatException e)
                    {
                        TripleStreamProcessor.report(e, subject, predicate, object);
                    }
                }
            }
        }
    }


    /*
     * Reads an RDF file: Turtle if its name ends with .ttl, N-Triples if it ends with .nt and RDF/XML otherwise,
     * gzipped if the name ends with .gz. Relative IRIs are resolved against file:/// followed by the path of the file
     * in the data directory, so that they do not depend on the directory the loader runs in.
     */
    public void load(String file) throws IOException, SQLException
    {
        String name = file.endsWith(".gz") ? file.substring(0, file.length() - 3) : file;
        Lang lang = name.endsWith(".ttl") ? Lang.TURTLE : name.endsWith(".nt") ? Lang.NTRIPLES : Lang.RDFXML;

        System.out.println("  load " + file);

        InputStream input = new FileInputStream(baseDirectory + file);

        if(file.endsWith(".gz"))
            input = new GZIPInputStream(input, 65536);

        input = new BufferedInputStream(input);

        if(lang != Lang.RDFXML)
            input = new InputStreamFixer(input);

        try(InputStream stream = input)
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    dispatch(subject, predicate, object);
                }
            }.load(stream, lang, "file:///" + file);
        }
        catch(RuntimeException e)
        {
            if(e.getCause() instanceof SQLException)
                throw(SQLException) e.getCause();
            else
                throw e;
        }
    }


    /*
     * Reads the files of the directory whose names match the pattern, several of them in parallel.
     */
    public void load(String path, String pattern) throws IOException, SQLException
    {
        processFiles(path, pattern, this::load);
    }


    /*
     * Runs the actions registered by after().
     */
    public void finish() throws IOException, SQLException
    {
        for(Action action : actions)
            action.run();
    }
}
