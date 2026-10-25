package cz.iocb.load.chembl;

import java.util.Arrays;
import cz.iocb.load.common.Problems;



/**
 * Checks that the triples of an inverse property are exactly the inverses of the triples of the property stored in
 * the database, because the mapping derives the inverse triples from the stored ones. Pairs are collected in the
 * orientation of the stored property.
 */
class InverseCheck
{
    /**
     * The name of the property, used in the reported errors.
     */
    private final String name;

    /**
     * The packed pairs of the triples of the property.
     */
    private long[] forward = new long[1024];

    /**
     * The packed pairs of the triples of the inverse property, in the orientation of the property.
     */
    private long[] inverse = new long[1024];

    /**
     * The number of the pairs in {@link #forward}.
     */
    private int forwardSize = 0;

    /**
     * The number of the pairs in {@link #inverse}.
     */
    private int inverseSize = 0;


    /**
     * Creates the check.
     *
     * @param name the name of the property, used in the reported errors
     */
    InverseCheck(String name)
    {
        this.name = name;
    }


    /**
     * Adds a triple 'subject property object' of the property.
     *
     * @param subject the id of the subject
     * @param object the id of the object
     */
    void forward(int subject, int object)
    {
        if(forwardSize == forward.length)
            forward = Arrays.copyOf(forward, 2 * forward.length);

        forward[forwardSize++] = pack(subject, object);
    }


    /**
     * Adds an inverse triple 'object property subject' given in the orientation of the stored property.
     *
     * @param subject the id of the subject in the orientation of the stored property
     * @param object the id of the object in the orientation of the stored property
     */
    void inverse(int subject, int object)
    {
        if(inverseSize == inverse.length)
            inverse = Arrays.copyOf(inverse, 2 * inverse.length);

        inverse[inverseSize++] = pack(subject, object);
    }


    /**
     * Reports an error for every triple without its inverse and for every inverse triple without its triple, and
     * releases the collected pairs.
     */
    void check()
    {
        long[] a = distinct(forward, forwardSize);
        long[] b = distinct(inverse, inverseSize);

        forward = null;
        inverse = null;

        int i = 0;
        int j = 0;

        while(i < a.length || j < b.length)
        {
            if(j == b.length || i < a.length && a[i] < b[j])
                Problems.error(name + " without its inverse triple", unpack(a[i++]));
            else if(i == a.length || b[j] < a[i])
                Problems.error("inverse triple of " + name + " without its triple", unpack(b[j++]));
            else
            {
                i++;
                j++;
            }
        }
    }


    /**
     * Sorts the first elements of an array and returns them without duplicates.
     *
     * @param array the array, sorted in place
     * @param size the number of the elements of the array in use
     * @return the sorted distinct elements
     */
    private static long[] distinct(long[] array, int size)
    {
        Arrays.sort(array, 0, size);

        int count = 0;

        for(int i = 0; i < size; i++)
            if(i == 0 || array[i] != array[i - 1])
                array[count++] = array[i];

        return Arrays.copyOf(array, count);
    }


    /**
     * Packs a pair of ids into one value whose order is the order of the pairs.
     *
     * @param subject the id of the subject
     * @param object the id of the object
     * @return the packed pair
     */
    private static long pack(int subject, int object)
    {
        return (long) subject << 32 | object & 0xffffffffL;
    }


    /**
     * Returns the text form of a packed pair, used in the reported errors.
     *
     * @param value the packed pair
     * @return the ids of the subject and the object separated by a space
     */
    private static String unpack(long value)
    {
        return (int) (value >> 32) + " " + (int) value;
    }
}
