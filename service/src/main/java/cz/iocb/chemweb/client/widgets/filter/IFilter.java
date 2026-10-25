package cz.iocb.chemweb.client.widgets.filter;



/**
 * Filter of the rows of a {@link FilteredListDataProvider}.
 *
 * @param <Type> the type of the rows
 */
public interface IFilter<Type>
{
    /**
     * Decides whether a row passes the filter.
     *
     * @param value the row
     * @return true if the row is displayed
     */
    boolean isValid(Type value);
}
