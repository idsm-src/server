package cz.iocb.load.mesh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Mesh extends Updater
{
    static final String prefix = "http://id.nlm.nih.gov/mesh/";
    static final int prefixLength = prefix.length();

    private static final StringSet keepMeshes = new StringSet();
    private static final StringSet newMeshes = new StringSet();
    private static final StringSet oldMeshes = new StringSet();

    @SuppressWarnings("serial") private static HashMap<String, Integer> zoneTable = new HashMap<String, Integer>()
    {
        {
            put("", -2147483648);
            put("-04:00", -14400);
            put("-05:00", -18000);
        }
    };


    private static String getVersion(String file) throws IOException
    {
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line = reader.readLine();

            if(!line.matches("# <http://id\\.nlm\\.nih\\.gov/mesh> exported at 20[0-9]{2}-[0-9]{2}-[0-9]{2} .*"))
                throw new IOException();

            return line.replaceFirst(
                    "^# <http://id\\.nlm\\.nih\\.gov/mesh> exported at (20[0-9]{2}-[0-9]{2}-[0-9]{2}) .*$", "$1");
        }
    }


    private static void loadTypes(Model model) throws IOException, SQLException
    {
        StringIntMap keepTypes = new StringIntMap();
        StringIntMap newTypes = new StringIntMap();
        StringIntMap oldTypes = new StringIntMap();

        load("select id from mesh.mesh_bases", oldMeshes);
        load("select id,type_id from mesh.mesh_bases where type_id is not null", oldTypes);

        new QueryResultProcessor(patternQuery("?mesh rdf:type ?type"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getStringID("mesh", prefix);
                Pair<Integer, Integer> type = Ontology.getId(getIRI("type"));

                if(type.getOne() != Ontology.unitUncategorized)
                    throw new IOException();

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
            }
        }.load(model);

        store("update mesh.mesh_bases set type_id=null where id=? and type_id=?", oldTypes);
        store("insert into mesh.mesh_bases(id,type_id) values(?,?) "
                + "on conflict(id) do update set type_id=EXCLUDED.type_id", newTypes);
    }


    private static void loadMultiStringValues(Model model, String property, String table, String column, String lang)
            throws IOException, SQLException
    {
        StringPairSet newValues = new StringPairSet();
        StringPairSet oldValues = new StringPairSet();

        load("select mesh," + column + " from mesh." + table, oldValues);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value. filter(lang(?value) = \"" + lang + "\")"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getMeshID(getIRI("mesh"));
                String value = getString("value");

                Pair<String, String> pair = Pair.getPair(meshID, value);

                if(!oldValues.remove(pair))
                    newValues.add(pair);
            }
        }.load(model);

        store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
        store("insert into mesh." + table + "(mesh," + column + ") values(?,?)", newValues);
    }


    private static void loadStringValues(Model model, String property, String table, String column, String lang)
            throws IOException, SQLException
    {
        StringStringMap keepValues = new StringStringMap();
        StringStringMap newValues = new StringStringMap();
        StringStringMap oldValues = new StringStringMap();

        load("select mesh," + column + " from mesh." + table, oldValues);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value. filter(lang(?value) = \"" + lang + "\")"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getMeshID(getIRI("mesh"));
                String value = getString("value");

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
            }
        }.load(model);

        store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
        store("insert into mesh." + table + "(mesh," + column + ") values(?,?) on conflict(mesh) do update set "
                + column + "=EXCLUDED." + column, newValues);
    }


    private static void loadBooleanValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        StringIntMap keepValues = new StringIntMap();
        StringIntMap newValues = new StringIntMap();
        StringIntMap oldValues = new StringIntMap();

        load("select mesh," + column + "::integer from mesh." + table, oldValues);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getMeshID(getIRI("mesh"));
                Integer value = getBoolean("value") ? 1 : 0;

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
            }
        }.load(model);

        store("delete from mesh." + table + " where mesh=? and " + column + "=?::boolean", oldValues);
        store("insert into mesh." + table + "(mesh," + column + ") values(?,?::boolean) "
                + "on conflict(mesh) do update set " + column + "=EXCLUDED." + column, newValues);
    }


    private static void loadIntegerValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        StringIntMap keepValues = new StringIntMap();
        StringIntMap newValues = new StringIntMap();
        StringIntMap oldValues = new StringIntMap();

        load("select mesh," + column + " from mesh." + table, oldValues);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getMeshID(getIRI("mesh"));
                Integer value = getInt("value");

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
            }
        }.load(model);

        store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
        store("insert into mesh." + table + "(mesh," + column + ") values(?,?) on conflict(mesh) do update set "
                + column + "=EXCLUDED." + column, newValues);
    }


    private static void loadDateValues(Model model, String property, String table) throws IOException, SQLException
    {
        StringStringIntPairMap keepValues = new StringStringIntPairMap();
        StringStringIntPairMap newValues = new StringStringIntPairMap();
        StringStringIntPairMap oldValues = new StringStringIntPairMap();

        load("select mesh,date::varchar,timezone from mesh." + table, oldValues);

        new QueryResultProcessor(
                "select ?mesh (str(?date) as ?value) (tz(?date) as ?zone) where { ?mesh " + property + " ?date }")
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getMeshID(getIRI("mesh"));
                String date = getString("value").replaceFirst("-0[45]:00$", "");
                Integer timezone = zoneTable.get(getString("zone"));
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
            }
        }.load(model);

        store("delete from mesh." + table + " where mesh=? and date=?::date and timezone=?", oldValues);
        store("insert into mesh." + table + "(mesh,date,timezone) values(?,?::date,?) "
                + "on conflict(mesh) do update set date=EXCLUDED.date, timezone=EXCLUDED.timezone", newValues);
    }


    private static void loadMultiMeshValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        StringPairSet newValues = new StringPairSet();
        StringPairSet oldValues = new StringPairSet();

        load("select mesh," + column + " from mesh." + table, oldValues);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getMeshID(getIRI("mesh"));
                String valueID = getMeshID(getIRI("value"));
                Pair<String, String> pair = Pair.getPair(meshID, valueID);

                if(!oldValues.remove(pair))
                    newValues.add(pair);
            }
        }.load(model);

        store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
        store("insert into mesh." + table + "(mesh," + column + ") values(?,?)", newValues);
    }


    private static void loadMeshValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        StringStringMap keepValues = new StringStringMap();
        StringStringMap newValues = new StringStringMap();
        StringStringMap oldValues = new StringStringMap();

        load("select mesh," + column + " from mesh." + table, oldValues);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getMeshID(getIRI("mesh"));
                String valueID = getMeshID(getIRI("value"));

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
            }
        }.load(model);

        store("delete from mesh." + table + " where mesh=? and " + column + "=?", oldValues);
        store("insert into mesh." + table + "(mesh," + column + ") values(?,?) on conflict(mesh) do update set "
                + column + "=EXCLUDED." + column, newValues);
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
                System.out.println("    add missing mesh " + meshID);

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
        String file = "mesh/mesh.nt";

        try
        {
            init();
            Ontology.loadCategories();

            String version = getVersion(baseDirectory + file);
            System.out.println("=== load MeSH version " + version + " ===");
            System.out.println();

            Model model = getModel(file);

            check(model, "mesh/check.sparql");

            loadTypes(model);

            loadMultiStringValues(model, "meshv:altLabel", "alt_labels", "label", "en");
            loadMultiStringValues(model, "meshv:previousIndexing", "previous_indexing_values", "value", "en");
            loadMultiStringValues(model, "meshv:source", "sources", "source", "en");
            loadMultiStringValues(model, "meshv:thesaurusID", "thesauruses", "thesaurus", "en");
            loadMultiStringValues(model, "rdfs:label", "labels", "label", "en");
            loadStringValues(model, "meshv:abbreviation", "abbreviations", "abbreviation", "en");
            loadStringValues(model, "meshv:annotation", "annotations", "annotation", "en");
            loadStringValues(model, "meshv:casn1_label", "casn1_labels", "label", "en");
            loadStringValues(model, "meshv:considerAlso", "consider_also_values", "value", "en");
            loadStringValues(model, "meshv:entryVersion", "entry_versions", "version", "en");
            loadStringValues(model, "meshv:historyNote", "history_notes", "note", "en");
            loadStringValues(model, "meshv:lastActiveYear", "last_active_years", "year", "en");
            loadStringValues(model, "meshv:lexicalTag", "lexical_tags", "tag", "en");
            loadStringValues(model, "meshv:note", "notese_notes", "note", "en");
            loadStringValues(model, "meshv:onlineNote", "online_notes", "note", "en");
            loadStringValues(model, "meshv:prefLabel", "pref_labels", "label", "en");
            loadStringValues(model, "meshv:publicMeSHNote", "public_mesh_notes", "note", "en");
            loadStringValues(model, "meshv:scopeNote", "scope_notes", "note", "en");
            loadStringValues(model, "meshv:sortVersion", "sort_versions", "version", "en");

            loadMultiStringValues(model, "meshv:relatedRegistryNumber", "related_registry_numbers", "number", "");
            loadStringValues(model, "meshv:identifier", "identifiers", "identifier", "");
            loadStringValues(model, "meshv:nlmClassificationNumber", "nlm_cassification_numbers", "number", "");
            loadStringValues(model, "meshv:registryNumber", "registry_numbers", "number", "");

            loadBooleanValues(model, "meshv:active", "active_property", "value");

            loadIntegerValues(model, "meshv:frequency", "frequencies", "frequency");

            loadDateValues(model, "meshv:dateCreated", "created_dates");
            loadDateValues(model, "meshv:dateRevised", "revised_dates");
            loadDateValues(model, "meshv:dateEstablished", "established_dates");

            loadMultiMeshValues(model, "meshv:allowableQualifier", "allowable_qualifiers", "qualifier");
            loadMultiMeshValues(model, "meshv:broaderConcept", "broader_concepts", "concept");
            loadMultiMeshValues(model, "meshv:broaderDescriptor", "broader_descriptors", "descriptor");
            loadMultiMeshValues(model, "meshv:broaderQualifier", "broader_qualifiers", "qualifier");
            loadMultiMeshValues(model, "meshv:concept", "concepts", "concept");
            loadMultiMeshValues(model, "meshv:indexerConsiderAlso", "indexer_consider_also_relations", "value");
            loadMultiMeshValues(model, "meshv:mappedTo", "mapped_to_relations", "value");
            loadMultiMeshValues(model, "meshv:narrowerConcept", "narrower_concepts", "concept");
            loadMultiMeshValues(model, "meshv:pharmacologicalAction", "pharmacological_actions", "action");
            loadMultiMeshValues(model, "meshv:preferredMappedTo", "preferred_mapped_to_relations", "value");
            loadMultiMeshValues(model, "meshv:relatedConcept", "related_concepts", "concept");
            loadMultiMeshValues(model, "meshv:seeAlso", "see_also_relations", "reference");
            loadMultiMeshValues(model, "meshv:term", "terms", "term");
            loadMultiMeshValues(model, "meshv:treeNumber", "tree_numbers", "number");
            loadMeshValues(model, "meshv:hasDescriptor", "descriptors", "descriptor");
            loadMeshValues(model, "meshv:hasQualifier", "qualifiers", "qualifier");
            loadMeshValues(model, "meshv:parentTreeNumber", "parent_tree_numbers", "number");
            loadMeshValues(model, "meshv:preferredConcept", "preferred_concept", "concept");
            loadMeshValues(model, "meshv:preferredTerm", "preferred_term", "term");
            loadMeshValues(model, "meshv:useInstead", "use_instead_relations", "value");

            finish();

            setVersion("Medical Subject Headings (MESH)", version);

            model.close();
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
