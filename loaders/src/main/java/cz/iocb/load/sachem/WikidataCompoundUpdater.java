package cz.iocb.load.sachem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Updater;



public class WikidataCompoundUpdater extends Updater
{
    private static String httpServer = "https://query.wikidata.org/sparql?query=";


    public static void main(String[] args) throws Throwable
    {
        try
        {
            init();

            Date date = new Date();
            String path = baseDirectory + "sachem-wikidata/" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(date);
            File directory = new File(path);
            directory.mkdirs();

            String isomeric = "SELECT?entity?smiles WHERE{?entity<http://www.wikidata.org/prop/direct/P2017>?smiles}";
            String canonical = "SELECT?entity?smiles WHERE{?entity<http://www.wikidata.org/prop/direct/P233>?smiles}";
            String inchi = "SELECT?entity?inchi WHERE{?entity<http://www.wikidata.org/prop/direct/P234>?inchi}";

            Path isomericPath = Path.of(path, "/isomeric.smiles");
            URL isomericDownloadUrl = new URL(httpServer + URLEncoder.encode(isomeric, Charset.defaultCharset()));
            HttpURLConnection isomericDownloadConnection = (HttpURLConnection) isomericDownloadUrl.openConnection();
            isomericDownloadConnection.addRequestProperty("Accept", "text/tab-separated-values");
            Files.copy(isomericDownloadConnection.getInputStream(), isomericPath);

            Path canonicalPath = Path.of(path, "/canonical.smiles");
            URL canonicalDownloadUrl = new URL(httpServer + URLEncoder.encode(canonical, Charset.defaultCharset()));
            HttpURLConnection canonicalDownloadConnection = (HttpURLConnection) canonicalDownloadUrl.openConnection();
            canonicalDownloadConnection.addRequestProperty("Accept", "text/tab-separated-values");
            Files.copy(canonicalDownloadConnection.getInputStream(), canonicalPath);

            Path inchiPath = Path.of(path, "/inchies");
            URL inchiDownloadUrl = new URL(httpServer + URLEncoder.encode(inchi, Charset.defaultCharset()));
            HttpURLConnection inchiDownloadConnection = (HttpURLConnection) inchiDownloadUrl.openConnection();
            inchiDownloadConnection.addRequestProperty("Accept", "text/tab-separated-values");
            Files.copy(inchiDownloadConnection.getInputStream(), inchiPath);


            IntStringSet oldIsomericSmiles = new IntStringSet();
            IntStringSet newIsomericSmiles = new IntStringSet();

            load("select compound, smiles from wikidata.isomeric_smiles", oldIsomericSmiles);

            try(BufferedReader reader = new BufferedReader(new FileReader(isomericPath.toFile())))
            {
                String line = reader.readLine();

                while((line = reader.readLine()) != null)
                {
                    String[] items = line.split("\t", 2);
                    Integer id = Integer
                            .valueOf(items[0].replaceFirst("^<http://www\\.wikidata\\.org/entity/Q([0-9]+)>$", "$1"));
                    String smiles = items[1].replaceFirst("^\"(.*)\"$", "$1");
                    Pair<Integer, String> pair = Pair.getPair(id, smiles);

                    if(!oldIsomericSmiles.remove(pair))
                        newIsomericSmiles.add(pair);
                }
            }

            store("delete from wikidata.isomeric_smiles where compound=? and smiles=?", oldIsomericSmiles);
            store("insert into wikidata.isomeric_smiles(compound,smiles) values(?,?)", newIsomericSmiles);


            IntStringSet oldCanonicalSmiles = new IntStringSet();
            IntStringSet newCanonicalSmiles = new IntStringSet();

            load("select compound, smiles from wikidata.canonical_smiles", oldCanonicalSmiles);

            try(BufferedReader reader = new BufferedReader(new FileReader(canonicalPath.toFile())))
            {
                String line = reader.readLine();

                while((line = reader.readLine()) != null)
                {
                    String[] items = line.split("\t", 2);
                    Integer id = Integer
                            .valueOf(items[0].replaceFirst("^<http://www\\.wikidata\\.org/entity/Q([0-9]+)>$", "$1"));
                    String smiles = items[1].replaceFirst("^\"(.*)\"$", "$1");
                    Pair<Integer, String> pair = Pair.getPair(id, smiles);

                    if(!oldCanonicalSmiles.remove(pair))
                        newCanonicalSmiles.add(pair);
                }
            }

            store("delete from wikidata.canonical_smiles where compound=? and smiles=?", oldCanonicalSmiles);
            store("insert into wikidata.canonical_smiles(compound,smiles) values(?,?)", newCanonicalSmiles);


            IntStringSet oldInchies = new IntStringSet();
            IntStringSet newInchies = new IntStringSet();

            load("select compound, inchi from wikidata.inchies", oldInchies);

            try(BufferedReader reader = new BufferedReader(new FileReader(inchiPath.toFile())))
            {
                String line = reader.readLine();

                while((line = reader.readLine()) != null)
                {
                    String[] items = line.split("\t", 2);
                    Integer id = Integer
                            .valueOf(items[0].replaceFirst("^<http://www\\.wikidata\\.org/entity/Q([0-9]+)>$", "$1"));
                    String value = items[1].replaceFirst("^\"(.*)\"$", "$1");
                    Pair<Integer, String> pair = Pair.getPair(id, value);

                    if(!oldInchies.remove(pair))
                        newInchies.add(pair);
                }
            }

            store("delete from wikidata.inchies where compound=? and inchi=?", oldInchies);
            store("insert into wikidata.inchies(compound,inchi) values(?,?)", newInchies);


            try(Statement statement = connection.createStatement())
            {
                statement.execute("select sachem.cleanup('wikidata')");

                statement.execute("delete from molecules.wikidata where "
                        + "not exists (select 1 from wikidata.isomeric_smiles where compound = id) and "
                        + "not exists (select 1 from wikidata.canonical_smiles where compound = id)");

                statement.execute("insert into molecules.wikidata select distinct on (compound) compound, smiles from ("
                        + "select compound, smiles, 1 as v from wikidata.isomeric_smiles union "
                        + "select compound, smiles, 2 as v from wikidata.canonical_smiles) "
                        + "order by compound, v, smiles " + "on conflict (id) do update set smiles=EXCLUDED.smiles "
                        + "where molecules.wikidata.smiles != EXCLUDED.smiles;");

                statement.execute("select sachem.sync_data('wikidata', false, true)");
            }

            try(PreparedStatement statement = connection
                    .prepareStatement("insert into info.sachem_stats(index,checkdate) values('wikidata',?) "
                            + "on conflict (index) do update set version=EXCLUDED.version, checkdate=EXCLUDED.checkdate"))
            {
                statement.setTimestamp(1, new Timestamp(date.getTime()));
                statement.executeUpdate();
            }

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
