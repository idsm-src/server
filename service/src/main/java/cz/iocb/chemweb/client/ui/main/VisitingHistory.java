package cz.iocb.chemweb.client.ui.main;

import java.util.Stack;



public class VisitingHistory
{
    private final Stack<String> prevStack = new Stack<String>();
    private final Stack<String> nextStack = new Stack<String>();
    private String currentIri;


    public String prev()
    {
        nextStack.push(currentIri);
        currentIri = prevStack.pop();

        return currentIri;
    }


    public String next()
    {
        prevStack.push(currentIri);
        currentIri = nextStack.pop();

        return currentIri;
    }


    public void visit(String iri)
    {
        if(currentIri != null)
            prevStack.push(currentIri);

        nextStack.clear();

        currentIri = iri;
    }


    public String getCurrent()
    {
        return currentIri;
    }


    public boolean hasPrev()
    {
        return !prevStack.isEmpty();
    }


    public boolean hasNext()
    {
        return !nextStack.isEmpty();
    }
}
