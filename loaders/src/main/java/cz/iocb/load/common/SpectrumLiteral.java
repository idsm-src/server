package cz.iocb.load.common;

import static cz.iocb.load.common.EntityTable.typed;
import java.util.Arrays;
import java.util.List;
import cz.iocb.load.common.EntityTable.Column;



/**
 * The text form of a pgms spectrum: m/z:intensity pairs separated by spaces. Two literals denote the same spectrum when
 * they hold the same peaks regardless of their order and of the format of the numbers, which is how a spectrum column
 * of an entity table compares its values with the stored ones.
 */
public final class SpectrumLiteral
{
    /**
     * Peak of a spectrum, ordered by its m/z and then by its intensity.
     *
     * @param mz the m/z of the peak
     * @param intensity the intensity of the peak
     */
    private static record Peak(float mz, float intensity) implements Comparable<Peak>
    {
        /**
         * Parses the text form of a peak.
         *
         * @param text the text form, {@code mz:intensity}
         * @return the peak
         */
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


    /**
     * Not instantiable.
     */
    private SpectrumLiteral()
    {
    }


    /**
     * Creates a column of an entity table that holds spectra.
     *
     * @param name the name of the column
     * @return the column
     */
    public static Column column(String name)
    {
        return typed(name, "pgms.spectrum").unique().comparedBy((a, b) -> same((String) a, (String) b));
    }


    /**
     * Tests whether two literals denote the same spectrum.
     *
     * @param a the first literal, or null
     * @param b the second literal, or null
     * @return true if both literals hold the same peaks, or both are null
     */
    public static boolean same(String a, String b)
    {
        if(a == null || b == null)
            return a == b;

        return peaks(a).equals(peaks(b));
    }


    /**
     * Returns the peaks of a spectrum literal in their order.
     *
     * @param literal the spectrum literal
     * @return the sorted peaks
     */
    private static List<Peak> peaks(String literal)
    {
        String text = literal.trim();

        if(text.isEmpty())
            return List.of();

        return Arrays.stream(text.split("\\s+")).map(Peak::parse).sorted().toList();
    }
}
