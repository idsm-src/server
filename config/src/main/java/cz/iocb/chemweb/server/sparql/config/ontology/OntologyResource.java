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
import java.util.regex.Pattern;
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
    static record Unit(short id, String prefix, int valueOffset, int valueLength, Pattern pattern)
    {
    }


    private static final String sqlQuery = "select resource_id from ontology.resources__reftable where iri = ?";
    private static OntologyResource instance;
    private static List<Unit> units = new ArrayList<>();
    private static Map<Column, Unit> unitMap = new HashMap<>();

    public static final short unitUncategorized = 0;
    public static final short unitBlank = 1;
    public static final short unitSIO = 2;
    public static final short unitCHEMINF = 3;
    public static final short unitBAO = 4;
    public static final short unitGO = 5;
    public static final short unitPR = 6;
    public static final short unitCHEBI = 7;
    public static final short unitThesaurus = 10;
    public static final short unitTaxonomy = 11;
    public static final short unitClassyFire = 12;
    public static final short unitPR0 = 31;
    public static final short unitPR1 = 32;
    public static final short unitPR2 = 33;
    public static final short unitAT = 34;
    public static final short unitZDBGENE = 35;
    public static final short unitPowo = 36;
    public static final short unitIpni = 37;
    public static final short unitNCBITaxon = 64;
    public static final short unitUberon = 65;
    public static final short unitCL = 71;
    public static final short unitUO = 74;
    public static final short unitIAO = 75;
    public static final short unitCLO = 77;
    public static final short unitEFO = 92;
    public static final short unitStar = 95;
    public static final short unitNCIT = 100;
    public static final short unitRareDiseases = 180;
    public static final short unitWormbaseGene = 244;


    private OntologyResource()
    {
        super("ontology:resource", "ontology", "ontology_resource", List.of(INT2, INT4),
                units.stream().map(c -> c.pattern.pattern()).collect(Collectors.joining("|")),
                GenericUserIriClass.SqlCheck.IF_NOT_MATCH);
    }


    @Override
    public List<Column> toColumns(Request request, Iri iri)
    {
        String val = iri.getValue();

        assert match(request, iri);

        Unit unit = findUnit(val);

        if(unit == null)
            return List.of(constant(unitUncategorized, INT2), constant(findResourceId(request, val), INT4));

        int id = parseId(unit.id, val.substring(unit.valueOffset - 1));

        return List.of(constant(unit.id, INT2), constant(id, INT4));
    }


    @Override
    public List<Column> toGeneralClass(ResourceClass superClass, List<Column> columns, boolean canBeNull)
    {
        ResourceClass targetClass = superClass.getEffectiveClass();

        if(targetClass.equals(this))
            return columns;

        String code = getInverseCode(columns);

        if(targetClass.equals(box))
            return List.of(new ExpressionColumn("sparql.rdfbox_create_from_iri(" + code + ")", RDFBOX));

        if(targetClass.equals(iri))
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
            builder.append(", '0')::varchar");

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

        return findUnit(val) != null || findResourceId(request, val) != null;
    }


    Unit getUnit(short id)
    {
        return unitMap.get(constant(id, INT2));
    }


    Unit findUnit(String iri)
    {
        for(Unit unit : units)
            if(unit.pattern.matcher(iri).matches())
                return unit;

        return null;
    }


    Integer findResourceId(Request request, String iri)
    {
        try
        {
            String sql = sqlQuery.replace("?", string(iri));

            try(ResultSet result = request.getStatement().executeQuery(sql))
            {
                return result.next() ? result.getInt(1) : null;
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
        else if(unit.id == unitWormbaseGene)
        {
            builder.append("('");
            builder.append(unit.prefix().replaceAll("'", "''"));
            builder.append("' || lpad((");
            builder.append(columns.get(1));
            builder.append(")::varchar, ");
            builder.append(unit.valueLength);
            builder.append(", '0') || ';class=Gene')");
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


    public static int parseId(int unit, String tail)
    {
        int id = 0;

        if(unit == unitPR0)
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
        else if(unit == unitPR1 || unit == unitPR2)
        {
            // [A-Z][0-9][A-Z0-9]{3}[0-9](-([12])?[0-9])?
            id = tail.charAt(0) - 'A';
            id = id * 10 + tail.charAt(1) - '0';
            id = id * 36 + code(tail.charAt(2));
            id = id * 36 + code(tail.charAt(3));
            id = id * 36 + code(tail.charAt(4));
            id = id * 10 + tail.charAt(5) - '0';

            if(unit == unitPR1)
                id = id * 30 + Integer.parseInt(tail.substring(7));
        }
        else if(unit == unitAT)
        {
            // [A-Z0-9]G[0-9]{5}
            id = code(tail.charAt(0)) * 100000 + Integer.parseInt(tail.substring(2));
        }
        else if(unit == unitZDBGENE)
        {
            // [0-9]{6}-([1-3])?[0-9]{1,3}$
            id = Integer.parseInt(tail.substring(0, 6));
            id = id * 4000 + Integer.parseInt(tail.substring(7));
        }
        else if(unit == unitPowo || unit == unitIpni)
        {
            id = Integer.parseInt(tail.replaceFirst("-", ""));
        }
        else if(unit == unitStar)
        {
            id = tail.charAt(0) - '0';
        }
        else if(unit == unitRareDiseases)
        {
            id = Integer.parseInt(tail.substring(0, tail.length() - 6));
        }
        else if(unit == unitWormbaseGene)
        {
            id = Integer.parseInt(tail.substring(0, 8));
        }
        else
        {
            id = Integer.parseInt(tail);
        }

        return id;
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
                        Unit unit = new Unit(r.getShort(1), r.getString(2), r.getInt(3), r.getInt(4),
                                Pattern.compile(r.getString(5)));
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
