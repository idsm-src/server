package cz.iocb.chemweb.shared.services.info;

import java.util.List;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.iocb.chemweb.shared.services.DatabaseException;



@RemoteServiceRelativePath("info")
public interface InfoService extends RemoteService
{
    List<CountItem> getCounts() throws DatabaseException;

    List<SourceItem> getSources() throws DatabaseException;
}
