package cz.iocb.load.common;



public class IntQuaterplet
{
    private final int one;
    private final int two;
    private final int three;
    private final int four;


    public IntQuaterplet(int one, int two, int three, int four)
    {
        this.one = one;
        this.two = two;
        this.three = three;
        this.four = four;
    }


    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        IntQuaterplet other = (IntQuaterplet) obj;

        return one == other.one && two == other.two && three == other.three && four == other.four;
    }


    @Override
    public int hashCode()
    {
        return Integer.hashCode(one) ^ Integer.hashCode(two) ^ Integer.hashCode(three) ^ Integer.hashCode(four);
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


    public int getFour()
    {
        return four;
    }
}
