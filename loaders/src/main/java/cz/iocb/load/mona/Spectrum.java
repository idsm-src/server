package cz.iocb.load.mona;



public class Spectrum
{
    String id;
    Long dateCreated;
    Long lastCurated;
    Long lastUpdated;
    String spectrum;
    Score score;
    SPLASH splash;
    Library library;
    Submitter submitter;
    Annotation[] annotations;
    Compound[] compound;
    MetaData[] metaData;
    Tag[] tags;
}
