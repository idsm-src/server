package cz.iocb.chemweb.shared.services.check;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.iocb.chemweb.shared.services.DatabaseException;



@RemoteServiceRelativePath("check")
public interface CheckService extends RemoteService
{
    CheckResult check(String code) throws DatabaseException;
}
