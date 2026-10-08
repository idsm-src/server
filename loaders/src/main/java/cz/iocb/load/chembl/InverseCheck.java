package cz.iocb.load.chembl;

import java.util.Arrays;



/*
 * Checks that the triples of an inverse property are exactly the inverses of the triples of the property stored in
 * the database, because the mapping derives the inverse triples from the stored ones. Pairs are collected in the
 * orientation of the stored property.
 */
class InverseCheck
{
    private final String name;
    private long[] forward = new long[1024];
    private long[] inverse = new long[1024];
    private int forwardSize = 0;
    private int inverseSize = 0;


    InverseCheck(String name)
    {
        this.name = name;
    }


    void forward(int subject, int object)
    {
        if(forwardSize == forward.length)
            forward = Arrays.copyOf(forward, 2 * forward.length);

        forward[forwardSize++] = pack(subject, object);
    }


    /*
     * Adds an inverse triple 'object property subject' given in the orientation of the stored property.
     */
    void inverse(int subject, int object)
    {
        if(inverseSize == inverse.length)
            inverse = Arrays.copyOf(inverse, 2 * inverse.length);

        inverse[inverseSize++] = pack(subject, object);
    }


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
                ChEMBL.warning(name + " without its inverse triple", unpack(a[i++]));
            else if(i == a.length || b[j] < a[i])
                ChEMBL.warning("inverse triple of " + name + " without its triple", unpack(b[j++]));
            else
            {
                i++;
                j++;
            }
        }
    }


    private static long[] distinct(long[] array, int size)
    {
        Arrays.sort(array, 0, size);

        int count = 0;

        for(int i = 0; i < size; i++)
            if(i == 0 || array[i] != array[i - 1])
                array[count++] = array[i];

        return Arrays.copyOf(array, count);
    }


    private static long pack(int subject, int object)
    {
        return (long) subject << 32 | object & 0xffffffffL;
    }


    private static String unpack(long value)
    {
        return (int) (value >> 32) + " " + (int) value;
    }
}
