package cz.iocb.load.pubchem;

import java.io.IOException;
import cz.iocb.load.common.Updater.IntSet;



public class Anatomy
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/anatomy/ANATOMYID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepAnatomies = new IntSet();
    private static final IntSet newAnatomies = new IntSet();
    private static final IntSet oldAnatomies = new IntSet();


    static Integer getAnatomyID(String value) throws IOException
    {
        return getAnatomyID(value, false);
    }


    static Integer getAnatomyID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer anatomyID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newAnatomies)
        {
            if(newAnatomies.contains(anatomyID))
            {
                if(forceKeep)
                {
                    newAnatomies.remove(anatomyID);
                    keepAnatomies.add(anatomyID);
                }
            }
            else if(!keepAnatomies.contains(anatomyID))
            {
                System.out.println("    add missing anatomy ANATOMYID" + anatomyID);

                if(!oldAnatomies.remove(anatomyID) && !forceKeep)
                    newAnatomies.add(anatomyID);
                else
                    keepAnatomies.add(anatomyID);
            }
        }

        return anatomyID;
    }
}
