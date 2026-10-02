package cz.iocb.chemweb.server.sparql.config.common;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.mapping.classes.SubsetLiteralClass;



public class StringSubsetLiteralClass extends SubsetLiteralClass
{
    public StringSubsetLiteralClass(String name)
    {
        super(name, xsdString);
    }
}
