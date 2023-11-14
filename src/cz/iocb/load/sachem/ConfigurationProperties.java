package cz.iocb.load.sachem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Properties;



@SuppressWarnings("serial")
public class ConfigurationProperties extends Properties
{
    public ConfigurationProperties(String fileName) throws FileNotFoundException, IOException
    {
        try(FileInputStream stream = new FileInputStream(fileName))
        {
            load(stream);
        }
    }


    @Override
    public String getProperty(String key)
    {
        final String property = super.getProperty(key);

        if(property == null)
            throw new InvalidParameterException("missing value for key " + key);

        return property;
    }


    public int getIntProperty(String key)
    {
        try
        {
            return Integer.parseInt(getProperty(key));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidParameterException("wrong value for key " + key);
        }
    }


    public boolean getBooleanProperty(String key)
    {
        try
        {
            return Boolean.parseBoolean(getProperty(key));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidParameterException("wrong value for key " + key);
        }
    }
}
