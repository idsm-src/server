package cz.iocb.load.mona;

import java.util.HashSet;
import java.util.Set;



/**
 * A class of the ChemOnt ontology of ClassyFire with the ChEBI and MeSH classes it is related to.
 */
public class ClassyFire
{
    /**
     * The ChemOnt id of the class.
     */
    int id;

    /**
     * The ids of the related ChEBI classes.
     */
    Set<Integer> chebi = new HashSet<>();

    /**
     * The ids of the related MeSH descriptors.
     */
    Set<String> mesh = new HashSet<>();


    /**
     * Creates a class without related classes.
     *
     * @param id the ChemOnt id of the class
     */
    public ClassyFire(int id)
    {
        this.id = id;
    }
}
