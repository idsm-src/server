package cz.iocb.load.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.graph.Node;



/*
 * Collects the values of chosen predicates at blank nodes while the triples are read, so that a structure of blank
 * nodes, such as an OWL restriction or an annotated axiom, can be processed once all of its triples are known. The
 * values of a predicate at a node form a set, as in an RDF graph.
 */
public class BlankNodes
{
    private final HashMap<String, Integer> indexes = new HashMap<>();
    private final HashMap<Node, Object[]> nodes = new HashMap<>();


    /*
     * Registers the handlers that collect the values of the predicates.
     */
    public BlankNodes(TripleDispatcher dispatcher, String... predicates)
    {
        for(String predicate : predicates)
        {
            int index = indexes.size();
            indexes.put(predicate, index);

            dispatcher.on(predicate, (subject, object) -> {
                if(subject.isBlank())
                    add(subject, index, object);
            });
        }
    }


    /*
     * Adds a value to the values of a node, which are kept as a node while there is only one of them.
     */
    @SuppressWarnings("unchecked")
    private void add(Node node, int index, Node value)
    {
        Object[] values = nodes.computeIfAbsent(node, k -> new Object[indexes.size()]);

        if(values[index] == null)
        {
            values[index] = value;
        }
        else if(values[index] instanceof Node current)
        {
            if(!current.equals(value))
                values[index] = new ArrayList<>(List.of(current, value));
        }
        else
        {
            List<Node> list = (List<Node>) values[index];

            if(!list.contains(value))
                list.add(value);
        }
    }


    /*
     * Returns the values of the predicate at the node.
     */
    @SuppressWarnings("unchecked")
    public List<Node> values(Node node, String predicate)
    {
        Object[] values = nodes.get(node);
        Object value = values == null ? null : values[indexes.get(predicate)];

        if(value == null)
            return List.of();
        else if(value instanceof Node single)
            return List.of(single);
        else
            return (List<Node>) value;
    }


    /*
     * Returns the values of the predicate at the node, or a list holding just null if there is none, so that a loop
     * over the values runs once also for a node without them.
     */
    public List<Node> optionalValues(Node node, String predicate)
    {
        List<Node> values = values(node, predicate);

        if(values.isEmpty())
        {
            List<Node> unbound = new ArrayList<>(1);
            unbound.add(null);
            return unbound;
        }

        return values;
    }
}
