package cz.iocb.chemweb.server.sparql.mapping;

import cz.iocb.chemweb.server.sparql.parser.model.triple.Node;



public class QuadMapping
{
    private final String table;
    private final String condition;
    private final NodeMapping graph;
    private final NodeMapping subject;
    private final NodeMapping predicate;
    private final NodeMapping object;


    public QuadMapping(String table, NodeMapping graph, NodeMapping subject, NodeMapping predicate, NodeMapping object)
    {
        this(table, graph, subject, predicate, object, null);
    }


    public QuadMapping(String table, NodeMapping graph, NodeMapping subject, NodeMapping predicate, NodeMapping object,
            String condition)
    {
        //TODO: add support for ParametrisedIriMapping graphs
        if(graph != null && !(graph instanceof ConstantIriMapping))
            throw new IllegalArgumentException();

        this.table = table;
        this.graph = graph;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.condition = condition;
    }


    public boolean match(Node graph, Node subject, Node predicate, Node object)
    {
        if(!match(this.graph, graph))
            return false;

        if(!match(this.subject, subject))
            return false;

        if(!match(this.predicate, predicate))
            return false;

        if(!match(this.object, object))
            return false;

        return true;
    }


    private boolean match(NodeMapping mapping, Node node)
    {
        if(node == null)
            return true;

        if(mapping == null)
            return false;

        return mapping.match(node);
    }


    public final String getTable()
    {
        return table;
    }


    public final String getCondition()
    {
        return condition;
    }


    public final NodeMapping getGraph()
    {
        return graph;
    }


    public final NodeMapping getSubject()
    {
        return subject;
    }


    public final NodeMapping getPredicate()
    {
        return predicate;
    }


    public final NodeMapping getObject()
    {
        return object;
    }
}
