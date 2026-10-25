package cz.iocb.load.common;

import java.io.BufferedInputStream;
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



/**
 * Reads RDF files as streams of triples and passes every triple to the handlers registered for its predicate, so that
 * all the data of the files are loaded in one pass and without holding the files in memory. A handler registered for
 * a type gets the triples whose predicate is rdf:type and whose object is the type. The handlers are called one at a
 * time, also when several files are read in parallel, and they must not depend on the order of the triples. A triple
 * that a filter refuses is passed to no handler. The actions registered by after() run, in their order, once all the
 * files have been read.
 */
public class TripleDispatcher extends Updater
{
    /**
     * Handler of the triples of a predicate or a type.
     */
    @FunctionalInterface
    public static interface Handler
    {
        /**
         * Processes a triple.
         *
         * @param subject the subject of the triple
         * @param object the object of the triple
         * @throws IOException if the triple cannot be loaded (a {@link DataException}, which is reported) or an I/O
         *             error occurs
         * @throws SQLException if a database error occurs
         */
        void handle(Node subject, Node object) throws IOException, SQLException;
    }


    /**
     * Handler of all the triples.
     */
    @FunctionalInterface
    public static interface TripleHandler
    {
        /**
         * Processes a triple.
         *
         * @param subject the subject of the triple
         * @param predicate the predicate of the triple
         * @param object the object of the triple
         * @throws IOException if the triple cannot be loaded (a {@link DataException}, which is reported) or an I/O
         *             error occurs
         * @throws SQLException if a database error occurs
         */
        void handle(Node subject, Node predicate, Node object) throws IOException, SQLException;
    }


    /**
     * Action run once all the files have been read.
     */
    @FunctionalInterface
    public static interface Action
    {
        /**
         * Runs the action.
         *
         * @throws IOException if the data are inconsistent (a {@link DataException}, which is reported) or an I/O
         *             error occurs
         * @throws SQLException if a database error occurs
         */
        void run() throws IOException, SQLException;
    }


    /**
     * Filter of the triples passed to the handlers.
     */
    @FunctionalInterface
    public static interface Filter
    {
        /**
         * Tests whether a triple is passed to the handlers.
         *
         * @param subject the subject of the triple
         * @param predicate the predicate of the triple
         * @param object the object of the triple
         * @return true if the triple is accepted
         */
        boolean accept(Node subject, Node predicate, Node object);
    }


    /**
     * IRI of {@code rdf:type}.
     */
    private static final String typePredicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    /**
     * Handlers of the triples by the IRIs of their predicates.
     */
    private final HashMap<String, List<Handler>> handlers = new HashMap<>();

    /**
     * Handlers of the {@code rdf:type} triples by the IRIs of their types.
     */
    private final HashMap<String, List<Handler>> typeHandlers = new HashMap<>();

    /**
     * Handlers of all the triples.
     */
    private final List<TripleHandler> tripleHandlers = new ArrayList<>();

    /**
     * Filters of the triples that have a handler for their predicate or type.
     */
    private final List<Filter> filters = new ArrayList<>();

    /**
     * Actions run once all the files have been read.
     */
    private final List<Action> actions = new ArrayList<>();


    /**
     * Creates a dispatcher without handlers.
     */
    public TripleDispatcher()
    {
    }


    /**
     * Registers a handler of the triples with the given predicate.
     *
     * @param predicate the IRI of the predicate
     * @param handler the handler
     */
    public void on(String predicate, Handler handler)
    {
        handlers.computeIfAbsent(predicate, k -> new ArrayList<>()).add(handler);
    }


    /**
     * Registers a handler of the rdf:type triples with the given type.
     *
     * @param type the IRI of the type
     * @param handler the handler
     */
    public void onType(String type, Handler handler)
    {
        typeHandlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
    }


    /**
     * Registers a handler of all the triples.
     *
     * @param handler the handler
     */
    public void onEvery(TripleHandler handler)
    {
        tripleHandlers.add(handler);
    }


    /**
     * Registers a filter of the triples that have a handler for their predicate or type: a triple that the filter
     * refuses is passed to no handler. The filter reports the triples that it refuses itself.
     *
     * @param filter the filter
     */
    public void filter(Filter filter)
    {
        filters.add(filter);
    }


    /**
     * Registers an action to run once all the files have been read.
     *
     * @param action the action
     */
    public void after(Action action)
    {
        actions.add(action);
    }


    /**
     * Returns the IRI of an IRI or the lexical form of a literal, as the SPARQL function str() does, and null for a
     * blank node.
     *
     * @param node the node
     * @return the IRI or the lexical form, or null for a blank node
     */
    public static String str(Node node)
    {
        if(node.isURI())
            return node.getURI();

        if(node.isLiteral())
            return node.getLiteralLexicalForm();

        return null;
    }


    /**
     * Returns the IRI of an IRI node and the string form of any other node, as the messages show them.
     *
     * @param node the node
     * @return the text of the node
     */
    public static String text(Node node)
    {
        return TripleStreamProcessor.text(node);
    }


    /**
     * Tests whether the IRI or literal value starts with any of the prefixes, as strstarts(str(...), ...) does.
     *
     * @param node the node
     * @param prefixes the prefixes
     * @return true if the IRI or the lexical form of the node starts with any of the prefixes
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


    /**
     * Tests whether the node is the given IRI.
     *
     * @param node the node
     * @param iri the IRI
     * @return true if the node is the IRI
     */
    public static boolean is(Node node, String iri)
    {
        return node.isURI() && node.getURI().equals(iri);
    }


    /**
     * Selects all the nodes.
     *
     * @return the predicate accepting every node
     */
    public static Predicate<Node> all()
    {
        return node -> true;
    }


    /**
     * Selects the IRIs starting with the prefix.
     *
     * @param prefix the prefix of the IRIs
     * @return the predicate accepting the IRIs starting with the prefix
     */
    public static Predicate<Node> startingWith(String prefix)
    {
        return node -> node.isURI() && node.getURI().startsWith(prefix);
    }


    /**
     * Selects the nodes that are not IRIs starting with the prefix, including the blank nodes.
     *
     * @param prefix the prefix of the IRIs
     * @return the predicate accepting the nodes other than the IRIs starting with the prefix
     */
    public static Predicate<Node> notStartingWith(String prefix)
    {
        return node -> !node.isURI() || !node.getURI().startsWith(prefix);
    }


    /**
     * Selects all the nodes except the given IRIs.
     *
     * @param iris the excluded IRIs
     * @return the predicate accepting the nodes other than the IRIs
     */
    public static Predicate<Node> except(String... iris)
    {
        Set<String> excluded = Set.of(iris);

        return node -> !node.isURI() || !excluded.contains(node.getURI());
    }


    /**
     * Reports the triples of the selected subjects whose predicates are other than the allowed ones as errors.
     *
     * @param subjects the selected subjects
     * @param allowed the IRIs of the allowed predicates
     */
    public void checkPredicates(Predicate<Node> subjects, String... allowed)
    {
        Set<String> predicates = Set.of(allowed);

        onEvery((subject, predicate, object) -> {
            if(subjects.test(subject) && !predicates.contains(predicate.getURI()))
                TripleStreamProcessor.unexpected(subject, predicate, object);
        });
    }


    /**
     * Reports the types of the selected subjects other than the allowed ones as errors.
     *
     * @param subjects the selected subjects
     * @param allowed the IRIs of the allowed types
     */
    public void checkTypes(Predicate<Node> subjects, String... allowed)
    {
        Set<String> types = Set.of(allowed);

        on(typePredicate, (subject, object) -> {
            if(subjects.test(subject) && !(object.isURI() && types.contains(object.getURI())))
                TripleStreamProcessor.unexpectedType(subject, object);
        });
    }


    /**
     * Reports the values of the predicate other than the allowed IRIs as errors.
     *
     * @param predicate the IRI of the predicate
     * @param allowed the allowed IRIs
     */
    public void checkValues(String predicate, String... allowed)
    {
        Set<String> values = Set.of(allowed);

        on(predicate, (subject, object) -> {
            if(!(object.isURI() && values.contains(object.getURI())))
                Problems.error("unexpected value of " + predicate, text(subject) + " " + text(object));
        });
    }


    /**
     * Reports the values of the predicate at the selected subjects that start with none of the prefixes as errors.
     *
     * @param subjects the selected subjects
     * @param predicate the IRI of the predicate
     * @param prefixes the allowed prefixes of the values
     */
    public void checkPrefixes(Predicate<Node> subjects, String predicate, String... prefixes)
    {
        checkPrefixes(subjects, predicate, all(), prefixes);
    }


    /**
     * Reports the selected values of the predicate at the selected subjects that start with none of the prefixes as
     * errors.
     *
     * @param subjects the selected subjects
     * @param predicate the IRI of the predicate
     * @param values the selected values
     * @param prefixes the allowed prefixes of the values
     */
    public void checkPrefixes(Predicate<Node> subjects, String predicate, Predicate<Node> values, String... prefixes)
    {
        on(predicate, (subject, object) -> {
            String value = str(object);

            if(value == null || !subjects.test(subject) || !values.test(object))
                return;

            for(String prefix : prefixes)
                if(value.startsWith(prefix))
                    return;

            Problems.error("unexpected value of " + predicate, text(subject) + " " + text(object));
        });
    }


    /**
     * Reports the subjects whose IRI without the pattern differs from their value of the predicate as errors.
     *
     * @param predicate the IRI of the predicate
     * @param pattern the regular expression removed from the IRIs of the subjects
     */
    public void checkIdentifier(String predicate, String pattern)
    {
        on(predicate, (subject, object) -> {
            if(subject.isURI() && !subject.getURI().replaceAll(pattern, "").equals(str(object)))
                Problems.error("value of " + predicate + " not matching the IRI", text(subject) + " " + text(object));
        });
    }


    /**
     * Reports the selected values of the predicate at the selected subjects whose rest without the value pattern
     * differs from the rest of the subject IRI without the subject pattern as errors.
     *
     * @param subjects the selected subjects
     * @param predicate the IRI of the predicate
     * @param values the selected values
     * @param subjectPattern the regular expression removed from the IRIs of the subjects
     * @param valuePattern the regular expression removed from the values
     */
    public void checkLink(Predicate<Node> subjects, String predicate, Predicate<Node> values, String subjectPattern,
            String valuePattern)
    {
        on(predicate, (subject, object) -> {
            String value = str(object);

            if(value == null || !subject.isURI() || !subjects.test(subject) || !values.test(object))
                return;

            if(!subject.getURI().replaceAll(subjectPattern, "").equals(value.replaceAll(valuePattern, "")))
                Problems.error("value of " + predicate + " not matching the IRI", text(subject) + " " + value);
        });
    }


    /**
     * Reports the values of the predicate that start with the prefix and lack a value with the pair prefix and the
     * same rest at the same subject as errors; a pairing in both directions is checked by two calls.
     *
     * @param predicate the IRI of the predicate
     * @param prefix the prefix of the checked values
     * @param pairPrefix the prefix of their pairs
     */
    public void checkPaired(String predicate, String prefix, String pairPrefix)
    {
        HashMap<Pair<Node, String>, String> values = new HashMap<>();
        HashSet<Pair<Node, String>> pairs = new HashSet<>();

        on(predicate, (subject, object) -> {
            String value = str(object);

            if(value != null && value.startsWith(prefix))
                values.put(Pair.getPair(subject, value.substring(prefix.length())), value);

            if(value != null && value.startsWith(pairPrefix))
                pairs.add(Pair.getPair(subject, value.substring(pairPrefix.length())));
        });

        after(() -> {
            for(Entry<Pair<Node, String>, String> entry : values.entrySet())
                if(!pairs.contains(entry.getKey()))
                    Problems.error("value of " + predicate + " without its " + pairPrefix + " pair",
                            text(entry.getKey().getOne()) + " " + entry.getValue());
        });
    }


    /**
     * Passes the triple to its handlers, unless a filter refuses it. A problem of the data that a handler finds is
     * reported, and the other handlers still get the triple.
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private synchronized void dispatch(Node subject, Node predicate, Node object) throws IOException, SQLException
    {
        List<Handler> list = handlers.get(predicate.getURI());
        List<Handler> types = object.isURI() && predicate.getURI().equals(typePredicate) ?
                typeHandlers.get(object.getURI()) : null;

        if(list != null || types != null)
            for(Filter filter : filters)
                if(!filter.accept(subject, predicate, object))
                    return;

        for(TripleHandler handler : tripleHandlers)
        {
            try
            {
                handler.handle(subject, predicate, object);
            }
            catch(DataException | NumberFormatException | UnsupportedOperationException e)
            {
                TripleStreamProcessor.report(e, subject, predicate, object);
            }
        }

        if(list != null)
        {
            for(Handler handler : list)
            {
                try
                {
                    handler.handle(subject, object);
                }
                catch(DataException | NumberFormatException | UnsupportedOperationException e)
                {
                    TripleStreamProcessor.report(e, subject, predicate, object);
                }
            }
        }

        if(types != null)
        {
            for(Handler handler : types)
            {
                try
                {
                    handler.handle(subject, object);
                }
                catch(DataException | NumberFormatException | UnsupportedOperationException e)
                {
                    TripleStreamProcessor.report(e, subject, predicate, object);
                }
            }
        }
    }


    /**
     * Reads an RDF file: Turtle if its name ends with .ttl, N-Triples if it ends with .nt and RDF/XML otherwise,
     * gzipped if the name ends with .gz. Relative IRIs are resolved against file:/// followed by the path of the file
     * in the data directory, so that they do not depend on the directory the loader runs in. A missing file is
     * reported as an error.
     *
     * @param file the path of the file in the data directory
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    public void load(String file) throws IOException, SQLException
    {
        String name = file.endsWith(".gz") ? file.substring(0, file.length() - 3) : file;
        Lang lang = name.endsWith(".ttl") ? Lang.TURTLE : name.endsWith(".nt") ? Lang.NTRIPLES : Lang.RDFXML;

        System.out.println("  load " + file);

        InputStream input = openFile(file);

        if(input == null)
            return;

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


    /**
     * Reads the files of the directory whose names match the pattern, several of them in parallel.
     *
     * @param path the path of the directory in the data directory
     * @param pattern the regular expression of the names of the files
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    public void load(String path, String pattern) throws IOException, SQLException
    {
        processFiles(path, pattern, this::load);
    }


    /**
     * Runs the actions registered by {@link #after(Action)}. A problem of the data that an action finds is reported,
     * and the other actions still run.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    public void finish() throws IOException, SQLException
    {
        for(Action action : actions)
        {
            try
            {
                action.run();
            }
            catch(DataException e)
            {
                Problems.error(e.getKind(), e.getDetail());
            }
        }
    }
}
