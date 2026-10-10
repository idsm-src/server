package cz.iocb.load.common;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Locale.ROOT;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



/*
 * Copies the standard and the error output of a loader into a log file while they still go to the console. The file
 * is named by the start time of the loader, e.g. 2026-10-10_15-30-00.log, and it gets the output as it is written,
 * so that it can be followed during the load and it is complete even if the loader exits abruptly.
 */
public class Log
{
    private static final DateTimeFormatter nameFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", ROOT);


    /*
     * Writes the output to the console and to the log, which the standard and the error output share; the log is
     * locked for each write, so that it gets the output in the order in which it is written.
     */
    private static class Tee extends OutputStream
    {
        private final OutputStream console;
        private final OutputStream log;


        Tee(OutputStream console, OutputStream log)
        {
            this.console = console;
            this.log = log;
        }


        @Override
        public void write(int b) throws IOException
        {
            console.write(b);

            synchronized(log)
            {
                log.write(b);
            }
        }


        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException
        {
            console.write(bytes, offset, length);

            synchronized(log)
            {
                log.write(bytes, offset, length);
            }
        }


        @Override
        public void flush() throws IOException
        {
            console.flush();

            synchronized(log)
            {
                log.flush();
            }
        }
    }


    /*
     * Starts to copy the output into a new log file in the directory, which is created if it does not exist.
     */
    public static void start(Path directory) throws IOException
    {
        Files.createDirectories(directory);

        Path file = directory.resolve(LocalDateTime.now().format(nameFormat) + ".log");
        OutputStream log = Files.newOutputStream(file, CREATE, APPEND);

        System.setOut(new PrintStream(new Tee(System.out, log), true, System.out.charset()));
        System.setErr(new PrintStream(new Tee(System.err, log), true, System.err.charset()));
    }
}
