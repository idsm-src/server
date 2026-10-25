package cz.iocb.load.mona;



/**
 * A spectrum in the JSON export of MoNA, as Gson reads it.
 */
public class Spectrum
{
    /**
     * The accession of the spectrum.
     */
    String id;

    /**
     * The time of the creation of the spectrum in milliseconds since the epoch.
     */
    Long dateCreated;

    /**
     * The time of the last curation of the spectrum in milliseconds since the epoch.
     */
    Long lastCurated;

    /**
     * The time of the last update of the spectrum in milliseconds since the epoch.
     */
    Long lastUpdated;

    /**
     * The peaks of the spectrum, space-separated pairs of the m/z and the intensity.
     */
    String spectrum;

    /**
     * The quality score of the spectrum.
     */
    Score score;

    /**
     * The SPLASH of the spectrum.
     */
    SPLASH splash;

    /**
     * The library of the spectrum.
     */
    Library library;

    /**
     * The submitter of the spectrum.
     */
    Submitter submitter;

    /**
     * The annotations of the peaks of the spectrum.
     */
    Annotation[] annotations;

    /**
     * The compounds of the spectrum.
     */
    Compound[] compound;

    /**
     * The metadata items of the spectrum.
     */
    MetaData[] metaData;

    /**
     * The tags of the spectrum.
     */
    Tag[] tags;


    /**
     * Creates an empty spectrum.
     */
    public Spectrum()
    {
    }
}
