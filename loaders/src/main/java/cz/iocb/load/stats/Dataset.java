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



/**
 * Set of quad mappings whose triples are counted together by the statistics, translated into the code of a pattern
 * binding the subjects and the objects of the triples.
 */
public class Dataset
{
    /**
     * Quad mappings of the dataset.
     */
    private Collection<QuadMapping> mappings = new ArrayList<>();


    /**
     * Creates an empty dataset.
     */
    public Dataset()
    {
    }


    /**
     * Creates the dataset of one quad mapping.
     *
     * @param map the quad mapping
     */
    public Dataset(QuadMapping map)
    {
        mappings.add(map);
    }


    /**
     * Returns the quad mappings of the dataset.
     *
     * @return the quad mappings
     */
    public Collection<QuadMapping> getMappings()
    {
        return mappings;
    }


    /**
     * Adds a quad mapping to the dataset.
     *
     * @param mapping the quad mapping
     */
    public void add(QuadMapping mapping)
    {
        mappings.add(mapping);
    }


    /**
     * Adds the quad mappings of another dataset to the dataset.
     *
     * @param dataset the other dataset
     */
    public void add(Dataset dataset)
    {
        mappings.addAll(dataset.mappings);
    }


    /**
     * Binds a variable to the columns of a term mapping and requires the nullable columns that the resource class does
     * not leave optional to be not null.
     *
     * @param request the current request
     * @param table the table of the term mapping
     * @param nodemap the term mapping
     * @param name the bound variable, or null when only the condition is needed
     * @param bindings the bindings to extend
     * @param condition the condition of the table to extend
     */
    private static void processNodeMapping(Request request, SourceTable table, TermMapping nodemap, Variable name,
            VariableBindings bindings, Condition condition)
    {
        DatabaseSchema schema = request.getConfiguration().getDatabaseSchema();
        ResourceClass resourceClass = nodemap.getResourceClass(request);
        List<Column> columns = nodemap.getColumns(request);

        // the optional columns of the class may be null
        for(int i = 0; i < columns.size(); i++)
            if(!resourceClass.isOptionalColumn(i) && schema.isNullableColumn(table, columns.get(i)))
                condition.addIsNotNull(columns.get(i));

        if(name == null)
            return;

        bindings.add(new VariableBinding(name, resourceClass, columns, false));
    }


    /**
     * Returns the columns that a table access of a distinct mapping keeps distinct: all the table columns touched by
     * the mapped terms.
     *
     * @param distinct whether the mapping of the table is distinct
     * @param bindings the bindings of the table access
     * @param condition the condition of the table access
     * @return the distinct columns, empty when the mapping is not distinct
     */
    private static Set<Column> distinctColumns(boolean distinct, VariableBindings bindings, Condition condition)
    {
        if(!distinct)
            return Set.of();

        // all table columns touched by the mapped terms, including those bound to constants by the pattern
        Set<Column> columns = new HashSet<>(bindings.getNonConstantColumns());
        columns.addAll(condition.getNonConstantColumns());

        return columns;
    }


    /**
     * Translates the dataset into the union of the table accesses of its quad mappings, optimized for the given
     * variables.
     *
     * @param request the current request
     * @param subject the variable bound to the subjects, or null when they are not needed
     * @param object the variable bound to the objects, or null when they are not needed
     * @return the code of the pattern
     * @throws UnsupportedOperationException if a mapping is of an unknown kind
     */
    public SqlIntercode translate(Request request, Variable subject, Variable object)
    {
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

                    processNodeMapping(request, table, map.getSubject(), subject, bindings, condition);
                    processNodeMapping(request, table, map.getObject(), object, bindings, condition);

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
                            processNodeMapping(request, table, map.getSubject(), subject, bindings, condition);

                        if(i == map.getObjectTableIdx())
                            processNodeMapping(request, table, map.getObject(), object, bindings, condition);

                        if(i > 0)
                        {
                            List<Column> columns = joinColumnsPairs.get(i - 1).getRightColumns();
                            TermMapping nodeMapping = new InternalNodeMapping(resourceClass, columns);
                            processNodeMapping(request, table, nodeMapping, node, bindings, condition);
                        }

                        if(i < tables.size() - 1)
                        {
                            List<Column> columns = joinColumnsPairs.get(i).getLeftColumns();
                            resourceClass = new InternalResourceClass(columns.stream().map(Column::getType).toList());
                            TermMapping nodeMapping = new InternalNodeMapping(resourceClass, columns);
                            node = new Variable("@var" + i);
                            processNodeMapping(request, table, nodeMapping, node, bindings, condition);
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
                    throw new UnsupportedOperationException(mapping.getClass().getName());
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
