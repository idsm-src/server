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
     * their identifiers. A record has to carry the data item exactly once and has to be terminated; a record that
     * does not, as well as a record the handler refuses by a DataException or a malformed number, is reported as an
     * error and skipped. A blank line left at the end of the file is tolerated.
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
            boolean repeated = false;

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

                    repeated |= id != null;
                    id = line.trim();
                }

                line = reader.readLine();
            }

            if(line == null)
            {
                Problems.error("unterminated SDF record", name + ": record " + record);
                break;
            }

            if(id == null || repeated)
            {
                Problems.error((repeated ? "repeated " : "missing ") + idTag + " of an SDF record",
                        name + ": record " + record);
                continue;
            }

            try
            {
                handler.record(id, id + "\n" + molfile);
            }
            catch(DataException e)
            {
                String detail = e.getDetail() == null ? "" : " (" + e.getDetail() + ")";
                Problems.error(e.getKind(), name + ": record " + record + detail);
            }
            catch(NumberFormatException e)
            {
                Problems.error("malformed number", name + ": record " + record + " (" + e.getMessage() + ")");
            }
        }
    }
}
