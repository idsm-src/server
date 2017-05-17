package cz.iocb.chemweb.server.sparql.mapping;

import java.util.List;
import cz.iocb.chemweb.server.sparql.mapping.classes.IriClass;
import cz.iocb.chemweb.server.sparql.parser.model.IRI;
import cz.iocb.chemweb.server.sparql.parser.model.Variable;
import cz.iocb.chemweb.server.sparql.parser.model.triple.Node;



public class ParametrisedIriMapping extends IriMapping
{
    private final List<String> columns;


    public ParametrisedIriMapping(IriClass iriClass, List<String> columns)
    {
        super(iriClass);
        this.columns = columns;
    }


    @Override
    public boolean match(Node node)
    {
        if(node instanceof Variable)
            return true;

        if(node instanceof IRI && getIriClass().match(((IRI) node).getUri().toString()))
            return true;

        return false;
    }


    @Override
    public String getSqlValueAccess(int i)
    {
        return columns.get(i);
    }
}
