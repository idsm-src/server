package cz.iocb.load.common;



public class IntTriplet
{
    private final int one;
    private final int two;
    private final int three;


    public IntTriplet(int one, int two, int three)
    {
        this.one = one;
        this.two = two;
        this.three = three;
    }


    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        IntTriplet other = (IntTriplet) obj;

        return one == other.one && two == other.two && three == other.three;
    }


    @Override
    public int hashCode()
    {
        return Integer.hashCode(one) ^ Integer.hashCode(two) ^ Integer.hashCode(three);
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
}
