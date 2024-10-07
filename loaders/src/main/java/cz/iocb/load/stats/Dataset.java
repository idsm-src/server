package cz.iocb.load.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Condition;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.InternalNodeMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping.JoinColumns;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.SingleTableQuadMapping;
import cz.iocb.sparql.engine.mapping.classes.InternalResourceClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.translator.UsedVariable;
import cz.iocb.sparql.engine.translator.UsedVariables;
import cz.iocb.sparql.engine.translator.imcode.SqlEmptySolution;
import cz.iocb.sparql.engine.translator.imcode.SqlIntercode;
import cz.iocb.sparql.engine.translator.imcode.SqlJoin;
import cz.iocb.sparql.engine.translator.imcode.SqlTableAccess;
import cz.iocb.sparql.engine.translator.imcode.SqlUnion;



public class Dataset
{
    private Collection<QuadMapping> mappings = new ArrayList<QuadMapping>();


    public Dataset()
    {
    }


    public Dataset(QuadMapping map)
    {
        mappings.add(map);
    }


    public Collection<QuadMapping> getMappings()
    {
        return mappings;
    }


    public void add(QuadMapping mapping)
    {
        mappings.add(mapping);
    }


    public void add(Dataset dataset)
    {
        mappings.addAll(dataset.mappings);
    }


    private static void processNodeMapping(DatabaseSchema schema, Table table, NodeMapping nodemap, String name,
            UsedVariables variables, Condition condition)
    {
        if(name == null)
            return;

        ResourceClass resourceClass = nodemap.getResourceClass(null);
        List<Column> columns = nodemap.getColumns(null);
        variables.add(new UsedVariable(name, resourceClass, columns, false));

        for(Column column : columns)
            if(schema.isNullableColumn(table, column))
                condition.addIsNotNull(column);
    }


    public SqlIntercode translate(Request request, String subject, String object)
    {
        DatabaseSchema schema = request.getConfiguration().getDatabaseSchema();

        List<SqlIntercode> branches = new ArrayList<SqlIntercode>();

        for(QuadMapping mapping : mappings)
        {
            switch(mapping)
            {
                case SingleTableQuadMapping map ->
                {
                    Table table = map.getTable();
                    Condition condition = new Condition();
                    UsedVariables variables = new UsedVariables();

                    processNodeMapping(schema, table, map.getSubject(), subject, variables, condition);
                    processNodeMapping(schema, table, map.getObject(), object, variables, condition);

                    Conditions conditions = Conditions.and(map.getConditions(), condition);
                    branches.add(SqlTableAccess.create(table, conditions, variables));
                }

                case JoinTableQuadMapping map ->
                {
                    List<Table> tables = map.getTables();
                    List<JoinColumns> joinColumnsPairs = map.getJoinColumnsPairs();

                    ResourceClass resourceClass = null;
                    String node = null;

                    SqlIntercode result = SqlEmptySolution.get();

                    for(int i = 0; i < tables.size(); i++)
                    {
                        Table table = tables.get(i);
                        Condition condition = new Condition();
                        UsedVariables variables = new UsedVariables();

                        if(i == map.getSubjectTableIdx())
                            processNodeMapping(schema, table, map.getSubject(), subject, variables, condition);

                        if(i == map.getObjectTableIdx())
                            processNodeMapping(schema, table, map.getObject(), object, variables, condition);

                        if(i > 0)
                        {
                            List<Column> columns = joinColumnsPairs.get(i - 1).getRightColumns();
                            NodeMapping nodeMapping = new InternalNodeMapping(resourceClass, columns);
                            processNodeMapping(schema, table, nodeMapping, node, variables, condition);
                        }

                        if(i < tables.size() - 1)
                        {
                            resourceClass = new InternalResourceClass(joinColumnsPairs.get(i).getLeftColumns().size());
                            List<Column> columns = joinColumnsPairs.get(i).getLeftColumns();
                            NodeMapping nodeMapping = new InternalNodeMapping(resourceClass, columns);
                            node = "@var" + i;
                            processNodeMapping(schema, table, nodeMapping, node, variables, condition);
                        }

                        Conditions conditions = Conditions.and(map.getConditions().get(i), condition);
                        SqlIntercode acess = SqlTableAccess.create(table, conditions, variables);

                        result = SqlJoin.join(result, acess);
                    }

                    branches.add(result);
                }

                default ->
                {
                    throw new UnsupportedOperationException();
                }
            }
        }


        HashSet<String> restrictions = new HashSet<String>();

        if(subject != null)
            restrictions.add(subject);

        if(object != null)
            restrictions.add(object);

        return SqlUnion.union(branches).optimize(request, restrictions, false);
    }
}
