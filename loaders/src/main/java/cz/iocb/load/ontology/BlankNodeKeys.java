package cz.iocb.load.ontology;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.graph.Node;



/*
 * Computes the keys of the blank nodes, which identify a blank node in the loads of the ontologies, although its label
 * changes. The key of a blank node is a hash of its content and its context. The content consists of the predicates
 * and the objects of the triples whose subject the blank node is, with the contents of the blank node objects in place
 * of the objects. The context consists of the subjects and the predicates of the triples whose object the blank node
 * is, with the keys of the blank node subjects in place of the subjects. So the key of a blank node stays the same as
 * long as its structure and the resources that refer to the structure do, and two blank nodes get the same key only
 * if they are equal structures in equal contexts, such as an axiom stated by two ontologies; such blank nodes can be
 * merged, as the merged graph is equivalent. A blank node in a cycle, which the structures of OWL do not have, and a
 * blank node referred to from a cycle are keyed by their labels.
 */
final class BlankNodeKeys
{
    private final HashMap<Node, Integer> indexes = new HashMap<>();
    private final HashMap<String, Long> predicateHashes = new HashMap<>();
    private final MessageDigest digest = getDigest();
    private Node[] nodes = new Node[1024];

    // the triples: the index of a blank node subject or -1 and the hash of another subject, the hash of the
    // predicate, and the index of a blank node object or -1 and the hash of another object
    private int size = 0;
    private int[] subjects = new int[1024];
    private long[] subjectHashes = new long[1024];
    private long[] predicates = new long[1024];
    private int[] objects = new int[1024];
    private long[] objectHashes = new long[1024];


    /*
     * Adds a triple whose subject or object is a blank node.
     */
    void add(Node subject, Node predicate, Node object)
    {
        if(size == subjects.length)
        {
            subjects = Arrays.copyOf(subjects, 2 * size);
            subjectHashes = Arrays.copyOf(subjectHashes, 2 * size);
            predicates = Arrays.copyOf(predicates, 2 * size);
            objects = Arrays.copyOf(objects, 2 * size);
            objectHashes = Arrays.copyOf(objectHashes, 2 * size);
        }

        subjects[size] = subject.isBlank() ? getIndex(subject) : -1;
        subjectHashes[size] = subject.isBlank() ? 0 : getHash(subject);
        predicates[size] = predicateHashes.computeIfAbsent(predicate.getURI(), iri -> getHash("I" + iri));
        objects[size] = object.isBlank() ? getIndex(object) : -1;
        objectHashes[size] = object.isBlank() ? 0 : getHash(object);
        size++;
    }


    /*
     * Returns the keys of the blank nodes of the added triples.
     */
    Map<Node, Long> compute()
    {
        int count = indexes.size();

        int[] outgoingOffsets = getOffsets(subjects, count);
        int[] outgoing = getTriples(subjects, outgoingOffsets, count);
        int[] incomingOffsets = getOffsets(objects, count);
        int[] incoming = getTriples(objects, incomingOffsets, count);


        // the contents, each computed after the contents of its blank node objects
        long[] contents = new long[count];
        byte[] states = new byte[count];
        int[] positions = new int[count];
        int[] stack = new int[count];

        for(int root = 0; root < count; root++)
        {
            if(states[root] != 0)
                continue;

            int top = 0;
            stack[top++] = root;
            states[root] = 1;
            positions[root] = outgoingOffsets[root];

            while(top > 0)
            {
                int node = stack[top - 1];

                if(positions[node] < outgoingOffsets[node + 1])
                {
                    int child = objects[outgoing[positions[node]++]];

                    if(child >= 0 && states[child] == 0)
                    {
                        stack[top++] = child;
                        states[child] = 1;
                        positions[child] = outgoingOffsets[child];
                    }
                }
                else
                {
                    long[] pairs = new long[outgoingOffsets[node + 1] - outgoingOffsets[node]];

                    for(int i = 0; i < pairs.length; i++)
                    {
                        int triple = outgoing[outgoingOffsets[node] + i];
                        int child = objects[triple];
                        long value = child < 0 ? objectHashes[triple] :
                                states[child] == 2 ? contents[child] : getLabelHash(child);

                        pairs[i] = getHash(predicates[triple], value);
                    }

                    Arrays.sort(pairs);
                    contents[node] = getHash(1, pairs);
                    states[node] = 2;
                    top--;
                }
            }
        }


        // the keys, each computed after the keys of its blank node subjects
        long[] keys = new long[count];
        boolean[] done = new boolean[count];
        int[] degrees = new int[count];
        int[] queue = new int[count];
        int head = 0;
        int tail = 0;

        for(int i = 0; i < size; i++)
            if(subjects[i] >= 0 && objects[i] >= 0)
                degrees[objects[i]]++;

        for(int node = 0; node < count; node++)
            if(degrees[node] == 0)
                queue[tail++] = node;

        while(head < tail)
        {
            int node = queue[head++];
            long[] pairs = new long[incomingOffsets[node + 1] - incomingOffsets[node]];

            for(int i = 0; i < pairs.length; i++)
            {
                int triple = incoming[incomingOffsets[node] + i];
                long subject = subjects[triple] >= 0 ? keys[subjects[triple]] : subjectHashes[triple];

                pairs[i] = getHash(subject, predicates[triple]);
            }

            Arrays.sort(pairs);
            keys[node] = getHash(contents[node], pairs);
            done[node] = true;

            for(int i = outgoingOffsets[node]; i < outgoingOffsets[node + 1]; i++)
            {
                int child = objects[outgoing[i]];

                if(child >= 0 && --degrees[child] == 0)
                    queue[tail++] = child;
            }
        }


        Map<Node, Long> result = new HashMap<>(2 * count);

        for(int node = 0; node < count; node++)
            result.put(nodes[node], done[node] ? keys[node] : getHash(2, new long[] { getLabelHash(node) }));

        return result;
    }


    /*
     * Returns the key of a blank node that has no key computed, which is given by its label.
     */
    static long getLabelKey(Node node)
    {
        BlankNodeKeys keys = new BlankNodeKeys();

        return keys.getHash(3, new long[] { keys.getHash("B" + node.getBlankNodeLabel()) });
    }


    private int getIndex(Node node)
    {
        Integer index = indexes.get(node);

        if(index == null)
        {
            index = indexes.size();
            indexes.put(node, index);

            if(index == nodes.length)
                nodes = Arrays.copyOf(nodes, 2 * index);

            nodes[index] = node;
        }

        return index;
    }


    /*
     * Returns the offsets of the triples of each blank node in the given position, as getTriples() lists them.
     */
    private int[] getOffsets(int[] position, int count)
    {
        int[] offsets = new int[count + 1];

        for(int i = 0; i < size; i++)
            if(position[i] >= 0)
                offsets[position[i] + 1]++;

        for(int node = 0; node < count; node++)
            offsets[node + 1] += offsets[node];

        return offsets;
    }


    /*
     * Returns the triples ordered by the blank nodes in the given position.
     */
    private int[] getTriples(int[] position, int[] offsets, int count)
    {
        int[] triples = new int[offsets[count]];
        int[] next = Arrays.copyOf(offsets, count);

        for(int i = 0; i < size; i++)
            if(position[i] >= 0)
                triples[next[position[i]]++] = i;

        return triples;
    }


    private long getLabelHash(int node)
    {
        return getHash("B" + nodes[node].getBlankNodeLabel());
    }


    private long getHash(Node node)
    {
        if(node.isURI())
            return getHash("I" + node.getURI());

        return getHash("L" + node.getLiteralLexicalForm() + "\0" + node.getLiteralDatatypeURI() + "\0"
                + node.getLiteralLanguage());
    }


    private long getHash(String text)
    {
        return toLong(digest.digest(text.getBytes(UTF_8)));
    }


    private long getHash(long first, long second)
    {
        return getHash(first, new long[] { second });
    }


    private long getHash(long first, long[] values)
    {
        byte[] bytes = new byte[8 * (values.length + 1)];

        put(bytes, 0, first);

        for(int i = 0; i < values.length; i++)
            put(bytes, 8 * (i + 1), values[i]);

        return toLong(digest.digest(bytes));
    }


    private static void put(byte[] bytes, int offset, long value)
    {
        for(int i = 0; i < 8; i++)
            bytes[offset + i] = (byte) (value >>> 56 - 8 * i);
    }


    private static long toLong(byte[] bytes)
    {
        long value = 0;

        for(int i = 0; i < 8; i++)
            value = value << 8 | bytes[i] & 0xff;

        return value;
    }


    private static MessageDigest getDigest()
    {
        try
        {
            return MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
