package cz.iocb.load.common;



/**
 * Immutable pair of two non-null values, used as a compound key or value of the sets and maps of the loaders.
 *
 * @param <T1> the type of the first value
 * @param <T2> the type of the second value
 */
public final class Pair<T1, T2>
{
    /**
     * First value.
     */
    private final T1 one;

    /**
     * Second value.
     */
    private final T2 two;


    /**
     * Creates the pair.
     *
     * @param one the first value
     * @param two the second value
     */
    private Pair(T1 one, T2 two)
    {
        this.one = one;
        this.two = two;
    }


    /**
     * Returns the pair of two values.
     *
     * @param <T1> the type of the first value
     * @param <T2> the type of the second value
     * @param one the first value
     * @param two the second value
     * @return the pair
     */
    public static <T1, T2> Pair<T1, T2> getPair(T1 one, T2 two)
    {
        return new Pair<>(one, two);
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


    /**
     * Returns the first value.
     *
     * @return the first value
     */
    public T1 getOne()
    {
        return one;
    }


    /**
     * Returns the second value.
     *
     * @return the second value
     */
    public T2 getTwo()
    {
        return two;
    }
}
