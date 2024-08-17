package cz.iocb.load.sachem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.iocb.load.common.Updater;



public class DrugbankCompoundUpdater
{
    private static final String index = "drugbank";
    private static final boolean optimize = true;
    private static final boolean autoclean = true;
    private static final boolean rename = true;

    private static final String httpServer = "https://go.drugbank.com";
    private static final String fileName = "drugbank_all_open_structures.sdf.zip";
    private static final String idTag = "DRUGBANK_ID";
    private static final String idPrefix = "DB";


    public static void main(String[] args) throws Throwable
    {
        Updater.lock("sachem-drugbank.lock");

        Date checkdate = new Date();

        Properties properties = new Properties();

        try(FileInputStream in = new FileInputStream("datasource.properties"))
        {
            properties.load(in);
        }

        String url = properties.getProperty("url");
        properties.remove("url");

        boolean autoCommit = Boolean.valueOf(properties.getProperty("autoCommit"));
        properties.remove("autoCommit");

        String basedir = properties.getProperty("base");
        properties.remove("base");

        if(!basedir.endsWith("/"))
            basedir += "/";

        String workdir = basedir + "/sachem/drugbank/";


        try(Connection connection = DriverManager.getConnection(url, properties))
        {
            connection.setAutoCommit(autoCommit);

            if(autoclean)
            {
                try(PreparedStatement statement = connection.prepareStatement("select sachem.cleanup(?)"))
                {
                    statement.setString(1, index);
                    statement.execute();
                }
            }


            URL infoUrl = new URI(httpServer + "/releases/latest#open-data").toURL();
            HttpURLConnection infoConnection = (HttpURLConnection) infoUrl.openConnection();
            infoConnection.addRequestProperty("User-Agent", "Java HttpURLConnection");
            infoConnection.addRequestProperty("Accept", "*/*");

            String versionTag = null;

            try(BufferedReader buffer = new BufferedReader(new InputStreamReader(infoConnection.getInputStream())))
            {
                Pattern pattern = Pattern.compile(httpServer + "/releases/([^/]+)/downloads/all-open-structures");
                String line;

                while((line = buffer.readLine()) != null)
                {
                    Matcher matcher = pattern.matcher(line);

                    while(matcher.find())
                        versionTag = matcher.group(1);
                }
            }

            if(versionTag == null)
                throw new IOException("the latest published version of DrugBank cannot be determined");


            String version = null;

            try(PreparedStatement statement = connection
                    .prepareStatement("select version from info.sachem_stats where index = ?"))
            {
                statement.setString(1, index);

                try(ResultSet result = statement.executeQuery())
                {
                    if(result.next())
                        version = result.getString(1);
                }
            }


            if(versionTag.equals(version))
            {
                try(PreparedStatement statement = connection
                        .prepareStatement("update info.sachem_stats set checkdate=? where index=?"))
                {
                    statement.setTimestamp(1, new Timestamp(checkdate.getTime()));
                    statement.setString(2, index);
                    statement.executeUpdate();
                }

                return;
            }


            String path = workdir + "/" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
            File directory = new File(path);
            directory.mkdirs();


            URL downloadUrl = new URI(httpServer + "/releases/" + versionTag + "/downloads/all-open-structures")
                    .toURL();
            HttpURLConnection downloadConnection = (HttpURLConnection) downloadUrl.openConnection();
            downloadConnection.addRequestProperty("User-Agent", "Java HttpURLConnection");
            downloadConnection.addRequestProperty("Accept", "*/*");

            InputStream is = downloadConnection.getInputStream();
            int fileSize = 0;


            try(FileOutputStream out = new FileOutputStream(path + "/" + fileName))
            {
                byte[] buffer = new byte[8 * 1024];
                int length;

                while((length = is.read(buffer)) > 0)
                {
                    out.write(buffer, 0, length);
                    fileSize += length;
                }
            }


            try
            {
                CompoundLoader loader = new CompoundLoader(connection, index, idTag, idPrefix, rename);
                loader.loadDirectory(directory);

                try(PreparedStatement statement = connection
                        .prepareStatement("delete from info.sachem_sources where index=?"))
                {
                    statement.setString(1, index);
                    statement.execute();
                }

                try(PreparedStatement statement = connection
                        .prepareStatement("insert into info.sachem_sources(index,name,size) values(?,?,?)"))
                {
                    statement.setString(1, index);
                    statement.setString(2, fileName);
                    statement.setLong(3, fileSize);
                    statement.addBatch();

                    statement.executeBatch();
                }

                try(PreparedStatement statement = connection.prepareStatement("select sachem.sync_data(?,false,?)"))
                {
                    statement.setString(1, index);
                    statement.setBoolean(2, optimize);
                    statement.execute();
                }

                try(PreparedStatement statement = connection
                        .prepareStatement("insert into info.sachem_stats(index,version,checkdate) values(?,?,?) "
                                + "on conflict (index) do update set version=EXCLUDED.version, checkdate=EXCLUDED.checkdate"))
                {
                    statement.setString(1, index);
                    statement.setString(2, versionTag);
                    statement.setTimestamp(3, new Timestamp(checkdate.getTime()));
                    statement.executeUpdate();
                }

                Updater.updateVersion(connection);
                connection.commit();
            }
            catch(Throwable e)
            {
                connection.rollback();
                throw e;
            }
        }
    }
}
