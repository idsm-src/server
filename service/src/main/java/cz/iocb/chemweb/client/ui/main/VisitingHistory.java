package cz.iocb.chemweb.client.ui.main;

import java.util.Stack;



/**
 * History of the visited resources, which the details and the properties tabs move back and forth in.
 */
public class VisitingHistory
{
    /**
     * Resources visited before the current one, the latest on the top.
     */
    private final Stack<String> prevStack = new Stack<>();

    /**
     * Resources left by going back, the nearest on the top.
     */
    private final Stack<String> nextStack = new Stack<>();

    /**
     * IRI of the current resource.
     */
    private String currentIri;


    /**
     * Creates an empty history.
     */
    public VisitingHistory()
    {
    }


    /**
     * Goes back to the previous resource.
     *
     * @return the IRI of the previous resource, which becomes the current one
     */
    public String prev()
    {
        nextStack.push(currentIri);
        currentIri = prevStack.pop();

        return currentIri;
    }


    /**
     * Goes forward to the next resource.
     *
     * @return the IRI of the next resource, which becomes the current one
     */
    public String next()
    {
        prevStack.push(currentIri);
        currentIri = nextStack.pop();

        return currentIri;
    }


    /**
     * Visits a new resource, which discards the resources left by going back.
     *
     * @param iri the IRI of the resource
     */
    public void visit(String iri)
    {
        if(currentIri != null)
            prevStack.push(currentIri);

        nextStack.clear();

        currentIri = iri;
    }


    /**
     * Returns the current resource.
     *
     * @return the IRI of the current resource, or null when none has been visited
     */
    public String getCurrent()
    {
        return currentIri;
    }


    /**
     * Tells whether there is a resource to go back to.
     *
     * @return true if there is a previous resource
     */
    public boolean hasPrev()
    {
        return !prevStack.isEmpty();
    }


    /**
     * Tells whether there is a resource to go forward to.
     *
     * @return true if there is a next resource
     */
    public boolean hasNext()
    {
        return !nextStack.isEmpty();
    }
}
