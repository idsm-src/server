package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class InchiKey extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/";
    static final int prefixLength = prefix.length();

    private static final StringIntMap keepKeys = new StringIntMap();
    private static final StringIntMap newKeys = new StringIntMap();
    private static int nextKeyID;


    private static void loadBases() throws IOException, SQLException
    {
        StringIntMap oldKeys = new StringIntMap();

        load("select inchikey,id from pubchem.inchikey_bases", oldKeys);
        nextKeyID = oldKeys.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

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

                        String inchikey = getStringID(subject, prefix);

                        if(!inchikey.equals(getString(object)))
                            throw new IOException();

                        synchronized(newKeys)
                        {
                            Integer inchikeyID = oldKeys.remove(inchikey);

                            if(inchikeyID != null)
                                keepKeys.put(inchikey, inchikeyID);
                            else if(!keepKeys.containsKey(inchikey))
                                newKeys.put(inchikey, nextKeyID++);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.inchikey_bases where inchikey=? and id=?", oldKeys);
        store("insert into pubchem.inchikey_bases(inchikey,id) values(?,?)", newKeys);
    }


    private static void loadCompounds() throws IOException, SQLException
    {
        IntIntMap keepCompounds = new IntIntMap();
        IntIntMap newCompounds = new IntIntMap();
        IntIntMap oldCompounds = new IntIntMap();

        load("select compound,inchikey from pubchem.inchikey_compounds", oldCompounds);

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

                        Integer inchikeyID = getKeyID(subject.getURI());
                        Integer compoundID = Compound.getCompoundID(object.getURI());

                        if(inchikeyID != null)
                        {
                            synchronized(newCompounds)
                            {
                                if(inchikeyID.equals(oldCompounds.remove(compoundID)))
                                {
                                    keepCompounds.put(compoundID, inchikeyID);
                                }
                                else
                                {
                                    Integer keep = keepCompounds.get(compoundID);

                                    if(inchikeyID.equals(keep))
                                        return;
                                    else if(keep != null)
                                        throw new IOException();

                                    Integer put = newCompounds.put(compoundID, inchikeyID);

                                    if(put != null && !inchikeyID.equals(put))
                                        throw new IOException();
                                }
                            }
                        }
                        else
                        {
                            System.out.println(
                                    "    missing inchikey " + getStringID(subject, prefix) + " for sio:SIO_000011");
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.inchikey_compounds where compound=? and inchikey=?", oldCompounds);
        store("insert into pubchem.inchikey_compounds(compound,inchikey) values(?,?) "
                + "on conflict(compound) do update set inchikey=EXCLUDED.inchikey", newCompounds);
    }


    private static void loadSubjects() throws IOException, SQLException
    {
        IntStringMap keepSubjects = new IntStringMap();
        IntStringMap newSubjects = new IntStringMap();
        IntStringMap oldSubjects = new IntStringMap();

        load("select inchikey,subject from pubchem.inchikey_subjects", oldSubjects);

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
                    Integer inchikeyID = getKeyID(subject.getURI());
                    String mesh = getStringID(object, "http://id.nlm.nih.gov/mesh/");

                    if(inchikeyID != null)
                    {
                        if(mesh.equals(oldSubjects.remove(inchikeyID)))
                        {
                            keepSubjects.put(inchikeyID, mesh);
                        }
                        else
                        {
                            String keep = keepSubjects.get(inchikeyID);

                            if(mesh.equals(keep))
                                return;
                            else if(keep != null)
                                throw new IOException();

                            String put = newSubjects.put(inchikeyID, mesh);

                            if(put != null && !mesh.equals(put))
                                throw new IOException();
                        }
                    }
                    else
                    {
                        System.out.println(
                                "    missing inchikey " + getStringID(subject, prefix) + " for dcterms:subject");
                    }
                }
            }.load(stream);
        }

        store("delete from pubchem.inchikey_subjects where inchikey=? and subject=?", oldSubjects);
        store("insert into pubchem.inchikey_subjects(inchikey,subject) values(?,?) "
                + "on conflict(inchikey) do update set subject=EXCLUDED.subject", newSubjects);
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
                        getStringID(subject, prefix);

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

        System.out.println();
    }


    static Integer getKeyID(String value) throws IOException
    {
        // workaround
        value = value.replaceFirst("/inichikey/", "/inchikey/");

        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        String inchikey = value.substring(prefixLength);

        synchronized(newKeys)
        {
            Integer inchikeyID = keepKeys.get(inchikey);

            if(inchikeyID != null)
                return inchikeyID;

            return newKeys.get(inchikey);
        }
    }
}
