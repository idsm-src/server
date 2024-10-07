package cz.iocb.load.stats;

import java.util.List;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ConstantColumn;



public record Resource(short unit, int id)
{
    public Resource(List<Column> columns)
    {
        this(Short.parseShort(((ConstantColumn) columns.get(0)).getValue()),
                Integer.parseInt(((ConstantColumn) columns.get(1)).getValue()));
    }
}
