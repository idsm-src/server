package cz.iocb.load.mona;



/**
 * A compound of a spectrum in the JSON export of MoNA.
 */
public class Compound
{
    /**
     * Whether the compound is computed.
     */
    boolean computed;

    /**
     * The InChI of the compound.
     */
    String inchi;

    /**
     * The InChIKey of the compound.
     */
    String inchiKey;

    /**
     * The kind of the compound, such as biological.
     */
    String kind;

    /**
     * The structure of the compound in the molfile format.
     */
    String molFile;

    /**
     * The tags of the compound.
     */
    Tag[] tags;

    /**
     * The classification items of the compound.
     */
    Classification[] classification;

    /**
     * The metadata items of the compound.
     */
    MetaData[] metaData;

    /**
     * The names of the compound.
     */
    Name[] names;


    /**
     * Creates an empty compound.
     */
    public Compound()
    {
    }
}
