package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem substance descriptors ({@code pubchem/RDF/descriptor/substance}): the versions of the
 * substances, stored in the table {@code pubchem.substance_versions}.
 */
class SubstanceDescriptor extends Updater
{
    /**
     * IRI prefix of the substance descriptors.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();


    /**
     * Not instantiable.
     */
    private SubstanceDescriptor()
    {
    }


    /**
     * Loads the versions of the substances from the version descriptors and from the substance files, which must agree,
     * and stores their changes.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadSubstanceVersions() throws IOException, SQLException
    {
        IntIntMap keepValues = new IntIntMap();
        IntIntMap newValues = new IntIntMap();
        IntIntMap oldValues = new IntIntMap();

        load("select substance,version from pubchem.substance_versions", oldValues);

        processFiles("pubchem/RDF/descriptor/substance", "pc_descr_SubstanceVersion_value_[0-9]+\\.ttl\\.gz", file -> {
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

                        Integer id = getDescriptorID(subject.getURI(), "_Substance_Version");
                        Integer value = getIntFromInteger(object);

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
                                    throw new DataException("multiple values of pubchem.substance_versions.version",
                                            id + ": " + keep + ", " + value);

                                Integer put = newValues.put(id, value);

                                if(put != null && !value.equals(put))
                                    throw new DataException("multiple values of pubchem.substance_versions.version",
                                            id + ": " + put + ", " + value);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        processFiles("pubchem/RDF/substance", "pc_substance_version_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#substance_version"))
                            return;

                        Integer id = Substance.getSubstanceID(subject.getURI());
                        Integer value = getIntFromInteger(object);

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
                                    throw new DataException("multiple values of pubchem.substance_versions.version",
                                            id + ": " + keep + ", " + value);

                                Integer put = newValues.put(id, value);

                                if(put != null && !value.equals(put))
                                    throw new DataException("multiple values of pubchem.substance_versions.version",
                                            id + ": " + put + ", " + value);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.substance_versions where substance=? and version=?", oldValues);
        store("insert into pubchem.substance_versions(substance,version) values(?,?) "
                + "on conflict(substance) do update set version=EXCLUDED.version", newValues);
    }


    /**
     * Checks the types of the version descriptors of the substances.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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

                        if(!checkPredicate(subject, predicate, object,
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            return;

                        checkType(subject, object, "http://purl.obolibrary.org/obo/IAO_0000129",
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#SubstanceVersion");
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the substance descriptors from the PubChemRDF files.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load substance descriptors ...");

        loadSubstanceVersions();
        checkSubstanceVersionTypes();

        System.out.println();
    }


    /**
     * Returns the id of the substance that the descriptor with the given IRI describes; a substance not described by
     * the data gets a row and is counted as missing.
     *
     * @param value the IRI of the descriptor
     * @param suffix the suffix of the IRI naming the kind of the descriptor
     * @return the id of the substance
     * @throws IOException if the IRI is not an IRI of a descriptor of the kind
     */
    private static Integer getDescriptorID(String value, String suffix) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        if(!value.endsWith(suffix))
            throw new DataException("unexpected IRI", value);

        Integer id = Integer.parseInt(value.substring(prefixLength, value.length() - suffix.length()));

        Substance.addSubstanceID(id);

        return id;
    }
}
