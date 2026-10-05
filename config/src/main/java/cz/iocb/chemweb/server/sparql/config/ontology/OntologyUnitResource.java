package cz.iocb.chemweb.server.sparql.config.ontology;

import static cz.iocb.sparql.engine.database.SqlType.INT2;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.box;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.iri;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.constant;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.expression;
import static cz.iocb.sparql.engine.mapping.classes.DerivedClass.subtract;
import static cz.iocb.sparql.engine.mapping.classes.DerivedClass.unionize;
import java.util.List;
import java.util.Set;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.Unit;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.request.Request;



public class OntologyUnitResource extends UserIriClass
{
    private final OntologyResource resource;
    private final short unit;
    private final Unit definition;


    public OntologyUnitResource(String name, OntologyResource resource, short unit)
    {
        super(name, List.of(INT4), Set.of(resource, iri, box));

        this.resource = resource;
        this.unit = unit;
        this.definition = unit == OntologyResource.unitUncategorized ? null : resource.getUnit(unit);

        if(unit != OntologyResource.unitUncategorized && definition == null)
            throw new IllegalArgumentException("unknown ontology unit " + unit);
    }


    @Override
    public boolean match(Request request, Iri iri)
    {
        String val = iri.getValue();

        if(definition == null)
            return resource.findUnit(val) == null && resource.findResourceId(request, val) != null;

        return definition.pattern().matcher(val).matches() && definition.equals(resource.findUnit(val));
    }


    @Override
    public List<Column> toColumns(Request request, Iri iri)
    {
        assert match(request, iri);

        return List.of(resource.toColumns(request, iri).get(1));
    }


    @Override
    public List<Column> toGeneralClass(ResourceClass superClass, List<Column> columns, boolean canBeNull)
    {
        assert isSubclassOf(superClass);

        ResourceClass targetClass = superClass.getEffectiveClass();

        if(targetClass.equals(this))
            return columns;

        Column id = columns.get(0);

        if(targetClass.equals(resource))
        {
            Column unitColumn = !canBeNull ? constant(unit, INT2) :
                    expression(INT2, "CASE WHEN %s IS NOT NULL THEN '%d'::int2 END", id, unit);

            return List.of(unitColumn, id);
        }

        return resource.toGeneralClass(targetClass, List.of(constant(unit, INT2), id), canBeNull);
    }


    @Override
    public List<Column> fromGeneralClass(ResourceClass superClass, List<Column> columns, boolean checkOptional)
    {
        if(superClass.equals(this))
            return columns;

        ResourceClass sourceClass = superClass.getEffectiveClass();

        assert isSubclassOf(sourceClass);

        // the check is needless when every IRI of the superclass belongs to this class
        boolean check = !checkOptional && !superClass.isSubclassOf(unionize(this, subtract(box, iri)));

        List<Column> general = sourceClass.equals(resource) ? columns :
                resource.fromGeneralClass(sourceClass, columns, checkOptional);

        if(!check)
            return List.of(general.get(1));

        return List.of(expression(INT4, "CASE WHEN %s = '%d'::int2 THEN %s END", general.get(0), unit, general.get(1)));
    }


    @Override
    public List<Column> toOrderColumns(List<Column> columns)
    {
        return resource.toOrderColumns(List.of(constant(unit, INT2), columns.get(0)));
    }


    @Override
    public String getPrefix(List<Column> columns)
    {
        return resource.getPrefix(List.of(constant(unit, INT2), columns.get(0)));
    }


    @Override
    public int getCheckCost()
    {
        return definition == null ? resource.getCheckCost() : 0;
    }


    public OntologyResource getResource()
    {
        return resource;
    }


    public short getUnit()
    {
        return unit;
    }


    @Override
    public boolean equals(Object object)
    {
        if(object == this)
            return true;

        if(!super.equals(object))
            return false;

        OntologyUnitResource other = (OntologyUnitResource) object;

        return name.equals(other.name) && unit == other.unit;
    }
}
