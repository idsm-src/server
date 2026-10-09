package cz.iocb.load.wikidata;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Updater;



public class Wikidata extends Updater
{
    private static final Pattern entityPattern = Pattern.compile("^<http://www\\.wikidata\\.org/entity/Q([0-9]+)>$");


    /*
     * Returns the value of a string literal of a TSV query result, i.e. with the escape sequences decoded.
     */
    private static String decode(String literal) throws IOException
    {
        if(literal.length() < 2 || !literal.startsWith("\"") || !literal.endsWith("\""))
            throw new IOException("unexpected literal " + literal);

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
                throw new IOException("unexpected literal " + literal);

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
                        throw new IOException("unexpected literal " + literal);

                    value.appendCodePoint(Integer.parseInt(literal.substring(i + 1, i + 5), 16));
                    i += 4;
                }
                case 'U' ->
                {
                    if(i + 9 >= literal.length())
                        throw new IOException("unexpected literal " + literal);

                    value.appendCodePoint(Integer.parseInt(literal.substring(i + 1, i + 9), 16));
                    i += 8;
                }
                default -> throw new IOException("unexpected literal " + literal);
            }
        }

        return value.toString();
    }


    /*
     * Loads the values of a property from the TSV result of its query into the given table.
     */
    private static void loadValues(String file, String table, String column) throws IOException, SQLException
    {
        IntStringSet oldValues = new IntStringSet();
        IntStringSet newValues = new IntStringSet();

        load("select compound, " + column + " from wikidata." + table, oldValues);

        System.out.println("  load wikidata/" + file);

        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(baseDirectory + "wikidata/" + file), StandardCharsets.UTF_8)))
        {
            String line = reader.readLine();

            if(line == null)
                throw new IOException(file + ": no header");

            while((line = reader.readLine()) != null)
            {
                String[] items = line.split("\t", 2);
                Matcher matcher = entityPattern.matcher(items[0]);

                if(items.length != 2 || !matcher.matches())
                    throw new IOException(file + ": unexpected line " + line);

                Pair<Integer, String> pair = Pair.getPair(Integer.valueOf(matcher.group(1)), decode(items[1]));

                if(!oldValues.remove(pair))
                    newValues.add(pair);
            }
        }

        store("delete from wikidata." + table + " where compound=? and " + column + "=?", oldValues);
        store("insert into wikidata." + table + "(compound," + column + ") values(?,?)", newValues);
    }


    /*
     * Sets the structures of the compounds to their SMILES, the isomeric one if available.
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


    public static void main(String[] args) throws IOException, SQLException
    {
        try
        {
            init();

            String version = getReader("wikidata/version.txt.gz").readLine();
            System.out.println("=== load wikidata version " + version + " ===");
            System.out.println();

            System.out.println("load compounds ...");
            loadValues("isomeric_smiles.tsv", "compound_isomeric_smileses", "smiles");
            loadValues("canonical_smiles.tsv", "compound_canonical_smileses", "smiles");
            loadValues("inchis.tsv", "compound_inchis", "inchi");
            System.out.println();

            loadStructures();

            syncIndex("wikidata", true);

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
            commit();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
