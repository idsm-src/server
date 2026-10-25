package cz.iocb.chemweb.shared.services.check;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.iocb.chemweb.shared.services.DatabaseException;



/**
 * Service checking SPARQL queries while they are edited.
 */
@RemoteServiceRelativePath("check")
public interface CheckService extends RemoteService
{
    /**
     * Checks a query for syntax and translation errors and warnings.
     *
     * @param code the text of the query
     * @return the messages of the checker
     * @throws DatabaseException if a database error occurs
     */
    CheckResult check(String code) throws DatabaseException;
}
