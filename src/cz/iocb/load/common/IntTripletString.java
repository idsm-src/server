package cz.iocb.load.common;



public class IntTripletString
{
    private final int one;
    private final int two;
    private final int three;
    private final String string;


    public IntTripletString(int one, int two, int three, String string)
    {
        this.one = one;
        this.two = two;
        this.three = three;
        this.string = string;
    }


    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        IntTripletString other = (IntTripletString) obj;

        return one == other.one && two == other.two && three == other.three && string.equals(other.string);
    }


    @Override
    public int hashCode()
    {
        return Integer.hashCode(one) ^ Integer.hashCode(two) ^ Integer.hashCode(three) ^ string.hashCode();
    }


    public int getOne()
    {
        return one;
    }


    public int getTwo()
    {
        return two;
    }


    public int getThree()
    {
        return three;
    }


    public String getString()
    {
        return string;
    }
}
