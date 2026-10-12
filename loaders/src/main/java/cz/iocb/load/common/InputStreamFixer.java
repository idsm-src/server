package cz.iocb.load.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;



/*
 * Repairs the errors of Turtle files that the parser would reject: some characters not allowed in an IRI (and every #
 * after the first one) are percent-encoded, an escaped space in an IRI becomes %20 and an escaped no-break space loses
 * its backslash, and some characters outside of strings, IRIs and comments are replaced by a space or a hyphen. The
 * strings (short and long ones, in double or single quotes), the comments, the escapes of local names and the RDF 1.2
 * << are followed as Turtle defines them, so that their content is never taken for IRIs or strings. The underlying
 * stream is read by blocks, the repaired stream can be read by single bytes as well as by blocks.
 */
public class InputStreamFixer extends InputStream
{
    // ANGLE follows a < that starts an IRI or, followed by another one, a <<; QUOTE and QUOTES follow the one or two
    // quotes that start a string, an empty string or a long string
    private static enum State
    {
        OUTSIDE, ANGLE, IRI, COMMENT, QUOTE, QUOTES, STRING, LONG_STRING
    }

    private static final byte[] forbidden = { ' ', '"', '<', '>', '{', '}', '|', '^', '`', '[', ']' };

    // the bytes that read() may change or that may change its state: in an IRI the end, an escape and the characters
    // that are percent-encoded, outside of strings, IRIs and comments the starts of strings, IRIs, comments and
    // escapes and the leading bytes of the replaced characters
    private static final boolean[] iriSpecial = new boolean[256];
    private static final boolean[] outsideSpecial = new boolean[256];

    static
    {
        for(byte c : forbidden)
            iriSpecial[c] = true;

        iriSpecial['>'] = true;
        iriSpecial['\\'] = true;
        iriSpecial['#'] = true;

        for(int c : new int[] { '<', '"', '\'', '#', '\\', 0xC2, 0xE2 })
            outsideSpecial[c] = true;
    }

    private final InputStream in;
    private final byte[] input = new byte[65536];
    private int position = 0;
    private int limit = 0;

    private State state = State.OUTSIDE;
    private boolean backslash = false;
    private int quote;
    private int quotes;
    private byte[] iri = new byte[256];
    private int iriLength = 0;
    private boolean hasSharp = false;
    private boolean bug = false;

    private byte[] buffer;
    private int idx;


    public InputStreamFixer(InputStream in)
    {
        this.in = in;
    }


    /*
     * Returns the next byte of the underlying stream, or -1 at its end.
     */
    private int next() throws IOException
    {
        while(position == limit)
        {
            int count = in.read(input);

            if(count == -1)
                return -1;

            position = 0;
            limit = count;
        }

        return input[position++] & 0xFF;
    }


    /*
     * Appends a byte to the IRI being read, which is kept for the report of its repair.
     */
    private void addIri(int c)
    {
        if(iriLength == iri.length)
            iri = Arrays.copyOf(iri, 2 * iriLength);

        iri[iriLength++] = (byte) c;
    }


    @Override
    public int read() throws IOException
    {
        if(buffer != null)
        {
            if(idx < buffer.length)
                return buffer[idx++] & 0xFF;

            idx = 0;
            buffer = null;
        }

        return process(next());
    }


    /*
     * Returns the byte of the repaired stream for the next byte of the underlying stream, or -1 at its end.
     */
    private int process(int c) throws IOException
    {
        if(c == -1)
            return -1;

        if(state == State.ANGLE)
        {
            if(c == '<')
            {
                // the << that starts a reified triple or a triple term
                state = State.OUTSIDE;
            }
            else
            {
                state = State.IRI;
                hasSharp = false;
                iriLength = 0;
                addIri('<');
                return process(c);
            }
        }
        else if(state == State.QUOTE)
        {
            if(c == quote)
            {
                state = State.QUOTES;
            }
            else
            {
                state = State.STRING;
                backslash = c == '\\';
            }
        }
        else if(state == State.QUOTES)
        {
            if(c == quote)
            {
                state = State.LONG_STRING;
                quotes = 0;
                backslash = false;
            }
            else
            {
                // the two quotes have been an empty string
                state = State.OUTSIDE;
                return process(c);
            }
        }
        else if(state == State.STRING)
        {
            if(!backslash && c == quote)
                state = State.OUTSIDE;
            else
                backslash = !backslash && c == '\\';
        }
        else if(state == State.LONG_STRING)
        {
            if(!backslash && c == quote)
            {
                if(++quotes == 3)
                    state = State.OUTSIDE;
            }
            else
            {
                quotes = 0;
                backslash = !backslash && c == '\\';
            }
        }
        else if(state == State.COMMENT)
        {
            if(c == '\n' || c == '\r')
                state = State.OUTSIDE;
        }
        else if(state == State.IRI)
        {
            addIri(c);

            if(c == '>')
            {
                if(bug)
                    Problems.warning("repaired IRI", new String(iri, 0, iriLength, StandardCharsets.UTF_8));

                bug = false;
                state = State.OUTSIDE;
            }
            else if(c == '\\')
            {
                buffer = new byte[5];

                for(int i = 0; i <= 4; i++)
                {
                    int cx = next();

                    if(cx == -1)
                        throw new IOException("unexpected end of the stream");

                    addIri(cx);
                    buffer[i] = (byte) cx;
                }

                if(buffer[0] == 'u' && buffer[1] == '0' && buffer[2] == '0' && buffer[3] == 'A' && buffer[4] == '0')
                {
                    bug = true;
                    return read();
                }

                if(buffer[0] == 'u' && buffer[1] == '0' && buffer[2] == '0' && buffer[3] == '2' && buffer[4] == '0')
                {
                    buffer = new byte[] { '2', '0' };
                    bug = true;
                    return '%';
                }
            }
            else
            {
                boolean escape = false;

                if(c == '#' && hasSharp)
                    escape = true;

                if(c == '#')
                    hasSharp = true;

                for(int i = 0; i < forbidden.length; i++)
                    if(c == forbidden[i])
                        escape = true;

                if(escape)
                {
                    buffer = new byte[] { toHex(c / 16), toHex(c % 16) };
                    bug = true;
                    return '%';
                }
            }
        }
        else if(state == State.OUTSIDE && backslash && c < 0x80)
        {
            // an escaped character of a local name, which can be only an ASCII one
            backslash = false;
        }
        else if(state == State.OUTSIDE)
        {
            backslash = false;

            if(c == '\\')
            {
                backslash = true;
            }
            else if(c == '<')
            {
                state = State.ANGLE;
            }
            else if(c == '"' || c == '\'')
            {
                state = State.QUOTE;
                quote = c;
            }
            else if(c == '#')
            {
                state = State.COMMENT;
            }
            else if(c == 0xC2)
            {
                int c1 = next();

                if(c1 == -1)
                    throw new IOException("unexpected end of the stream");

                if(c1 != 0xAC && c1 != 0xA0)
                {
                    buffer = new byte[] { (byte) c1 };
                    return 0xC2;
                }

                if(c1 == 0xAC)
                    Problems.warning("replaced character", "U+00AC (not sign)");
                else
                    Problems.warning("replaced character", "U+00A0 (no-break space)");

                return c1 == 0xAC ? '-' : ' ';
            }
            else if(c == 0xE2)
            {
                int c1 = next();
                int c2 = next();

                if(c1 == -1 || c2 == -1)
                    throw new IOException("unexpected end of the stream");

                if(c1 != 0x80 || c2 != 0xA9 && c2 != 0x91 && c2 != 0x93)
                {
                    buffer = new byte[] { (byte) c1, (byte) c2 };
                    return 0xE2;
                }

                if(c2 == 0xA9)
                    Problems.warning("replaced character", "U+2029 (paragraph separator)");
                else if(c2 == 0x91)
                    Problems.warning("replaced character", "U+2011 (non-breaking hyphen)");
                else if(c2 == 0x93)
                    Problems.warning("replaced character", "U+2013 (en dash)");

                return c2 == 0xA9 ? ' ' : '-';
            }
        }

        return c;
    }


    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException
    {
        Objects.checkFromIndexSize(offset, length, bytes.length);

        if(length == 0)
            return 0;

        int count = 0;

        while(count < length)
        {
            // a byte that read() would pass unchanged and without a change of its state is copied at once
            if(buffer == null && position < limit)
            {
                int c = input[position] & 0xFF;

                if(state == State.OUTSIDE && !backslash && !outsideSpecial[c] || state == State.IRI && !iriSpecial[c]
                        || state == State.STRING && !backslash && c != quote && c != '\\'
                        || state == State.LONG_STRING && quotes == 0 && !backslash && c != quote && c != '\\'
                        || state == State.COMMENT && c != '\n' && c != '\r')
                {
                    position++;

                    if(state == State.IRI)
                        addIri(c);

                    bytes[offset + count++] = (byte) c;
                    continue;
                }
            }

            int c = read();

            if(c == -1)
                break;

            bytes[offset + count++] = (byte) c;
        }

        return count == 0 ? -1 : count;
    }


    @Override
    public void close() throws IOException
    {
        in.close();
    }


    private byte toHex(int i)
    {
        if(i < 10)
            return (byte) ('0' + i);
        else
            return (byte) ('A' - 10 + i);
    }
}
