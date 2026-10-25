/*
 * based on GWT CellTable Filtering
 * http://www.artiom.pro/2012/09/gwt-celltable-filtering.html
 */
package cz.iocb.chemweb.client.widgets.filter;

import java.util.ArrayList;
import java.util.List;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;



/**
 * List data provider that displays only the rows of the visible range that pass a filter.
 *
 * @param <Type> the type of the rows
 */
public class FilteredListDataProvider<Type> extends ListDataProvider<Type>
{
    /**
     * Filter of the rows, null when all rows are displayed.
     */
    public IFilter<Type> filter;


    /**
     * Creates the data provider without a filter.
     */
    public FilteredListDataProvider()
    {
    }


    /**
     * Returns the filter of the rows.
     *
     * @return the filter, or null when there is none
     */
    public IFilter<Type> getFilter()
    {
        return filter;
    }

    /**
     * Sets the filter of the rows and refreshes the displays.
     *
     * @param filter the filter
     */
    public void setFilter(IFilter<Type> filter)
    {
        this.filter = filter;
        refresh();
    }

    /**
     * Removes the filter of the rows and refreshes the displays.
     */
    public void resetFilter()
    {
        filter = null;
        refresh();
    }

    /**
     * Tells whether the rows are filtered.
     *
     * @return true if a filter is set
     */
    public boolean hasFilter()
    {
        return !(filter == null /*|| "".equals(filterValue)*/);
    }

    @Override
    protected void updateRowData(HasData<Type> display, int start, List<Type> values)
    {
        if(!hasFilter() || filter == null)
        { // we don't need to filter, so call base class
            super.updateRowData(display, start, values);
        }
        else
        {
            int end = start + values.size();
            Range range = display.getVisibleRange();
            int curStart = range.getStart();
            int curLength = range.getLength();
            int curEnd = curStart + curLength;
            if(start == curStart || curStart < end && curEnd > start)
            {
                int realStart = curStart < start ? start : curStart;
                int realEnd = curEnd > end ? end : curEnd;
                int realLength = realEnd - realStart;
                List<Type> resulted = new ArrayList<>(realLength);
                for(int i = realStart - start; i < realStart - start + realLength; i++)
                {
                    if(filter.isValid(values.get(i)))
                    {
                        resulted.add(values.get(i));
                    }
                }
                display.setRowData(realStart, resulted);
                display.setRowCount(resulted.size());
            }
        }
    }
}
