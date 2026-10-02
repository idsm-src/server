package cz.iocb.load.stats;

import java.util.List;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ValueColumn;



public record Resource(short unit, int id)
{
    public Resource(List<Column> columns)
    {
        this(Short.parseShort(((ValueColumn) columns.get(0)).getValue()),
                Integer.parseInt(((ValueColumn) columns.get(1)).getValue()));
    }
}
