package cz.iocb.chemweb.shared.services.info;

import java.util.List;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.iocb.chemweb.shared.services.DatabaseException;



/**
 * Service providing the information shown on the about page.
 */
@RemoteServiceRelativePath("info")
public interface InfoService extends RemoteService
{
    /**
     * Returns the statistics of the database (the table {@code idsm.stats}).
     *
     * @return the rows of the statistics in their order
     * @throws DatabaseException if a database error occurs
     */
    List<CountItem> getCounts() throws DatabaseException;

    /**
     * Returns the data sources of the database (the table {@code idsm.sources}).
     *
     * @return the data sources in their order
     * @throws DatabaseException if a database error occurs
     */
    List<SourceItem> getSources() throws DatabaseException;
}
