package cz.iocb.load.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;



/**
 * A table of the structures (molfiles or SMILES) of entities with integer identifiers, on which a Sachem index is
 * built. The table is loaded from a complete dump of the structures: the structure of every entity of the dump is
 * put, the rows whose structure has not changed are left untouched (so that the audit trigger of the index records
 * only real changes), and the rows of the entities missing from the dump are deleted at the end. The structures are
 * compared by the hash of their text without the first two lines, because the name line of a molfile holds the
 * identifier and the header line a time stamp that some sources renew with every dump. Only the identifiers and the
 * hashes of the stored rows are kept in memory, the structures are streamed, and they can be put from several
 * threads at once.
 */
public class StructureTable extends Updater
{
    /**
     * The rows one thread has put and not yet sent.
     */
    private static final class Batch
    {
        /**
         * Identifiers of the entities of the rows.
         */
        final List<Integer> ids = new ArrayList<>(structureBatchSize);

        /**
         * Structures of the rows, in the order of their identifiers.
         */
        final List<String> structures = new ArrayList<>(structureBatchSize);


        /**
         * Creates an empty batch.
         */
        private Batch()
        {
        }
    }


    /**
     * MD5 digests of the threads.
     */
    private static final ThreadLocal<MessageDigest> digests = ThreadLocal.withInitial(() -> {
        try
        {
            return MessageDigest.getInstance("MD5");
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    });


    /**
     * Number of the rows a thread collects before it sends them.
     */
    private static final int structureBatchSize = 1000;

    /**
     * Qualified name of the table.
     */
    private final String table;

    /**
     * Name of the identifier column.
     */
    private final String idColumn;

    /**
     * Name of the structure column.
     */
    private final String structureColumn;

    /**
     * Batch of the current thread.
     */
    private final ThreadLocal<Batch> batches = ThreadLocal.withInitial(this::createBatch);

    /**
     * Batches of all the threads, flushed by {@link #store()}.
     */
    private final ConcurrentLinkedQueue<Batch> allBatches = new ConcurrentLinkedQueue<>();

    /**
     * Identifiers of the stored rows.
     */
    private final BitSet stored = new BitSet();

    /**
     * Identifiers of the entities whose structures have been put.
     */
    private final BitSet seen = new BitSet();

    /**
     * Hashes of the structures of the stored rows by their identifiers.
     */
    private long[] hashes = new long[0];

    /**
     * Statement inserting or updating a row, null until {@link #load()}.
     */
    private PreparedStatement upsert;

    /**
     * Number of the structures that have been put.
     */
    private int count;

    /**
     * Number of the rows sent to the table.
     */
    private int sent;

    /**
     * Whether the table has been stored and can no longer be used.
     */
    private boolean finished;


    /**
     * Creates the table.
     *
     * @param table the qualified name of the table
     * @param idColumn the name of the identifier column
     * @param structureColumn the name of the structure column
     */
    public StructureTable(String table, String idColumn, String structureColumn)
    {
        this.table = table;
        this.idColumn = idColumn;
        this.structureColumn = structureColumn;
    }


    /**
     * Reads the identifiers and the hashes of the stored rows and prepares the upsert.
     *
     * @throws SQLException if a database error occurs
     */
    public void load() throws SQLException
    {
        long time = System.currentTimeMillis();
        String query = "select " + idColumn + ", md5(regexp_replace(" + structureColumn
                + ", E'^[^\\\\n]*\\\\n[^\\\\n]*\\\\n', '')) from " + table;

        System.out.print("  " + query);

        try(PreparedStatement statement = connection.prepareStatement("select max(" + idColumn + ") from " + table))
        {
            try(ResultSet result = statement.executeQuery())
            {
                result.next();
                hashes = new long[result.getInt(1) + 1];
            }
        }

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(ResultSet result = statement.executeQuery())
            {
                while(result.next())
                {
                    int id = result.getInt(1);
                    stored.set(id);
                    hashes[id] = Long.parseUnsignedLong(result.getString(2).substring(0, 16), 16);
                }
            }
        }

        System.out.println(" -> count: " + stored.cardinality() + " / time: "
                + ((System.currentTimeMillis() - time) / 6000 / 10.0));

        upsert = connection.prepareStatement(
                "insert into " + table + "(" + idColumn + "," + structureColumn + ") values(?,?) on conflict("
                        + idColumn + ") do update set " + structureColumn + "=EXCLUDED." + structureColumn);
    }


    /**
     * Puts the structure of an entity: a new or changed structure is sent to the table, an unchanged one is only
     * noted. The structure of an entity can be put only once.
     *
     * @param id the identifier of the entity
     * @param structure the structure of the entity
     * @throws IOException if the identifier is negative or the structure of the entity has already been put (a
     *             {@link DataException})
     * @throws SQLException if a database error occurs
     */
    public void put(int id, String structure) throws IOException, SQLException
    {
        if(upsert == null || finished)
            throw new IllegalStateException(table + " is not open");

        if(id < 0)
            throw new DataException("negative identifier", String.valueOf(id));

        long hash = hash(structure);

        synchronized(this)
        {
            if(seen.get(id))
                throw new DataException("duplicate structure", String.valueOf(id));

            seen.set(id);
            count++;
        }

        if(stored.get(id) && hashes[id] == hash)
            return;

        Batch batch = batches.get();
        batch.ids.add(id);
        batch.structures.add(structure);

        if(batch.ids.size() == structureBatchSize)
            flush(batch);
    }


    /**
     * Sends the remaining rows and deletes the rows of the entities whose structures have not been put; the table
     * cannot be used afterwards. A dump without any structure is reported as an error and the table is left as it is,
     * so that a missing dump does not empty it.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    public synchronized void store() throws IOException, SQLException
    {
        if(upsert == null || finished)
            throw new IllegalStateException(table + " is not open");

        finished = true;

        if(count == 0)
        {
            Problems.error("no structure", table);
            upsert.close();
            return;
        }

        for(Batch batch : allBatches)
            flush(batch);

        upsert.close();

        BitSet added = (BitSet) seen.clone();
        added.andNot(stored);

        BitSet deleted = (BitSet) stored.clone();
        deleted.andNot(seen);

        System.out.println("  insert into " + table + "(" + idColumn + "," + structureColumn + ") -> count: " + sent
                + " (new: " + added.cardinality() + ", changed: " + (sent - added.cardinality()) + ")");

        String command = "delete from " + table + " where " + idColumn + "=?";
        System.out.println("  " + command + " -> count: " + deleted.cardinality());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            int pending = 0;

            for(int id = deleted.nextSetBit(0); id >= 0; id = deleted.nextSetBit(id + 1))
            {
                statement.setInt(1, id);
                statement.addBatch();

                if(++pending == batchSize)
                {
                    statement.executeBatch();
                    pending = 0;
                }
            }

            if(pending > 0)
                statement.executeBatch();
        }
    }


    /**
     * Returns the number of the structures that have been put.
     *
     * @return the number of the structures that have been put
     */
    public synchronized int size()
    {
        return count;
    }


    /**
     * Creates the batch of a thread and registers it for {@link #store()}.
     *
     * @return the new batch
     */
    private Batch createBatch()
    {
        Batch batch = new Batch();
        allBatches.add(batch);
        return batch;
    }


    /**
     * Sends the rows of a batch to the table and empties it.
     *
     * @param batch the batch
     * @throws SQLException if a database error occurs
     */
    private synchronized void flush(Batch batch) throws SQLException
    {
        for(int i = 0; i < batch.ids.size(); i++)
        {
            upsert.setInt(1, batch.ids.get(i));
            upsert.setString(2, batch.structures.get(i));
            upsert.addBatch();
        }

        if(!batch.ids.isEmpty())
            upsert.executeBatch();

        sent += batch.ids.size();
        batch.ids.clear();
        batch.structures.clear();
    }


    /**
     * Returns the hash of a structure without its first two lines, computed the way the database does it.
     *
     * @param structure the structure
     * @return the first 64 bits of the MD5 digest of the structure without its first two lines
     */
    private static long hash(String structure)
    {
        int start = 0;
        int first = structure.indexOf('\n');
        int second = first < 0 ? -1 : structure.indexOf('\n', first + 1);

        if(second >= 0)
            start = second + 1;

        MessageDigest digest = digests.get();
        digest.reset();
        byte[] md5 = digest.digest(structure.substring(start).getBytes(StandardCharsets.UTF_8));

        long hash = 0;

        for(int i = 0; i < 8; i++)
            hash = hash << 8 | (md5[i] & 0xff);

        return hash;
    }
}
