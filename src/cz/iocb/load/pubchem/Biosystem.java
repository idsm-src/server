package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Biosystem extends Updater
{
    private static void loadBases(Model model) throws IOException, SQLException
    {
        IntHashSet newBiosystems = new IntHashSet(1000000);
        IntHashSet oldBiosystems = getIntSet("select id from biosystem_bases", 1000000);

        new QueryResultProcessor(patternQuery("?biosystem rdf:type bp:Pathway"))
        {
            @Override
            protected void parse() throws IOException
            {
                int biosystemID = getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");

                if(!oldBiosystems.remove(biosystemID))
                    newBiosystems.add(biosystemID);
            }
        }.load(model);

        batch("delete from biosystem_bases where id = ?", oldBiosystems);


        IntStringMap newTitles = new IntStringMap(1000000);
        IntStringMap oldTitles = getIntStringMap("select id, title from biosystem_bases", 1000000);

        new QueryResultProcessor(patternQuery("?biosystem dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int biosystemID = getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");
                String title = getString("title");

                if(!title.equals(oldTitles.remove(biosystemID)))
                    newTitles.put(biosystemID, title);
            }
        }.load(model);

        oldBiosystems.forEach(key -> oldTitles.remove(key));

        if(!oldTitles.isEmpty())
            throw new IOException();


        IntIntHashMap newSources = new IntIntHashMap(1000000);
        IntIntHashMap oldSources = getIntIntMap("select id, source from biosystem_bases", 1000000);

        new QueryResultProcessor(patternQuery("?biosystem dcterms:source ?source"))
        {
            @Override
            protected void parse() throws IOException
            {
                int biosystemID = getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");
                int sourceID = Source.getSourceID(getIRI("source"));

                if(oldSources.removeKeyIfAbsent(biosystemID, NO_VALUE) != sourceID)
                    newSources.put(biosystemID, sourceID);
            }
        }.load(model);

        oldBiosystems.forEach(key -> oldSources.remove(key));

        if(!oldSources.isEmpty())
            throw new IOException();


        batch("insert into biosystem_bases(id, source, title) values (?,?,?)", newBiosystems,
                (PreparedStatement statement, int biosystem) -> {
                    statement.setInt(1, biosystem);
                    statement.setInt(2, newSources.getOrThrow(biosystem));
                    statement.setString(3, newTitles.remove(biosystem));
                    newSources.remove(biosystem);
                });

        batch("update biosystem_bases set source = ? where id = ?", newSources, Direction.REVERSE);
        batch("update biosystem_bases set title = ? where id = ?", newTitles, Direction.REVERSE);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntHashMap newOrganisms = new IntIntHashMap(1000000);
        IntIntHashMap oldOrganisms = getIntIntMap(
                "select id, organism_id from biosystem_bases where organism_id is not null", 1000000);

        new QueryResultProcessor(patternQuery("?biosystem bp:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                int biosystemID = getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");
                int organismID = getIntID("organism", "http://identifiers.org/taxonomy/");

                if(organismID != oldOrganisms.removeKeyIfAbsent(biosystemID, NO_VALUE))
                    newOrganisms.put(biosystemID, organismID);
            }
        }.load(model);

        batch("update biosystem_bases set organism_id = null where id = ?", oldOrganisms.keySet());
        batch("update biosystem_bases set organism_id = ? where id = ?", newOrganisms, Direction.REVERSE);
    }


    private static void loadComponents(Model model) throws IOException, SQLException
    {
        IntPairSet newComponents = new IntPairSet(1000000);
        IntPairSet oldComponents = getIntPairSet("select biosystem, component from biosystem_components", 1000000);

        new QueryResultProcessor(patternQuery("?biosystem bp:pathwayComponent ?component"))
        {
            @Override
            protected void parse() throws IOException
            {
                int biosystemID = getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");
                int componentID = getIntID("component", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");

                IntIntPair pair = PrimitiveTuples.pair(biosystemID, componentID);

                if(!oldComponents.remove(pair))
                    newComponents.add(pair);
            }
        }.load(model);

        batch("delete from biosystem_components where biosystem = ? and component = ?", oldComponents);
        batch("insert into biosystem_components(biosystem, component) values (?,?)", newComponents);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet(1000000);
        IntPairSet oldReferences = getIntPairSet("select biosystem, reference from biosystem_references", 1000000);

        new QueryResultProcessor(patternQuery("?biosystem cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                int biosystemID = getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");
                int referenceID = getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");

                IntIntPair pair = PrimitiveTuples.pair(biosystemID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        batch("delete from biosystem_references where biosystem = ? and reference = ?", oldReferences);
        batch("insert into biosystem_references(biosystem, reference) values (?,?)", newReferences);
    }


    private static void loadMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet(1000000);
        IntPairSet oldMatches = getIntPairSet("select biosystem, wikipathway from biosystem_matches", 1000000);

        new QueryResultProcessor(patternQuery("?biosystem skos:exactMatch ?wikipathway"))
        {
            @Override
            protected void parse() throws IOException
            {
                int biosystemID = getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");
                int wikipathwayID = getIntID("wikipathway", "http://identifiers.org/wikipathways/WP");

                IntIntPair pair = PrimitiveTuples.pair(biosystemID, wikipathwayID);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from biosystem_matches where biosystem = ? and wikipathway = ?", oldMatches);
        batch("insert into biosystem_matches(biosystem, wikipathway) values (?,?)", newMatches);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load biosystems ...");

        Model model = getModel("RDF/biosystem/pc_biosystem.ttl.gz");
        check(model, "biosystem/check.sparql");

        loadBases(model);
        loadOrganisms(model);
        loadComponents(model);
        loadReferences(model);
        loadMatches(model);

        model.close();
        System.out.println();
    }
}
