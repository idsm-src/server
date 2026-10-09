package cz.iocb.load.common;

import static cz.iocb.load.common.EntityTable.typed;
import java.util.Arrays;
import java.util.List;
import cz.iocb.load.common.EntityTable.Column;



/*
 * The text form of a pgms spectrum: m/z:intensity pairs separated by spaces. Two literals denote the same spectrum
 * when they hold the same peaks regardless of their order and of the format of the numbers, which is how a spectrum
 * column of an entity table compares its values with the stored ones.
 */
public final class SpectrumLiteral
{
    private static record Peak(float mz, float intensity) implements Comparable<Peak>
    {
        static Peak parse(String text)
        {
            int separator = text.indexOf(':');

            if(separator < 0)
                throw new IllegalArgumentException("malformed spectrum literal: " + text);

            return new Peak(Float.parseFloat(text.substring(0, separator)),
                    Float.parseFloat(text.substring(separator + 1)));
        }


        @Override
        public int compareTo(Peak other)
        {
            int result = Float.compare(mz, other.mz);
            return result != 0 ? result : Float.compare(intensity, other.intensity);
        }
    }


    private SpectrumLiteral()
    {
    }


    /*
     * Creates a column of an entity table that holds spectra.
     */
    public static Column column(String name)
    {
        return typed(name, "pgms.spectrum").unique().comparedBy((a, b) -> same((String) a, (String) b));
    }


    /*
     * Tests whether two literals denote the same spectrum.
     */
    public static boolean same(String a, String b)
    {
        if(a == null || b == null)
            return a == b;

        return peaks(a).equals(peaks(b));
    }


    private static List<Peak> peaks(String literal)
    {
        String text = literal.trim();

        if(text.isEmpty())
            return List.of();

        return Arrays.stream(text.split("\\s+")).map(Peak::parse).sorted().toList();
    }
}
