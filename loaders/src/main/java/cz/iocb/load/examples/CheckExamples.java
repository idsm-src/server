package cz.iocb.load.examples;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.rowset.RowSetReaderRegistry;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDFCountingBase;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExecResult;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sys.JenaSystem;



/**
 * Check of the SPARQL examples of an endpoint: the examples that the endpoint describes as executable queries
 * ({@code sh:SPARQLExecutable} with the text of the query as {@code sh:select}, {@code sh:ask}, {@code sh:construct} or
 * {@code spex:describe}, as the SPARQL editors read them) are read from it, and those related to the endpoint are run
 * one after another, each on the endpoint it is meant for ({@code schema:target}): an example targeting the endpoint on
 * the endpoint, and an example calling the endpoint by SERVICE ({@code spex:federatesWith}) on each endpoint it
 * targets; an example without a target is taken for one targeting the endpoint it has been read from, and the other
 * examples are left out. The number of the results of every example and the time it took, from sending the query until
 * its whole result has been read, are printed as the example ends, and at the end the examples that failed and those
 * that returned no result (no solution, no triple, or the answer false), with their comments. The endpoint is given as
 * the argument, or asked for when none is given. Unlike the loaders, it does not connect to the database.
 */
public class CheckExamples
{
    /**
     * The query reading the examples of the endpoint with their comments, the endpoints they target and the endpoints
     * they call by SERVICE.
     */
    private static final String examplesQuery = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX schema: <https://schema.org/>
            PREFIX sh: <http://www.w3.org/ns/shacl#>
            PREFIX spex: <https://purl.expasy.org/sparql-examples/ontology#>

            SELECT ?example ?comment ?query ?target ?federatesWith
            WHERE {
              ?example a sh:SPARQLExecutable ;
                sh:select|sh:ask|sh:construct|spex:describe ?query .
              OPTIONAL {
                ?example rdfs:comment ?comment .
              }
              OPTIONAL {
                ?example schema:target ?target .
              }
              OPTIONAL {
                ?example spex:federatesWith ?federatesWith .
              }
            }
            ORDER BY ?example""";

    /**
     * The media types accepted for a result: the results of SELECT and ASK queries and the graphs of CONSTRUCT and
     * DESCRIBE queries, those that are read the fastest preferred.
     */
    private static final String acceptedTypes = """
            application/sparql-results+json, application/sparql-results+xml;q=0.9, application/n-triples;q=0.8, \
            text/turtle;q=0.7, application/rdf+xml;q=0.6""";

    /**
     * The time allowed for connecting to the endpoint.
     */
    private static final Duration connectTimeout = Duration.ofSeconds(60);

    /**
     * The most bytes of the body of an error response read for its message.
     */
    private static final int errorBodyLimit = 65536;

    /**
     * The most characters of an error message printed in the summary.
     */
    private static final int errorLength = 500;

    /**
     * The most characters of an error message printed as an example ends.
     */
    private static final int shortErrorLength = 150;

    /**
     * The most characters of the comment of an example printed.
     */
    private static final int commentLength = 150;


    /**
     * An example read from the endpoint, with the endpoints it is related to.
     */
    private static final class Example
    {
        /**
         * The IRI of the example.
         */
        private final String iri;

        /**
         * The text of the query of the example.
         */
        private final String query;

        /**
         * The comment of the example, a literal, or null.
         */
        private Node comment;

        /**
         * The IRIs of the endpoints the query of the example is meant for ({@code schema:target}).
         */
        private final Set<String> targets = new LinkedHashSet<>();

        /**
         * The IRIs of the endpoints the query of the example calls by SERVICE ({@code spex:federatesWith}).
         */
        private final Set<String> federatedEndpoints = new LinkedHashSet<>();


        /**
         * Creates the example, without a comment and the endpoints it is related to yet.
         *
         * @param iri the IRI of the example
         * @param query the text of the query of the example
         */
        Example(String iri, String query)
        {
            this.iri = iri;
            this.query = query;
        }
    }


    /**
     * A run of an example on an endpoint it is meant for.
     *
     * @param example the example
     * @param target the IRI of the endpoint the example is run on
     */
    private record Run(Example example, String target)
    {
    }


    /**
     * The whole result of a query that has been read.
     *
     * @param size the number of the solutions or of the triples, or 1 for the answer true and 0 for the answer false
     * @param description the description of the result, like {@code 12 results}
     */
    private record Answer(long size, String description)
    {
    }


    /**
     * The outcome of a run of an example.
     *
     * @param run the run
     * @param seconds the time the example took in seconds
     * @param answer the result of the example, or null if it failed
     * @param error the message of the failure, or null if the example has not failed
     */
    private record Outcome(Run run, double seconds, Answer answer, String error)
    {
    }


    /**
     * A failure reported by the endpoint itself, or a response that cannot be read, whose message is printed as it is.
     */
    @SuppressWarnings("serial")
    private static class EndpointException extends IOException
    {
        /**
         * Creates the exception.
         *
         * @param message the message of the failure
         */
        EndpointException(String message)
        {
            super(message);
        }
    }


    /**
     * Not instantiable.
     */
    private CheckExamples()
    {
    }


    /**
     * Reads the examples of the endpoint, runs those related to it and prints the outcomes; exits with a non-zero
     * status when the examples cannot be read, when none is related to the endpoint, or when an example has failed or
     * returned no result.
     *
     * @param args the command-line arguments: the IRI of the endpoint, or none to ask for it
     * @throws IOException if the endpoint cannot be read from the standard input
     * @throws InterruptedException if the thread is interrupted
     */
    public static void main(String[] args) throws IOException, InterruptedException
    {
        if(args.length > 1)
            exit("usage: mvn exec:java@check-examples [-Dexec.args=<endpoint>]");

        // the languages of the results are registered as Jena is initialised
        JenaSystem.init();

        String iri = args.length == 1 ? args[0].strip() : askForEndpoint();

        if(iri.isEmpty())
            exit("no endpoint given");

        URI endpoint;

        try
        {
            endpoint = new URI(iri);
        }
        catch(URISyntaxException e)
        {
            exit("not an IRI of an endpoint: " + iri);
            return;
        }

        if(endpoint.getScheme() == null || !endpoint.getScheme().matches("(?i)https?") || endpoint.getHost() == null)
            exit("not an http or https IRI of an endpoint: " + iri);

        try(HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NORMAL).connectTimeout(connectTimeout)
                .build())
        {
            List<Example> examples;

            try
            {
                examples = readExamples(client, endpoint);
            }
            catch(IOException | RuntimeException e)
            {
                exit("cannot read the examples of " + endpoint + ": " + describe(e));
                return;
            }

            if(examples.isEmpty())
                exit("the endpoint " + endpoint + " has no examples");

            List<Run> runs = selectRuns(examples, endpoint.toString());

            if(runs.isEmpty())
                exit("no example of the endpoint " + endpoint + " targets it or calls it by SERVICE (" + examples.size()
                        + " read)");

            long elsewhere = runs.stream().filter(r -> !r.target().equals(endpoint.toString())).count();

            System.out.println("=== examples of " + endpoint + " ===");
            System.out.println(String.format(ROOT, "%d examples read, %d related to the endpoint", examples.size(),
                    runs.stream().map(Run::example).distinct().count()));
            System.out.println(String.format(ROOT, "%d to be run on the endpoint, %d on the endpoints calling it",
                    runs.size() - elsewhere, elsewhere));
            System.out.println();

            List<Outcome> outcomes = new ArrayList<>();
            String counter = "[%" + Integer.toString(runs.size()).length() + "d/" + runs.size() + "] ";
            long start = System.nanoTime();

            for(Run run : runs)
            {
                System.out.print(String.format(ROOT, counter, outcomes.size() + 1) + label(run, endpoint) + ": ");
                System.out.flush();

                Outcome outcome = run(client, run);
                outcomes.add(outcome);

                if(outcome.error() != null)
                    System.out.println(String.format(ROOT, "failed after %.2f s: %s", outcome.seconds(),
                            shorten(outcome.error(), shortErrorLength)));
                else
                    System.out.println(
                            String.format(ROOT, "%s in %.2f s", outcome.answer().description(), outcome.seconds()));
            }

            double seconds = (System.nanoTime() - start) / 1e9;

            List<Outcome> failed = outcomes.stream().filter(o -> o.error() != null).toList();
            List<Outcome> empty = outcomes.stream().filter(o -> o.error() == null && o.answer().size() == 0).toList();

            System.out.println();
            System.out.println("=== summary ===");
            System.out.println(String.format(ROOT,
                    "%d examples run in %.2f s: %d with results, %d without results, %d failed", outcomes.size(),
                    seconds, outcomes.size() - failed.size() - empty.size(), empty.size(), failed.size()));

            if(!failed.isEmpty())
            {
                System.out.println();
                System.out.println("failed:");

                for(Outcome outcome : failed)
                {
                    printExample(outcome, endpoint);
                    System.out.println("    " + outcome.error());
                }
            }

            if(!empty.isEmpty())
            {
                System.out.println();
                System.out.println("without results:");

                for(Outcome outcome : empty)
                    printExample(outcome, endpoint);
            }

            if(!failed.isEmpty() || !empty.isEmpty())
                System.exit(1);
        }
    }


    /**
     * Asks for the IRI of the endpoint on the standard input.
     *
     * @return the IRI of the endpoint, or an empty string if none is given
     * @throws IOException if the standard input cannot be read
     */
    private static String askForEndpoint() throws IOException
    {
        System.out.print("SPARQL endpoint: ");
        System.out.flush();

        String line = new BufferedReader(new InputStreamReader(System.in, UTF_8)).readLine();

        return line != null ? line.strip() : "";
    }


    /**
     * Reads the examples of the endpoint, each once, in the order of their IRIs, with the endpoints they are related
     * to.
     *
     * @param client the HTTP client
     * @param endpoint the endpoint
     * @return the examples
     * @throws IOException if the endpoint fails or its response cannot be read
     * @throws InterruptedException if the thread is interrupted
     */
    private static List<Example> readExamples(HttpClient client, URI endpoint) throws IOException, InterruptedException
    {
        HttpResponse<InputStream> response = send(client, endpoint, examplesQuery);

        try(InputStream in = response.body())
        {
            Lang lang = getLang(response);

            if(lang == null || !RowSetReaderRegistry.isRegistered(lang))
                throw unexpectedContentType(response);

            QueryExecResult result = RowSetReaderRegistry.createReader(lang).readAny(in, ARQ.getContext());

            if(!result.isRowSet())
                throw new EndpointException("the endpoint has not returned the solutions of a SELECT query");

            Map<String, Example> examples = new LinkedHashMap<>();
            RowSet rows = result.rowSet();

            while(rows.hasNext())
            {
                Binding row = rows.next();
                Node node = row.get("example");
                Node comment = row.get("comment");
                Node query = row.get("query");
                Node target = row.get("target");
                Node federatedEndpoint = row.get("federatesWith");

                if(node == null || query == null || !query.isLiteral())
                    continue;

                String iri = node.isURI() ? node.getURI() : node.toString();
                Example example = examples.computeIfAbsent(iri, key -> new Example(key, query.getLiteralLexicalForm()));

                if(comment != null && comment.isLiteral() && isPreferred(comment, example.comment))
                    example.comment = comment;

                if(target != null && target.isURI())
                    example.targets.add(target.getURI());

                if(federatedEndpoint != null && federatedEndpoint.isURI())
                    example.federatedEndpoints.add(federatedEndpoint.getURI());
            }

            return new ArrayList<>(examples.values());
        }
    }


    /**
     * Tells whether a comment of an example is preferred to another one: any comment to none, and a comment without a
     * language tag or in English to one in another language.
     *
     * @param comment the comment, or null
     * @param other the other comment, or null
     * @return whether the comment is preferred
     */
    private static boolean isPreferred(Node comment, Node other)
    {
        return comment != null && (other == null || isEnglish(comment) && !isEnglish(other));
    }


    /**
     * Tells whether a comment of an example has no language tag or is in English.
     *
     * @param comment the comment
     * @return whether the comment has no language tag or is in English
     */
    private static boolean isEnglish(Node comment)
    {
        return comment.getLiteralLanguage().matches("(?i)(en(-.*)?)?");
    }


    /**
     * Selects the runs of the examples related to the endpoint: an example whose targets include the endpoint is run on
     * the endpoint, and an example that calls the endpoint by SERVICE and targets other endpoints is run on each of
     * them; an example without a target is taken for one targeting the endpoint it has been read from. IRIs of an
     * endpoint that differ only in the case of the scheme and the host or in a trailing slash are taken for the same
     * endpoint.
     *
     * @param examples the examples read from the endpoint
     * @param endpoint the IRI of the endpoint
     * @return the runs of the examples, in the order of the examples
     */
    private static List<Run> selectRuns(List<Example> examples, String endpoint)
    {
        String key = normalize(endpoint);
        List<Run> runs = new ArrayList<>();

        for(Example example : examples)
        {
            if(example.targets.isEmpty() || example.targets.stream().anyMatch(t -> normalize(t).equals(key)))
                runs.add(new Run(example, endpoint));
            else if(example.federatedEndpoints.stream().anyMatch(e -> normalize(e).equals(key)))
                for(String target : example.targets)
                    runs.add(new Run(example, target));
        }

        return runs;
    }


    /**
     * Normalises the IRI of an endpoint for its comparison with another one: the scheme and the host are put in lower
     * case and the trailing slashes of the path are removed.
     *
     * @param iri the IRI of the endpoint
     * @return the normalised IRI, or the IRI itself if it is no hierarchical URI
     */
    private static String normalize(String iri)
    {
        try
        {
            URI uri = new URI(iri).normalize();

            if(uri.getScheme() == null || uri.getRawAuthority() == null)
                return iri;

            return uri.getScheme().toLowerCase(ROOT) + "://" + uri.getRawAuthority().toLowerCase(ROOT)
                    + (uri.getRawPath() != null ? uri.getRawPath().replaceFirst("/+$", "") : "")
                    + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        }
        catch(URISyntaxException e)
        {
            return iri;
        }
    }


    /**
     * Runs an example on the endpoint it is meant for and measures the time it takes.
     *
     * @param client the HTTP client
     * @param run the run of the example
     * @return the outcome of the run
     * @throws InterruptedException if the thread is interrupted
     */
    private static Outcome run(HttpClient client, Run run) throws InterruptedException
    {
        long start = System.nanoTime();
        Answer answer = null;
        String error = null;

        try
        {
            answer = execute(client, new URI(run.target()), run.example().query);
        }
        catch(IOException | URISyntaxException | RuntimeException e)
        {
            error = describe(e);
        }

        return new Outcome(run, (System.nanoTime() - start) / 1e9, answer, error);
    }


    /**
     * Runs a query on the endpoint and reads its whole result.
     *
     * @param client the HTTP client
     * @param endpoint the endpoint
     * @param query the text of the query
     * @return the result of the query
     * @throws IOException if the endpoint fails or its response cannot be read
     * @throws InterruptedException if the thread is interrupted
     */
    private static Answer execute(HttpClient client, URI endpoint, String query)
            throws IOException, InterruptedException
    {
        HttpResponse<InputStream> response = send(client, endpoint, query);

        try(InputStream in = response.body())
        {
            Lang lang = getLang(response);

            if(lang != null && RowSetReaderRegistry.isRegistered(lang))
            {
                QueryExecResult result = RowSetReaderRegistry.createReader(lang).readAny(in, ARQ.getContext());

                if(result.isBoolean())
                    return new Answer(result.booleanResult() ? 1 : 0, result.booleanResult().toString());

                if(result.isRowSet())
                {
                    RowSet rows = result.rowSet();
                    long size = 0;

                    while(rows.hasNext())
                    {
                        rows.next();
                        size++;
                    }

                    return new Answer(size, size + (size == 1 ? " result" : " results"));
                }
            }
            else if(lang != null && RDFParserRegistry.isRegistered(lang))
            {
                StreamRDFCountingBase triples = new StreamRDFCountingBase(StreamRDFLib.sinkNull());

                RDFParser.source(in).lang(lang).base(endpoint.toString()).checking(false)
                        .errorHandler(ErrorHandlerFactory.errorHandlerExceptionOnError()).parse(triples);

                long size = triples.count();

                return new Answer(size, size + (size == 1 ? " triple" : " triples"));
            }

            throw unexpectedContentType(response);
        }
    }


    /**
     * Sends a query to the endpoint as the SPARQL protocol says, by the POST of a form.
     *
     * @param client the HTTP client
     * @param endpoint the endpoint
     * @param query the text of the query
     * @return the response of a successful request, whose body is to be read
     * @throws IOException if the request fails or the endpoint reports a failure
     * @throws InterruptedException if the thread is interrupted
     */
    private static HttpResponse<InputStream> send(HttpClient client, URI endpoint, String query)
            throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/x-www-form-urlencoded").header("Accept", acceptedTypes)
                .POST(BodyPublishers.ofString("query=" + URLEncoder.encode(query, UTF_8))).build();

        HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());

        if(response.statusCode() / 100 == 2)
            return response;

        String message;

        try(InputStream in = response.body())
        {
            ContentType type = getContentType(response);
            Charset charset = type != null && type.getCharset() != null ? Charset.forName(type.getCharset()) : UTF_8;
            message = new String(in.readNBytes(errorBodyLimit), charset);

            if(type != null && type.getContentTypeStr().contains("html"))
                message = message.replaceAll("(?is)<(script|style)\\b.*?</\\1\\s*>", " ").replaceAll("<[^>]*>", " ");
        }
        catch(IOException | RuntimeException e)
        {
            message = "";
        }

        message = shorten(message, errorLength);

        throw new EndpointException("HTTP " + response.statusCode() + (message.isEmpty() ? "" : ": " + message));
    }


    /**
     * Gets the content type of a response.
     *
     * @param response the response
     * @return the content type, or null if the response has none
     */
    private static ContentType getContentType(HttpResponse<?> response)
    {
        return response.headers().firstValue("Content-Type").map(ContentType::create).orElse(null);
    }


    /**
     * Creates the exception of a response whose content type is none of those of the results.
     *
     * @param response the response
     * @return the exception
     */
    private static EndpointException unexpectedContentType(HttpResponse<?> response)
    {
        return new EndpointException(
                "unexpected content type: " + response.headers().firstValue("Content-Type").orElse("none"));
    }


    /**
     * Gets the language of the body of a response by its content type.
     *
     * @param response the response
     * @return the language of the results or of the RDF data, the generic types of JSON and XML taken for those of the
     *         results, or null if the content type is unknown or missing
     */
    private static Lang getLang(HttpResponse<?> response)
    {
        ContentType type = getContentType(response);

        return type != null ? WebContent.contentTypeToLangResultSet(type.getContentTypeStr()) : null;
    }


    /**
     * Describes a run of an example: the IRI of the example, followed by the endpoint it is run on when that is another
     * endpoint than the checked one.
     *
     * @param run the run
     * @param endpoint the checked endpoint
     * @return the description of the run
     */
    private static String label(Run run, URI endpoint)
    {
        return run.example().iri + (run.target().equals(endpoint.toString()) ? "" : " on " + run.target());
    }


    /**
     * Prints an example that has failed or returned no result in the summary.
     *
     * @param outcome the outcome of the run of the example
     * @param endpoint the checked endpoint
     */
    private static void printExample(Outcome outcome, URI endpoint)
    {
        Node comment = outcome.run().example().comment;

        System.out.println(String.format(ROOT, "  %s (%.2f s)%s", label(outcome.run(), endpoint), outcome.seconds(),
                comment != null ? ": " + shorten(comment.getLiteralLexicalForm(), commentLength) : ""));
    }


    /**
     * Describes the failure of a request: the message of a failure reported by the endpoint, the class and the message
     * of any other exception, or of its cause when it has no message.
     *
     * @param exception the exception of the failure
     * @return the message of the failure
     */
    private static String describe(Throwable exception)
    {
        String message = exception.getMessage();

        if(exception instanceof EndpointException)
            return message;

        String name = exception.getClass().getSimpleName();

        if(message != null)
            return name + ": " + shorten(message, errorLength);

        if(exception.getCause() == null)
            return name;

        String cause = describe(exception.getCause());

        // a cause of the same class, which the HTTP client wraps its exceptions in, is not named twice
        return cause.equals(name) || cause.startsWith(name + ": ") ? cause : name + ": " + cause;
    }


    /**
     * Puts a text on a single line and shortens it.
     *
     * @param text the text
     * @param length the most characters of the result
     * @return the text with its white space collapsed to single spaces, ending with three dots if it is shortened
     */
    private static String shorten(String text, int length)
    {
        String line = text.replaceAll("\\s+", " ").strip();

        return line.length() > length ? line.substring(0, length - 3) + "..." : line;
    }


    /**
     * Prints a message to the standard error and exits with a non-zero status.
     *
     * @param message the message
     */
    private static void exit(String message)
    {
        System.err.println(message);
        System.exit(1);
    }
}
