package cz.iocb.chemweb.server.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import jakarta.servlet.http.HttpSession;



/**
 * Storage of objects in HTTP sessions under generated ids, used to keep the state of a query between the calls of a
 * service. The objects of one storage are kept in a map held by a session attribute named after the storage.
 *
 * @param <Data> the type of the stored objects
 */
public class SessionData<Data>
{
    /**
     * Source of the random part of the ids.
     */
    private final Random random = new Random();

    /**
     * Name of the session attribute holding the objects.
     */
    private final String storageName;


    /**
     * Creates the storage.
     *
     * @param name the name of the session attribute holding the objects
     */
    public SessionData(String name)
    {
        storageName = name;
    }


    /**
     * Stores an object in a session.
     *
     * @param session the session
     * @param data the object to store
     * @return the id of the stored object
     */
    public long insert(HttpSession session, Data data)
    {
        synchronized(session)
        {
            @SuppressWarnings("unchecked")
            Map<Long, Data> sessionStorage = (Map<Long, Data>) session.getAttribute(storageName);

            if(sessionStorage == null)
            {
                sessionStorage = new HashMap<>();
                session.setAttribute(storageName, sessionStorage);
            }

            long id = ((long) random.nextInt() << 32) + sessionStorage.size();
            sessionStorage.put(id, data);

            return id;
        }
    }


    /**
     * Returns an object stored in a session.
     *
     * @param session the session
     * @param id the id of the object
     * @return the object, or null if the session holds no object of the id
     */
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


    /**
     * Removes an object from a session.
     *
     * @param session the session
     * @param id the id of the object
     */
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


    /**
     * Removes an object from a session and returns it.
     *
     * @param session the session
     * @param id the id of the object
     * @return the removed object, or null if the session held no object of the id
     */
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
