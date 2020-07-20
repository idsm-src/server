package cz.iocb.pubchem.load.common;



public class MD5
{
    private final long hi;
    private final long lo;


    public MD5(String value)
    {
        this(value, 0);
    }


    public MD5(String value, int offset)
    {
        hi = parse(offset + 0, value);
        lo = parse(offset + 16, value);
    }


    private static long parse(int offset, String value)
    {
        long ret = 0;

        for(int i = offset; i < offset + 16; i++)
        {
            char ch = value.charAt(i);
            long digit = ch >= 'a' ? 10 + ch - 'a' : ch - '0';

            if(digit < 0 || digit > 15)
                throw new RuntimeException("unexpected md5 value: " + ch);

            ret = ret << 4 | digit;
        }

        return ret;
    }


    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        MD5 other = (MD5) obj;

        return hi == other.hi && lo == other.lo;
    }


    @Override
    public int hashCode()
    {
        return (int) lo;
    }


    @Override
    public String toString()
    {
        char[] ret = new char[32];

        long lo = this.lo;
        long hi = this.hi;

        for(int i = 31; i >= 16; i--)
        {
            long digit = 0x0f & lo;
            lo >>>= 4;
            ret[i] = (char) (digit >= 10 ? 'a' + digit - 10 : '0' + digit);
        }

        for(int i = 15; i >= 0; i--)
        {
            long digit = 0x0f & hi;
            hi >>>= 4;
            ret[i] = (char) (digit >= 10 ? 'a' + digit - 10 : '0' + digit);
        }

        return new String(ret);
    }
}
