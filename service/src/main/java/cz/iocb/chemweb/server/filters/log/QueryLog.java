package cz.iocb.chemweb.server.filters.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import cz.iocb.sparql.engine.error.TranslateExceptions;
import cz.iocb.sparql.engine.request.LimitExceedException;
import cz.iocb.sparql.engine.translator.ServiceException;



/**
 * Log of the SPARQL queries that have ended by an error of the engine or of the database, that have run out of their
 * time limit, or that have run longer than the time of a slow query (the context parameter
 * {@value #SLOW_TIME_PARAMETER} in seconds, {@value #DEFAULT_SLOW_TIME} by default). Each of the three kinds is written
 * by a logger of its own, named by this class followed by {@code .errors}, {@code .timeouts} or {@code .slow}, which
 * {@code log4j2.xml} directs to a file of its own. A query is one record of several lines: a header with the time the
 * query has ended, the kind, the duration, the address of the client and the endpoint, the details of the request and
 * of its outcome, and the text of the query as it has been sent, followed by the stack trace of an error of the engine.
 */
public class QueryLog
{
    /**
     * Where a query comes from, captured while the HTTP request submitting it is served, as the container reuses the
     * request once it has been answered.
     *
     * @param endpoint the path of the endpoint or of the service evaluating the query
     * @param request the HTTP method, the path and the parameters of the request, without the query
     * @param address the address of the client
     * @param forwardedFor the value of the header X-Forwarded-For, or null
     * @param userAgent the value of the header User-Agent, or null
     * @param referer the value of the header Referer, or null
     */
    public record Origin(String endpoint, String request, String address, String forwardedFor, String userAgent,
            String referer)
    {
        /**
         * Captures where a query comes from.
         *
         * @param req the HTTP request submitting the query
         * @param request the HTTP method, the path and the parameters of the request, without the query
         * @return where the query comes from
         */
        public static Origin of(HttpServletRequest req, String request)
        {
            return new Origin(req.getRequestURI(), request, req.getRemoteAddr(), req.getHeader("X-Forwarded-For"),
                    req.getHeader("User-Agent"), req.getHeader("Referer"));
        }
    }


    /**
     * Kind of a logged query, each written by a logger of its own.
     */
    private enum Kind
    {
        /**
         * The query has ended by an error of the engine or of the database.
         */
        ERROR("errors", Level.ERROR),

        /**
         * The query has run out of its time limit.
         */
        TIMEOUT("timeouts", Level.WARN),

        /**
         * The query has run longer than the time of a slow query and has ended otherwise.
         */
        SLOW("slow", Level.INFO);


        /**
         * Logger of the queries of the kind.
         */
        private final Logger logger;

        /**
         * Level the queries of the kind are logged at.
         */
        private final Level level;


        /**
         * Creates the kind.
         *
         * @param name the last part of the name of the logger
         * @param level the level the queries of the kind are logged at
         */
        Kind(String name, Level level)
        {
            this.logger = LoggerFactory.getLogger(QueryLog.class.getName() + "." + name);
            this.level = level;
        }
    }


    /**
     * Name of the context parameter setting the time of a slow query in seconds.
     */
    public static final String SLOW_TIME_PARAMETER = "slow-query-time";

    /**
     * Default time of a slow query in seconds.
     */
    public static final long DEFAULT_SLOW_TIME = 120;

    /**
     * Format of the times in the records.
     */
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS xxx",
            Locale.ROOT);

    /**
     * Time of a slow query in nanoseconds.
     */
    private final long slowTime;


    /**
     * Creates the log with the time of a slow query given by the context parameter {@value #SLOW_TIME_PARAMETER}.
     *
     * @param context the servlet context
     * @throws ServletException if the parameter is not a whole number of seconds
     */
    public QueryLog(ServletContext context) throws ServletException
    {
        String value = context.getInitParameter(SLOW_TIME_PARAMETER);

        try
        {
            slowTime = (value != null ? Long.parseLong(value.trim()) : DEFAULT_SLOW_TIME) * 1000000000l;
        }
        catch(NumberFormatException e)
        {
            throw new ServletException("invalid value of the context parameter " + SLOW_TIME_PARAMETER, e);
        }
    }


    /**
     * Logs a query that has ended by an error of the engine or of the database, that has run out of its time limit, or
     * that has run longer than the time of a slow query; any other query is left out. An error of the query itself
     * ({@link TranslateExceptions}) or of a remote SERVICE endpoint ({@link ServiceException}) is no error of the
     * engine, and a query its client has cancelled has neither failed nor run out of time.
     *
     * @param origin where the query comes from
     * @param query the text of the query
     * @param duration how long the query has run, in nanoseconds
     * @param status the HTTP status the query has been answered with, or null when it has none
     * @param error the exception the query has ended by, or null
     * @param cancelled whether the client has cancelled the query
     */
    public void log(Origin origin, String query, long duration, Integer status, Throwable error, boolean cancelled)
    {
        Kind kind;

        if(error != null && !cancelled && isTimeout(error))
            kind = Kind.TIMEOUT;
        else if(error != null && !cancelled && isFailure(error))
            kind = Kind.ERROR;
        else if(duration > slowTime)
            kind = Kind.SLOW;
        else
            return;

        if(!kind.logger.isEnabledForLevel(kind.level))
            return;


        ZonedDateTime ended = ZonedDateTime.now();
        StringBuilder record = new StringBuilder();

        record.append(String.format(Locale.ROOT, "==== %s | %s | %.3f s | %s | %s\n", timeFormat.format(ended),
                kind.name().toLowerCase(Locale.ROOT), duration / 1e9, origin.address(), origin.endpoint()));

        appendField(record, "started", timeFormat.format(ended.minusNanos(duration)));
        appendField(record, "forwarded", origin.forwardedFor());
        appendField(record, "user-agent", origin.userAgent());
        appendField(record, "referer", origin.referer());
        appendField(record, "request", origin.request());
        appendField(record, "status", status != null ? status.toString() : null);
        appendField(record, "result", cancelled ? "cancelled by the client" :
                error == null ? "completed" : isTimeout(error) ? "timed out" : "failed");
        appendField(record, "error", error != null ? error.toString() : null);

        // the line breaks of a query sent by a form are CR LF
        record.append("query:\n").append(query.replace("\r\n", "\n").replace('\r', '\n').stripTrailing());

        // the message of the database tells its error, whereas an error of the engine is found by its stack trace
        if(kind == Kind.ERROR && !(error instanceof SQLException) && !(error instanceof LimitExceedException))
        {
            StringWriter trace = new StringWriter();
            error.printStackTrace(new PrintWriter(trace));
            record.append("\nstack trace:\n").append(trace.toString().stripTrailing());
        }

        kind.logger.atLevel(kind.level).log("{}", record);
    }


    /**
     * Appends a field of the record as a line of its own, unless its value is null.
     *
     * @param record the record
     * @param name the name of the field
     * @param value the value of the field, or null
     */
    private static void appendField(StringBuilder record, String name, String value)
    {
        if(value != null)
            record.append(String.format(Locale.ROOT, "%-11s %s\n", name + ":", value));
    }


    /**
     * Tells whether the query has run out of its time limit (the SQL state 57014 of a cancelled statement, which the
     * engine gives to a request whose time has run out).
     *
     * @param error the exception the query has ended by
     * @return whether the query has run out of its time limit
     */
    private static boolean isTimeout(Throwable error)
    {
        return error instanceof SQLException e && e.getErrorCode() == 0 && "57014".equals(e.getSQLState());
    }


    /**
     * Tells whether the query has ended by an error of the engine or of the database, i.e. by neither an error of the
     * query itself nor an error of a remote SERVICE endpoint.
     *
     * @param error the exception the query has ended by
     * @return whether the query has ended by an error of the engine or of the database
     */
    private static boolean isFailure(Throwable error)
    {
        return !(error instanceof TranslateExceptions) && !(error instanceof ServiceException);
    }
}
