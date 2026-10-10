package cz.iocb.load.isdb;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.real;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static java.util.Locale.ROOT;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.regex.Pattern;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.EntityTable.Key;
import cz.iocb.load.common.MgfReader;
import cz.iocb.load.common.MgfReader.Record;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
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

    private static final Key<Pair<Integer, String>> spectrumKey = new Key<>("compound", "ionmode")
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

    private static final EntityTable<Integer> compounds = new EntityTable<>("isdb.compounds", intKey("id"), null,
            uniqueVarchar("accession"), uniqueVarchar("inchikey"),
            real("exact_mass")
                    .comparedBy((a, b) -> a != null && b != null && Math.abs((Float) a - (Float) b) <= massTolerance),
            varchar("formula"), uniqueVarchar("smiles"), uniqueVarchar("inchi"));
    private static final EntityTable<Pair<Integer, String>> spectra = new EntityTable<>("isdb.spectra", spectrumKey,
            null, real("pepmass"), SpectrumLiteral.column("spectrum"));
    private static final StringIntMap compoundIDs = new StringIntMap();
    private static int nextCompoundID;


    /*
     * Returns the exception that refuses a record because of the given parameter, missing or with the given value.
     */
    private static DataException error(String file, Record record, String kind, String value)
    {
        return new DataException(kind, file + ":" + record.line() + (value == null ? "" : " '" + value + "'"));
    }


    /*
     * Returns the value of a required parameter, which must not contain white space.
     */
    private static String parameter(String file, Record record, String name) throws DataException
    {
        String value = record.parameters().get(name);

        if(value == null || value.isEmpty())
            throw error(file, record, "missing " + name, null);

        if(value.chars().anyMatch(Character::isWhitespace))
            throw error(file, record, "unexpected " + name, value);

        return value;
    }


    private static void check(String file, Record record, String name, String expected) throws DataException
    {
        String value = record.parameters().get(name);

        if(value == null)
            throw error(file, record, "missing " + name, null);

        if(!expected.equals(value))
            throw error(file, record, "unexpected " + name, value);
    }


    private static float number(String file, Record record, String name) throws DataException
    {
        String value = parameter(file, record, name);

        try
        {
            return Float.parseFloat(value);
        }
        catch(NumberFormatException e)
        {
            throw error(file, record, "unexpected " + name, value);
        }
    }


    private static synchronized int getCompoundID(String accession)
    {
        Integer id = compoundIDs.get(accession);

        if(id == null)
            compoundIDs.put(accession, id = nextCompoundID++);

        return id;
    }


    /*
     * Reports the parameters of a record that the loader does not know as errors, as their values would be left out.
     */
    private static void checkParameters(String file, Record record)
    {
        for(String name : record.parameters().keySet())
            if(!knownParameters.contains(name))
                Problems.error("unexpected parameter " + name, file + ":" + record.line());
    }


    private static void loadFile(String file) throws IOException, SQLException
    {
        System.out.println("  load " + file);

        InputStream input = openFile(file);

        if(input == null)
            return;

        try(MgfReader reader = new MgfReader(file,
                new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8), 1 << 16)))
        {
            Record record;

            while((record = reader.next()) != null)
            {
                try
                {
                    loadRecord(file, record);
                }
                catch(DataException e)
                {
                    Problems.error(e.getKind(), e.getDetail());
                }
            }
        }
    }


    /*
     * Loads a record of an MGF file, i.e. a compound and its spectrum of the polarity of the record; a record with an
     * unexpected value is refused.
     */
    private static void loadRecord(String file, Record record) throws IOException
    {
        checkParameters(file, record);

        String ionmode = switch(parameter(file, record, "IONMODE").toLowerCase(ROOT))
        {
            case "negative" -> "N";
            case "positive" -> "P";
            default -> throw error(file, record, "unexpected IONMODE", record.parameters().get("IONMODE"));
        };

        boolean negative = ionmode.equals("N");

        check(file, record, "CHARGE", negative ? "1-" : "1+");
        check(file, record, "ADDUCT", negative ? "[M-H]-" : "[M+H]+");
        check(file, record, "COLLISION_ENERGY", "energySum");
        check(file, record, "FRAGMENTATION_MODE", "in silico");

        String accession = parameter(file, record, "COMPOUND_NAME");

        if(!accessionPattern.matcher(accession).matches())
            throw error(file, record, "unexpected COMPOUND_NAME", accession);

        String inchikey = parameter(file, record, "INCHIKEY");

        if(!inchikeyPattern.matcher(inchikey).matches())
            throw error(file, record, "unexpected INCHIKEY", inchikey);

        String inchi = parameter(file, record, "INCHI");

        if(!inchi.startsWith("InChI="))
            throw error(file, record, "unexpected INCHI", inchi);

        String formula = parameter(file, record, "MOLECULAR_FORMULA");
        String smiles = parameter(file, record, "SMILES");
        float exactMass = number(file, record, "PARENT_MASS");
        float pepmass = number(file, record, "PRECURSOR_MZ");

        if(!String.valueOf(record.peaks()).equals(record.parameters().get("NUM_PEAKS")))
            throw error(file, record, "unexpected NUM_PEAKS", record.parameters().get("NUM_PEAKS"));


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


    /*
     * Loads the negative file before the positive one, so that the parent masses of the negative polarity are the ones
     * stored.
     */
    private static void loadFiles() throws IOException, SQLException
    {
        load("select accession,id from isdb.compounds", compoundIDs);

        nextCompoundID = compoundIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        loadFile("isdb/isdb_wikidata_neg_energySum.mgf");
        loadFile("isdb/isdb_wikidata_pos_energySum.mgf");

        if(spectra.size() == 0)
        {
            Problems.error("no spectrum", "isdb");
            return;
        }

        compounds.store();
        spectra.store();
    }


    public static void main(String[] args) throws Exception
    {
        try
        {
            init("isdb");

            String version = getReader("isdb/version.txt.gz").readLine();
            System.out.println("=== load isdb version " + version + " ===");
            System.out.println();

            loadFiles();

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement.executeQuery("select count(*) from isdb.spectra"))
                {
                    if(result.next())
                        setCount("ISDB Mass Spectra", result.getInt(1));
                }
            }

            setVersion("In Silico Spectral Database (ISDB)", version);

            updateVersion();

            // the metadata of the Zenodo record, which the download script uses
            checkFiles("isdb", "record\\.json");
            checkProblems();

            syncIndex("isdb", true);

            try(Statement statement = connection.createStatement())
            {
                statement.execute("refresh materialized view isdb.compound_pubchem_compounds");
                statement.execute("refresh materialized view isdb.compound_wikidata_compounds");
            }

            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
