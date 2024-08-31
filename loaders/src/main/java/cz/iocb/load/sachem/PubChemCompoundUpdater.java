package cz.iocb.load.sachem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import cz.iocb.load.common.Updater;



public class PubChemCompoundUpdater
{
    private static final int DAY = 24 * 60 * 60 * 1000;
    private static final String DAILY = "/Daily";
    private static final String WEEKLY = "/Weekly";


    public static void main(String[] args) throws Exception
    {
        Date checkdate = new Date();
        ConfigurationProperties properties = new ConfigurationProperties("config/pubchem.properties");

        String pgHost = properties.getProperty("postgres.host");
        int pgPort = properties.getIntProperty("postgres.port");
        String pgUserName = properties.getProperty("postgres.username");
        String pgPassword = properties.getProperty("postgres.password");
        String pgDatabase = properties.getProperty("postgres.database");
        String index = properties.getProperty("sachem.index");
        boolean optimize = properties.getBooleanProperty("sachem.optimize");
        boolean autoclean = properties.getBooleanProperty("sachem.autoclean");
        boolean rename = properties.getBooleanProperty("sachem.rename");

        String ftpServer = properties.getProperty("ftp.server");
        int ftpPort = properties.getIntProperty("ftp.port");
        String ftpUserName = properties.getProperty("ftp.username");
        String ftpPassword = properties.getProperty("ftp.password");
        String ftpPath = properties.getProperty("ftp.path");

        String filePattern = properties.getProperty("sdf.pattern");
        String workdir = properties.getProperty("sdf.directory");
        String idTag = properties.getProperty("sdf.idtag");
        String idPrefix = properties.getProperty("sdf.idprefix");

        String baseDirectory = properties.getProperty("base.directory");
        String baseVersion = properties.getProperty("base.version");


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


            String version = null;

            try(PreparedStatement statement = connection
                    .prepareStatement("select version from info.sachem_stats where index=?"))
            {
                statement.setString(1, index);

                try(ResultSet result = statement.executeQuery())
                {
                    if(result.next())
                        version = result.getString(1);
                }
            }


            LinkedList<String> updateList = new LinkedList<String>();
            LinkedList<String> downloadList = new LinkedList<String>();
            String finalVersion = null;
            FTPClient ftpClient = new FTPClient();

            try
            {
                ftpClient.connect(ftpServer, ftpPort);
                ftpClient.login(ftpUserName, ftpPassword);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                String lastVersion = version != null ? version : baseVersion;
                finalVersion = lastVersion;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                LinkedList<String> dailyList = new LinkedList<String>();
                LinkedList<String> weeklyList = new LinkedList<String>();


                FTPFile[] dailyUpdates = ftpClient.listFiles(ftpPath + "/Daily");
                Arrays.sort(dailyUpdates, (a, b) -> a.getName().compareTo(b.getName()));

                if(dailyUpdates.length == 0)
                    throw new Exception("ftp directory is empty");

                String oldestDailyUpdate = null;
                boolean coveredByDaily = false;

                for(FTPFile update : dailyUpdates)
                {
                    String name = update.getName();

                    if(name.compareTo(lastVersion) == 0)
                    {
                        coveredByDaily = true;
                    }
                    if(name.compareTo(lastVersion) > 0)
                    {
                        dailyList.add(DAILY + "/" + name);

                        if(oldestDailyUpdate == null || name.compareTo(oldestDailyUpdate) < 0)
                            oldestDailyUpdate = name;

                        if(name.compareTo(finalVersion) > 0)
                            finalVersion = name;
                    }
                }


                if(!coveredByDaily)
                {
                    if(oldestDailyUpdate == null)
                        throw new Exception("inconsistent server daily data");

                    FTPFile[] weeklyUpdates = ftpClient.listFiles(ftpPath + "/Weekly");
                    Arrays.sort(weeklyUpdates, (a, b) -> a.getName().compareTo(b.getName()));

                    if(weeklyUpdates.length == 0)
                        throw new Exception("ftp directory is empty");

                    String limit = format.format(new Date(format.parse(oldestDailyUpdate).getTime() + 6 * DAY));
                    String oldestWeeklyUpdate = null;

                    for(FTPFile update : weeklyUpdates)
                    {
                        String name = update.getName();

                        if(name.compareTo(lastVersion) > 0 && name.compareTo(limit) < 0)
                        {
                            weeklyList.add(WEEKLY + "/" + name);

                            if(oldestWeeklyUpdate == null || name.compareTo(oldestWeeklyUpdate) < 0)
                                oldestWeeklyUpdate = name;
                        }
                    }

                    if(oldestWeeklyUpdate == null)
                        throw new Exception("inconsistent server weekly data");

                    String firstUncovered = format
                            .format(new Date(format.parse(oldestWeeklyUpdate).getTime() - 7 * DAY));

                    if(firstUncovered.compareTo(lastVersion) > 0)
                        throw new Exception("database version is too old");
                }

                updateList.addAll(weeklyList);
                updateList.addAll(dailyList);


                for(String update : updateList)
                {
                    String basePath = workdir + update;
                    String sdfPath = basePath + "/" + "SDF";
                    File directory = new File(sdfPath);
                    directory.mkdirs();

                    downloadList.add(update + "/killed-CIDs");

                    FTPFile[] sdfUpdates = ftpClient.listFiles(ftpPath + update + "/SDF");

                    for(FTPFile sdfUpdate : sdfUpdates)
                    {
                        String name = sdfUpdate.getName();

                        if(name.matches(filePattern))
                            downloadList.add(update + "/SDF/" + name);
                    }
                }
            }
            finally
            {
                ftpClient.logout();
                ftpClient.disconnect();
            }


            for(String name : downloadList)
            {
                FTPClient ftpDownloadClient = new FTPClient();

                try
                {
                    ftpDownloadClient.connect(ftpServer, ftpPort);
                    ftpDownloadClient.login(ftpUserName, ftpPassword);
                    ftpDownloadClient.setFileType(FTP.BINARY_FILE_TYPE);
                    ftpDownloadClient.enterLocalPassiveMode();

                    try(FileOutputStream output = new FileOutputStream(workdir + "/" + name))
                    {
                        System.out.println("  " + name);

                        if(!ftpDownloadClient.retrieveFile(ftpPath + name, output))
                        {
                            if(ftpDownloadClient.getReplyCode() == 550)
                                System.out.println("    skiped");
                            else
                                throw new Exception("cannot download: " + ftpPath + name);
                        }
                    }
                }
                finally
                {
                    ftpDownloadClient.logout();
                    ftpDownloadClient.disconnect();
                }
            }


            if(version != null && updateList.isEmpty())
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


            connection.setAutoCommit(version == null);

            try
            {
                CompoundLoader loader = new CompoundLoader(connection, index, idTag, idPrefix, rename);

                if(version == null)
                {
                    System.out.println("load " + baseDirectory);
                    loader.loadDirectory(new File(baseDirectory));
                }

                for(String update : updateList)
                {
                    try(PreparedStatement deleteStatement = connection
                            .prepareStatement("delete from " + loader.schema + "." + loader.table + " where id = ?"))
                    {
                        System.out.println("load " + workdir + update);

                        try(BufferedReader removeList = new BufferedReader(
                                new FileReader(workdir + update + "/killed-CIDs")))
                        {
                            int count = 0;
                            String line;

                            while((line = removeList.readLine()) != null)
                            {
                                count++;
                                deleteStatement.setInt(1, Integer.parseInt(line));
                                deleteStatement.addBatch();

                                if(count % 10000 == 0)
                                    deleteStatement.executeBatch();
                            }

                            if(count % 10000 != 0)
                                deleteStatement.executeBatch();
                        }
                    }

                    loader.loadDirectory(new File(workdir + update + "/SDF"), false);
                }

                try(PreparedStatement statement = connection.prepareStatement("select sachem.sync_data(?, false, ?)"))
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
                    statement.setString(2, finalVersion);
                    statement.setTimestamp(3, new Timestamp(checkdate.getTime()));
                    statement.executeUpdate();
                }

                Updater.updateVersion(connection);

                if(!connection.getAutoCommit())
                    connection.commit();
            }
            catch(Throwable e)
            {
                if(!connection.getAutoCommit())
                    connection.rollback();

                throw e;
            }
        }
    }
}
