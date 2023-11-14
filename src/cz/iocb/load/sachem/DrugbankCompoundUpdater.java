package cz.iocb.load.sachem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class DrugbankCompoundUpdater
{
    public static void main(String[] args) throws Throwable
    {
        Date checkdate = new Date();
        ConfigurationProperties properties = new ConfigurationProperties("config/drugbank.properties");

        String pgHost = properties.getProperty("postgres.host");
        int pgPort = properties.getIntProperty("postgres.port");
        String pgUserName = properties.getProperty("postgres.username");
        String pgPassword = properties.getProperty("postgres.password");
        String pgDatabase = properties.getProperty("postgres.database");
        String index = properties.getProperty("sachem.index");
        boolean optimize = properties.getBooleanProperty("sachem.optimize");
        boolean autoclean = properties.getBooleanProperty("sachem.autoclean");
        boolean rename = properties.getBooleanProperty("sachem.rename");

        String httpServer = properties.getProperty("http.server");

        String workdir = properties.getProperty("sdf.directory");
        String fileName = properties.getProperty("sdf.file");
        String idTag = properties.getProperty("sdf.idtag");
        String idPrefix = properties.getProperty("sdf.idprefix");


        String pgUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase;

        try(Connection connection = DriverManager.getConnection(pgUrl, pgUserName, pgPassword))
        {
            if(autoclean)
            {
                try(PreparedStatement statement = connection.prepareStatement("select sachem.cleanup(?)"))
                {
                    statement.setString(1, index);
                    statement.execute();
                }
            }


            URL infoUrl = new URL(httpServer + "/releases/latest#open-data");
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


            URL downloadUrl = new URL(httpServer + "/releases/" + versionTag + "/downloads/all-open-structures");
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


            connection.setAutoCommit(false);

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
