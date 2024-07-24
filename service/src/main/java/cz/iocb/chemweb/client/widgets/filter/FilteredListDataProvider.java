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



public class FilteredListDataProvider<Type> extends ListDataProvider<Type>
{
    public IFilter<Type> filter;


    public IFilter<Type> getFilter()
    {
        return filter;
    }

    public void setFilter(IFilter<Type> filter)
    {
        this.filter = filter;
        refresh();
    }

    public void resetFilter()
    {
        filter = null;
        refresh();
    }

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
                List<Type> resulted = new ArrayList<Type>(realLength);
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
