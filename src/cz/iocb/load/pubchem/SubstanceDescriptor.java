package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class SubstanceDescriptor extends Updater
{
    private static void loadSubstanceVersions() throws IOException, SQLException
    {
        IntIntHashMap newValues = new IntIntHashMap();
        IntIntHashMap oldValues = getIntIntMap("select substance, version from pubchem.descriptor_substance_bases");

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

                        int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID",
                                "_Substance_Version");
                        int value = getInteger(object);

                        Substance.addSubstanceID(id);

                        synchronized(newValues)
                        {
                            if(value != oldValues.removeKeyIfAbsent(id, NO_VALUE))
                                newValues.put(id, value);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.descriptor_substance_bases where substance = ?", oldValues.keySet());
        batch("insert into pubchem.descriptor_substance_bases(substance, version) values (?,?) "
                + "on conflict (substance) do update set version=EXCLUDED.version", newValues);
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
}
