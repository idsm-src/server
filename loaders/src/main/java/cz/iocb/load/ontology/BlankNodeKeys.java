package cz.iocb.load.ontology;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.graph.Node;



/**
 * Computes the keys of the blank nodes, which identify a blank node in the loads of the ontologies, although its label
 * changes. The key of a blank node is a hash of its content and its context. The content consists of the predicates and
 * the objects of the triples whose subject the blank node is, with the contents of the blank node objects in place of
 * the objects. The context consists of the subjects and the predicates of the triples whose object the blank node is,
 * with the keys of the blank node subjects in place of the subjects. So the key of a blank node stays the same as long
 * as its structure and the resources that refer to the structure do, and two blank nodes get the same key only if they
 * are equal structures in equal contexts, such as an axiom stated by two ontologies; such blank nodes can be merged, as
 * the merged graph is equivalent. A blank node in a cycle, which the structures of OWL do not have, and a blank node
 * referred to from a cycle are keyed by their labels.
 */
final class BlankNodeKeys
{
    /**
     * Indexes of the blank nodes.
     */
    private final HashMap<Node, Integer> indexes = new HashMap<>();

    /**
     * Hashes of the predicates by their IRIs.
     */
    private final HashMap<String, Long> predicateHashes = new HashMap<>();

    /**
     * Digest computing the hashes.
     */
    private final MessageDigest digest = getDigest();

    /**
     * Blank nodes by their indexes.
     */
    private Node[] nodes = new Node[1024];

    /**
     * Number of the added triples, which the following arrays hold: the index of a blank node subject or -1 and the
     * hash of another subject, the hash of the predicate, and the index of a blank node object or -1 and the hash of
     * another object.
     */
    private int size = 0;

    /**
     * Indexes of the blank node subjects of the triples, -1 for the other subjects.
     */
    private int[] subjects = new int[1024];

    /**
     * Hashes of the subjects of the triples that are not blank nodes.
     */
    private long[] subjectHashes = new long[1024];

    /**
     * Hashes of the predicates of the triples.
     */
    private long[] predicates = new long[1024];

    /**
     * Indexes of the blank node objects of the triples, -1 for the other objects.
     */
    private int[] objects = new int[1024];

    /**
     * Hashes of the objects of the triples that are not blank nodes.
     */
    private long[] objectHashes = new long[1024];


    /**
     * Creates the computation without any triple.
     */
    BlankNodeKeys()
    {
    }


    /**
     * Adds a triple whose subject or object is a blank node.
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
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


    /**
     * Returns the keys of the blank nodes of the added triples.
     *
     * @return the keys of the blank nodes
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


    /**
     * Returns the key of a blank node that has no key computed, which is given by its label.
     *
     * @param node the blank node
     * @return the key of the blank node
     */
    static long getLabelKey(Node node)
    {
        BlankNodeKeys keys = new BlankNodeKeys();

        return keys.getHash(3, new long[] { keys.getHash("B" + node.getBlankNodeLabel()) });
    }


    /**
     * Returns the index of a blank node, assigning the next one to a new node.
     *
     * @param node the blank node
     * @return the index of the blank node
     */
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


    /**
     * Returns the offsets of the triples of each blank node in the given position, as getTriples() lists them.
     *
     * @param position the indexes of the blank nodes of the triples in the position, -1 for other nodes
     * @param count the number of the blank nodes
     * @return the offset of the first triple of each blank node, followed by the number of the triples
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


    /**
     * Returns the triples ordered by the blank nodes in the given position.
     *
     * @param position the indexes of the blank nodes of the triples in the position, -1 for other nodes
     * @param offsets the offsets of the triples of each blank node, as getOffsets() returns them
     * @param count the number of the blank nodes
     * @return the indexes of the triples whose node in the position is a blank node, ordered by the blank nodes
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


    /**
     * Returns the hash of the label of a blank node.
     *
     * @param node the index of the blank node
     * @return the hash of the label
     */
    private long getLabelHash(int node)
    {
        return getHash("B" + nodes[node].getBlankNodeLabel());
    }


    /**
     * Returns the hash of an IRI or a literal.
     *
     * @param node the IRI or the literal
     * @return the hash of the node
     */
    private long getHash(Node node)
    {
        if(node.isURI())
            return getHash("I" + node.getURI());

        return getHash("L" + node.getLiteralLexicalForm() + "\0" + node.getLiteralDatatypeURI() + "\0"
                + node.getLiteralLanguage());
    }


    /**
     * Returns the hash of a text.
     *
     * @param text the text
     * @return the hash of the text
     */
    private long getHash(String text)
    {
        return toLong(digest.digest(text.getBytes(UTF_8)));
    }


    /**
     * Returns the hash of a pair of values.
     *
     * @param first the first value
     * @param second the second value
     * @return the hash of the pair
     */
    private long getHash(long first, long second)
    {
        return getHash(first, new long[] { second });
    }


    /**
     * Returns the hash of a value followed by a sequence of values.
     *
     * @param first the first value
     * @param values the following values
     * @return the hash of the values
     */
    private long getHash(long first, long[] values)
    {
        byte[] bytes = new byte[8 * (values.length + 1)];

        put(bytes, 0, first);

        for(int i = 0; i < values.length; i++)
            put(bytes, 8 * (i + 1), values[i]);

        return toLong(digest.digest(bytes));
    }


    /**
     * Writes a value into an array of bytes in the big-endian order.
     *
     * @param bytes the array of bytes
     * @param offset the offset of the value in the array
     * @param value the value
     */
    private static void put(byte[] bytes, int offset, long value)
    {
        for(int i = 0; i < 8; i++)
            bytes[offset + i] = (byte) (value >>> 56 - 8 * i);
    }


    /**
     * Returns the value of the first eight bytes of an array in the big-endian order.
     *
     * @param bytes the array of bytes
     * @return the value
     */
    private static long toLong(byte[] bytes)
    {
        long value = 0;

        for(int i = 0; i < 8; i++)
            value = value << 8 | bytes[i] & 0xff;

        return value;
    }


    /**
     * Returns a new SHA-256 digest.
     *
     * @return the digest
     * @throws IllegalStateException if the platform does not provide SHA-256
     */
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
