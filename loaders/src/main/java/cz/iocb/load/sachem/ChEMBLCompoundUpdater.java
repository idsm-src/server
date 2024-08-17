package cz.iocb.load.sachem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import cz.iocb.load.common.Updater;



public class ChEMBLCompoundUpdater
{
    private static final String index = "chembl";
    private static final boolean optimize = true;
    private static final boolean autoclean = true;
    private static final boolean rename = true;

    private static final String ftpServer = "ftp.ebi.ac.uk";
    private static final int ftpPort = 21;
    private static final String ftpUserName = "anonymous";
    private static final String ftpPassword = "anonymous";
    private static final String ftpPath = "/pub/databases/chembl/ChEMBLdb/latest";

    private static final String filePattern = "chembl_.+\\.sdf\\.gz";
    private static final String idTag = "chembl_id";
    private static final String idPrefix = "CHEMBL";


    public static void main(String[] args) throws Exception
    {
        Updater.lock("sachem-chembl.lock");

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

        String workdir = basedir + "sachem/chembl/";


        String path = workdir + "/" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
        File directory = new File(path);

        LinkedList<FTPFile> sdfFiles = new LinkedList<FTPFile>();
        boolean hasNewItem = false;


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
