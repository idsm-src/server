package cz.iocb.chemweb.server.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.servlet.http.HttpSession;



public class SessionData<Data>
{
    private final Random random = new Random();
    private final String storageName;


    public SessionData(String name)
    {
        storageName = name;
    }


    public long insert(HttpSession session, Data data)
    {
        synchronized(session)
        {
            @SuppressWarnings("unchecked")
            Map<Long, Data> sessionStorage = (Map<Long, Data>) session.getAttribute(storageName);

            if(sessionStorage == null)
            {
                sessionStorage = new HashMap<Long, Data>();
                session.setAttribute(storageName, sessionStorage);
            }

            long id = ((long) random.nextInt() << 32) + sessionStorage.size();
            sessionStorage.put(id, data);

            return id;
        }
    }


    public Data get(HttpSession session, long id)
    {
        synchronized(session)
        {
            @SuppressWarnings("unchecked")
            Map<Long, Data> sessionStorage = (Map<Long, Data>) session.getAttribute(storageName);

            if(sessionStorage == null)
                return null;


            Data ret = sessionStorage.get(id);

            return ret;
        }
    }


    public void remove(HttpSession session, long id)
    {
        synchronized(session)
        {
            @SuppressWarnings("unchecked")
            Map<Long, Data> sessionStorage = (Map<Long, Data>) session.getAttribute(storageName);

            if(sessionStorage == null)
                return;


            sessionStorage.remove(id);
        }
    }


    public Data getAndRemove(HttpSession session, long id)
    {
        synchronized(session)
        {
            @SuppressWarnings("unchecked")
            Map<Long, Data> sessionStorage = (Map<Long, Data>) session.getAttribute(storageName);

            if(sessionStorage == null)
                return null;


            Data ret = sessionStorage.get(id);
            sessionStorage.remove(id);

            return ret;
        }
    }
}
