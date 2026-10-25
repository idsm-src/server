package cz.iocb.load.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;



/**
 * Counts the entities that get their rows only because they are referenced, not described by the data. While the
 * triples that describe the entities are being read, a newly referenced entity is only remembered, because its
 * description may still follow; {@link #settle()} then counts the ones that remained undescribed, and an entity
 * referenced afterwards is counted at once. {@link #printSummary()} prints the counts of all kinds.
 *
 * @param <K> the type of the keys of the entities
 */
public class MissingEntities<K>
{
    /**
     * All the created counters, whose counts the summary prints.
     */
    private static final List<MissingEntities<?>> instances = new ArrayList<>();

    /**
     * Name of the kind of the entities, shown in the summary.
     */
    private final String name;

    /**
     * Referenced entities whose description may still follow, or null once the counting is not deferred.
     */
    private HashSet<K> pending;

    /**
     * Number of the counted missing entities.
     */
    private int count;


    /**
     * Creates the counter of a kind of entities. The name of the kind of the entities is shown in the summary.
     * Deferred counting is meant for the class that reads the descriptions of the entities, until it calls
     * {@link #settle()}.
     *
     * @param name the name of the kind of the entities
     * @param deferred whether the referenced entities are counted only by {@link #settle()}
     */
    public MissingEntities(String name, boolean deferred)
    {
        this.name = name;
        this.pending = deferred ? new HashSet<>() : null;

        synchronized(instances)
        {
            instances.add(this);
        }
    }


    /**
     * Records an entity that has got its row by a reference.
     *
     * @param key the key of the entity
     */
    public synchronized void referenced(K key)
    {
        if(pending == null)
            count++;
        else
            pending.add(key);
    }


    /**
     * Records an entity described by the data.
     *
     * @param key the key of the entity
     */
    public synchronized void described(K key)
    {
        if(pending != null)
            pending.remove(key);
    }


    /**
     * Counts the entities referenced but not described so far; the entities referenced later are counted at once.
     */
    public synchronized void settle()
    {
        if(pending == null)
            return;

        count += pending.size();
        pending = null;
    }


    /**
     * Returns the number of the missing entities, including the pending ones.
     *
     * @return the number of the missing entities
     */
    private synchronized int count()
    {
        return pending == null ? count : count + pending.size();
    }


    /**
     * Prints the numbers of the missing entities of each kind.
     */
    public static void printSummary()
    {
        List<MissingEntities<?>> missing = new ArrayList<>();

        synchronized(instances)
        {
            for(MissingEntities<?> entities : instances)
                if(entities.count() > 0)
                    missing.add(entities);
        }

        missing.sort(Comparator.comparing(entities -> entities.name));

        System.out.println(missing.isEmpty() ? "no missing entities added" : "missing entities added:");

        for(MissingEntities<?> entities : missing)
            System.out.println("    " + entities.name + ": " + entities.count());

        System.out.println();
    }
}
