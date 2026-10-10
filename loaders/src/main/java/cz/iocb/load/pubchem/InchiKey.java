package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.Problems;
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

        load("select inchikey,id from pubchem.inchikeys", oldKeys);
        nextKeyID = oldKeys.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        processFiles("pubchem/RDF/inchikey", "pc_inchikey_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000300"))
                            return;

                        String inchikey = getStringID(subject, prefix);

                        if(!inchikey.equals(getString(object)))
                        {
                            Problems.error("value of " + predicate.getURI() + " not matching the IRI",
                                    text(subject) + " " + text(object));
                            return;
                        }

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

        store("delete from pubchem.inchikeys where inchikey=? and id=?", oldKeys);
        store("insert into pubchem.inchikeys(inchikey,id) values(?,?)", newKeys);
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
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000011"))
                            return;

                        Integer inchikeyID = getKeyID(subject.getURI());
                        Integer compoundID = Compound.getCompoundID(object.getURI());

                        // workaround
                        if(compoundID == 24405717
                                && subject.getURI().substring(prefixLength).equals("AOKQBPHIDSLJFA-UHFFFAOYSA-N"))
                        {
                            Problems.warning("ignored conflicting InChIKey of a compound",
                                    text(subject) + " " + text(object));
                            return;
                        }

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
                                        throw new DataException(
                                                "multiple values of pubchem.inchikey_compounds.inchikey",
                                                compoundID + ": " + keep + ", " + inchikeyID);

                                    Integer put = newCompounds.put(compoundID, inchikeyID);

                                    if(put != null && !inchikeyID.equals(put))
                                        throw new DataException(
                                                "multiple values of pubchem.inchikey_compounds.inchikey",
                                                compoundID + ": " + put + ", " + inchikeyID);
                                }
                            }
                        }
                        else
                        {
                            Problems.warning("ignored triple of an unknown InChIKey in " + predicate.getURI(),
                                    text(subject) + " " + text(object));
                        }
                    }
                }.load(stream);
            }
        });

        checkCompoundKeys(keepCompounds, newCompounds);

        store("delete from pubchem.inchikey_compounds where compound=? and inchikey=?", oldCompounds);
        store("insert into pubchem.inchikey_compounds(compound,inchikey) values(?,?) "
                + "on conflict(compound) do update set inchikey=EXCLUDED.inchikey", newCompounds);
    }


    /*
     * Checks the InChIKeys that the files of the compounds state against the compounds of the InChIKeys.
     */
    private static void checkCompoundKeys(IntIntMap keepCompounds, IntIntMap newCompounds)
            throws IOException, SQLException
    {
        processFiles("pubchem/RDF/compound/general", "pc_compound2inchikey_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inchikey"))
                            return;

                        Integer compoundID = Compound.getCompoundID(subject.getURI());
                        Integer inchikeyID = getKeyID(prefix + getString(object));
                        Integer expected = keepCompounds.get(compoundID);

                        if(expected == null)
                            expected = newCompounds.get(compoundID);

                        if(expected == null)
                            throw new DataException("value without an InChIKey of the compound");

                        if(!expected.equals(inchikeyID))
                            throw new DataException("value different from the InChIKey of the compound");
                    }
                }.load(stream);
            }
        });
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
                    if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/subject"))
                        return;

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
                                throw new DataException("multiple values of pubchem.inchikey_subjects.subject",
                                        inchikeyID + ": " + keep + ", " + mesh);

                            String put = newSubjects.put(inchikeyID, mesh);

                            if(put != null && !mesh.equals(put))
                                throw new DataException("multiple values of pubchem.inchikey_subjects.subject",
                                        inchikeyID + ": " + put + ", " + mesh);
                        }
                    }
                    else
                    {
                        Problems.warning("ignored triple of an unknown InChIKey in " + predicate.getURI(),
                                text(subject) + " " + text(object));
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

                        if(!checkPredicate(subject, predicate, object,
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            return;

                        checkType(subject, object, "http://semanticscience.org/resource/CHEMINF_000399",
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#InChIKey");
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
        if(value.contains("/inichikey/"))
        {
            Problems.warning("repaired IRI", value);
            value = value.replaceFirst("/inichikey/", "/inchikey/");
        }

        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

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
