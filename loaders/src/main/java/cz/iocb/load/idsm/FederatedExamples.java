package cz.iocb.load.idsm;

import static cz.iocb.load.common.TripleStreamProcessor.checkType;
import static cz.iocb.load.common.TripleStreamProcessor.text;
import static cz.iocb.load.common.TripleStreamProcessor.unexpected;
import static cz.iocb.load.common.TripleStreamProcessor.unexpectedValue;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the federated SPARQL examples of the repository federated-sparql-examples into the tables
 * {@code idsm.federated_queries} and {@code idsm.federated_query_targets}. Each example is described by a Turtle file
 * in a subdirectory of the directory {@code examples} of the repository, or in its subdirectory {@code variants}, as an
 * executable SELECT query of SHACL whose IRI ends with the seven digits of its identifier.
 */
public class FederatedExamples extends Updater
{
    /**
     * Example as stored in a row of {@code idsm.federated_queries}.
     *
     * @param id the identifier of the example
     * @param comment the question that the example answers
     * @param target the IRI of the SPARQL endpoint that the query is sent to
     * @param query the text of the query
     */
    private static record Example(int id, String comment, String target, String query)
    {
    }


    /**
     * Set of examples, read from the columns of {@code idsm.federated_queries} and set to the parameters of a command
     * in the order of the columns.
     */
    @SuppressWarnings("serial")
    private static class ExampleSet extends SqlSet<Example>
    {
        /**
         * Creates an empty set.
         */
        ExampleSet()
        {
        }

        @Override
        public Example get(ResultSet result) throws SQLException
        {
            return new Example(result.getInt(1), result.getString(2), result.getString(3), result.getString(4));
        }

        @Override
        public void set(PreparedStatement statement, Example example) throws SQLException
        {
            statement.setInt(1, example.id());
            statement.setString(2, example.comment());
            statement.setString(3, example.target());
            statement.setString(4, example.query());
        }
    }


    /**
     * Directory of the examples in the data directory.
     */
    private static final String directory = "idsm/examples";

    /**
     * Path of the Turtle file of an example relative to the directory of the examples.
     */
    private static final Pattern filePattern = Pattern.compile("[^/]+/(variants/)?[^/]+\\.ttl");

    /**
     * IRI of an example, capturing its identifier.
     */
    private static final Pattern examplePattern = Pattern
            .compile("https://idsm\\.elixir-czech\\.cz/\\.well-known/federated-sparql-examples/([0-9]{7})");

    /**
     * Namespace of RDF.
     */
    private static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /**
     * Namespace of RDF Schema.
     */
    private static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";

    /**
     * Namespace of the XML Schema datatypes.
     */
    private static final String xsd = "http://www.w3.org/2001/XMLSchema#";

    /**
     * Namespace of SHACL.
     */
    private static final String sh = "http://www.w3.org/ns/shacl#";

    /**
     * Namespace of schema.org.
     */
    private static final String schema = "https://schema.org/";

    /**
     * Namespace of the SPARQL examples ontology.
     */
    private static final String spex = "https://purl.expasy.org/sparql-examples/ontology#";

    /**
     * Predicate {@code rdf:type}.
     */
    private static final Node rdfType = NodeFactory.createURI(rdf + "type");

    /**
     * Predicate {@code rdfs:comment}, the question that an example answers.
     */
    private static final Node rdfsComment = NodeFactory.createURI(rdfs + "comment");

    /**
     * Predicate {@code sh:prefixes}, the prefix declarations shared by the examples, which are not loaded.
     */
    private static final Node shPrefixes = NodeFactory.createURI(sh + "prefixes");

    /**
     * Predicate {@code sh:select}, the query of an example.
     */
    private static final Node shSelect = NodeFactory.createURI(sh + "select");

    /**
     * Predicate {@code schema:target}, the SPARQL endpoint that the query of an example is sent to.
     */
    private static final Node schemaTarget = NodeFactory.createURI(schema + "target");

    /**
     * Predicate {@code spex:federatesWith}, a SPARQL endpoint that the query of an example calls.
     */
    private static final Node spexFederatesWith = NodeFactory.createURI(spex + "federatesWith");

    /**
     * Predicates of the description of an example.
     */
    private static final Set<Node> predicates = Set.of(rdfType, rdfsComment, shPrefixes, shSelect, schemaTarget,
            spexFederatesWith);

    /**
     * Types of an example, all of which its description states.
     */
    private static final List<String> types = List.of(sh + "SPARQLExecutable", sh + "SPARQLSelectExecutable");


    /**
     * Not instantiable.
     */
    private FederatedExamples()
    {
    }


    /**
     * Returns the paths of the Turtle files of the examples in the data directory, sorted: the files in the
     * subdirectories of the directory of the examples and in their subdirectories {@code variants}. The other files are
     * left to {@link #checkFiles(String, String...)}, which reports them.
     *
     * @return the paths of the files in the data directory
     * @throws IOException if the directory cannot be listed
     */
    private static List<String> getFiles() throws IOException
    {
        Path start = Path.of(baseDirectory, directory);

        if(!Files.isDirectory(start))
            return List.of();

        try(Stream<Path> stream = Files.walk(start))
        {
            return stream.filter(Files::isRegularFile).map(path -> start.relativize(path).toString())
                    .filter(name -> filePattern.matcher(name).matches()).sorted().map(name -> directory + "/" + name)
                    .toList();
        }
    }


    /**
     * Reads the triples of a Turtle file, grouped by their subjects and predicates. A malformed file is reported as an
     * error and gives no triples, so that its examples, which would be incomplete, cause no further errors.
     *
     * @param file the path of the file in the data directory
     * @return the objects of the triples by their subjects and predicates
     * @throws IOException if the file cannot be read
     */
    private static Map<Node, Map<Node, List<Node>>> readFile(String file) throws IOException
    {
        Map<Node, Map<Node, List<Node>>> triples = new LinkedHashMap<>();

        System.out.println("  load " + file);

        InputStream input = openFile(file);

        if(input == null)
            return triples;

        try(InputStream stream = new BufferedInputStream(input))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object)
                {
                    triples.computeIfAbsent(subject, k -> new LinkedHashMap<>())
                            .computeIfAbsent(predicate, k -> new ArrayList<>()).add(object);
                }
            }.load(stream, Lang.TURTLE, "file:///" + file);
        }
        catch(RiotException e)
        {
            Problems.error("malformed Turtle file", file + ": " + e.getMessage());
            return Map.of();
        }

        return triples;
    }


    /**
     * Returns the only object of a predicate at a subject; no object or several objects are reported as an error.
     *
     * @param subject the subject
     * @param values the objects of the triples of the subject by their predicates
     * @param predicate the predicate
     * @return the object, or null if there is none or more than one
     */
    private static Node getValue(Node subject, Map<Node, List<Node>> values, Node predicate)
    {
        List<Node> objects = values.getOrDefault(predicate, List.of());

        if(objects.size() != 1)
        {
            Problems.error((objects.isEmpty() ? "missing value of " : "multiple values of ") + predicate.getURI(),
                    text(subject));
            return null;
        }

        return objects.get(0);
    }


    /**
     * Returns the question of an example, a plain or English string literal; another value is reported as an error.
     *
     * @param subject the example
     * @param object the value of {@code rdfs:comment}, or null if the example has no single value
     * @return the question, or null if the value is missing or unexpected
     */
    private static String getComment(Node subject, Node object)
    {
        if(object == null)
            return null;

        if(object.isLiteral() && (object.getLiteralDatatypeURI().equals(xsd + "string")
                || object.getLiteralDatatypeURI().equals(rdf + "langString")
                        && object.getLiteralLanguage().equalsIgnoreCase("en")))
            return object.getLiteralLexicalForm();

        unexpectedValue(subject, rdfsComment, object);
        return null;
    }


    /**
     * Returns the query of an example, an {@code xsd:string} literal holding a SPARQL SELECT query; another value, a
     * malformed query and a query of another form are reported as errors.
     *
     * @param subject the example
     * @param object the value of {@code sh:select}, or null if the example has no single value
     * @return the text of the query, or null if the value is missing or unexpected
     */
    private static String getQuery(Node subject, Node object)
    {
        if(object == null)
            return null;

        if(!object.isLiteral() || !object.getLiteralDatatypeURI().equals(xsd + "string"))
        {
            unexpectedValue(subject, shSelect, object);
            return null;
        }

        String query = object.getLiteralLexicalForm();

        try
        {
            if(QueryFactory.create(query, Syntax.syntaxSPARQL_12).isSelectType())
                return query;

            Problems.error("unexpected query form in " + shSelect.getURI(), text(subject));
        }
        catch(QueryException e)
        {
            String message = e.getMessage() == null ? "" : e.getMessage().lines().findFirst().orElse("");
            Problems.error("malformed query in " + shSelect.getURI(), text(subject) + " (" + message + ")");
        }

        return null;
    }


    /**
     * Returns the IRI of a SPARQL endpoint of an example; a value that is not the IRI of a known endpoint, i.e. of an
     * endpoint of the table {@code idsm.sparql_endpoints}, is reported as an error.
     *
     * @param subject the example
     * @param predicate the predicate of the endpoint
     * @param object the value of the predicate, or null if the example has no single value
     * @param endpoints the IRIs of the known endpoints
     * @return the IRI of the endpoint, or null if the value is missing or unexpected
     */
    private static String getEndpoint(Node subject, Node predicate, Node object, Set<String> endpoints)
    {
        if(object == null)
            return null;

        if(!object.isURI())
        {
            unexpectedValue(subject, predicate, object);
            return null;
        }

        if(!endpoints.contains(object.getURI()))
        {
            Problems.error("unknown SPARQL endpoint in " + predicate.getURI(), text(subject) + " " + text(object));
            return null;
        }

        return object.getURI();
    }


    /**
     * Returns the example described by the triples of a subject, and adds the IRIs of the endpoints that it federates
     * with to the given list. A problem of the description, such as an unexpected predicate or a missing value, is
     * reported as an error and null is returned.
     *
     * @param subject the subject of the triples
     * @param values the objects of the triples of the subject by their predicates
     * @param endpoints the IRIs of the known SPARQL endpoints
     * @param federated the list that gets the IRIs of the endpoints that the example federates with
     * @return the example, or null if its description has a problem
     */
    private static Example getExample(Node subject, Map<Node, List<Node>> values, Set<String> endpoints,
            List<String> federated)
    {
        Matcher matcher = examplePattern.matcher(subject.isURI() ? subject.getURI() : "");

        if(!matcher.matches())
        {
            Problems.error("unexpected subject", text(subject));
            return null;
        }

        boolean valid = true;

        for(Entry<Node, List<Node>> entry : values.entrySet())
        {
            if(!predicates.contains(entry.getKey()))
            {
                for(Node object : entry.getValue())
                    unexpected(subject, entry.getKey(), object);

                valid = false;
            }
        }

        List<Node> stated = values.getOrDefault(rdfType, List.of());

        for(Node type : stated)
            valid &= checkType(subject, type, types.toArray(String[]::new));

        for(String type : types)
        {
            if(stated.stream().noneMatch(node -> node.isURI() && node.getURI().equals(type)))
            {
                Problems.error("missing rdf:type " + type, text(subject));
                valid = false;
            }
        }

        String comment = getComment(subject, getValue(subject, values, rdfsComment));
        String query = getQuery(subject, getValue(subject, values, shSelect));
        String target = getEndpoint(subject, schemaTarget, getValue(subject, values, schemaTarget), endpoints);

        for(Node object : values.getOrDefault(spexFederatesWith, List.of()))
        {
            String endpoint = getEndpoint(subject, spexFederatesWith, object, endpoints);

            if(endpoint != null)
                federated.add(endpoint);
            else
                valid = false;
        }

        if(!valid || comment == null || query == null || target == null)
            return null;

        return new Example(Integer.parseInt(matcher.group(1)), comment, target, query);
    }


    /**
     * Loads the examples from their Turtle files: the examples and the endpoints that are no longer described are
     * deleted, the new and the changed ones are inserted. A problem of a description, also an example described by two
     * files, is reported as an error.
     *
     * @throws IOException if a file cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadExamples() throws IOException, SQLException
    {
        StringSet endpoints = new StringSet();
        load("select iri from idsm.sparql_endpoints", endpoints);

        ExampleSet oldExamples = new ExampleSet();
        ExampleSet newExamples = new ExampleSet();
        load("select id, comment, target, query from idsm.federated_queries", oldExamples);

        IntStringSet oldTargets = new IntStringSet();
        IntStringSet newTargets = new IntStringSet();
        load("select query, target from idsm.federated_query_targets", oldTargets);

        List<String> files = getFiles();

        if(files.isEmpty())
            Problems.error("no input file", directory);

        HashMap<Integer, String> descriptions = new HashMap<>();

        for(String file : files)
        {
            for(Entry<Node, Map<Node, List<Node>>> entry : readFile(file).entrySet())
            {
                List<String> federated = new ArrayList<>();
                Example example = getExample(entry.getKey(), entry.getValue(), endpoints, federated);

                if(example == null)
                    continue;

                String other = descriptions.putIfAbsent(example.id(), file);

                if(other != null)
                {
                    Problems.error("example described twice", text(entry.getKey()) + " (" + other + ", " + file + ")");
                    continue;
                }

                if(!oldExamples.remove(example))
                    newExamples.add(example);

                for(String endpoint : federated)
                {
                    Pair<Integer, String> pair = Pair.getPair(example.id(), endpoint);

                    if(!oldTargets.remove(pair))
                        newTargets.add(pair);
                }
            }
        }

        store("delete from idsm.federated_query_targets where query=? and target=?", oldTargets);
        store("delete from idsm.federated_queries where id=? and comment=? and target=? and query=?", oldExamples);
        store("insert into idsm.federated_queries(id,comment,target,query) values(?,?,?,?)", newExamples);
        store("insert into idsm.federated_query_targets(query,target) values(?,?)", newTargets);
    }


    /**
     * Runs the loader.
     *
     * @param args the command-line arguments (ignored)
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    public static void main(String[] args) throws IOException, SQLException
    {
        try
        {
            init("idsm");

            String version = getReader("idsm/version.txt.gz").readLine();
            System.out.println("=== load federated examples version " + version + " ===");
            System.out.println();

            System.out.println("load federated examples ...");
            loadExamples();
            System.out.println();

            updateVersion();

            checkFiles("idsm", "examples/prefixes\\.ttl");
            checkProblems();

            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
