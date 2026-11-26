package cz.iocb.load.stats;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration;
import cz.iocb.load.common.Updater;
import cz.iocb.sparql.engine.config.ExtendedStatistics;
import cz.iocb.sparql.engine.config.ExtendedStatistics.Definition;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Generator of the extended statistics of the database that the SPARQL engine needs for the tables of the IDSM mapping
 * (see {@link ExtendedStatistics}): the statistics of the columns holding a term of the mapping together, like the
 * parts of an IRI, by which the planner of the database estimates the rows of a constant term, the groups and the
 * lookups, and the join optimizer of the engine the joins, and the function through which the join optimizer reads them
 * for the endpoint, whose role {@code sparql} does not own the tables ({@code idsm.extended_statistics}, see
 * {@link IdsmConfiguration}), granted to that role. In one transaction, the missing statistics are made, those made
 * before that the mapping no longer needs are dropped and the tables whose statistics have not been computed yet are
 * analysed. It is run after the loads and again whenever the mapping changes; when only the data change, the database
 * computes the statistics anew as it analyses the tables. With the argument {@code --script}, the psql script doing the
 * same is printed instead.
 */
public class GenerateExtendedStatistics extends Updater
{
    /**
     * The role of the SPARQL endpoint, to which the function reading the statistics is granted.
     */
    private static final String endpointRole = "sparql";


    /**
     * Not instantiable.
     */
    private GenerateExtendedStatistics()
    {
    }


    /**
     * Makes the extended statistics of the database, or prints the psql script making them; the connection to the
     * database is read from the file {@code datasource.properties}, whose user has to own the tables.
     *
     * @param args the command-line arguments: none, or {@code --script} to print the script instead
     * @throws SQLException if a database error occurs
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            boolean script = args.length == 1 && args[0].equals("--script");

            if(args.length > 0 && !script)
                throw new IllegalArgumentException("unknown arguments: " + String.join(" ", args));

            init("extended-statistics");

            DataSource dataSource = getDataSource();
            IdsmConfiguration config = new IdsmConfiguration(null, dataSource, new DatabaseSchema(dataSource));
            ExtendedStatistics statistics = new ExtendedStatistics(config, List.of(endpointRole));

            if(script)
            {
                System.out.print(statistics.getScript());
                rollback();
                return;
            }

            System.out.println("=== extended statistics ===");

            for(Definition definition : statistics.getDefinitions())
                System.out.println(definition.table() + " (" + String.join(", ", definition.columns()) + ")");

            System.out.println();

            statistics.apply(connection);

            checkProblems();
            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
