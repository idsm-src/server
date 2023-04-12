package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class InchiKey extends Updater
{
    private static StringIntMap usedKeys;
    private static int nextkeyID;


    private static void loadBases() throws IOException, SQLException
    {
        usedKeys = new StringIntMap();
        StringIntMap newKeys = new StringIntMap();
        StringIntMap oldKeys = getStringIntMap("select inchikey, id from pubchem.inchikey_bases");
        nextkeyID = getIntValue("select coalesce(max(id)+1,0) from pubchem.inchikey_bases");

        processFiles("pubchem/RDF/inchikey", "pc_inchikey_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
                            throw new IOException();

                        String inchikey = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/");

                        if(!inchikey.equals(getString(object)))
                            throw new IOException();

                        synchronized(newKeys)
                        {
                            int inchikeyID;

                            if((inchikeyID = oldKeys.removeKeyIfAbsent(inchikey, NO_VALUE)) == NO_VALUE)
                                newKeys.put(inchikey, inchikeyID = nextkeyID++);

                            usedKeys.put(inchikey, inchikeyID);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.inchikey_bases where id = ?", oldKeys.values());
        batch("insert into pubchem.inchikey_bases(inchikey, id) values (?,?)", newKeys);
    }


    private static void loadCompounds() throws IOException, SQLException
    {
        IntIntHashMap newCompounds = new IntIntHashMap();
        IntIntHashMap oldCompounds = getIntIntMap("select compound, inchikey from pubchem.inchikey_compounds");

        processFiles("pubchem/RDF/inchikey", "pc_inchikey2compound_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000011"))
                            throw new IOException();

                        String inchikey = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/");
                        int inchikeyID = usedKeys.getIfAbsent(inchikey, NO_VALUE);
                        int compoundID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                        Compound.addCompoundID(compoundID);

                        if(inchikeyID != NO_VALUE)
                        {
                            synchronized(newCompounds)
                            {
                                if(inchikeyID != oldCompounds.removeKeyIfAbsent(compoundID, NO_VALUE))
                                    newCompounds.put(compoundID, inchikeyID);
                            }
                        }
                        else
                        {
                            System.out.println("    missing inchikey " + inchikey + " for sio:SIO_000011");
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.inchikey_compounds where compound = ?", oldCompounds.keySet());
        batch("insert into pubchem.inchikey_compounds(compound, inchikey) values (?,?) "
                + "on conflict (compound) do update set inchikey=EXCLUDED.inchikey", newCompounds);
    }


    private static void loadSubjects() throws IOException, SQLException
    {
        IntStringMap newSubjects = new IntStringMap();
        IntStringMap oldSubjects = getIntStringMap("select inchikey, subject from pubchem.inchikey_subjects");

        try(InputStream stream = getTtlStream("pubchem/RDF/inchikey/pc_inchikey_topic.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/dc/terms/subject"))
                        throw new IOException();

                    // workaround
                    String inchikey = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/inichikey/");
                    int inchikeyID = usedKeys.getIfAbsent(inchikey, NO_VALUE);

                    if(inchikeyID != NO_VALUE)
                    {
                        String subjectID = getStringID(object, "http://id.nlm.nih.gov/mesh/");

                        if(!subjectID.equals(oldSubjects.remove(inchikeyID)))
                            newSubjects.put(inchikeyID, subjectID);
                    }
                    else
                    {
                        System.out.println("    missing inchikey " + inchikey + " for dcterms:subject");
                    }
                }
            }.load(stream);
        }

        batch("delete from pubchem.inchikey_subjects where inchikey = ?", oldSubjects.keySet());
        batch("insert into pubchem.inchikey_subjects(inchikey, subject) values (?,?) "
                + "on conflict (inchikey) do update set subject=EXCLUDED.subject", newSubjects);
    }


    private static void checkTypes() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/inchikey", "pc_inchikey_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/");

                        if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            throw new IOException();

                        if(!object.getURI().equals("http://semanticscience.org/resource/CHEMINF_000399"))
                            throw new IOException();
                    }
                }.load(stream);
            }
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load inchikeys ...");

        loadBases();
        loadCompounds();
        loadSubjects();
        checkTypes();

        usedKeys = null;

        System.out.println();
    }
}
