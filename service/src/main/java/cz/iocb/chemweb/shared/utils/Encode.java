package cz.iocb.chemweb.shared.utils;




public class Encode
{
    static final private byte[] table32 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7' };


    static final private byte[] table64 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '+', '/' };


    public static String base32m(String value)
    {
        return encode(value, 5, 5, table32, (byte) '_');
    }


    public static String base32(String value)
    {
        return encode(value, 5, 5, table32, (byte) '=');
    }


    public static String base64(String value)
    {
        return encode(value, 3, 6, table64, (byte) '=');
    }


    private static String encode(String value, int groupSize, int indexBitSize, byte[] table, byte pad)
    {
        final int encodedGroupSize = groupSize * 8 / indexBitSize;
        final long indexBitMask = ~(~0l << indexBitSize);

        byte[] data = value.getBytes();
        byte[] result = new byte[encodedGroupSize * ((data.length + groupSize - 1) / groupSize)];
        int off = 0;



        for(int i = 0; i < data.length; i += groupSize)
        {
            long val = 0;

            for(int j = 0; j < groupSize; j++)
                val = val << 8 | (i + j < data.length ? data[i + j] : 0);

            for(int j = encodedGroupSize - 1; j >= 0; j--)
                result[off++] = table[(int) ((val & indexBitMask << indexBitSize * j) >> indexBitSize * j)];
        }

        if(data.length % groupSize > 0)
        {
            int used = (data.length % groupSize * 8 + indexBitSize - 1) / indexBitSize;

            for(int i = used; i < encodedGroupSize; i++)
                result[--off] = pad;
        }

        return new String(result);
    }
}
