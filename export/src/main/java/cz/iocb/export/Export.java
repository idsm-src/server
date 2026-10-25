package cz.iocb.export;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.postgresql.ds.PGSimpleDataSource;
import cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Condition;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.SourceTable;
import cz.iocb.sparql.engine.imcode.SqlEmptySolution;
import cz.iocb.sparql.engine.imcode.SqlIntercode;
import cz.iocb.sparql.engine.imcode.SqlJoin;
import cz.iocb.sparql.engine.imcode.SqlSelect;
import cz.iocb.sparql.engine.imcode.SqlTableAccess;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.InternalNodeMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping.JoinColumns;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.SingleTableQuadMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.InternalResourceClass;
import cz.iocb.sparql.engine.mapping.classes.IriClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.rdf.BlankNode;
import cz.iocb.sparql.engine.rdf.DirLangStringLiteral;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.rdf.LangStringLiteral;
import cz.iocb.sparql.engine.rdf.RdfTerm;
import cz.iocb.sparql.engine.rdf.TripleTerm;
import cz.iocb.sparql.engine.rdf.TypedLiteral;
import cz.iocb.sparql.engine.rdf.Variable;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.request.Result;
import cz.iocb.sparql.engine.request.Result.ResultType;
import cz.iocb.sparql.engine.translator.VariableBinding;
import cz.iocb.sparql.engine.translator.VariableBindings;



/**
 * Exports every graph of the IDSM endpoint into a directory of gzipped TriG files, one per predicate. A file is written
 * under a temporary name and renamed when it is complete; a failed file is removed and reported, and the export then
 * ends with exit status 1.
 */
public class Export
{
    /**
     * Pattern of the local names written in prefixed names: a subset of PN_LOCAL of Turtle (ASCII only, without dots
     * and escapes).
     */
    private static final Pattern localNamePattern = Pattern.compile("[A-Za-z0-9_:][-A-Za-z_0-9:]*");

    /**
     * Pattern of the language tags that can be written.
     */
    private static final Pattern languageTagPattern = Pattern.compile("[a-zA-Z]+(-[a-zA-Z0-9]+)*");

    /**
     * Format of the hexadecimal digits of the escapes in IRIs.
     */
    private static final HexFormat hexFormat = HexFormat.of().withUpperCase();

    /**
     * Number of the files exported at the same time.
     */
    private static final int threads = 64;

    /**
     * Variable of the subjects of the exported triples.
     */
    private static final Variable subject = new Variable("S");

    /**
     * Variable of the objects of the exported triples.
     */
    private static final Variable object = new Variable("O");


    /**
     * Not instantiable.
     */
    private Export()
    {
    }


    /**
     * Exports the endpoint whose database is given by the file {@code datasource.properties} of the working directory
     * ({@code host}, {@code port}, {@code dbname}, {@code user} and {@code password}) into the directory {@code idsm-}
     * followed by the current date under the directory given by {@code base}.
     *
     * @param args the command-line arguments (ignored)
     * @throws IOException if the properties cannot be read or the directories cannot be created
     * @throws SQLException if a database error occurs
     */
    public static void main(String[] args) throws IOException, SQLException
    {
        Properties properties = new Properties();

        try(FileInputStream in = new FileInputStream("datasource.properties"))
        {
            properties.load(in);
        }

        // a file holds one connection for its whole export, so a pool would save only one connection setup per file
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[] { properties.getProperty("host") });
        dataSource.setPortNumbers(new int[] { Integer.parseInt(properties.getProperty("port")) });
        dataSource.setDatabaseName(properties.getProperty("dbname"));
        dataSource.setUser(properties.getProperty("user"));
        dataSource.setPassword(properties.getProperty("password"));


        Path base = Path.of(properties.getProperty("base"), "idsm-" + LocalDate.now().format(ISO_LOCAL_DATE));
        Files.createDirectories(base);


        DatabaseSchema schema = new DatabaseSchema(dataSource);

        IdsmConfiguration config = new IdsmConfiguration(null, dataSource, schema);

        if(!export(config, base))
        {
            System.out.println("*** failed ***");
            System.exit(1);
        }

        System.out.println("*** done ***");
    }


    /**
     * Exports the named graphs of a configuration, a directory per graph and a file per predicate, the files being
     * written in parallel. The names of all directories and files are checked before anything is written.
     *
     * @param config the configuration
     * @param base the directory to export into
     * @return true if all files were exported, false if some failed
     * @throws IOException if a directory cannot be created
     */
    private static boolean export(SparqlDatabaseConfiguration config, Path base) throws IOException
    {
        Map<Iri, Map<Iri, Set<QuadMapping>>> mappings = new HashMap<>();

        for(QuadMapping mapping : config.getMappings(config.getServiceIri()))
        {
            ConstantIriMapping graphMapping = (ConstantIriMapping) mapping.getGraph();

            // the default graph is the union of the named graphs
            if(graphMapping == null)
                continue;

            Iri graph = graphMapping.getIri();
            Iri predicate = ((ConstantIriMapping) mapping.getPredicate()).getIri();

            mappings.computeIfAbsent(graph, k -> new HashMap<Iri, Set<QuadMapping>>())
                    .computeIfAbsent(predicate, k -> new HashSet<QuadMapping>()).add(mapping);
        }


        // the names are not injective, so all of them are checked before anything is written
        Map<Path, Iri> directories = new HashMap<>();
        Map<Path, Iri> files = new HashMap<>();

        for(Entry<Iri, Map<Iri, Set<QuadMapping>>> g : mappings.entrySet())
        {
            Path directory = base.resolve(convertIriToName(g.getKey()));
            checkName(directories, directory, g.getKey());

            for(Iri predicate : g.getValue().keySet())
                checkName(files, directory.resolve(convertIriToName(predicate) + ".trig.gz"), predicate);
        }

        for(Path directory : directories.keySet())
            Files.createDirectory(directory);


        Map<Path, Future<Long>> tasks = new LinkedHashMap<>();

        try(ExecutorService exec = Executors.newFixedThreadPool(threads))
        {
            for(Entry<Iri, Map<Iri, Set<QuadMapping>>> g : mappings.entrySet())
            {
                Path directory = base.resolve(convertIriToName(g.getKey()));

                for(Entry<Iri, Set<QuadMapping>> p : g.getValue().entrySet())
                {
                    Path file = directory.resolve(convertIriToName(p.getKey()) + ".trig.gz");
                    tasks.put(file, exec.submit(() -> writeTrig(file, config, g.getKey(), p.getKey(), p.getValue())));
                }
            }
        }


        int failed = 0;

        for(Entry<Path, Future<Long>> task : tasks.entrySet())
        {
            if(task.getValue().state() == Future.State.FAILED)
            {
                System.err.println("error: " + task.getKey() + " was not exported:");
                task.getValue().exceptionNow().printStackTrace();
                failed++;
            }
        }

        if(failed > 0)
            System.err.println("error: " + failed + " of " + tasks.size() + " files were not exported");

        return failed == 0;
    }


    /**
     * Registers the path an IRI is exported to and refuses a path already taken by another IRI.
     *
     * @param names the paths taken so far with their IRIs
     * @param name the path
     * @param iri the IRI
     * @throws IllegalStateException if another IRI is exported to the same path
     */
    private static void checkName(Map<Path, Iri> names, Path name, Iri iri)
    {
        Iri other = names.putIfAbsent(name, iri);

        if(other != null)
            throw new IllegalStateException(iri + " and " + other + " are both exported as " + name);
    }


    /**
     * Writes the triples of one predicate in one graph into a gzipped TriG file. The prefixes are made of the IRI
     * prefixes of the mapped classes and the predicate is the empty prefix. The file is written under the suffix
     * {@code .part} and renamed when it is complete; a failed file is removed.
     *
     * @param file the file
     * @param config the configuration
     * @param graph the graph
     * @param predicate the predicate
     * @param mappings the quad mappings of the predicate in the graph
     * @return the number of the written triples
     * @throws SQLException if a database error occurs
     * @throws IOException if an I/O error occurs
     */
    private static long writeTrig(Path file, SparqlDatabaseConfiguration config, Iri graph, Iri predicate,
            Set<QuadMapping> mappings) throws SQLException, IOException
    {
        GzipParameters parameters = new GzipParameters();
        parameters.setCompressionLevel(Deflater.BEST_SPEED);

        Path part = file.resolveSibling(file.getFileName() + ".part");
        OutputStream fos = Files.newOutputStream(part, StandardOpenOption.CREATE_NEW);
        long count = 0;

        try(Request request = new Request(config);
                OutputStream fout = new BufferedOutputStream(fos, 1 << 26);
                GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(fout, parameters);
                Writer out = new BufferedWriter(new OutputStreamWriter(gzos, StandardCharsets.UTF_8), 1 << 16))
        {
            List<String> prefixList = new ArrayList<>();
            List<SqlSelect> selects = new ArrayList<>();

            for(QuadMapping map : mappings)
            {
                SqlSelect select = translate(request, map);
                selects.add(select);

                prefixList.addAll(getPrefixes(map.getSubject(), select.getVariable(subject)));
                prefixList.addAll(getPrefixes(map.getObject(), select.getVariable(object)));
            }

            // the longest prefix is tried first
            prefixList.sort(Comparator.comparingInt(String::length).reversed());
            Map<String, String> prefixes = new LinkedHashMap<>();

            for(String prefix : prefixList)
                prefixes.putIfAbsent(prefix, "p" + (prefixes.size() + 1));

            for(Entry<String, String> prefix : prefixes.entrySet())
            {
                out.write("@prefix " + prefix.getValue() + ": ");
                writeIriRef(out, prefix.getKey());
                out.write(".\n");
            }

            out.write("@prefix : ");
            writeIriRef(out, predicate.getValue());
            out.write(".\n");
            writeIriRef(out, graph.getValue());
            out.write("\n{\n");

            for(SqlSelect select : selects)
            {
                try(Result result = new Result(ResultType.SELECT, select.getResultDescription(),
                        request.getStatement(10000).executeQuery(select.translate(request)), 0, 0))
                {
                    while(result.next())
                    {
                        writeTerm(out, result.get(0), prefixes);
                        out.write(" : ");
                        writeTerm(out, result.get(1), prefixes);
                        out.write(".\n");
                        count++;
                    }
                }
            }

            out.write("}\n");
        }
        catch(Throwable e)
        {
            try
            {
                fos.close();
                Files.deleteIfExists(part);
            }
            catch(IOException suppressed)
            {
                e.addSuppressed(suppressed);
            }

            throw e;
        }

        Files.move(part, file);
        System.out.println(graph + " " + predicate + ": " + count + " triples");

        return count;
    }


    /**
     * Returns the candidate prefixes of the terms of a mapping: its IRI if it is constant, otherwise the IRI prefixes
     * of its IRI classes and the datatype IRIs of its literal classes.
     *
     * @param mapping the term mapping
     * @param var the binding of the term in the translated select, null if the mapping has no solution
     * @return the prefixes
     */
    private static List<String> getPrefixes(TermMapping mapping, VariableBinding var)
    {
        if(mapping instanceof ConstantIriMapping iriMapping)
            return List.of(iriMapping.getIri().getValue());

        // the mapping has no solution
        if(var == null)
            return List.of();


        List<String> prefixList = new ArrayList<>();

        for(Entry<ResourceClass, List<Column>> e : var.getMappings().entrySet())
        {
            String prefix = switch(e.getKey())
            {
                case IriClass rc -> rc.getPrefix(e.getValue());
                case LiteralClass rc when rc.getTypeIri() != null -> rc.getTypeIri().getValue();
                default -> null;
            };

            if(prefix != null && !prefix.isEmpty())
                prefixList.add(prefix);
        }

        return prefixList;
    }


    /**
     * Binds a term mapping of a table to a variable and requires its nullable columns that are not optional for its
     * class to be not null.
     *
     * @param request the request
     * @param table the table of the mapping
     * @param nodemap the term mapping
     * @param name the variable, null to add the conditions only
     * @param bindings the bindings to extend
     * @param condition the condition to extend
     */
    private static void processNodeMapping(Request request, SourceTable table, TermMapping nodemap, Variable name,
            VariableBindings bindings, Condition condition)
    {
        DatabaseSchema schema = request.getConfiguration().getDatabaseSchema();
        ResourceClass resourceClass = nodemap.getResourceClass(request);
        List<Column> columns = nodemap.getColumns(request);

        // the optional columns of the class may be null
        for(int i = 0; i < columns.size(); i++)
            if(!resourceClass.isOptionalColumn(i) && schema.isNullableColumn(table, columns.get(i)))
                condition.addIsNotNull(columns.get(i));

        if(name == null)
            return;

        bindings.add(new VariableBinding(name, resourceClass, columns, false));
    }


    /**
     * Returns the columns a table access of a distinct mapping is distinct on: the columns of its bindings and of its
     * condition.
     *
     * @param distinct whether the mapping is distinct
     * @param bindings the bindings of the access
     * @param condition the condition of the access
     * @return the columns, empty if the mapping is not distinct
     */
    private static Set<Column> distinctColumns(boolean distinct, VariableBindings bindings, Condition condition)
    {
        if(!distinct)
            return Set.of();

        // all table columns touched by the mapped terms, including those bound to constants by the pattern
        Set<Column> columns = new HashSet<>(bindings.getNonConstantColumns());
        columns.addAll(condition.getNonConstantColumns());

        return columns;
    }


    /**
     * Translates a quad mapping into the select of the subjects and the objects of its triples.
     *
     * @param request the request
     * @param mapping the quad mapping
     * @return the optimized select
     */
    private static SqlSelect translate(Request request, QuadMapping mapping)
    {
        SqlIntercode imcode = switch(mapping)
        {
            case SingleTableQuadMapping map ->
            {
                SourceTable table = map.getTable();
                Condition condition = new Condition();
                VariableBindings bindings = new VariableBindings();

                processNodeMapping(request, table, map.getSubject(), subject, bindings, condition);
                processNodeMapping(request, table, map.getObject(), object, bindings, condition);

                Conditions conditions = Conditions.and(map.getConditions(), condition);

                yield SqlTableAccess.create(request, table, conditions, bindings, false,
                        distinctColumns(map.isDistinct(), bindings, condition));
            }

            case JoinTableQuadMapping map ->
            {
                List<SourceTable> tables = map.getTables();
                List<JoinColumns> joinColumnsPairs = map.getJoinColumnsPairs();

                ResourceClass resourceClass = null;
                Variable node = null;

                SqlIntercode result = SqlEmptySolution.get();

                for(int i = 0; i < tables.size(); i++)
                {
                    SourceTable table = tables.get(i);
                    Condition condition = new Condition();
                    VariableBindings bindings = new VariableBindings();

                    if(i == map.getSubjectTableIdx())
                        processNodeMapping(request, table, map.getSubject(), subject, bindings, condition);

                    if(i == map.getObjectTableIdx())
                        processNodeMapping(request, table, map.getObject(), object, bindings, condition);

                    if(i > 0)
                    {
                        List<Column> columns = joinColumnsPairs.get(i - 1).getRightColumns();
                        TermMapping nodeMapping = new InternalNodeMapping(resourceClass, columns);
                        processNodeMapping(request, table, nodeMapping, node, bindings, condition);
                    }

                    if(i < tables.size() - 1)
                    {
                        List<Column> columns = joinColumnsPairs.get(i).getLeftColumns();
                        resourceClass = new InternalResourceClass(columns.stream().map(Column::getType).toList());
                        TermMapping nodeMapping = new InternalNodeMapping(resourceClass, columns);
                        node = new Variable("@var" + i);
                        processNodeMapping(request, table, nodeMapping, node, bindings, condition);
                    }

                    Conditions conditions = Conditions.and(map.getConditions().get(i), condition);
                    SqlIntercode acess = SqlTableAccess.create(request, table, conditions, bindings, false,
                            distinctColumns(map.getDistinct().get(i), bindings, condition));

                    result = SqlJoin.join(request, result, acess);
                }

                yield result;
            }

            default ->
            {
                throw new UnsupportedOperationException(mapping.getClass().getName());
            }
        };

        return SqlSelect.createTopLevel(request, List.of(subject, object), imcode, null, null).optimize(request, false);
    }


    /**
     * Converts an IRI to a file name by replacing the runs of colons and slashes with underscores.
     *
     * @param iri the IRI
     * @return the file name
     */
    private static String convertIriToName(Iri iri)
    {
        return iri.getValue().replaceAll("[:/]+", "_");
    }


    /**
     * Writes an RDF term in the syntax of TriG.
     *
     * @param out the output
     * @param term the term
     * @param prefixes the names of the prefixes by their IRIs, the longest IRIs first
     * @throws IOException if an I/O error occurs
     */
    private static void writeTerm(Writer out, RdfTerm term, Map<String, String> prefixes) throws IOException
    {
        switch(term)
        {
            case Iri iri -> writeIri(out, iri, prefixes);

            case BlankNode bnode ->
            {
                out.write("_:");
                out.write(bnode.getLabel());
            }

            case TypedLiteral literal ->
            {
                writeString(out, literal.getValue());
                out.write("^^");
                writeIri(out, literal.getType(), prefixes);
            }

            case LangStringLiteral literal ->
            {
                writeString(out, literal.getValue());
                writeLanguageTag(out, literal.getTag());
            }

            case DirLangStringLiteral literal ->
            {
                writeString(out, literal.getValue());
                writeLanguageTag(out, literal.getTag());
                out.write("--");
                out.write(literal.getDirection().getText());
            }

            case TripleTerm triple ->
            {
                out.write("<<( ");
                writeTerm(out, triple.getSubject(), prefixes);
                out.write(' ');
                writeTerm(out, triple.getPredicate(), prefixes);
                out.write(' ');
                writeTerm(out, triple.getObject(), prefixes);
                out.write(" )>>");
            }

            default -> throw new IllegalArgumentException("unexpected term " + term);
        }
    }


    /**
     * Writes an IRI as a prefixed name of the longest prefix whose local name can be written, otherwise as an IRIREF.
     *
     * @param out the output
     * @param iri the IRI
     * @param prefixes the names of the prefixes by their IRIs, the longest IRIs first
     * @throws IOException if an I/O error occurs
     */
    private static void writeIri(Writer out, Iri iri, Map<String, String> prefixes) throws IOException
    {
        String value = iri.getValue();

        for(Entry<String, String> prefix : prefixes.entrySet())
        {
            if(value.startsWith(prefix.getKey()))
            {
                String name = value.substring(prefix.getKey().length());

                if(name.isEmpty() || localNamePattern.matcher(name).matches())
                {
                    out.write(prefix.getValue());
                    out.write(':');
                    out.write(name);
                    return;
                }
            }
        }

        writeIriRef(out, value);
    }


    /**
     * Writes an IRI as an IRIREF, the characters an IRIREF does not admit being written as UCHAR escapes.
     *
     * @param out the output
     * @param iri the IRI
     * @throws IOException if an I/O error occurs
     */
    private static void writeIriRef(Writer out, String iri) throws IOException
    {
        out.write('<');

        int begin = 0;

        for(int i = 0; i < iri.length(); i++)
        {
            char c = iri.charAt(i);

            // IRIREF admits no other escapes than UCHAR
            if(c <= ' ' || c == '<' || c == '>' || c == '"' || c == '{' || c == '}' || c == '|' || c == '^' || c == '`'
                    || c == '\\')
            {
                out.write(iri, begin, i - begin);
                out.write("\\u");
                out.write(hexFormat.toHexDigits(c));
                begin = i + 1;
            }
        }

        out.write(iri, begin, iri.length() - begin);
        out.write('>');
    }


    /**
     * Writes a string as a quoted literal, escaping the quotes, the backslashes and the line breaks.
     *
     * @param out the output
     * @param value the string
     * @throws IOException if an I/O error occurs
     */
    private static void writeString(Writer out, String value) throws IOException
    {
        out.write('"');

        int begin = 0;

        for(int i = 0; i < value.length(); i++)
        {
            String escape = switch(value.charAt(i))
            {
                case '"' -> "\\\"";
                case '\\' -> "\\\\";
                case '\n' -> "\\n";
                case '\r' -> "\\r";
                default -> null;
            };

            if(escape != null)
            {
                out.write(value, begin, i - begin);
                out.write(escape);
                begin = i + 1;
            }
        }

        out.write(value, begin, value.length() - begin);
        out.write('"');
    }


    /**
     * Writes a language tag; a tag that cannot be written (a tag cannot be escaped) is refused by an
     * {@link IllegalArgumentException}.
     *
     * @param out the output
     * @param tag the language tag
     * @throws IOException if an I/O error occurs
     */
    private static void writeLanguageTag(Writer out, String tag) throws IOException
    {
        // a language tag cannot be escaped
        if(!languageTagPattern.matcher(tag).matches())
            throw new IllegalArgumentException("invalid language tag '" + tag + "'");

        out.write('@');
        out.write(tag);
    }
}
