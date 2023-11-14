package cz.iocb.load.sachem;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;



public class ChEMBLCompoundUpdater
{
    public static void main(String[] args) throws Exception
    {
        Date checkdate = new Date();
        ConfigurationProperties properties = new ConfigurationProperties("config/chembl.properties");

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


        String pgUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase;
        String path = workdir + "/" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
        File directory = new File(path);

        LinkedList<FTPFile> sdfFiles = new LinkedList<FTPFile>();
        boolean hasNewItem = false;


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


            FTPClient ftpClient = new FTPClient();

            try
            {
                try(PreparedStatement statement = connection.prepareStatement(
                        "select true from info.sachem_sources where index=? and name=? and size=? and timestamp=?"))
                {
                    ftpClient.connect(ftpServer, ftpPort);
                    ftpClient.login(ftpUserName, ftpPassword);
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    ftpClient.enterLocalPassiveMode();

                    FTPFile[] files = ftpClient.listFiles(ftpPath);

                    if(files.length == 0)
                        throw new Exception("ftp directory is empty");

                    for(FTPFile file : files)
                    {
                        if(!file.isDirectory() && file.getName().matches(filePattern))
                        {
                            statement.setString(1, index);
                            statement.setString(2, file.getName());
                            statement.setLong(3, file.getSize());
                            statement.setTimestamp(4, new Timestamp(file.getTimestamp().getTimeInMillis()));

                            sdfFiles.add(file);

                            try(ResultSet result = statement.executeQuery())
                            {
                                if(!result.next())
                                    hasNewItem = true;
                            }
                        }
                    }
                }

                if(hasNewItem)
                {
                    directory.mkdirs();

                    for(FTPFile sdfFile : sdfFiles)
                    {
                        String name = sdfFile.getName();

                        try(FileOutputStream output = new FileOutputStream(path + "/" + name))
                        {
                            if(!ftpClient.retrieveFile(ftpPath + "/" + name, output))
                                throw new Exception("cannot download: " + ftpPath + "/" + name);
                        }
                    }
                }
            }
            finally
            {
                ftpClient.logout();
                ftpClient.disconnect();
            }


            if(!hasNewItem)
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
                        .prepareStatement("insert into info.sachem_sources(index,name,size,timestamp) values(?,?,?,?)"))
                {
                    for(FTPFile sdfFile : sdfFiles)
                    {
                        statement.setString(1, index);
                        statement.setString(2, sdfFile.getName());
                        statement.setLong(3, sdfFile.getSize());
                        statement.setTimestamp(4, new Timestamp(sdfFile.getTimestamp().getTimeInMillis()));
                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                try(PreparedStatement statement = connection.prepareStatement("select sachem.sync_data(?,false,?)"))
                {
                    statement.setString(1, index);
                    statement.setBoolean(2, optimize);
                    statement.execute();
                }

                try(PreparedStatement statement = connection
                        .prepareStatement("insert into info.sachem_stats(index,checkdate) values(?,?) "
                                + "on conflict (index) do update set checkdate=EXCLUDED.checkdate"))
                {
                    statement.setString(1, index);
                    statement.setTimestamp(2, new Timestamp(checkdate.getTime()));
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
