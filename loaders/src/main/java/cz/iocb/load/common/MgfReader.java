package cz.iocb.load.common;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;



/**
 * Reads the records of a Mascot generic format (MGF) file. A record is enclosed in the lines BEGIN IONS and END IONS,
 * it starts with its parameters (KEY=value lines) and continues with its peaks (an m/z value and an intensity separated
 * by white space); the parameters before the first record apply to every record. Blank lines and comments (lines
 * starting with #, ;, ! or /) are skipped. The peaks of a record are returned in the text form of a pgms spectrum, i.e.
 * as m/z:intensity pairs separated by spaces, with the numbers converted to single precision as the extension stores
 * them; a number the extension would refuse (out of the range of single precision) is refused here. A malformed record,
 * i.e. one with a malformed peak or a repeated parameter or one without its END IONS, and a line outside the records
 * that is not a global parameter are reported as errors and skipped, so that the reading goes on.
 */
public class MgfReader implements Closeable
{
    /**
     * A record of the file: the line of its BEGIN IONS, its parameters, its peaks as a spectrum literal and their
     * number.
     *
     * @param line the number of the line of its BEGIN IONS
     * @param parameters the parameters of the record, including the global ones it does not override
     * @param spectrum the peaks in the text form of a pgms spectrum
     * @param peaks the number of the peaks
     */
    public static record Record(int line, Map<String, String> parameters, String spectrum, int peaks)
    {
    }


    /**
     * Syntax of a number of a peak.
     */
    private static final Pattern numberPattern = Pattern
            .compile("[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][+-]?[0-9]+)?");

    /**
     * Name of the file, used in the reported problems.
     */
    private final String name;

    /**
     * Reader of the file.
     */
    private final BufferedReader reader;

    /**
     * Global parameters, given before the first record.
     */
    private final HashMap<String, String> global = new HashMap<>();

    /**
     * Number of the last read line.
     */
    private int lineNumber;

    /**
     * Whether the first record has been found, after which no global parameter may follow.
     */
    private boolean started;

    /**
     * Whether the BEGIN IONS of the next record has already been read by the previous, unterminated record.
     */
    private boolean pending;


    /**
     * Creates the reader of a file.
     *
     * @param name the name of the file, used in the reported problems
     * @param reader the reader of the file
     */
    public MgfReader(String name, BufferedReader reader)
    {
        this.name = name;
        this.reader = reader;
    }


    /**
     * Reads the next line and counts it.
     *
     * @return the line, or null at the end of the file
     * @throws IOException if an I/O error occurs
     */
    private String readLine() throws IOException
    {
        String line = reader.readLine();

        if(line != null)
            lineNumber++;

        return line;
    }


    /**
     * Tests whether a non-empty line is a comment.
     *
     * @param line the line
     * @return true if the line starts with one of the comment characters
     */
    private static boolean isComment(String line)
    {
        char c = line.charAt(0);
        return c == '#' || c == ';' || c == '!' || c == '/';
    }


    /**
     * Converts the text of a number to single precision; the values that overflow or underflow it are refused as the
     * extension refuses them, null stands for a refused number.
     *
     * @param text the text of the number
     * @return the number, or null if it is refused
     */
    private static Float number(String text)
    {
        if(!numberPattern.matcher(text).matches())
            return null;

        float value = Float.parseFloat(text);
        boolean zero = !text.split("[eE]", 2)[0].matches(".*[1-9].*");

        if(Float.isInfinite(value) || value == 0 && !zero || value != 0 && Math.abs(value) < Float.MIN_NORMAL)
            return null;

        return value;
    }


    /**
     * Returns the next record, or null at the end of the file.
     *
     * @return the next well-formed record, or null at the end of the file
     * @throws IOException if an I/O error occurs
     */
    public Record next() throws IOException
    {
        while(true)
        {
            if(!pending && !findRecord())
                return null;

            pending = false;
            started = true;

            Record record = readRecord();

            if(record != null)
                return record;
        }
    }


    /**
     * Reads the lines up to the next BEGIN IONS; returns false at the end of the file.
     *
     * @return true if a record has been found, false at the end of the file
     * @throws IOException if an I/O error occurs
     */
    private boolean findRecord() throws IOException
    {
        String line;

        while((line = readLine()) != null)
        {
            if(line.isBlank() || isComment(line))
                continue;

            if(line.equals("BEGIN IONS"))
                return true;

            int separator = started ? -1 : line.indexOf('=');

            if(separator < 0)
                Problems.error("unexpected line of an MGF file", name + ":" + lineNumber);
            else
                global.put(line.substring(0, separator), line.substring(separator + 1));
        }

        return false;
    }


    /**
     * Reads the record following its BEGIN IONS; returns null for a malformed record, which is reported.
     *
     * @return the record, or null if it is malformed
     * @throws IOException if an I/O error occurs
     */
    private Record readRecord() throws IOException
    {
        int begin = lineNumber;
        HashMap<String, String> parameters = new HashMap<>(global);
        HashSet<String> own = new HashSet<>();
        StringBuilder spectrum = new StringBuilder();
        boolean malformed = false;
        int peaks = 0;

        while(true)
        {
            String line = readLine();

            if(line == null || line.equals("BEGIN IONS"))
            {
                Problems.error("unterminated MGF record", name + ":" + begin);
                pending = line != null;
                return null;
            }

            if(line.isBlank() || isComment(line))
                continue;

            if(line.equals("END IONS"))
                break;

            if(malformed)
                continue;

            int separator = peaks == 0 ? line.indexOf('=') : -1;

            if(separator >= 0)
            {
                String parameter = line.substring(0, separator);

                if(!own.add(parameter))
                {
                    Problems.error("repeated parameter " + parameter + " of an MGF record", name + ":" + lineNumber);
                    malformed = true;
                    continue;
                }

                parameters.put(parameter, line.substring(separator + 1));
            }
            else
            {
                String[] values = line.trim().split("\\s+");
                Float mz = values.length == 2 ? number(values[0]) : null;
                Float intensity = values.length == 2 ? number(values[1]) : null;

                if(mz == null || intensity == null)
                {
                    Problems.error("malformed peak of an MGF record", name + ":" + lineNumber);
                    malformed = true;
                    continue;
                }

                if(peaks++ > 0)
                    spectrum.append(' ');

                spectrum.append(mz).append(':').append(intensity);
            }
        }

        return malformed ? null : new Record(begin, parameters, spectrum.toString(), peaks);
    }


    @Override
    public void close() throws IOException
    {
        reader.close();
    }
}
