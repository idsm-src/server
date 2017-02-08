package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.jena.graph.Node;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.StreamTableLoader;



public class Substance extends Loader
{
    static private void loadCompounds(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into substance_bases(id, compound) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000477"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
                setValue(2, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    static private void loadAvailabilities(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "update substance_bases set available=cast(? as date) where id=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/available"))
                    throw new IOException();

                setValue(1, getString(object));
                setValue(2, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
            }
        }.load();

        stream.close();
    }


    static private void loadModifiedDates(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "update substance_bases set modified=cast(? as date) where id=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/modified"))
                    throw new IOException();

                setValue(1, getString(object));
                setValue(2, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
            }
        }.load();

        stream.close();
    }


    static private void loadSources(String file, Map<String, Short> sources, List<String> newSources)
            throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "update substance_bases set source=? where id=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/source"))
                    throw new IOException();

                String sourceIri = object.getURI();
                Short sourceId = sources.get(sourceIri);

                if(sourceId == null)
                {
                    synchronized(sources)
                    {
                        sourceId = sources.get(sourceIri);

                        if(sourceId == null)
                        {
                            sourceId = (short) (sources.size() + 1);
                            sources.put(sourceIri, sourceId);
                            newSources.add(sourceIri);
                        }
                    }
                }

                setValue(1, sourceId);
                setValue(2, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
            }
        }.load();

        stream.close();
    }


    static private void loadMatches(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into substance_matches(substance, match) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://www.w3.org/2004/02/skos/core#exactMatch"))
                    throw new IOException();

                String value = object.getURI();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));

                if(value.length() > 46 && value.charAt(46) == 'C')
                    setValue(2, getIntID(object, "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL"));
                else
                    setValue(2, -getIntID(object, "http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL"));
            }
        }.load();

        stream.close();
    }


    static private void loadTypes(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into substance_types(substance, chebi) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
                setValue(2, getIntID(object, "http://purl.obolibrary.org/obo/CHEBI_"));
            }
        }.load();

        stream.close();
    }


    static private void loadPdbLinks(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into substance_pdblinks(substance, pdblink) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://rdf.wwpdb.org/schema/pdbx-v40.owl#link_to_pdb"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
                setValue(2, getStringID(object, "http://rdf.wwpdb.org/pdb/"));
            }
        }.load();

        stream.close();
    }


    static private void loadReferences(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into substance_references(substance, reference) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/spar/cito/isDiscussedBy"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
                setValue(2, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
            }
        }.load();

        stream.close();
    }


    static private void loadSynonyms(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into substance_synonyms(substance, synonym) values (?,(select id from synonym_bases where md5=?))")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-attribute"))
                    throw new IOException();

                if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID"))
                    return;

                // workaround
                if(subject.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"))
                    setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
                else
                    setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/"));

                setValue(2, getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_"));
            }
        }.load();

        stream.close();
    }


    public static void loadDirectory(String path) throws IOException, SQLException
    {
        File dir = new File(getPubchemDirectory() + path);
        List<File> files = Arrays.asList(dir.listFiles());


        files.parallelStream().map(f -> f.getName()).filter(n -> n.startsWith("pc_substance2compound"))
                .forEach(name -> {
                    try
                    {
                        loadCompounds(path + File.separatorChar + name);
                    }
                    catch (IOException | SQLException e)
                    {
                        System.err.println("exception for " + name);
                        e.printStackTrace();
                        System.exit(1);
                    }
                });


        Map<String, Short> sources = Collections.synchronizedMap(getMapping("source_bases"));
        ArrayList<String> newSources = new ArrayList<String>();
        int newSourceOffset = sources.size();

        files.parallelStream().map(f -> f.getName()).forEach(name -> {
            try
            {
                if(name.startsWith("pc_substance_available"))
                    loadAvailabilities(path + File.separatorChar + name);
                else if(name.startsWith("pc_substance_match.ttl"))
                    loadMatches(path + File.separatorChar + name);
                else if(name.startsWith("pc_substance_modified"))
                    loadModifiedDates(path + File.separatorChar + name);
                else if(name.startsWith("pc_substance_source"))
                    loadSources(path + File.separatorChar + name, sources, newSources);
                else if(name.startsWith("pc_substance_type"))
                    loadTypes(path + File.separatorChar + name);
                else if(name.startsWith("pc_substance2pdb"))
                    loadPdbLinks(path + File.separatorChar + name);
                else if(name.startsWith("pc_substance2reference"))
                    loadReferences(path + File.separatorChar + name);
                else if(name.startsWith("pc_substance2descriptor"))
                    loadSynonyms(path + File.separatorChar + name);
                else if(name.startsWith("pc_substance2measuregroup"))
                    System.out.println("ignore " + path + File.separator + name);
                else if(!name.startsWith("pc_substance2compound"))
                    System.out.println("unsupported " + path + File.separator + name);
            }
            catch (IOException | SQLException e)
            {
                System.err.println("exception for " + name);
                e.printStackTrace();
                System.exit(1);
            }
        });


        try (Connection connection = getConnection())
        {
            try (PreparedStatement insertStatement = connection
                    .prepareStatement("insert into source_bases (id, iri) values (?,?)"))
            {
                for(int i = 0; i < newSources.size(); i++)
                {
                    short sourceID = (short) (newSourceOffset + i);
                    String sourceIri = newSources.get(i);

                    System.out.println("  add missing source: " + sourceIri);

                    insertStatement.setShort(1, sourceID);
                    insertStatement.setString(2, sourceIri);

                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }
        }
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        loadDirectory("RDF/substance");
    }
}
