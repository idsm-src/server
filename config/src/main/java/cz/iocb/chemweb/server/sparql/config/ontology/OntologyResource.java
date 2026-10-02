package cz.iocb.chemweb.server.sparql.config.ontology;

import static cz.iocb.sparql.engine.database.SqlType.INT2;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.RDFBOX;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.box;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.iri;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.constant;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.string;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.SQLRuntimeException;
import cz.iocb.sparql.engine.database.ValueColumn;
import cz.iocb.sparql.engine.mapping.classes.GenericUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.request.Request;



public class OntologyResource extends GenericUserIriClass
{
    private static record Unit(short id, String prefix, int valueOffset, int valueLength, String pattern)
    {
    }


    private static final String sqlQuery = "select resource_id from ontology.resources__reftable where iri = ?";
    private static OntologyResource instance;
    private static List<Unit> units = new ArrayList<>();
    private static Map<Column, Unit> unitMap = new HashMap<>();

    private static final short unitUncategorized = 0;
    private static final short unitPR0 = 31;
    private static final short unitPR1 = 32;
    private static final short unitPR2 = 33;
    private static final short unitAT = 34;
    private static final short unitZDBGENE = 35;
    private static final short unitPowo = 36;
    private static final short unitIpni = 37;
    private static final short unitStar = 95;
    private static final short unitRareDiseases = 180;


    private OntologyResource()
    {
        super("ontology:resource", "ontology", "ontology_resource", List.of(INT2, INT4),
                units.stream().map(c -> c.pattern).collect(Collectors.joining("|")),
                GenericUserIriClass.SqlCheck.IF_NOT_MATCH);
    }


    @Override
    public List<Column> toColumns(Request request, Iri iri)
    {
        String val = iri.getValue();

        assert match(request, iri);

        for(Unit unit : units)
        {
            if(val.matches(unit.pattern))
            {
                String tail = val.substring(unit.valueOffset - 1);
                int id = 0;

                if(unit.id == unitPR0)
                {
                    // [0-9][A-Z0-9][0-9][A-Z0-9]{3}[0-9]
                    id = tail.charAt(0) - '0';
                    id = id * 36 + code(tail.charAt(1));
                    id = id * 10 + tail.charAt(2) - '0';
                    id = id * 36 + code(tail.charAt(3));
                    id = id * 36 + code(tail.charAt(4));
                    id = id * 36 + code(tail.charAt(5));
                    id = id * 10 + tail.charAt(6) - '0';
                }
                else if(unit.id == unitPR1 || unit.id == unitPR2)
                {
                    // [A-Z][0-9][A-Z0-9]{3}[0-9](-([12])?[0-9])?
                    id = tail.charAt(0) - 'A';
                    id = id * 10 + tail.charAt(1) - '0';
                    id = id * 36 + code(tail.charAt(2));
                    id = id * 36 + code(tail.charAt(3));
                    id = id * 36 + code(tail.charAt(4));
                    id = id * 10 + tail.charAt(5) - '0';

                    if(unit.id == unitPR1)
                        id = id * 30 + Integer.parseInt(tail.substring(7));
                }
                else if(unit.id == unitAT)
                {
                    // [A-Z0-9]G[0-9]{5}
                    id = code(tail.charAt(0)) * 100000 + Integer.parseInt(tail.substring(2));
                }
                else if(unit.id == unitZDBGENE)
                {
                    // [0-9]{6}-([1-3])?[0-9]{1,3}$
                    id = Integer.parseInt(tail.substring(0, 6));
                    id = id * 4000 + Integer.parseInt(tail.substring(7));
                }
                else if(unit.id == unitPowo || unit.id == unitIpni)
                {
                    id = Integer.parseInt(tail.replaceFirst("-", ""));
                }
                else if(unit.id == unitStar)
                {
                    id = tail.charAt(0) - '0';
                }
                else if(unit.id == unitRareDiseases)
                {
                    id = Integer.parseInt(tail.substring(0, tail.length() - 6));
                }
                else
                {
                    id = Integer.parseInt(tail);
                }

                List<Column> columns = new ArrayList<>();
                columns.add(constant(unit.id, INT2));
                columns.add(constant(id, INT4));

                return columns;
            }
        }

        try
        {
            String sql = sqlQuery.replaceAll("\\?", string(val));

            try(ResultSet result = request.getStatement().executeQuery(sql))
            {
                result.next();

                return List.of(constant(unitUncategorized, INT2), constant(result.getInt(1), INT4));
            }
        }
        catch(SQLException e)
        {
            throw new SQLRuntimeException(e);
        }
    }


    @Override
    public List<Column> toGeneralClass(ResourceClass superClass, List<Column> columns, boolean canBeNull)
    {
        if(superClass.equals(this))
            return columns;

        String code = getInverseCode(columns);

        if(superClass.equals(box))
            return List.of(new ExpressionColumn("sparql.rdfbox_create_from_iri(" + code + ")", RDFBOX));

        if(superClass.equals(iri))
            return List.of(new ExpressionColumn(code, VARCHAR));

        throw new IllegalArgumentException();
    }


    @Override
    public List<Column> toOrderColumns(List<Column> columns)
    {
        Unit unit = unitMap.get(columns.get(0));

        if(unit == null || unit.valueLength < 0)
        {
            return super.toOrderColumns(columns);
        }
        else if(unit.id == unitStar)
        {
            return List.of(columns.get(1));
        }
        else if(unit.id == unitRareDiseases)
        {
            return List.of(columns.get(1));
        }
        else if(unit.valueLength == 0)
        {
            StringBuilder builder = new StringBuilder();

            builder.append("(");
            builder.append(columns.get(1));
            builder.append(")::varchar");

            return List.of(new ExpressionColumn(builder.toString(), VARCHAR));
        }
        else
        {
            StringBuilder builder = new StringBuilder();

            builder.append("lpad((");
            builder.append(columns.get(1));
            builder.append(")::varchar, ");
            builder.append(unit.valueLength);
            builder.append(", '0')::varchar)");

            return List.of(new ExpressionColumn(builder.toString(), VARCHAR));
        }
    }


    @Override
    public String getPrefix(List<Column> columns)
    {
        Unit unit = unitMap.get(columns.get(0));

        if(unit == null)
            return "";

        return unit.prefix();
    }


    @Override
    public boolean match(Request request, Iri iri)
    {
        String val = iri.getValue();

        for(Unit unit : units)
            if(val.matches(unit.pattern))
                return true;

        try
        {
            String sql = sqlQuery.replaceAll("\\?", string(val));

            try(ResultSet result = request.getStatement().executeQuery(sql))
            {
                return result.next();
            }
        }
        catch(SQLException e)
        {
            throw new SQLRuntimeException(e);
        }
    }


    private String getInverseCode(List<Column> columns)
    {
        StringBuilder builder = new StringBuilder();
        Unit unit = unitMap.get(columns.get(0));

        if(unit == null || unit.valueLength < 0)
        {
            builder.append(function);
            builder.append("(");

            for(int i = 0; i < getColumnCount(); i++)
            {
                if(i > 0)
                    builder.append(", ");

                builder.append(columns.get(i));
            }

            builder.append(")");
        }
        else if(unit.id == unitStar)
        {
            builder.append("('");
            builder.append(unit.prefix().replaceAll("'", "''"));
            builder.append("' || (");
            builder.append(columns.get(1));
            builder.append(")::varchar || '_STAR')");
        }
        else if(unit.id == unitRareDiseases)
        {
            builder.append("('");
            builder.append(unit.prefix().replaceAll("'", "''"));
            builder.append("' || (");
            builder.append(columns.get(1));
            builder.append(")::varchar || '/index')");
        }
        else if(unit.valueLength == 0)
        {
            builder.append("('");
            builder.append(unit.prefix().replaceAll("'", "''"));
            builder.append("' || (");
            builder.append(columns.get(1));
            builder.append(")::varchar)");
        }
        else
        {
            builder.append("('");
            builder.append(unit.prefix().replaceAll("'", "''"));
            builder.append("' || lpad((");
            builder.append(columns.get(1));
            builder.append(")::varchar, ");
            builder.append(unit.valueLength);
            builder.append(", '0')::varchar)");
        }

        return builder.toString();
    }


    private static int code(char value)
    {
        return value > '9' ? 10 + value - 'A' : value - '0';
    }


    public static synchronized OntologyResource get(SparqlDatabaseConfiguration config) throws SQLException
    {
        if(instance != null)
            return instance;

        try(Connection connection = config.getConnectionPool().getConnection())
        {
            connection.setAutoCommit(true);

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet r = statement.executeQuery("select unit_id, prefix, value_offset, value_length, pattern "
                        + "from ontology.resource_categories__reftable order by unit_id"))
                {
                    while(r.next())
                    {
                        Unit unit = new Unit(r.getShort(1), r.getString(2), r.getInt(3), r.getInt(4), r.getString(5));
                        units.add(unit);
                        unitMap.put(new ValueColumn(Short.toString(unit.id), INT2), unit);
                    }
                }
            }
        }

        instance = new OntologyResource();

        return instance;
    }
}
