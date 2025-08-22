package cz.iocb.chemweb.server.sparql.config.common;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinDataTypes.xsdStringIri;
import static cz.iocb.sparql.engine.mapping.classes.ResultTag.STRING;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.SimpleLiteralClass;



public class StringSubsetLiteralClass extends SimpleLiteralClass
{
    public StringSubsetLiteralClass(String name)
    {
        super(name, STRING, "varchar", xsdStringIri);
    }


    @Override
    public ResourceClass getGeneralClass()
    {
        return xsdString;
    }


    @Override
    public boolean canBeDerivatedFromGeneral()
    {
        return false;
    }
}
