package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
     * Shows a message describing the failure of a call of the server, with the action that failed in the caption. The
     * texts of the failure are shown as they are, not interpreted as HTML.
     *
     * @param context the action that failed
     * @param error the failure
     */
    public static void show(Context context, Throwable error)
    {
        String caption = switch(context)
        {
            case QUERY -> "Query Evaluation Error";
            case DETAILS -> "Details Loading Error";
            case PROPERTIES -> "Properties Loading Error";
        };

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
            message = "The unexpected exception \"" + error.getClass().getSimpleName() + "\" has been caught: "
                    + error.getMessage();
        }

        // the message is plain text quoting the server, which may quote the query, so it must not be read as HTML
        final MessageDialog dialog = new MessageDialog(caption, SafeHtmlUtils.htmlEscape(String.valueOf(message)));

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
