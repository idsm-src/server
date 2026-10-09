package cz.iocb.load.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;



/*
 * Reads the records of an SDF file and passes the identifier and the molfile of each of them on.
 */
public class SdfReader
{
    public static interface RecordHandler
    {
        void record(String id, String molfile) throws IOException, SQLException;
    }


    /*
     * Reads the records of the given file. The identifier of a record is the value of the given data item; the
     * molfile is passed with the identifier in place of its name line, so that the stored structures are named by
     * their identifiers. A record has to carry the data item exactly once and has to be terminated, otherwise the
     * file is refused; a blank line left at the end of the file is tolerated.
     */
    public static void read(String name, BufferedReader reader, String idTag, RecordHandler handler)
            throws IOException, SQLException
    {
        String tag = "> <" + idTag + ">";
        String line;
        int record = 0;

        while((line = reader.readLine()) != null)
        {
            String nameLine = line;
            record++;

            line = reader.readLine();

            if(line == null && nameLine.isBlank())
                break;

            StringBuilder molfile = new StringBuilder();
            String id = null;

            while(line != null && !line.startsWith(">") && !line.equals("$$$$"))
            {
                molfile.append(line).append('\n');
                line = reader.readLine();
            }

            while(line != null && !line.equals("$$$$"))
            {
                if(line.equals(tag))
                {
                    line = reader.readLine();

                    if(line == null)
                        break;

                    if(id != null)
                        throw new IOException(name + ": record " + record + " has more than one " + idTag);

                    id = line.trim();
                }

                line = reader.readLine();
            }

            if(line == null)
                throw new IOException(name + ": record " + record + " is not terminated");

            if(id == null)
                throw new IOException(name + ": record " + record + " has no " + idTag);

            handler.record(id, id + "\n" + molfile);
        }
    }
}
