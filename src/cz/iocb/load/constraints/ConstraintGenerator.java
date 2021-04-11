package cz.iocb.load.constraints;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration;
import cz.iocb.chemweb.server.sparql.database.Column;
import cz.iocb.chemweb.server.sparql.database.ConstantColumn;
import cz.iocb.chemweb.server.sparql.database.DatabaseSchema;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.database.TableColumn;
import cz.iocb.chemweb.server.sparql.mapping.JoinTableQuadMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.ParametrisedMapping;
import cz.iocb.chemweb.server.sparql.mapping.QuadMapping;
import cz.iocb.chemweb.server.sparql.mapping.SingleTableQuadMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.LiteralClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.ResourceClass;
import cz.iocb.load.common.DummyDataSource;
import cz.iocb.load.common.Updater;



public class ConstraintGenerator extends Updater
{
    private static class QuadNodeMappingPair
    {
        QuadNodeMapping left;
        QuadNodeMapping right;

        QuadNodeMappingPair(QuadNodeMapping left, QuadNodeMapping right)
        {
            this.left = left;
            this.right = right;
        }
    }


    private static class QuadNodeMapping
    {
        Table table;
        ParametrisedMapping nodeMapping;

        QuadNodeMapping(Table table, ParametrisedMapping nodeMapping)
        {
            this.table = table;
            this.nodeMapping = nodeMapping;
        }

        @Override
        public boolean equals(Object o)
        {
            QuadNodeMapping mapping = (QuadNodeMapping) o;

            if(!mapping.table.equals(table))
                return false;

            if(mapping.nodeMapping.getResourceClass() != nodeMapping.getResourceClass())
                return false;

            int count = mapping.nodeMapping.getResourceClass().getPatternPartsCount();

            for(int i = 0; i < count; i++)
                if(!mapping.nodeMapping.getSqlColumn(i).equals(nodeMapping.getSqlColumn(i)))
                    return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            return table.hashCode();
        }
    }


    private static LinkedHashMap<ResourceClass, ArrayList<QuadNodeMapping>> getResourceMappings(
            SparqlDatabaseConfiguration configuration)
    {
        LinkedHashMap<ResourceClass, ArrayList<QuadNodeMapping>> resourceMappings = new LinkedHashMap<>();

        for(QuadMapping quadMapping : configuration.getMappings(null))
        {
            for(NodeMapping nodeMapping : new NodeMapping[] { quadMapping.getGraph(), quadMapping.getSubject(),
                    quadMapping.getPredicate(), quadMapping.getObject() })
            {
                if(nodeMapping instanceof ParametrisedMapping)
                {
                    ResourceClass resourceClass = nodeMapping.getResourceClass();

                    if(resourceClass instanceof LiteralClass)
                        continue;

                    ArrayList<QuadNodeMapping> mappings = resourceMappings.get(resourceClass);

                    if(mappings == null)
                    {
                        mappings = new ArrayList<QuadNodeMapping>();
                        resourceMappings.put(resourceClass, mappings);
                    }


                    Table table = null;

                    if(quadMapping instanceof SingleTableQuadMapping)
                        table = ((SingleTableQuadMapping) quadMapping).getTable();
                    else if(nodeMapping == quadMapping.getSubject())
                        table = ((JoinTableQuadMapping) quadMapping).getSubjectTable();
                    else if(nodeMapping == quadMapping.getObject())
                        table = ((JoinTableQuadMapping) quadMapping).getObjectTable();

                    QuadNodeMapping map = new QuadNodeMapping(table, (ParametrisedMapping) nodeMapping);

                    if(!mappings.contains(map))
                        mappings.add(map);
                }
            }
        }

        return resourceMappings;
    }


    private static String buildTableAccess(QuadNodeMapping mapping)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("select * from ");
        builder.append(mapping.table.getCode());
        builder.append(" where ");

        for(int c = 0; c < mapping.nodeMapping.getResourceClass().getPatternPartsCount(); c++)
        {
            if(c > 0)
                builder.append(" and ");

            builder.append(mapping.nodeMapping.getSqlColumn(c).getCode());
            builder.append(" is not null");
        }

        return builder.toString();
    }


    private static boolean isAditionalForeignKey(QuadNodeMapping left, QuadNodeMapping right)
            throws SQLException, IOException
    {
        ResourceClass resourceClass = left.nodeMapping.getResourceClass();

        boolean containsConstant = false;

        for(int c = 0; c < resourceClass.getPatternPartsCount(); c++)
            if(left.nodeMapping.getSqlColumn(c) instanceof ConstantColumn
                    || right.nodeMapping.getSqlColumn(c) instanceof ConstantColumn)
                containsConstant = true;

        if(!containsConstant)
            return false;


        StringBuilder builder = new StringBuilder();

        builder.append("select distinct 1 from (");
        builder.append(buildTableAccess(left));
        builder.append(") t1 right join (");
        builder.append(buildTableAccess(right));
        builder.append(") t2 on (");

        for(int c = 0; c < resourceClass.getPatternPartsCount(); c++)
        {
            if(c > 0)
                builder.append(" and ");

            Column c1 = left.nodeMapping.getSqlColumn(c);
            Column c2 = right.nodeMapping.getSqlColumn(c);

            if(c1 instanceof TableColumn)
                builder.append("t1.");

            builder.append(c1.getCode());

            builder.append(" = ");

            if(c2 instanceof TableColumn)
                builder.append("t2.");

            builder.append(c2.getCode());
        }

        builder.append(") where ");

        for(int c = 0; c < resourceClass.getPatternPartsCount(); c++)
        {
            if(c > 0)
                builder.append(" or ");

            Column c1 = left.nodeMapping.getSqlColumn(c);

            if(c1 instanceof TableColumn)
                builder.append("t1.");

            builder.append(c1.getCode());

            builder.append(" is null");
        }

        builder.append(" limit 1");


        try(Statement statement = connection.createStatement())
        {
            try(ResultSet result = statement.executeQuery(builder.toString()))
            {
                return !result.next();
            }
        }
    }


    private static boolean isUnjoinable(QuadNodeMapping left, QuadNodeMapping right) throws SQLException, IOException
    {
        ResourceClass resourceClass = left.nodeMapping.getResourceClass();

        StringBuilder builder = new StringBuilder();

        builder.append("select distinct 1 from (");
        builder.append(buildTableAccess(left));
        builder.append(") t1 inner join (");
        builder.append(buildTableAccess(right));
        builder.append(") t2 on (");

        for(int c = 0; c < resourceClass.getPatternPartsCount(); c++)
        {
            if(c > 0)
                builder.append(" and ");

            Column c1 = left.nodeMapping.getSqlColumn(c);
            Column c2 = right.nodeMapping.getSqlColumn(c);

            if(c1 instanceof TableColumn)
                builder.append("t1.");

            builder.append(c1.getCode());

            builder.append(" = ");

            if(c2 instanceof TableColumn)
                builder.append("t2.");

            builder.append(c2.getCode());
        }

        builder.append(") limit 1");


        try(Statement statement = connection.createStatement())
        {
            try(ResultSet result = statement.executeQuery(builder.toString()))
            {
                return !result.next();
            }
        }
    }


    public static void storeAdditionalForeignKeys(SparqlDatabaseConfiguration configuration,
            LinkedHashMap<ResourceClass, ArrayList<QuadNodeMapping>> resourceMappings) throws SQLException, IOException
    {
        ArrayList<QuadNodeMappingPair> mappingPairs = new ArrayList<QuadNodeMappingPair>();

        for(ArrayList<QuadNodeMapping> mappings : resourceMappings.values())
            for(int i = 0; i < mappings.size(); i++)
                for(int j = 0; j < mappings.size(); j++)
                    if(i != j)
                        mappingPairs.add(new QuadNodeMappingPair(mappings.get(i), mappings.get(j)));


        try(PreparedStatement statement = connection.prepareStatement(
                "insert into constraints.foreign_keys(__, parent_schema, parent_table, parent_columns, foreign_schema, foreign_table, foreign_columns) values (?,?,?,?,?,?,?)"))
        {
            AtomicInteger id = new AtomicInteger(0);

            mappingPairs.parallelStream().forEach(mappingPair -> {
                ResourceClass resourceClass = mappingPair.left.nodeMapping.getResourceClass();

                try
                {
                    if(isAditionalForeignKey(mappingPair.left, mappingPair.right))
                    {
                        String[] parentColumns = new String[resourceClass.getPatternPartsCount()];
                        String[] foreignColumns = new String[resourceClass.getPatternPartsCount()];

                        for(int c = 0; c < resourceClass.getPatternPartsCount(); c++)
                        {
                            parentColumns[c] = mappingPair.left.nodeMapping.getSqlColumn(c).getName();
                            foreignColumns[c] = mappingPair.right.nodeMapping.getSqlColumn(c).getName();
                        }

                        synchronized(ConstraintGenerator.class)
                        {
                            statement.setInt(1, id.getAndIncrement());
                            statement.setString(2, mappingPair.left.table.getSchema());
                            statement.setString(3, mappingPair.left.table.getName());
                            statement.setArray(4, connection.createArrayOf("varchar", parentColumns));
                            statement.setString(5, mappingPair.right.table.getSchema());
                            statement.setString(6, mappingPair.right.table.getName());
                            statement.setArray(7, connection.createArrayOf("varchar", foreignColumns));
                            statement.addBatch();
                        }
                    }
                }
                catch(SQLException | IOException e)
                {
                    throw new RuntimeException(e);
                }
            });

            statement.executeBatch();
        }
    }


    public static void storeUnjoinableColumns(SparqlDatabaseConfiguration configuration,
            LinkedHashMap<ResourceClass, ArrayList<QuadNodeMapping>> resourceMappings) throws SQLException, IOException
    {
        ArrayList<QuadNodeMappingPair> mappingPairs = new ArrayList<QuadNodeMappingPair>();

        for(ArrayList<QuadNodeMapping> mappings : resourceMappings.values())
            for(int i = 0; i < mappings.size(); i++)
                for(int j = i + 1; j < mappings.size(); j++)
                    mappingPairs.add(new QuadNodeMappingPair(mappings.get(i), mappings.get(j)));


        try(PreparedStatement statement = connection.prepareStatement(
                "insert into constraints.unjoinable_columns(__, left_schema, left_table, left_columns, right_schema, right_table, right_columns) values (?,?,?,?,?,?,?)"))
        {
            AtomicInteger id = new AtomicInteger(0);

            mappingPairs.parallelStream().forEach(mappingPair -> {
                ResourceClass resourceClass = mappingPair.left.nodeMapping.getResourceClass();

                try
                {
                    if(isUnjoinable(mappingPair.left, mappingPair.right))
                    {
                        String[] leftColumns = new String[resourceClass.getPatternPartsCount()];
                        String[] rightColumns = new String[resourceClass.getPatternPartsCount()];

                        for(int c = 0; c < resourceClass.getPatternPartsCount(); c++)
                        {
                            leftColumns[c] = mappingPair.left.nodeMapping.getSqlColumn(c).getName();
                            rightColumns[c] = mappingPair.right.nodeMapping.getSqlColumn(c).getName();
                        }

                        synchronized(ConstraintGenerator.class)
                        {
                            statement.setInt(1, id.getAndIncrement());
                            statement.setString(2, mappingPair.left.table.getSchema());
                            statement.setString(3, mappingPair.left.table.getName());
                            statement.setArray(4, connection.createArrayOf("varchar", leftColumns));
                            statement.setString(5, mappingPair.right.table.getSchema());
                            statement.setString(6, mappingPair.right.table.getName());
                            statement.setArray(7, connection.createArrayOf("varchar", rightColumns));
                            statement.addBatch();
                        }
                    }
                }
                catch(SQLException | IOException e)
                {
                    throw new RuntimeException(e);
                }
            });

            statement.executeBatch();
        }
    }


    public static void load() throws SQLException, IOException
    {
        DataSource pool = new DummyDataSource(connection);
        SparqlDatabaseConfiguration configuration = new IdsmConfiguration(null, pool, new DatabaseSchema(pool));
        LinkedHashMap<ResourceClass, ArrayList<QuadNodeMapping>> resourceMappings = getResourceMappings(configuration);

        storeUnjoinableColumns(configuration, resourceMappings);
        storeAdditionalForeignKeys(configuration, resourceMappings);
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        init();
        load();
        commit();
    }
}
