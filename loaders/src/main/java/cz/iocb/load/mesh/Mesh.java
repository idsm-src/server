package cz.iocb.load.mesh;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUncategorized;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.text;
import static cz.iocb.load.common.TripleStreamProcessor.getBoolean;
import static cz.iocb.load.common.TripleStreamProcessor.getInt;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.common.TripleStreamProcessor.isDate;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Mesh extends Updater
{
    static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
    static final String xsd = "http://www.w3.org/2001/XMLSchema#";
    static final String meshv = "http://id.nlm.nih.gov/mesh/vocab#";

    static final String prefix = "http://id.nlm.nih.gov/mesh/";
    static final int prefixLength = prefix.length();

    private static final StringSet keepMeshes = new StringSet();
    private static final StringSet newMeshes = new StringSet();
    private static final StringSet oldMeshes = new StringSet();
    private static final MissingEntities<String> missingMeshes = new MissingEntities<>("mesh", true);


    private static final Pattern zonePattern = Pattern.compile("(Z|[+-][0-9]{2}:[0-9]{2})$");


    /*
     * Returns the version of MeSH from the header of its dump; null stands for an unknown version.
     */
    private static String getVersion(String file) throws IOException
    {
        InputStream input = openFile(file);

        if(input == null)
            return null;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(input, 65536), UTF_8)))
        {
            String line;

            while((line = reader.readLine()) != null)
            {
                if(line.matches("# <http://id\\.nlm\\.nih\\.gov/mesh> exported at 20[0-9]{2}-[0-9]{2}-[0-9]{2} .*"))
                    return line.replaceFirst(
                            "^# <http://id\\.nlm\\.nih\\.gov/mesh> exported at (20[0-9]{2}-[0-9]{2}-[0-9]{2}) .*$",
                            "$1");
            }

            return null;
        }
    }


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", meshv + "altLabel", meshv + "previousIndexing",
                meshv + "source", meshv + "thesaurusID", rdfs + "label", meshv + "abbreviation", meshv + "annotation",
                meshv + "casn1_label", meshv + "considerAlso", meshv + "entryVersion", meshv + "historyNote",
                meshv + "lastActiveYear", meshv + "lexicalTag", meshv + "note", meshv + "onlineNote",
                meshv + "prefLabel", meshv + "publicMeSHNote", meshv + "scopeNote", meshv + "sortVersion",
                meshv + "relatedRegistryNumber", meshv + "identifier", meshv + "nlmClassificationNumber",
                meshv + "registryNumber", meshv + "active", meshv + "frequency", meshv + "dateCreated",
                meshv + "dateRevised", meshv + "dateEstablished", meshv + "allowableQualifier",
                meshv + "broaderConcept", meshv + "broaderDescriptor", meshv + "broaderQualifier", meshv + "concept",
                meshv + "indexerConsiderAlso", meshv + "mappedTo", meshv + "narrowerConcept",
                meshv + "pharmacologicalAction", meshv + "preferredMappedTo", meshv + "relatedConcept",
                meshv + "seeAlso", meshv + "term", meshv + "treeNumber", meshv + "hasDescriptor",
                meshv + "hasQualifier", meshv + "parentTreeNumber", meshv + "preferredConcept", meshv + "preferredTerm",
                meshv + "useInstead", meshv + "Concept");

        // a few concepts are related by meshv:Concept, which the vocabulary does not define, so it is not loaded
        dispatcher.on(meshv + "Concept", (subject, object) -> {
            Problems.warning("ignored predicate " + meshv + "Concept", text(subject) + " " + text(object));
        });
    }


    private static void loadTypes(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        StringIntMap keepTypes = new StringIntMap();
        StringIntMap newTypes = new StringIntMap();
        StringIntMap oldTypes = new StringIntMap();

        load("select id from mesh.resources", oldMeshes);
        load("select id,type_id from mesh.resources where type_id is not null", oldTypes);

        dispatcher.on(rdf + "type", (subject, object) -> {
            String meshID = getStringID(subject, prefix);
            Pair<Integer, Integer> type = Ontology.getResourceId(object, unitUncategorized);

            // a reference may have added the mesh as a missing one already
            missingMeshes.described(meshID);
            newMeshes.remove(meshID);
            oldMeshes.remove(meshID);
            keepMeshes.add(meshID);

            if(type.getTwo().equals(oldTypes.remove(meshID)))
            {
                keepTypes.put(meshID, type.getTwo());
            }
            else
            {
                Integer keep = keepTypes.get(meshID);

                if(type.getTwo().equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of mesh.resources.type_id",
                            meshID + ": " + keep + ", " + type.getTwo());

                Integer put = newTypes.put(meshID, type.getTwo());

                if(put != null && !type.getTwo().equals(put))
                    throw new DataException("multiple values of mesh.resources.type_id",
                            meshID + ": " + put + ", " + type.getTwo());
            }
        });

        dispatcher.after(() -> {
            store("update mesh.resources set type_id=null where id=? and type_id=?", oldTypes);
            store("insert into mesh.resources(id,type_id) values(?,?) "
                    + "on conflict(id) do update set type_id=EXCLUDED.type_id", newTypes);
        });
    }


    private static void loadMultiStringValues(TripleDispatcher dispatcher, String property, String table, String column,
            String lang) throws IOException, SQLException
    {
        StringPairSet keepValues = new StringPairSet();
        StringPairSet newValues = new StringPairSet();
        StringPairSet oldValues = new StringPairSet();

        load("select resource," + column + " from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            if(isTranslation(object, lang))
            {
                Problems.warning("ignored value of " + property + " in another language",
                        text(subject) + " " + text(object));
                return;
            }

            String meshID = getMeshID(subject.getURI());
            String value = getLiteral(object, lang);

            Pair<String, String> pair = Pair.getPair(meshID, value);

            if(oldValues.remove(pair))
                keepValues.add(pair);
            else if(!keepValues.contains(pair))
                newValues.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where resource=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(resource," + column + ") values(?,?)", newValues);
        });
    }


    private static void loadStringValues(TripleDispatcher dispatcher, String property, String table, String column,
            String lang) throws IOException, SQLException
    {
        StringStringMap keepValues = new StringStringMap();
        StringStringMap newValues = new StringStringMap();
        StringStringMap oldValues = new StringStringMap();

        load("select resource," + column + " from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            if(isTranslation(object, lang))
            {
                Problems.warning("ignored value of " + property + " in another language",
                        text(subject) + " " + text(object));
                return;
            }

            String meshID = getMeshID(subject.getURI());
            String value = getLiteral(object, lang);

            if(value.equals(oldValues.remove(meshID)))
            {
                keepValues.put(meshID, value);
            }
            else
            {
                String keep = keepValues.get(meshID);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of mesh." + table + "." + column,
                            meshID + ": " + keep + ", " + value);

                String put = newValues.put(meshID, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of mesh." + table + "." + column,
                            meshID + ": " + put + ", " + value);
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where resource=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(resource," + column + ") values(?,?) "
                    + "on conflict(resource) do update set " + column + "=EXCLUDED." + column, newValues);
        });
    }


    private static void loadBooleanValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        StringIntMap keepValues = new StringIntMap();
        StringIntMap newValues = new StringIntMap();
        StringIntMap oldValues = new StringIntMap();

        load("select resource," + column + "::integer from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            String meshID = getMeshID(subject.getURI());
            Integer value = getBoolean(object) ? 1 : 0;

            if(value.equals(oldValues.remove(meshID)))
            {
                keepValues.put(meshID, value);
            }
            else
            {
                Integer keep = keepValues.get(meshID);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of mesh." + table + "." + column,
                            meshID + ": " + keep + ", " + value);

                Integer put = newValues.put(meshID, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of mesh." + table + "." + column,
                            meshID + ": " + put + ", " + value);
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where resource=? and " + column + "=?::boolean", oldValues);
            store("insert into mesh." + table + "(resource," + column + ") values(?,?::boolean) "
                    + "on conflict(resource) do update set " + column + "=EXCLUDED." + column, newValues);
        });
    }


    private static void loadIntegerValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        StringIntMap keepValues = new StringIntMap();
        StringIntMap newValues = new StringIntMap();
        StringIntMap oldValues = new StringIntMap();

        load("select resource," + column + " from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            String meshID = getMeshID(subject.getURI());
            Integer value = getInt(object);

            if(value.equals(oldValues.remove(meshID)))
            {
                keepValues.put(meshID, value);
            }
            else
            {
                Integer keep = keepValues.get(meshID);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of mesh." + table + "." + column,
                            meshID + ": " + keep + ", " + value);

                Integer put = newValues.put(meshID, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of mesh." + table + "." + column,
                            meshID + ": " + put + ", " + value);
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where resource=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(resource," + column + ") values(?,?) "
                    + "on conflict(resource) do update set " + column + "=EXCLUDED." + column, newValues);
        });
    }


    private static void loadDateValues(TripleDispatcher dispatcher, String property, String table)
            throws IOException, SQLException
    {
        StringStringIntPairMap keepValues = new StringStringIntPairMap();
        StringStringIntPairMap newValues = new StringStringIntPairMap();
        StringStringIntPairMap oldValues = new StringStringIntPairMap();

        load("select resource,date::varchar,timezone from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            String meshID = getMeshID(subject.getURI());
            Pair<String, Integer> value = getDate(object);

            if(value.equals(oldValues.remove(meshID)))
            {
                keepValues.put(meshID, value);
            }
            else
            {
                Pair<String, Integer> keep = keepValues.get(meshID);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of mesh." + table + ".date",
                            meshID + ": " + keep + ", " + value);

                Pair<String, Integer> put = newValues.put(meshID, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of mesh." + table + ".date",
                            meshID + ": " + put + ", " + value);
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where resource=? and date=?::date and timezone=?", oldValues);
            store("insert into mesh." + table + "(resource,date,timezone) values(?,?::date,?) "
                    + "on conflict(resource) do update set date=EXCLUDED.date, timezone=EXCLUDED.timezone", newValues);
        });
    }


    private static void loadMultiMeshValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        StringPairSet keepValues = new StringPairSet();
        StringPairSet newValues = new StringPairSet();
        StringPairSet oldValues = new StringPairSet();

        load("select resource," + column + " from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            String meshID = getMeshID(subject.getURI());
            String valueID = getMeshID(object.getURI());

            Pair<String, String> pair = Pair.getPair(meshID, valueID);

            if(oldValues.remove(pair))
                keepValues.add(pair);
            else if(!keepValues.contains(pair))
                newValues.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where resource=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(resource," + column + ") values(?,?)", newValues);
        });
    }


    private static void loadMeshValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        StringStringMap keepValues = new StringStringMap();
        StringStringMap newValues = new StringStringMap();
        StringStringMap oldValues = new StringStringMap();

        load("select resource," + column + " from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            String meshID = getMeshID(subject.getURI());
            String valueID = getMeshID(object.getURI());

            if(valueID.equals(oldValues.remove(meshID)))
            {
                keepValues.put(meshID, valueID);
            }
            else
            {
                String keep = keepValues.get(meshID);

                if(valueID.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of mesh." + table + "." + column,
                            meshID + ": " + keep + ", " + valueID);

                String put = newValues.put(meshID, valueID);

                if(put != null && !valueID.equals(put))
                    throw new DataException("multiple values of mesh." + table + "." + column,
                            meshID + ": " + put + ", " + valueID);
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where resource=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(resource," + column + ") values(?,?) "
                    + "on conflict(resource) do update set " + column + "=EXCLUDED." + column, newValues);
        });
    }


    /*
     * Tests whether the node is a literal in another language than the given one, i.e. a translation, which is not
     * loaded.
     */
    private static boolean isTranslation(Node node, String lang)
    {
        return !lang.isEmpty() && node.isLiteral() && !node.getLiteralLanguage().isEmpty()
                && !node.getLiteralLanguage().equals(lang);
    }


    /*
     * Returns the value of a literal of the given language, or of a literal without a language if the language is
     * empty; any other value is refused.
     */
    private static String getLiteral(Node node, String lang) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralLanguage().equals(lang))
        {
            String literal = lang.isEmpty() ? "a literal without a language" : "a literal in language " + lang;
            throw new DataException("unexpected value instead of " + literal, text(node));
        }

        return node.getLiteralLexicalForm();
    }


    /*
     * Returns the date of an xsd:date literal without its timezone and the timezone in seconds, or the minimal
     * integer for a date without a timezone.
     */
    private static Pair<String, Integer> getDate(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsd + "date"))
            throw new DataException("unexpected value instead of an xsd:date literal", text(node));

        String date = node.getLiteralLexicalForm();
        Matcher matcher = zonePattern.matcher(date);
        boolean zoned = matcher.find();
        String day = zoned ? date.substring(0, matcher.start()) : date;

        if(!isDate(day))
            throw new DataException("unexpected value instead of an xsd:date literal", text(node));

        if(!zoned)
            return Pair.getPair(day, Integer.MIN_VALUE);

        String zone = matcher.group();

        if(zone.equals("Z"))
            return Pair.getPair(day, 0);

        int seconds = Integer.parseInt(zone.substring(1, 3)) * 3600 + Integer.parseInt(zone.substring(4, 6)) * 60;

        return Pair.getPair(day, zone.startsWith("-") ? -seconds : seconds);
    }


    static void finish() throws IOException, SQLException
    {
        store("delete from mesh.resources where id=?", oldMeshes);
        store("insert into mesh.resources(id) values(?)", newMeshes);
    }


    static String getMeshID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        String meshID = value.substring(prefixLength);

        synchronized(newMeshes)
        {
            if(!newMeshes.contains(meshID) && !keepMeshes.contains(meshID))
            {
                missingMeshes.referenced(meshID);

                if(!oldMeshes.remove(meshID))
                    newMeshes.add(meshID);
                else
                    keepMeshes.add(meshID);
            }
        }

        return meshID;
    }



    public static void main(String[] args) throws SQLException, IOException
    {
        String file = "mesh/mesh.nt.gz";

        try
        {
            init("mesh");
            Ontology.loadCategories();

            String version = getVersion(file);
            System.out.println("=== load MeSH version " + version + " ===");
            System.out.println();

            TripleDispatcher dispatcher = new TripleDispatcher();

            check(dispatcher);
            loadTypes(dispatcher);

            loadMultiStringValues(dispatcher, meshv + "altLabel", "resource_alt_labels", "label", "en");
            loadMultiStringValues(dispatcher, meshv + "previousIndexing", "resource_previous_indexing_values", "value",
                    "en");
            loadMultiStringValues(dispatcher, meshv + "source", "resource_sources", "source", "en");
            loadMultiStringValues(dispatcher, meshv + "thesaurusID", "resource_thesauruses", "thesaurus", "en");
            loadMultiStringValues(dispatcher, rdfs + "label", "resource_labels", "label", "en");
            loadStringValues(dispatcher, meshv + "abbreviation", "resource_abbreviations", "abbreviation", "en");
            loadStringValues(dispatcher, meshv + "annotation", "resource_annotations", "annotation", "en");
            loadStringValues(dispatcher, meshv + "casn1_label", "resource_casn1_labels", "label", "en");
            loadStringValues(dispatcher, meshv + "considerAlso", "resource_consider_also_values", "value", "en");
            loadStringValues(dispatcher, meshv + "entryVersion", "resource_entry_versions", "version", "en");
            loadStringValues(dispatcher, meshv + "historyNote", "resource_history_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "lastActiveYear", "resource_last_active_years", "year", "en");
            loadStringValues(dispatcher, meshv + "lexicalTag", "resource_lexical_tags", "tag", "en");
            loadStringValues(dispatcher, meshv + "note", "resource_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "onlineNote", "resource_online_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "prefLabel", "resource_pref_labels", "label", "en");
            loadStringValues(dispatcher, meshv + "publicMeSHNote", "resource_public_mesh_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "scopeNote", "resource_scope_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "sortVersion", "resource_sort_versions", "version", "en");

            loadMultiStringValues(dispatcher, meshv + "relatedRegistryNumber", "resource_related_registry_numbers",
                    "number", "");
            loadStringValues(dispatcher, meshv + "identifier", "resource_identifiers", "identifier", "");
            loadStringValues(dispatcher, meshv + "nlmClassificationNumber", "resource_nlm_classification_numbers",
                    "number", "");
            loadStringValues(dispatcher, meshv + "registryNumber", "resource_registry_numbers", "number", "");

            loadBooleanValues(dispatcher, meshv + "active", "resource_active_flags", "flag");

            loadIntegerValues(dispatcher, meshv + "frequency", "resource_frequencies", "frequency");

            loadDateValues(dispatcher, meshv + "dateCreated", "resource_created_dates");
            loadDateValues(dispatcher, meshv + "dateRevised", "resource_revised_dates");
            loadDateValues(dispatcher, meshv + "dateEstablished", "resource_established_dates");

            loadMultiMeshValues(dispatcher, meshv + "allowableQualifier", "resource_allowable_qualifiers", "qualifier");
            loadMultiMeshValues(dispatcher, meshv + "broaderConcept", "resource_broader_concepts", "concept");
            loadMultiMeshValues(dispatcher, meshv + "broaderDescriptor", "resource_broader_descriptors", "descriptor");
            loadMultiMeshValues(dispatcher, meshv + "broaderQualifier", "resource_broader_qualifiers", "qualifier");
            loadMultiMeshValues(dispatcher, meshv + "concept", "resource_concepts", "concept");
            loadMultiMeshValues(dispatcher, meshv + "indexerConsiderAlso", "resource_indexer_consider_also_relations",
                    "value");
            loadMultiMeshValues(dispatcher, meshv + "mappedTo", "resource_mapped_to_relations", "value");
            loadMultiMeshValues(dispatcher, meshv + "narrowerConcept", "resource_narrower_concepts", "concept");
            loadMultiMeshValues(dispatcher, meshv + "pharmacologicalAction", "resource_pharmacological_actions",
                    "action");
            loadMultiMeshValues(dispatcher, meshv + "preferredMappedTo", "resource_preferred_mapped_to_relations",
                    "value");
            loadMultiMeshValues(dispatcher, meshv + "relatedConcept", "resource_related_concepts", "concept");
            loadMultiMeshValues(dispatcher, meshv + "seeAlso", "resource_see_also_relations", "reference");
            loadMultiMeshValues(dispatcher, meshv + "term", "resource_terms", "term");
            loadMultiMeshValues(dispatcher, meshv + "treeNumber", "resource_tree_numbers", "number");
            loadMeshValues(dispatcher, meshv + "hasDescriptor", "resource_descriptors", "descriptor");
            loadMeshValues(dispatcher, meshv + "hasQualifier", "resource_qualifiers", "qualifier");
            loadMeshValues(dispatcher, meshv + "parentTreeNumber", "resource_parent_tree_numbers", "number");
            loadMeshValues(dispatcher, meshv + "preferredConcept", "resource_preferred_concepts", "concept");
            loadMeshValues(dispatcher, meshv + "preferredTerm", "resource_preferred_terms", "term");
            loadMeshValues(dispatcher, meshv + "useInstead", "resource_use_instead_relations", "value");

            dispatcher.load(file);
            missingMeshes.settle();
            dispatcher.finish();

            finish();

            setVersion("Medical Subject Headings (MESH)", version);

            updateVersion();

            checkFiles("mesh");
            MissingEntities.printSummary();
            checkProblems();

            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
