package cz.iocb.load.isdb;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.real;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static java.util.Locale.ROOT;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.EntityTable.Key;
import cz.iocb.load.common.MgfReader;
import cz.iocb.load.common.MgfReader.Record;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.SpectrumLiteral;
import cz.iocb.load.common.Updater;



/*
 * Loads the In Silico Spectral Database (ISDB) from the MGF files of its summed spectra: a compound is identified by
 * its name (usually the first block of its InChIKey) and has one spectrum per polarity.
 */
public final class ISDB extends Updater
{
    private static final Pattern accessionPattern = Pattern.compile("[A-Z]{14}");
    private static final Pattern inchikeyPattern = Pattern.compile("[A-Z]{14}-[A-Z]{10}-[A-Z]");

    private static final Set<String> knownParameters = Set.of("CHARGE", "IONMODE", "COLLISION_ENERGY", "ADDUCT",
            "FRAGMENTATION_MODE", "MOLECULAR_FORMULA", "SMILES", "INCHI", "INCHIKEY", "SCANS", "NUM_PEAKS",
            "CONFIDENCE", "PARENT_MASS", "COMPOUND_NAME", "PRECURSOR_MZ");

    /*
     * The parent mass of a compound is derived from the precursor m/z of each polarity, so the two values differ in
     * their last digits; the first of them is kept.
     */
    private static final float massTolerance = 0.001f;

    private static final Key<Pair<Integer, String>> spectrumKey = new Key<>("id", "ionmode")
    {
        @Override
        protected Pair<Integer, String> read(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1), result.getString(2));
        }

        @Override
        protected void write(PreparedStatement statement, Pair<Integer, String> key) throws SQLException
        {
            statement.setInt(1, key.getOne());
            statement.setString(2, key.getTwo());
        }
    };

    private static final EntityTable<Integer> compounds = new EntityTable<>("isdb.compound_bases", intKey("id"), null,
            uniqueVarchar("accession"), uniqueVarchar("inchikey"),
            real("exact_mass").comparedBy((a, b) -> a != null && b != null
                    && Math.abs((Float) a - (Float) b) <= massTolerance),
            varchar("formula"), uniqueVarchar("smiles"), uniqueVarchar("inchi"));
    private static final EntityTable<Pair<Integer, String>> spectra = new EntityTable<>("isdb.spectrum_bases",
            spectrumKey, null, real("pepmass"), SpectrumLiteral.column("spectrum"));
    private static final StringIntMap compoundIDs = new StringIntMap();
    private static final HashSet<String> unknownParameters = new HashSet<>();
    private static int nextCompoundID;


    private static IOException error(String file, Record record, String message)
    {
        return new IOException(file + ":" + record.line() + ": " + message);
    }


    /*
     * Returns the value of a required parameter, which must not contain white space.
     */
    private static String parameter(String file, Record record, String name) throws IOException
    {
        String value = record.parameters().get(name);

        if(value == null || value.isEmpty())
            throw error(file, record, "missing " + name);

        if(value.chars().anyMatch(Character::isWhitespace))
            throw error(file, record, "unexpected " + name + " '" + value + "'");

        return value;
    }


    private static void check(String file, Record record, String name, String expected) throws IOException
    {
        String value = record.parameters().get(name);

        if(!expected.equals(value))
            throw error(file, record, "unexpected " + name + " '" + value + "'");
    }


    private static float number(String file, Record record, String name) throws IOException
    {
        try
        {
            return Float.parseFloat(parameter(file, record, name));
        }
        catch(NumberFormatException e)
        {
            throw error(file, record, "unexpected " + name + " '" + record.parameters().get(name) + "'");
        }
    }


    private static synchronized int getCompoundID(String accession)
    {
        Integer id = compoundIDs.get(accession);

        if(id == null)
            compoundIDs.put(accession, id = nextCompoundID++);

        return id;
    }


    private static synchronized void checkParameters(Record record)
    {
        for(String name : record.parameters().keySet())
            if(!knownParameters.contains(name) && unknownParameters.add(name))
                System.err.println("warning: unknown parameter " + name);
    }


    private static void loadFile(String file) throws IOException, SQLException
    {
        System.out.println("  load " + file);

        try(MgfReader reader = new MgfReader(file, new BufferedReader(
                new InputStreamReader(new FileInputStream(baseDirectory + file), StandardCharsets.UTF_8), 1 << 16)))
        {
            Record record;

            while((record = reader.next()) != null)
            {
                checkParameters(record);

                String ionmode = switch(parameter(file, record, "IONMODE").toLowerCase(ROOT))
                {
                    case "negative" -> "N";
                    case "positive" -> "P";
                    default -> throw error(file, record,
                            "unexpected IONMODE '" + record.parameters().get("IONMODE") + "'");
                };

                boolean negative = ionmode.equals("N");

                check(file, record, "CHARGE", negative ? "1-" : "1+");
                check(file, record, "ADDUCT", negative ? "[M-H]-" : "[M+H]+");
                check(file, record, "COLLISION_ENERGY", "energySum");
                check(file, record, "FRAGMENTATION_MODE", "in silico");

                String accession = parameter(file, record, "COMPOUND_NAME");

                if(!accessionPattern.matcher(accession).matches())
                    throw error(file, record, "unexpected COMPOUND_NAME '" + accession + "'");

                String inchikey = parameter(file, record, "INCHIKEY");

                if(!inchikeyPattern.matcher(inchikey).matches())
                    throw error(file, record, "unexpected INCHIKEY '" + inchikey + "'");

                String inchi = parameter(file, record, "INCHI");

                if(!inchi.startsWith("InChI="))
                    throw error(file, record, "unexpected INCHI '" + inchi + "'");

                String formula = parameter(file, record, "MOLECULAR_FORMULA");
                String smiles = parameter(file, record, "SMILES");
                float exactMass = number(file, record, "PARENT_MASS");
                float pepmass = number(file, record, "PRECURSOR_MZ");

                if(!String.valueOf(record.peaks()).equals(record.parameters().get("NUM_PEAKS")))
                    throw error(file, record, "unexpected NUM_PEAKS '" + record.parameters().get("NUM_PEAKS") + "'");


                int id = getCompoundID(accession);

                compounds.set(id, "accession", accession);
                compounds.set(id, "inchikey", inchikey);
                compounds.set(id, "exact_mass", exactMass);
                compounds.set(id, "formula", formula);
                compounds.set(id, "smiles", smiles);
                compounds.set(id, "inchi", inchi);


                Pair<Integer, String> spectrum = Pair.getPair(id, ionmode);

                spectra.set(spectrum, "pepmass", pepmass);
                spectra.set(spectrum, "spectrum", record.spectrum());
            }
        }
    }


    /*
     * Loads the files in the order of their names, so that the negative file precedes the positive one and the parent
     * masses of the negative polarity are the ones stored.
     */
    private static void loadFiles() throws IOException, SQLException
    {
        load("select accession,id from isdb.compound_bases", compoundIDs);

        nextCompoundID = compoundIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        String[] files = new File(baseDirectory + "isdb").list((dir, file) -> file.matches(".*\\.mgf"));

        if(files == null || files.length == 0)
            throw new IOException("no MGF file in isdb");

        Arrays.sort(files);

        for(String file : files)
            loadFile("isdb/" + file);

        if(spectra.size() == 0)
            throw new IOException("no spectrum in isdb");

        compounds.store();
        spectra.store();
    }


    public static void main(String[] args) throws Exception
    {
        try
        {
            init();

            String version = getReader("isdb/version.txt.gz").readLine();
            System.out.println("=== load isdb version " + version + " ===");
            System.out.println();

            loadFiles();

            syncIndex("isdb", true);

            try(Statement statement = connection.createStatement())
            {
                statement.execute("refresh materialized view isdb.compound_pubchem_compounds");
                statement.execute("refresh materialized view isdb.compound_wikidata_compounds");

                try(ResultSet result = statement.executeQuery("select count(*) from isdb.spectrum_bases"))
                {
                    if(result.next())
                        setCount("ISDB Mass Spectra", result.getInt(1));
                }
            }

            setVersion("In Silico Spectral Database (ISDB)", version);

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
