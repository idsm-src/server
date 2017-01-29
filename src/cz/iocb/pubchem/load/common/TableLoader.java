package cz.iocb.pubchem.load.common;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;



public abstract class TableLoader
{
    protected PreparedStatement statement = null;
    protected boolean set;


    protected void setValue(int idx, Integer value) throws SQLException
    {
        set = true;

        if(value != null)
            statement.setInt(idx, value);
        else
            statement.setNull(idx, Types.INTEGER);
    }


    protected void setValue(int idx, Short value) throws SQLException
    {
        set = true;

        if(value != null)
            statement.setInt(idx, value);
        else
            statement.setNull(idx, Types.SMALLINT);
    }


    protected void setValue(int idx, Float value) throws SQLException
    {
        set = true;

        if(value != null)
            statement.setFloat(idx, value);
        else
            statement.setNull(idx, Types.FLOAT);
    }


    protected void setValue(int idx, String value) throws SQLException
    {
        set = true;

        statement.setString(idx, value);
    }
}
