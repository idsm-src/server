package cz.iocb.load.mesh;

import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getBoolean;
import static cz.iocb.load.common.TripleStreamProcessor.getInt;
import static cz.iocb.load.common.TripleStreamProcessor.getLexicalForm;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.jena.graph.Node;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
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


    private static HashMap<String, Integer> zoneTable = new HashMap<>()
    {
        {
            put("", -2147483648);
            put("-04:00", -14400);
            put("-05:00", -18000);
        }
    };

    private static final Pattern zonePattern = Pattern.compile("(Z|[+-][0-9]{2}:[0-9]{2})$");


    private static String getVersion(String file) throws IOException
    {
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(new FileInputStream(file), 65536), UTF_8)))
        {
            String line;

            while((line = reader.readLine()) != null)
            {
                if(line.matches("# <http://id\\.nlm\\.nih\\.gov/mesh> exported at 20[0-9]{2}-[0-9]{2}-[0-9]{2} .*"))
                    return line.replaceFirst(
                            "^# <http://id\\.nlm\\.nih\\.gov/mesh> exported at (20[0-9]{2}-[0-9]{2}-[0-9]{2}) .*$",
                            "$1");
            }

            throw new IOException();
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
                meshv + "useInstead");
    }


    private static void loadTypes(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        StringIntMap keepTypes = new StringIntMap();
        StringIntMap newTypes = new StringIntMap();
        StringIntMap oldTypes = new StringIntMap();

        load("select id from mesh.mesh_bases", oldMeshes);
        load("select id,type_id from mesh.mesh_bases where type_id is not null", oldTypes);

        dispatcher.on(rdf + "type", (subject, object) -> {
            String meshID = getStringID(subject, prefix);
            Pair<Integer, Integer> type = Ontology.getId(object.getURI());

            if(type == null || type.getOne() != OntologyResource.unitUncategorized)
                throw new IOException(object.getURI());

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
                    throw new IOException();

                Integer put = newTypes.put(meshID, type.getTwo());

                if(put != null && !type.getTwo().equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("update mesh.mesh_bases set type_id=null where id=? and type_id=?", oldTypes);
            store("insert into mesh.mesh_bases(id,type_id) values(?,?) "
                    + "on conflict(id) do update set type_id=EXCLUDED.type_id", newTypes);
        });
    }


    private static void loadMultiStringValues(TripleDispatcher dispatcher, String property, String table, String column,
            String lang) throws IOException, SQLException
    {
        StringPairSet keepValues = new StringPairSet();
        StringPairSet newValues = new StringPairSet();
        StringPairSet oldValues = new StringPairSet();

        load("select mesh," + column + " from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            if(!object.isLiteral() || !object.getLiteralLanguage().equals(lang))
                return;

            String meshID = getMeshID(subject.getURI());
            String value = getLexicalForm(object);

            Pair<String, String> pair = Pair.getPair(meshID, value);

            if(oldValues.remove(pair))
                keepValues.add(pair);
            else if(!keepValues.contains(pair))
                newValues.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(mesh," + column + ") values(?,?)", newValues);
        });
    }


    private static void loadStringValues(TripleDispatcher dispatcher, String property, String table, String column,
            String lang) throws IOException, SQLException
    {
        StringStringMap keepValues = new StringStringMap();
        StringStringMap newValues = new StringStringMap();
        StringStringMap oldValues = new StringStringMap();

        load("select mesh," + column + " from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            if(!object.isLiteral() || !object.getLiteralLanguage().equals(lang))
                return;

            String meshID = getMeshID(subject.getURI());
            String value = getLexicalForm(object);

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
                    throw new IOException();

                String put = newValues.put(meshID, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(mesh," + column + ") values(?,?) on conflict(mesh) do update set "
                    + column + "=EXCLUDED." + column, newValues);
        });
    }


    private static void loadBooleanValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        StringIntMap keepValues = new StringIntMap();
        StringIntMap newValues = new StringIntMap();
        StringIntMap oldValues = new StringIntMap();

        load("select mesh," + column + "::integer from mesh." + table, oldValues);

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
                    throw new IOException();

                Integer put = newValues.put(meshID, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where mesh=? and " + column + "=?::boolean", oldValues);
            store("insert into mesh." + table + "(mesh," + column + ") values(?,?::boolean) "
                    + "on conflict(mesh) do update set " + column + "=EXCLUDED." + column, newValues);
        });
    }


    private static void loadIntegerValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        StringIntMap keepValues = new StringIntMap();
        StringIntMap newValues = new StringIntMap();
        StringIntMap oldValues = new StringIntMap();

        load("select mesh," + column + " from mesh." + table, oldValues);

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
                    throw new IOException();

                Integer put = newValues.put(meshID, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(mesh," + column + ") values(?,?) on conflict(mesh) do update set "
                    + column + "=EXCLUDED." + column, newValues);
        });
    }


    private static void loadDateValues(TripleDispatcher dispatcher, String property, String table)
            throws IOException, SQLException
    {
        StringStringIntPairMap keepValues = new StringStringIntPairMap();
        StringStringIntPairMap newValues = new StringStringIntPairMap();
        StringStringIntPairMap oldValues = new StringStringIntPairMap();

        load("select mesh,date::varchar,timezone from mesh." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            String meshID = getMeshID(subject.getURI());
            String date = getDate(object).replaceFirst("-0[45]:00$", "");
            Integer timezone = zoneTable.get(getZone(object));
            Pair<String, Integer> value = Pair.getPair(date, timezone);

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
                    throw new IOException();

                Pair<String, Integer> put = newValues.put(meshID, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where mesh=? and date=?::date and timezone=?", oldValues);
            store("insert into mesh." + table + "(mesh,date,timezone) values(?,?::date,?) "
                    + "on conflict(mesh) do update set date=EXCLUDED.date, timezone=EXCLUDED.timezone", newValues);
        });
    }


    private static void loadMultiMeshValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        StringPairSet keepValues = new StringPairSet();
        StringPairSet newValues = new StringPairSet();
        StringPairSet oldValues = new StringPairSet();

        load("select mesh," + column + " from mesh." + table, oldValues);

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
            store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(mesh," + column + ") values(?,?)", newValues);
        });
    }


    private static void loadMeshValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        StringStringMap keepValues = new StringStringMap();
        StringStringMap newValues = new StringStringMap();
        StringStringMap oldValues = new StringStringMap();

        load("select mesh," + column + " from mesh." + table, oldValues);

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
                    throw new IOException();

                String put = newValues.put(meshID, valueID);

                if(put != null && !valueID.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
            store("insert into mesh." + table + "(mesh," + column + ") values(?,?) on conflict(mesh) do update set "
                    + column + "=EXCLUDED." + column, newValues);
        });
    }


    /*
     * Returns the lexical form of an xsd:date literal.
     */
    private static String getDate(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsd + "date"))
            throw new IOException("unexpected value instead of an xsd:date literal: " + node);

        return node.getLiteralLexicalForm();
    }


    /*
     * Returns the timezone of an xsd:date literal as the SPARQL function tz() does, the empty string for a date
     * without a timezone.
     */
    private static String getZone(Node node) throws IOException
    {
        Matcher matcher = zonePattern.matcher(getDate(node));

        return matcher.find() ? matcher.group() : "";
    }


    static void finish() throws IOException, SQLException
    {
        store("delete from mesh.mesh_bases where id=?", oldMeshes);
        store("insert into mesh.mesh_bases(id) values(?)", newMeshes);
    }


    static String getMeshID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

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
            init();
            Ontology.loadCategories();

            String version = getVersion(baseDirectory + file);
            System.out.println("=== load MeSH version " + version + " ===");
            System.out.println();

            TripleDispatcher dispatcher = new TripleDispatcher();

            check(dispatcher);
            loadTypes(dispatcher);

            loadMultiStringValues(dispatcher, meshv + "altLabel", "alt_labels", "label", "en");
            loadMultiStringValues(dispatcher, meshv + "previousIndexing", "previous_indexing_values", "value", "en");
            loadMultiStringValues(dispatcher, meshv + "source", "sources", "source", "en");
            loadMultiStringValues(dispatcher, meshv + "thesaurusID", "thesauruses", "thesaurus", "en");
            loadMultiStringValues(dispatcher, rdfs + "label", "labels", "label", "en");
            loadStringValues(dispatcher, meshv + "abbreviation", "abbreviations", "abbreviation", "en");
            loadStringValues(dispatcher, meshv + "annotation", "annotations", "annotation", "en");
            loadStringValues(dispatcher, meshv + "casn1_label", "casn1_labels", "label", "en");
            loadStringValues(dispatcher, meshv + "considerAlso", "consider_also_values", "value", "en");
            loadStringValues(dispatcher, meshv + "entryVersion", "entry_versions", "version", "en");
            loadStringValues(dispatcher, meshv + "historyNote", "history_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "lastActiveYear", "last_active_years", "year", "en");
            loadStringValues(dispatcher, meshv + "lexicalTag", "lexical_tags", "tag", "en");
            loadStringValues(dispatcher, meshv + "note", "notese_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "onlineNote", "online_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "prefLabel", "pref_labels", "label", "en");
            loadStringValues(dispatcher, meshv + "publicMeSHNote", "public_mesh_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "scopeNote", "scope_notes", "note", "en");
            loadStringValues(dispatcher, meshv + "sortVersion", "sort_versions", "version", "en");

            loadMultiStringValues(dispatcher, meshv + "relatedRegistryNumber", "related_registry_numbers", "number",
                    "");
            loadStringValues(dispatcher, meshv + "identifier", "identifiers", "identifier", "");
            loadStringValues(dispatcher, meshv + "nlmClassificationNumber", "nlm_cassification_numbers", "number", "");
            loadStringValues(dispatcher, meshv + "registryNumber", "registry_numbers", "number", "");

            loadBooleanValues(dispatcher, meshv + "active", "active_property", "value");

            loadIntegerValues(dispatcher, meshv + "frequency", "frequencies", "frequency");

            loadDateValues(dispatcher, meshv + "dateCreated", "created_dates");
            loadDateValues(dispatcher, meshv + "dateRevised", "revised_dates");
            loadDateValues(dispatcher, meshv + "dateEstablished", "established_dates");

            loadMultiMeshValues(dispatcher, meshv + "allowableQualifier", "allowable_qualifiers", "qualifier");
            loadMultiMeshValues(dispatcher, meshv + "broaderConcept", "broader_concepts", "concept");
            loadMultiMeshValues(dispatcher, meshv + "broaderDescriptor", "broader_descriptors", "descriptor");
            loadMultiMeshValues(dispatcher, meshv + "broaderQualifier", "broader_qualifiers", "qualifier");
            loadMultiMeshValues(dispatcher, meshv + "concept", "concepts", "concept");
            loadMultiMeshValues(dispatcher, meshv + "indexerConsiderAlso", "indexer_consider_also_relations", "value");
            loadMultiMeshValues(dispatcher, meshv + "mappedTo", "mapped_to_relations", "value");
            loadMultiMeshValues(dispatcher, meshv + "narrowerConcept", "narrower_concepts", "concept");
            loadMultiMeshValues(dispatcher, meshv + "pharmacologicalAction", "pharmacological_actions", "action");
            loadMultiMeshValues(dispatcher, meshv + "preferredMappedTo", "preferred_mapped_to_relations", "value");
            loadMultiMeshValues(dispatcher, meshv + "relatedConcept", "related_concepts", "concept");
            loadMultiMeshValues(dispatcher, meshv + "seeAlso", "see_also_relations", "reference");
            loadMultiMeshValues(dispatcher, meshv + "term", "terms", "term");
            loadMultiMeshValues(dispatcher, meshv + "treeNumber", "tree_numbers", "number");
            loadMeshValues(dispatcher, meshv + "hasDescriptor", "descriptors", "descriptor");
            loadMeshValues(dispatcher, meshv + "hasQualifier", "qualifiers", "qualifier");
            loadMeshValues(dispatcher, meshv + "parentTreeNumber", "parent_tree_numbers", "number");
            loadMeshValues(dispatcher, meshv + "preferredConcept", "preferred_concept", "concept");
            loadMeshValues(dispatcher, meshv + "preferredTerm", "preferred_term", "term");
            loadMeshValues(dispatcher, meshv + "useInstead", "use_instead_relations", "value");

            dispatcher.load(file);
            missingMeshes.settle();
            dispatcher.finish();

            finish();
            MissingEntities.printSummary();

            setVersion("Medical Subject Headings (MESH)", version);

            updateVersion();
            commit();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
