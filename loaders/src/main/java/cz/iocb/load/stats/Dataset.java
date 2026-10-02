package cz.iocb.load.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Condition;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.SourceTable;
import cz.iocb.sparql.engine.imcode.SqlEmptySolution;
import cz.iocb.sparql.engine.imcode.SqlIntercode;
import cz.iocb.sparql.engine.imcode.SqlIntercode.Restrictions;
import cz.iocb.sparql.engine.imcode.SqlJoin;
import cz.iocb.sparql.engine.imcode.SqlTableAccess;
import cz.iocb.sparql.engine.imcode.SqlUnion;
import cz.iocb.sparql.engine.mapping.InternalNodeMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping.JoinColumns;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.SingleTableQuadMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.InternalResourceClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.rdf.Variable;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.translator.VariableBinding;
import cz.iocb.sparql.engine.translator.VariableBindings;



public class Dataset
{
    private Collection<QuadMapping> mappings = new ArrayList<>();


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


    private static void processNodeMapping(DatabaseSchema schema, SourceTable table, TermMapping nodemap, Variable name,
            VariableBindings bindings, Condition condition)
    {
        List<Column> columns = nodemap.getColumns(null);

        for(Column column : columns)
            if(schema.isNullableColumn(table, column))
                condition.addIsNotNull(column);

        if(name == null)
            return;

        ResourceClass resourceClass = nodemap.getResourceClass(null);
        bindings.add(new VariableBinding(name, resourceClass, columns, false));
    }


    private static Set<Column> distinctColumns(boolean distinct, VariableBindings bindings, Condition condition)
    {
        if(!distinct)
            return Set.of();

        // all table columns touched by the mapped terms, including those bound to constants by the pattern
        Set<Column> columns = new HashSet<>(bindings.getNonConstantColumns());
        columns.addAll(condition.getNonConstantColumns());

        return columns;
    }


    public SqlIntercode translate(Request request, Variable subject, Variable object)
    {
        DatabaseSchema schema = request.getConfiguration().getDatabaseSchema();

        List<SqlIntercode> branches = new ArrayList<>();

        for(QuadMapping mapping : mappings)
        {
            switch(mapping)
            {
                case SingleTableQuadMapping map ->
                {
                    SourceTable table = map.getTable();
                    Condition condition = new Condition();
                    VariableBindings bindings = new VariableBindings();

                    processNodeMapping(schema, table, map.getSubject(), subject, bindings, condition);
                    processNodeMapping(schema, table, map.getObject(), object, bindings, condition);

                    Conditions conditions = Conditions.and(map.getConditions(), condition);

                    branches.add(SqlTableAccess.create(request, table, conditions, bindings, false,
                            distinctColumns(map.isDistinct(), bindings, condition)));
                }

                case JoinTableQuadMapping map ->
                {
                    List<SourceTable> tables = map.getTables();
                    List<JoinColumns> joinColumnsPairs = map.getJoinColumnsPairs();

                    ResourceClass resourceClass = null;
                    Variable node = null;

                    SqlIntercode result = SqlEmptySolution.get();

                    for(int i = 0; i < tables.size(); i++)
                    {
                        SourceTable table = tables.get(i);
                        Condition condition = new Condition();
                        VariableBindings bindings = new VariableBindings();

                        if(i == map.getSubjectTableIdx())
                            processNodeMapping(schema, table, map.getSubject(), subject, bindings, condition);

                        if(i == map.getObjectTableIdx())
                            processNodeMapping(schema, table, map.getObject(), object, bindings, condition);

                        if(i > 0)
                        {
                            List<Column> columns = joinColumnsPairs.get(i - 1).getRightColumns();
                            TermMapping nodeMapping = new InternalNodeMapping(resourceClass, columns);
                            processNodeMapping(schema, table, nodeMapping, node, bindings, condition);
                        }

                        if(i < tables.size() - 1)
                        {
                            List<Column> columns = joinColumnsPairs.get(i).getLeftColumns();
                            resourceClass = new InternalResourceClass(columns.stream().map(Column::getType).toList());
                            TermMapping nodeMapping = new InternalNodeMapping(resourceClass, columns);
                            node = new Variable("@var" + i);
                            processNodeMapping(schema, table, nodeMapping, node, bindings, condition);
                        }

                        Conditions conditions = Conditions.and(map.getConditions().get(i), condition);
                        SqlIntercode acess = SqlTableAccess.create(request, table, conditions, bindings, false,
                                distinctColumns(map.getDistinct().get(i), bindings, condition));

                        result = SqlJoin.join(request, result, acess);
                    }

                    branches.add(result);
                }

                default ->
                {
                    throw new UnsupportedOperationException();
                }
            }
        }


        Restrictions restrictions = new Restrictions();

        if(subject != null)
            restrictions.add(subject);

        if(object != null)
            restrictions.add(object);

        return SqlUnion.union(request, branches).optimize(request, restrictions, false, false);
    }
}
