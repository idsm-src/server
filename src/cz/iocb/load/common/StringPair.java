package cz.iocb.load.common;



public class StringPair
{
    private final String one;
    private final String two;


    public StringPair(String one, String two)
    {
        this.one = one;
        this.two = two;
    }


    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        StringPair other = (StringPair) obj;

        return one.equals(other.one) && two.equals(other.two);
    }


    @Override
    public int hashCode()
    {
        int hash1 = one.hashCode();
        int hash2 = two.hashCode();

        return hash1 == hash2 ? hash1 : hash1 ^ hash2;
    }


    public String getOne()
    {
        return one;
    }


    public String getTwo()
    {
        return two;
    }
}
