package cz.iocb.chemweb.server.sparql.parser.model.pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import cz.iocb.chemweb.server.sparql.parser.ElementVisitor;
import cz.iocb.chemweb.server.sparql.parser.Parser;
import cz.iocb.chemweb.server.sparql.parser.model.IRI;



/**
 * Custom pattern used to represent call to a procedure with multiple return values.
 *
 * <p>
 * It is written as a triple where the subject represents the results of the procedure ({@link #getResults}), the
 * predicate is the name of the procedure ( {@link #getProcedure}) (which is one of the predefined names, see
 * {@link Parser#getProcedures}) and the object is a blank node property list containing the parameters of the procedure
 * ({@link #getParameters}).
 */
public class MultiProcedureCall extends ProcedureCallBase
{
    private List<Parameter> results;

    public MultiProcedureCall()
    {
        results = new ArrayList<>();
    }

    public MultiProcedureCall(Collection<Parameter> results, IRI procedure, Collection<Parameter> parameters)
    {
        super(procedure, parameters);
        this.results = new ArrayList<>(results);
    }

    public List<Parameter> getResults()
    {
        return results;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor)
    {
        return visitor.visit(this);
    }
}
