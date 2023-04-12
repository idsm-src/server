package cz.iocb.load.common;

import java.io.IOException;
import java.io.InputStream;
import org.apache.jena.ext.com.google.common.base.Charsets;
import org.eclipse.collections.impl.list.mutable.primitive.ByteArrayList;



public class InputStreamFixer extends InputStream
{
    private static enum State
    {
        OUTSIDE, IRI, STRING1, STRING2
    }

    private static final byte[] forbidden = { ' ', '"', '<', '>', '{', '}', '|', '^', '`', '[', ']' };

    private final InputStream in;
    private State state = State.OUTSIDE;
    private boolean backslash = false;
    private ByteArrayList iri = new ByteArrayList();
    private boolean hasSharp = false;
    private boolean bug = false;

    private byte[] buffer;
    private int idx;


    public InputStreamFixer(InputStream in)
    {
        this.in = in;
    }


    @Override
    public int read() throws IOException
    {
        if(buffer != null)
        {
            if(idx < buffer.length)
                return buffer[idx++];

            idx = 0;
            buffer = null;
        }

        int c = in.read();

        if(state == State.STRING1 || state == State.STRING1)
        {
            if(state == State.STRING1 && !backslash && c == '"')
                state = State.OUTSIDE;
            else if(state == State.STRING2 && !backslash && c == '\'')
                state = State.OUTSIDE;
            else
                backslash = !backslash && c == '\\';
        }
        else if(state == State.IRI)
        {
            iri.add((byte) c);

            if(c == '>')
            {
                if(bug)
                    System.err.println("    bad iri: " + new String(iri.toArray(), Charsets.UTF_8));

                bug = false;
                state = State.OUTSIDE;
            }
            else if(c == '\\')
            {
                buffer = new byte[5];

                for(int i = 0; i <= 4; i++)
                {
                    int cx = in.read();

                    if(cx == -1)
                        throw new IOException();

                    iri.add((byte) cx);
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
        else if(state == State.OUTSIDE)
        {
            if(c == '<')
            {
                state = State.IRI;
                hasSharp = false;
                iri.clear();
                iri.add((byte) '<');
            }
            else if(c == '"')
            {
                state = State.STRING1;
            }
            else if(c == '\'')
            {
                state = State.STRING2;
            }
            else if(c == 0xC2)
            {
                int c1 = in.read();

                if(c1 == -1)
                    throw new IOException();

                if(c1 != 0xAC && c1 != 0xA0)
                {
                    buffer = new byte[] { (byte) c1 };
                    return 0xC2;
                }

                if(c1 == 0xAC)
                    System.err.println("    bad character: U+00AC (not sign)");
                else
                    System.err.println("    bad character: U+00A0 (no-break space)");

                return c1 == 0xAC ? '-' : ' ';
            }
            else if(c == 0xE2)
            {
                int c1 = in.read();
                int c2 = in.read();

                if(c1 == -1 || c2 == -1)
                    throw new IOException();

                if(c1 != 0x80 || c2 != 0xA9 && c2 != 0x91 && c2 != 0x93)
                {
                    buffer = new byte[] { (byte) c1, (byte) c2 };
                    return 0xE2;
                }

                if(c2 == 0xA9)
                    System.err.println("    bad character: U+2029 (paragraph separator)");
                else if(c2 == 0x91)
                    System.err.println("    bad character: U+2010 (non-breaking hyphen)");
                if(c2 == 0x93)
                    System.err.println("    bad character: U+2013 (en dash)");

                return c2 == 0xA9 ? ' ' : '-';
            }
        }

        return c;
    }


    private byte toHex(int i)
    {
        if(i < 10)
            return (byte) ('0' + i);
        else
            return (byte) ('A' - 10 + i);
    }
}
