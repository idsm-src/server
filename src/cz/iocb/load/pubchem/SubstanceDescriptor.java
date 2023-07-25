package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class SubstanceDescriptor extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID";
    static final int prefixLength = prefix.length();


    private static void loadSubstanceVersions() throws IOException, SQLException
    {
        IntIntMap keepValues = new IntIntMap();
        IntIntMap newValues = new IntIntMap();
        IntIntMap oldValues = new IntIntMap();

        load("select substance,version from pubchem.descriptor_substance_bases", oldValues);

        processFiles("pubchem/RDF/descriptor/substance", "pc_descr_SubstanceVersion_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
                            throw new IOException();

                        Integer id = getDescriptorID(subject.getURI(), "_Substance_Version");
                        Integer value = getInteger(object);

                        synchronized(newValues)
                        {
                            if(value.equals(oldValues.remove(id)))
                            {
                                keepValues.put(id, value);
                            }
                            else
                            {
                                Integer keep = keepValues.get(id);


                                if(value.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Integer put = newValues.put(id, value);

                                if(put != null && !value.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.descriptor_substance_bases where substance=? and version=?", oldValues);
        store("insert into pubchem.descriptor_substance_bases(substance,version) values(?,?) "
                + "on conflict(substance) do update set version=EXCLUDED.version", newValues);
    }


    private static void checkSubstanceVersionTypes() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/descriptor/substance", "pc_descr_SubstanceVersion_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID", "_Substance_Version");

                        if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            throw new IOException();

                        if(!object.getURI().equals("http://purl.obolibrary.org/obo/IAO_0000129"))
                            throw new IOException();
                    }
                }.load(stream);
            }
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load substance descriptors ...");

        loadSubstanceVersions();
        checkSubstanceVersionTypes();

        System.out.println();
    }


    private static Integer getDescriptorID(String value, String suffix) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        if(!value.endsWith(suffix))
            throw new IOException("unexpected IRI: " + value);

        Integer id = Integer.parseInt(value.substring(prefixLength, value.length() - suffix.length()));

        Substance.addSubstanceID(id);

        return id;
    }
}
