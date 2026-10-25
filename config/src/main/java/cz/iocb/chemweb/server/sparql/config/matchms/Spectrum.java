package cz.iocb.chemweb.server.sparql.config.matchms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Mass spectrum parsed from the lexical form of an {@code ms:spectrum} literal. The lexical forms are those that the
 * type {@code pgms.spectrum} reads, with the numbers restricted to unsigned decimals, and the canonical form is the one
 * that the type prints.
 */
public class Spectrum
{
    /**
     * Pattern of a number, an unsigned decimal with an optional exponent.
     */
    private static final Pattern numberPattern = Pattern
            .compile("(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?");

    /**
     * The m/z values and intensities of the peaks, interleaved, sorted by the m/z values.
     */
    final private float[] value;

    /**
     * Creates the spectrum.
     *
     * @param value the m/z values and intensities of the peaks, interleaved, sorted by the m/z values
     */
    Spectrum(float[] value)
    {
        this.value = value;
    }

    /**
     * Parses a spectrum given as a list of {@code [mz,intensity]} pairs in brackets, of {@code {mz,intensity}} pairs in
     * braces, or of {@code mz:intensity} pairs separated by whitespace, a comma or a semicolon. Whitespace may surround
     * the numbers and the punctuation and precede a list, but only a list of the last kind may end with it. The peaks
     * get sorted by the m/z values, as {@code pgms.spectrum} keeps them.
     *
     * @param literal the lexical form
     * @return the spectrum, or null if the lexical form is invalid
     */
    public static Spectrum valueOf(String literal)
    {
        ParsePosition position = new ParsePosition(0);
        List<float[]> peaks = new ArrayList<>();

        skipSpaces(literal, position);

        if(skip(literal, position, '[') || skip(literal, position, '{'))
        {
            char begin = literal.charAt(position.getIndex() - 1);
            char end = begin == '[' ? ']' : '}';

            if(!skip(literal, position, end))
            {
                do
                {
                    if(!skip(literal, position, begin))
                        return null;

                    float mz = readNumber(literal, position);

                    if(Float.isNaN(mz) || !skip(literal, position, ','))
                        return null;

                    float intensity = readNumber(literal, position);

                    if(Float.isNaN(intensity) || !skip(literal, position, end))
                        return null;

                    peaks.add(new float[] { mz, intensity });
                }
                while(skip(literal, position, ','));

                if(!skip(literal, position, end))
                    return null;
            }
        }
        else if(position.getIndex() < literal.length())
        {
            boolean separated;

            do
            {
                float mz = readNumber(literal, position);

                if(Float.isNaN(mz) || !skip(literal, position, ':'))
                    return null;

                float intensity = readNumber(literal, position);

                if(Float.isNaN(intensity))
                    return null;

                peaks.add(new float[] { mz, intensity });
                separated = skipSpaces(literal, position);
            }
            while(position.getIndex() < literal.length()
                    && (skip(literal, position, ',') || skip(literal, position, ';') || separated));
        }

        if(position.getIndex() < literal.length())
            return null;

        peaks.sort(Comparator.comparingDouble(peak -> peak[0]));

        float[] value = new float[2 * peaks.size()];

        for(int i = 0; i < peaks.size(); i++)
        {
            value[2 * i] = peaks.get(i)[0];
            value[2 * i + 1] = peaks.get(i)[1];
        }

        return new Spectrum(value);
    }

    /**
     * Skips the whitespace, the characters for which the C function {@code isspace} holds.
     *
     * @param text the lexical form
     * @param position the position in the lexical form, moved behind the whitespace
     * @return true if some whitespace was skipped
     */
    private static boolean skipSpaces(String text, ParsePosition position)
    {
        int start = position.getIndex();
        int index = start;

        while(index < text.length()
                && (text.charAt(index) == ' ' || text.charAt(index) >= '\t' && text.charAt(index) <= '\r'))
            index++;

        position.setIndex(index);
        return index > start;
    }

    /**
     * Skips the whitespace and the given character following it.
     *
     * @param text the lexical form
     * @param position the position in the lexical form, moved behind the character if it follows
     * @param character the character
     * @return true if the character follows
     */
    private static boolean skip(String text, ParsePosition position, char character)
    {
        skipSpaces(text, position);

        if(position.getIndex() == text.length() || text.charAt(position.getIndex()) != character)
            return false;

        position.setIndex(position.getIndex() + 1);
        return true;
    }

    /**
     * Reads the number following the whitespace.
     *
     * @param text the lexical form
     * @param position the position in the lexical form, moved behind the number
     * @return the value of the number, or NaN if no valid number follows
     */
    private static float readNumber(String text, ParsePosition position)
    {
        skipSpaces(text, position);

        Matcher matcher = numberPattern.matcher(text).region(position.getIndex(), text.length());

        if(!matcher.lookingAt())
            return Float.NaN;

        position.setIndex(matcher.end());
        return parse(matcher.group());
    }

    /**
     * Converts a number to a float. A number that overflows the floats, or that is not zero and underflows their normal
     * range, is invalid, since {@code strtof} reports it and {@code pgms.spectrum} rejects it then.
     *
     * @param number the unsigned decimal
     * @return the value, or NaN if the number is invalid
     */
    private static float parse(String number)
    {
        float value = Float.parseFloat(number);

        if(Float.isInfinite(value) || value < Float.MIN_NORMAL && !number.replaceFirst("[eE].*", "").matches("[0.]*"))
            return Float.NaN;

        return value;
    }

    /**
     * Formats a number as {@code pgms.spectrum} prints it, by the algorithm Ryu as PostgreSQL implements it: the
     * shortest decimal strictly inside the interval of the decimals that round to the number, the closest one to it if
     * there are more, written in the fixed notation for the decimal exponents from -4 to 5 and in the exponential one,
     * such as {@code 1.5e+07}, otherwise.
     *
     * @param number the number, not negative
     * @return the formatted number
     */
    private static String format(float number)
    {
        if(number == 0)
            return "0";

        int bits = Float.floatToIntBits(number);
        int ieeeExponent = bits >>> 23 & 0xff;
        int ieeeMantissa = bits & 0x7fffff;
        long m2 = ieeeExponent == 0 ? ieeeMantissa : ieeeMantissa | 0x800000;
        int e2 = Math.max(ieeeExponent, 1) - 152;

        // the number and the bounds of its interval as integers multiplied by 10^e10, the upper bound excluded
        BigInteger scale = e2 < 0 ? BigInteger.valueOf(5).pow(-e2) : BigInteger.TWO.pow(e2);
        BigInteger vr = BigInteger.valueOf(4 * m2).multiply(scale);
        BigInteger vp = BigInteger.valueOf(4 * m2 + 2).multiply(scale).subtract(BigInteger.ONE);
        BigInteger vm = BigInteger.valueOf(4 * m2 - (ieeeMantissa != 0 || ieeeExponent <= 1 ? 2 : 1)).multiply(scale);
        int e10 = Math.min(e2, 0);

        boolean vrIsTrailingZeros = true;
        int lastRemovedDigit = 0;

        while(vp.divide(BigInteger.TEN).compareTo(vm.divide(BigInteger.TEN)) > 0)
        {
            vrIsTrailingZeros &= lastRemovedDigit == 0;
            lastRemovedDigit = vr.mod(BigInteger.TEN).intValue();
            vr = vr.divide(BigInteger.TEN);
            vp = vp.divide(BigInteger.TEN);
            vm = vm.divide(BigInteger.TEN);
            e10++;
        }

        if(vrIsTrailingZeros && lastRemovedDigit == 5 && !vr.testBit(0))
            lastRemovedDigit = 4;

        BigInteger output = vr.equals(vm) || lastRemovedDigit >= 5 ? vr.add(BigInteger.ONE) : vr;
        String digits = output.toString();
        int exponent = e10 + digits.length() - 1;

        if(exponent >= -4 && exponent < 6)
            return new BigDecimal(output, -e10).toPlainString();

        StringBuilder builder = new StringBuilder();

        builder.append(digits.charAt(0));

        if(digits.length() > 1)
            builder.append('.').append(digits, 1, digits.length());

        builder.append(exponent < 0 ? "e-" : "e+");

        if(Math.abs(exponent) < 10)
            builder.append('0');

        builder.append(Math.abs(exponent));

        return builder.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < value.length / 2; i++)
        {
            if(i > 0)
                builder.append(" ");

            builder.append(format(value[2 * i + 0]));
            builder.append(":");
            builder.append(format(value[2 * i + 1]));
        }

        return builder.toString();
    }
}
