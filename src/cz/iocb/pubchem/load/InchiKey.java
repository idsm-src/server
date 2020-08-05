package cz.iocb.pubchem.load;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import cz.iocb.pubchem.load.common.TripleStreamProcessor;
import cz.iocb.pubchem.load.common.Updater;



class InchiKey extends Updater
{
    private static StringIntMap usedKeys;
    private static int nextkeyID;


    private static void loadBases() throws IOException, SQLException
    {
        usedKeys = new StringIntMap(200000000);
        StringIntMap newKeys = new StringIntMap(200000000);
        StringIntMap oldKeys = getStringIntMap("select inchikey, id from inchikey_bases", 200000000);
        nextkeyID = getIntValue("select coalesce(max(id)+1,0) from inchikey_bases");

        processFiles("RDF/inchikey", "pc_inchikey_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
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

        batch("delete from inchikey_bases where id = ?", oldKeys.values());
        batch("insert into inchikey_bases(inchikey, id) values (?,?)", newKeys);
    }


    private static void loadCompounds() throws IOException, SQLException
    {
        IntIntHashMap newCompounds = new IntIntHashMap(200000000);
        IntIntHashMap oldCompounds = getIntIntMap("select compound, inchikey from inchikey_compounds", 200000000);

        processFiles("RDF/inchikey", "pc_inchikey2compound_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/is-attribute-of"))
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
                            System.out.println("    missing inchikey " + inchikey + " for sio:is-attribute-of");
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from inchikey_compounds where compound = ?", oldCompounds.keySet());
        batch("insert into inchikey_compounds(compound, inchikey) values (?,?) "
                + "on conflict (compound) do update set inchikey=EXCLUDED.inchikey", newCompounds);
    }


    private static void loadSubjects() throws IOException, SQLException
    {
        IntStringMap newSubjects = new IntStringMap(20000);
        IntStringMap oldSubjects = getIntStringMap("select inchikey, subject from inchikey_subjects", 20000);

        try(InputStream stream = getStream("RDF/inchikey/pc_inchikey_topic.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/dc/terms/subject"))
                        throw new IOException();

                    String inchikey = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/");
                    int inchikeyID = usedKeys.getIfAbsent(inchikey, NO_VALUE);

                    if(inchikeyID != NO_VALUE)
                    {
                        String subjectID = getStringID(object, "http://id.nlm.nih.gov/mesh/");

                        synchronized(newSubjects)
                        {
                            if(!subjectID.equals(oldSubjects.remove(inchikeyID)))
                                newSubjects.put(inchikeyID, subjectID);
                        }
                    }
                    else
                    {
                        System.out.println("    missing inchikey " + inchikey + " for dcterms:subject");
                    }
                }
            }.load(stream);
        }

        batch("delete from inchikey_subjects where inchikey = ?", oldSubjects.keySet());
        batch("insert into inchikey_subjects(inchikey, subject) values (?,?) "
                + "on conflict (inchikey) do update set subject=EXCLUDED.subject", newSubjects);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load inchikeys ...");

        loadBases();
        loadCompounds();
        loadSubjects();

        usedKeys = null;

        System.out.println();
    }
}
