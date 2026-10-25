package cz.iocb.load.stats;

import java.util.List;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ValueColumn;



/**
 * Class, property or datatype of the statistics, a resource of the ontology IRI class ({@code ontology:resource})
 * stored as its ontology unit and its id within the unit.
 *
 * @param unit the ontology unit of the resource
 * @param id the id of the resource within the unit
 */
public record Resource(short unit, int id)
{
    /**
     * Creates the resource from the constant columns of an IRI of the ontology IRI class.
     *
     * @param columns the constant unit and id columns
     */
    public Resource(List<Column> columns)
    {
        this(Short.parseShort(((ValueColumn) columns.get(0)).getValue()),
                Integer.parseInt(((ValueColumn) columns.get(1)).getValue()));
    }
}
