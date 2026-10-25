package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.StatusCodeException;
import cz.iocb.chemweb.client.widgets.dialog.MessageDialog;
import cz.iocb.chemweb.shared.services.DatabaseException;
import cz.iocb.chemweb.shared.services.SessionException;



/**
 * Shows the failures of the calls of the server in a message dialog.
 */
public class ErrorMessage
{
    /**
     * Action that failed.
     */
    public static enum Context
    {
        /**
         * Evaluation of a query.
         */
        QUERY,

        /**
         * Cancellation of a query.
         */
        CANCEL,

        /**
         * Loading of the details of a resource.
         */
        DETAILS,

        /**
         * Loading of the properties of a resource.
         */
        PROPERTIES
    }


    /**
     * Not instantiable.
     */
    private ErrorMessage()
    {
    }


    /**
     * Shows a message describing the failure of a call of the server.
     *
     * @param context the action that failed (not distinguished in the message yet)
     * @param error the failure
     */
    public static void show(Context context, Throwable error)
    {
        String message = null;

        if(error instanceof StatusCodeException)
        {
            message = "The connection with the server has failed.";
        }
        else if(error instanceof SessionException)
        {
            message = error.getMessage();
        }
        else if(error instanceof DatabaseException)
        {
            message = "The database server has returned the following error: " + error.getMessage();
        }
        else
        {
            message = "The unexpected exception \"" + error.getClass().getSimpleName() + "\" has been catched: "
                    + error.getMessage();
        }

        final MessageDialog dialog = new MessageDialog("Error Message", message);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand()
        {
            @Override
            public void execute()
            {
                dialog.center();
            }
        });
    }
}
