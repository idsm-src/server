package cz.iocb.load.common;



public final class Pair<T1, T2>
{
    private final T1 one;
    private final T2 two;


    private Pair(T1 one, T2 two)
    {
        this.one = one;
        this.two = two;
    }


    public static <T1, T2> Pair<T1, T2> getPair(T1 one, T2 two)
    {
        return new Pair<T1, T2>(one, two);
    }


    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        Pair<?, ?> other = (Pair<?, ?>) obj;

        return one.equals(other.one) && two.equals(other.two);
    }


    @Override
    public int hashCode()
    {
        return one.hashCode() + two.hashCode();
    }


    public T1 getOne()
    {
        return one;
    }


    public T2 getTwo()
    {
        return two;
    }
}
