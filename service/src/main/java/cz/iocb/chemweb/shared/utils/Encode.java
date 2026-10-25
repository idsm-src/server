package cz.iocb.chemweb.shared.utils;




/**
 * Base32 and base64 encodings of strings, used by the client for the identifiers and CSS classes of the elements
 * showing a resource and for the content of the query file the user saves.
 */
public class Encode
{
    /**
     * Alphabet of base32.
     */
    static final private byte[] table32 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7' };


    /**
     * Alphabet of base64.
     */
    static final private byte[] table64 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '+', '/' };


    /**
     * Not instantiable.
     */
    private Encode()
    {
    }


    /**
     * Encodes a string in base32 padded with underscores instead of equal signs, so that the result can be used in
     * identifiers and CSS class names.
     *
     * @param value the string to encode
     * @return the encoded string
     */
    public static String base32m(String value)
    {
        return encode(value, 5, 5, table32, (byte) '_');
    }


    /**
     * Encodes a string in base32.
     *
     * @param value the string to encode
     * @return the encoded string
     */
    public static String base32(String value)
    {
        return encode(value, 5, 5, table32, (byte) '=');
    }


    /**
     * Encodes a string in base64.
     *
     * @param value the string to encode
     * @return the encoded string
     */
    public static String base64(String value)
    {
        return encode(value, 3, 6, table64, (byte) '=');
    }


    /**
     * Encodes the bytes of a string (in the platform charset) group by group, each group of bytes into a fixed number
     * of characters of the alphabet, and pads the last incomplete group.
     *
     * @param value the string to encode
     * @param groupSize the number of bytes encoded together
     * @param indexBitSize the number of bits encoded by one character
     * @param table the alphabet
     * @param pad the padding character
     * @return the encoded string
     */
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
