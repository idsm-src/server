package cz.iocb.load.mesh;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.ObjectIntPair;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.StringPair;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



public class Mesh extends Updater
{
    private static ObjectIntHashMap<String> zoneTable = new ObjectIntHashMap<String>()
    {
        {
            put("", -2147483648);
            put("-04:00", -14400);
            put("-05:00", -18000);
        }
    };


    private static void loadTypes(Model model) throws IOException, SQLException
    {
        StringIntMap newValues = new StringIntMap();
        StringIntMap oldValues = getStringIntMap("select id, type_id from mesh.mesh_bases");

        new QueryResultProcessor(patternQuery("?mesh rdf:type ?type"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getStringID("mesh", "http://id.nlm.nih.gov/mesh/");
                Identifier type = Ontology.getId(getIRI("type"));

                if(type.unit != Ontology.unitUncategorized)
                    throw new IOException();

                if(type.id != oldValues.removeKeyIfAbsent(meshID, NO_VALUE))
                    newValues.put(meshID, type.id);
            }
        }.load(model);

        batch("delete from mesh.mesh_bases where id = ? and type_id = ?", oldValues);
        batch("insert into mesh.mesh_bases(id, type_id) values(?,?) "
                + "on conflict (id) do update set type_id=EXCLUDED.type_id", newValues);
    }


    private static void loadMultiStringValues(Model model, String property, String table, String column, String lang)
            throws IOException, SQLException
    {
        StringPairSet newValues = new StringPairSet();
        StringPairSet oldValues = getStringPairSet("select mesh, " + column + " from mesh." + table);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value. filter(lang(?value) = \"" + lang + "\")"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getStringID("mesh", "http://id.nlm.nih.gov/mesh/");
                String value = getString("value");
                StringPair pair = new StringPair(meshID, value);

                if(!oldValues.remove(pair))
                    newValues.add(pair);
            }
        }.load(model);

        batch("delete from mesh." + table + " where mesh = ? and " + column + " = ?", oldValues);
        batch("insert into mesh." + table + "(mesh, " + column + ") values(?,?)", newValues);
    }


    private static void loadStringValues(Model model, String property, String table, String column, String lang)
            throws IOException, SQLException
    {
        StringStringMap newValues = new StringStringMap();
        StringStringMap oldValues = getStringStringMap("select mesh, " + column + " from mesh." + table);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value. filter(lang(?value) = \"" + lang + "\")"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getStringID("mesh", "http://id.nlm.nih.gov/mesh/");
                String value = getString("value");

                if(!value.equals(oldValues.remove(meshID)))
                    newValues.put(meshID, value);
            }
        }.load(model);

        batch("delete from mesh." + table + " where mesh = ? and " + column + " = ?", oldValues);
        batch("insert into mesh." + table + "(mesh, " + column + ") values(?,?) on conflict (mesh) do update set "
                + column + "=EXCLUDED." + column, newValues);
    }


    private static void loadBooleanValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        StringIntMap newValues = new StringIntMap();
        StringIntMap oldValues = getStringIntMap("select mesh, " + column + "::integer from mesh." + table);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getStringID("mesh", "http://id.nlm.nih.gov/mesh/");
                int value = getBoolean("value") ? 1 : 0;

                if(value != oldValues.removeKeyIfAbsent(meshID, NO_VALUE))
                    newValues.put(meshID, value);
            }
        }.load(model);

        batch("delete from mesh." + table + " where mesh = ? and " + column + " = cast(? as boolean)", oldValues);
        batch("insert into mesh." + table + "(mesh, " + column + ") values(?,cast(? as boolean)) "
                + "on conflict (mesh) do update set " + column + "=EXCLUDED." + column, newValues);
    }


    private static void loadIntegerValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        StringIntMap newValues = new StringIntMap();
        StringIntMap oldValues = getStringIntMap("select mesh, " + column + " from mesh." + table);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getStringID("mesh", "http://id.nlm.nih.gov/mesh/");
                int value = getInt("value");

                if(value != oldValues.removeKeyIfAbsent(meshID, NO_VALUE))
                    newValues.put(meshID, value);
            }
        }.load(model);

        batch("delete from mesh." + table + " where mesh = ? and " + column + " = ?", oldValues);
        batch("insert into mesh." + table + "(mesh, " + column + ") values(?,?) on conflict (mesh) do update set "
                + column + "=EXCLUDED." + column, newValues);
    }


    private static void loadDateValues(Model model, String property, String table) throws IOException, SQLException
    {
        StringStringIntPairMap newValues = new StringStringIntPairMap();
        StringStringIntPairMap oldValues = getStringStringIntPairMap(
                "select mesh, date::varchar, timezone from mesh." + table);

        new QueryResultProcessor(
                "select ?mesh (str(?date) as ?value) (tz(?date) as ?zone) where { ?mesh " + property + " ?date }")
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getStringID("mesh", "http://id.nlm.nih.gov/mesh/");
                String value = getString("value").replaceFirst("-0[45]:00$", "");
                Integer timezone = zoneTable.get(getString("zone"));
                ObjectIntPair<String> date = PrimitiveTuples.pair(value, timezone);

                if(!date.equals(oldValues.remove(meshID)))
                    newValues.put(meshID, date);
            }
        }.load(model);

        batch("delete from mesh." + table + " where mesh = ? and date = cast(? as date) and timezone = ?", oldValues);
        batch("insert into mesh." + table + "(mesh, date, timezone) values(?,cast(? as date),?) "
                + "on conflict (mesh) do update set date=EXCLUDED.date, timezone=EXCLUDED.timezone", newValues);
    }


    private static void loadMultiMeshValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        StringPairSet newValues = new StringPairSet();
        StringPairSet oldValues = getStringPairSet("select mesh, " + column + " from mesh." + table);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getStringID("mesh", "http://id.nlm.nih.gov/mesh/");
                String valueID = getStringID("value", "http://id.nlm.nih.gov/mesh/");
                StringPair pair = new StringPair(meshID, valueID);

                if(!oldValues.remove(pair))
                    newValues.add(pair);
            }
        }.load(model);

        batch("delete from mesh." + table + " where mesh = ? and " + column + " = ?", oldValues);
        batch("insert into mesh." + table + "(mesh, " + column + ") values(?,?)", newValues);
    }


    private static void loadMeshValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        StringStringMap newValues = new StringStringMap();
        StringStringMap oldValues = getStringStringMap("select mesh, " + column + " from mesh." + table);

        new QueryResultProcessor(patternQuery("?mesh " + property + " ?value"))
        {
            @Override
            protected void parse() throws IOException
            {
                String meshID = getStringID("mesh", "http://id.nlm.nih.gov/mesh/");
                String valueID = getStringID("value", "http://id.nlm.nih.gov/mesh/");

                if(!valueID.equals(oldValues.remove(meshID)))
                    newValues.put(meshID, valueID);
            }
        }.load(model);

        batch("delete from mesh." + table + " where mesh = ? and " + column + " = ?", oldValues);
        batch("insert into mesh." + table + "(mesh, " + column + ") values(?,?) on conflict (mesh) do update set "
                + column + "=EXCLUDED." + column, newValues);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load MeSH ...");

        Model model = getModel("mesh/mesh.nt");

        Ontology.loadCategories();

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

        model.close();
        System.out.println();
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();
            load();
            commit();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
