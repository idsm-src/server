package cz.iocb.chemweb.server.sparql.config.common;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.mapping.classes.SubsetLiteralClass;



/**
 * Subset of the {@code xsd:string} literals stored in particular columns, such as the molfiles of one dataset, which
 * the engine then treats as disjoint from the strings of the other subsets.
 */
public class StringSubsetLiteralClass extends SubsetLiteralClass
{
    /**
     * Creates the subset of {@code xsd:string} under the given (unique) name.
     *
     * @param name the name
     */
    public StringSubsetLiteralClass(String name)
    {
        super(name, xsdString);
    }
}
