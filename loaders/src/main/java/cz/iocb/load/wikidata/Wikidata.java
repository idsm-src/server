package cz.iocb.load.wikidata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.Updater;



/**
 * Loader of the Wikidata compounds from the TSV results of the queries of their isomeric SMILES, canonical SMILES and
 * InChI into the tables of the schema {@code wikidata}, which also gets the structures of the compounds derived from
 * the SMILES.
 */
public class Wikidata extends Updater
{
    /**
     * The pattern of the IRI of a Wikidata entity in a query result, capturing its number.
     */
    private static final Pattern entityPattern = Pattern.compile("^<http://www\\.wikidata\\.org/entity/Q([0-9]+)>$");

    /**
     * The pattern of the skolem IRI of an unknown value in a query result.
     */
    private static final Pattern unknownValuePattern = Pattern
            .compile("^<http://www\\.wikidata\\.org/\\.well-known/genid/[0-9a-f]+>$");


    /**
     * Not instantiable.
     */
    private Wikidata()
    {
    }


    /**
     * Returns the value of a string literal of a TSV query result, i.e. with the escape sequences decoded.
     *
     * @param literal the literal in the TSV form, quoted
     * @return the value of the literal
     * @throws DataException if the literal is malformed
     */
    private static String decode(String literal) throws DataException
    {
        if(literal.length() < 2 || !literal.startsWith("\"") || !literal.endsWith("\""))
            throw new DataException("unexpected literal", literal);

        StringBuilder value = new StringBuilder();

        for(int i = 1; i < literal.length() - 1; i++)
        {
            char c = literal.charAt(i);

            if(c != '\\')
            {
                value.append(c);
                continue;
            }

            if(++i == literal.length() - 1)
                throw new DataException("unexpected literal", literal);

            switch(literal.charAt(i))
            {
                case 't' -> value.append('\t');
                case 'b' -> value.append('\b');
                case 'n' -> value.append('\n');
                case 'r' -> value.append('\r');
                case 'f' -> value.append('\f');
                case '"' -> value.append('"');
                case '\'' -> value.append('\'');
                case '\\' -> value.append('\\');
                case 'u' ->
                {
                    if(i + 5 >= literal.length())
                        throw new DataException("unexpected literal", literal);

                    value.appendCodePoint(Integer.parseInt(literal.substring(i + 1, i + 5), 16));
                    i += 4;
                }
                case 'U' ->
                {
                    if(i + 9 >= literal.length())
                        throw new DataException("unexpected literal", literal);

                    int codePoint = Integer.parseInt(literal.substring(i + 1, i + 9), 16);

                    if(!Character.isValidCodePoint(codePoint))
                        throw new DataException("unexpected literal", literal);

                    value.appendCodePoint(codePoint);
                    i += 8;
                }
                default -> throw new DataException("unexpected literal", literal);
            }
        }

        return value.toString();
    }


    /**
     * Loads the values of a property from the TSV result of its query into the given table; an unexpected line is
     * reported as an error and skipped. A statement of an unknown value, which the query service gives as a skolem
     * IRI, has no value to load and is skipped silently.
     *
     * @param file the name of the TSV file in the directory {@code wikidata}
     * @param table the name of the table in the schema {@code wikidata}
     * @param column the name of the value column of the table, which is also the name of the value variable of the
     *            query
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadValues(String file, String table, String column) throws IOException, SQLException
    {
        IntStringSet oldValues = new IntStringSet();
        IntStringSet newValues = new IntStringSet();

        load("select compound, " + column + " from wikidata." + table, oldValues);

        System.out.println("  load wikidata/" + file);

        InputStream input = openFile("wikidata/" + file);

        if(input == null)
            return;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)))
        {
            String line = reader.readLine();

            if(!("?entity\t?" + column).equals(line))
            {
                Problems.error("unexpected header", "wikidata/" + file + ": " + line);
                return;
            }

            int values = 0;

            while((line = reader.readLine()) != null)
            {
                try
                {
                    String[] items = line.split("\t", 2);
                    Matcher matcher = entityPattern.matcher(items[0]);

                    if(items.length != 2 || !matcher.matches())
                        throw new DataException("unexpected line", line);

                    if(unknownValuePattern.matcher(items[1]).matches())
                        continue;

                    Pair<Integer, String> pair = Pair.getPair(Integer.valueOf(matcher.group(1)), decode(items[1]));

                    if(!oldValues.remove(pair))
                        newValues.add(pair);

                    values++;
                }
                catch(DataException e)
                {
                    Problems.error(e.getKind(), "wikidata/" + file + ": " + e.getDetail());
                }
                catch(NumberFormatException e)
                {
                    Problems.error("malformed number", "wikidata/" + file + ": " + line);
                }
            }

            if(values == 0)
            {
                Problems.error("no value", "wikidata/" + file);
                return;
            }
        }

        store("delete from wikidata." + table + " where compound=? and " + column + "=?", oldValues);
        store("insert into wikidata." + table + "(compound," + column + ") values(?,?)", newValues);
    }


    /**
     * Sets the structures of the compounds to their SMILES, the isomeric one if available.
     *
     * @throws SQLException if a database error occurs
     */
    private static void loadStructures() throws SQLException
    {
        System.out.println("load structures ...");

        try(Statement statement = connection.createStatement())
        {
            statement.execute("""
                    delete from wikidata.compound_structures where \
                    not exists (select 1 from wikidata.compound_isomeric_smileses \
                    where compound = compound_structures.compound) and \
                    not exists (select 1 from wikidata.compound_canonical_smileses \
                    where compound = compound_structures.compound)""");

            statement.execute("""
                    insert into wikidata.compound_structures select distinct on (compound) compound, smiles from (\
                    select compound, smiles, 1 as v from wikidata.compound_isomeric_smileses union \
                    select compound, smiles, 2 as v from wikidata.compound_canonical_smileses) \
                    order by compound, v, smiles \
                    on conflict (compound) do update set smiles=EXCLUDED.smiles \
                    where compound_structures.smiles != EXCLUDED.smiles""");
        }

        System.out.println();
    }


    /**
     * Runs the loader.
     *
     * @param args the command-line arguments (ignored)
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    public static void main(String[] args) throws IOException, SQLException
    {
        try
        {
            init("wikidata");

            String version = getReader("wikidata/version.txt.gz").readLine();
            System.out.println("=== load wikidata version " + version + " ===");
            System.out.println();

            System.out.println("load compounds ...");
            loadValues("isomeric_smiles.tsv", "compound_isomeric_smileses", "smiles");
            loadValues("canonical_smiles.tsv", "compound_canonical_smileses", "smiles");
            loadValues("inchis.tsv", "compound_inchis", "inchi");
            System.out.println();

            loadStructures();

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement.executeQuery("select count(*) from wikidata.compound_structures"))
                {
                    if(result.next())
                        setCount("Wikidata Chemical Entities", result.getInt(1));
                }
            }

            setVersion("Wikidata Compounds", version);

            updateVersion();

            checkFiles("wikidata");
            checkProblems();

            syncIndex("wikidata", true);
            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
