package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.ModelTableLoader;



public class Ontology extends Loader
{
    private static class Unit
    {
        short id;
        int valueOffset;
        String pattern;
    }


    protected static class Identifier
    {
        short unit;
        int id;

        Identifier(short unit, int id)
        {
            this.unit = unit;
            this.id = id;
        }
    }


    protected static abstract class OntologyModelTableLoader extends ModelTableLoader
    {
        public OntologyModelTableLoader(Model model, String sparql, String sql)
        {
            super(model, sparql, sql);
        }

        public Identifier getId(String name)
        {
            return Ontology.getId(solution.getResource(name));
        }

        public void setValue(int idx, Identifier identifier) throws SQLException
        {
            setValue(idx, identifier.unit);
            setValue(idx + 1, identifier.id);
        }
    }


    private static final List<Unit> units = new ArrayList<Unit>();
    private static final HashMap<String, Integer> blankResourceIds = new HashMap<String, Integer>();
    private static final HashMap<String, Integer> uncategorizedResources = new HashMap<String, Integer>();
    private static final HashMap<String, Integer> newUncategorizedResources = new HashMap<String, Integer>();

    public static final short unitUncategorized = 0;
    public static final short unitBlank = 1;
    public static final short unitSIO = 2;
    public static final short unitCHEMINF = 3;
    public static final short unitBAO = 4;
    public static final short unitGO = 5;
    public static final short unitPR = 6;
    public static final short unitTaxonomy = 11;
    public static final short unitPR1 = 32;
    public static final short unitPR2 = 33;
    public static final short unitAT = 34;
    public static final short unitZDBGENE = 35;


    static
    {
        try(Connection connection = getConnection())
        {
            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement.executeQuery(
                        "select unit_id, value_offset - 1, pattern from ontology_resource_categories__reftable"))
                {
                    while(result.next())
                    {
                        Unit unit = new Unit();
                        unit.id = result.getShort(1);
                        unit.valueOffset = result.getInt(2);
                        unit.pattern = result.getString(3);

                        units.add(unit);
                    }
                }
            }


            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement
                        .executeQuery("select resource_id, iri from ontology_resources__reftable"))
                {
                    while(result.next())
                        uncategorizedResources.put(result.getString(2), result.getInt(1));
                }
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public static Identifier getId(String iri)
    {
        for(Unit unit : units)
        {
            if(iri.matches(unit.pattern))
            {
                String tail = iri.substring(unit.valueOffset);
                int id = 0;

                if(unit.id == unitPR1 || unit.id == unitPR2)
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
                else
                {
                    id = Integer.parseInt(tail);
                }

                return new Identifier(unit.id, id);
            }
        }


        Integer id = uncategorizedResources.get(iri);

        if(id == null)
            return null;

        Identifier result = new Identifier(unitUncategorized, id);
        result.unit = unitUncategorized;
        result.id = id;

        return result;
    }


    private static int code(char value)
    {
        return value > '9' ? 10 + value - 'A' : value - '0';
    }


    public static Identifier getId(Resource resource)
    {
        if(resource.isAnon())
        {
            String blankLabel = resource.getId().getLabelString();
            Integer id = blankResourceIds.get(blankLabel);

            if(id == null)
            {
                id = blankResourceIds.size() + 1;
                blankResourceIds.put(blankLabel, id);
            }

            return new Identifier(unitBlank, id);
        }
        else
        {
            String iri = resource.getURI();
            Identifier result = getId(iri);

            if(result != null)
                return result;


            Integer id = uncategorizedResources.get(iri);

            if(id == null)
            {
                id = uncategorizedResources.size();
                uncategorizedResources.put(iri, id);
                newUncategorizedResources.put(iri, id);
            }

            return new Identifier(unitUncategorized, id);
        }
    }


    private static void loadClasses(Model model) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, loadQuery("ontology/classes.sparql"),
                "insert into ontology_resource_classes(class_unit, class_id) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getId("iri"));
            }
        }.load();
    }


    private static void loadProperties(Model model) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, loadQuery("ontology/properties.sparql"),
                "insert into ontology_resource_properties(property_unit, property_id) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getId("iri"));
            }
        }.load();
    }


    private static void loadIndividuals(Model model) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, patternQuery("?iri rdf:type owl:NamedIndividual"),
                "insert into ontology_resource_individuals(individual_unit, individual_id) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getId("iri"));
            }
        }.load();
    }


    private static void loadResourceLabels(Model model) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, loadQuery("ontology/labels.sparql"),
                "insert into ontology_resource_labels(resource_unit, resource_id, label) values (?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getId("iri"));
                setValue(3, getLiteralValue("label"));
            }
        }.load();
    }


    private static void loadSuperClasses(Model model) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, patternQuery("?class rdfs:subClassOf ?superclass"),
                "insert into ontology_resource_superclasses(class_unit, class_id, superclass_unit, superclass_id) values (?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getId("class"));
                setValue(3, getId("superclass"));
            }
        }.load();
    }


    private static void loadMissingSuperClasses(Model model) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, loadQuery("ontology/superclasses.sparql"),
                "insert into ontology_resource_superclasses(class_unit, class_id, superclass_unit, superclass_id) values (?,?,?,?)")
        {
            Identifier thing = Ontology.getId("http://www.w3.org/2002/07/owl#Thing");

            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getId("class"));
                setValue(3, thing);
            }
        }.load();
    }


    private static void loadSuperProperties(Model model) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, patternQuery("?property rdfs:subPropertyOf ?superproperty"),
                "insert into ontology_resource_superproperties(property_unit, property_id, superproperty_unit, superproperty_id) values (?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getId("property"));
                setValue(3, getId("superproperty"));
            }
        }.load();
    }


    private static void loadDomains(Model model) throws SQLException, IOException
    {
        new OntologyModelTableLoader(model, patternQuery("?property rdfs:domain ?domain"),
                "insert into ontology_resource_domains(property_unit, property_id, domain_unit, domain_id) values (?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getId("property"));
                setValue(3, getId("domain"));
            }
        }.load();
    }


    private static void loadRanges(Model model) throws SQLException, IOException
    {
        new OntologyModelTableLoader(model, patternQuery("?property rdfs:range ?range"),
                "insert into ontology_resource_ranges(property_unit, property_id, range_unit, range_id) values (?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getId("property"));
                setValue(3, getId("range"));
            }
        }.load();
    }


    private static void loadSomeValuesFromRestriction(Model model) throws SQLException, IOException
    {
        new OntologyModelTableLoader(model,
                patternQuery(
                        "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:someValuesFrom ?class"),
                "insert into ontology_resource_somevaluesfrom_restrictions(restriction_id, property_unit, property_id, class_unit, class_id) values (?,?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                Identifier restriction = getId("restriction");

                if(restriction.unit != Ontology.unitBlank)
                    throw new IOException();

                setValue(1, restriction.id);
                setValue(2, getId("property"));
                setValue(4, getId("class"));
            }
        }.load();
    }


    private static void loadAllValuesFromRestriction(Model model) throws SQLException, IOException
    {
        new OntologyModelTableLoader(model,
                patternQuery(
                        "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:allValuesFrom ?class"),
                "insert into ontology_resource_allvaluesfrom_restrictions(restriction_id, property_unit, property_id, class_unit, class_id) values (?,?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                Identifier restriction = getId("restriction");

                if(restriction.unit != Ontology.unitBlank)
                    throw new IOException();

                setValue(1, restriction.id);
                setValue(2, getId("property"));
                setValue(4, getId("class"));
            }
        }.load();
    }


    private static void loadCardinalityRestriction(Model model) throws SQLException, IOException
    {
        new OntologyModelTableLoader(model, patternQuery(
                "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:cardinality ?cardinality"),
                "insert into ontology_resource_cardinality_restrictions(restriction_id, property_unit, property_id, cardinality) values (?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                Identifier restriction = getId("restriction");

                if(restriction.unit != Ontology.unitBlank)
                    throw new IOException();

                setValue(1, restriction.id);
                setValue(2, getId("property"));
                setValue(4, getIntValue("cardinality"));
            }
        }.load();
    }


    private static void loadMinCardinalityRestriction(Model model) throws SQLException, IOException
    {
        new OntologyModelTableLoader(model, patternQuery(
                "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:minCardinality ?cardinality"),
                "insert into ontology_resource_mincardinality_restrictions(restriction_id, property_unit, property_id, cardinality) values (?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                Identifier restriction = getId("restriction");

                if(restriction.unit != Ontology.unitBlank)
                    throw new IOException();

                setValue(1, restriction.id);
                setValue(2, getId("property"));
                setValue(4, getIntValue("cardinality"));
            }
        }.load();
    }


    private static void loadMaxCardinalityRestriction(Model model) throws SQLException, IOException
    {
        new OntologyModelTableLoader(model, patternQuery(
                "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:maxCardinality ?cardinality"),
                "insert into ontology_resource_maxcardinality_restrictions(restriction_id, property_unit, property_id, cardinality) values (?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                Identifier restriction = getId("restriction");

                if(restriction.unit != Ontology.unitBlank)
                    throw new IOException();

                setValue(1, restriction.id);
                setValue(2, getId("property"));
                setValue(4, getIntValue("cardinality"));
            }
        }.load();
    }


    static void storeUncategorizedResources() throws SQLException, IOException
    {
        try(Connection connection = getConnection())
        {
            try(PreparedStatement insertStatement = connection
                    .prepareStatement("insert into ontology_resources__reftable(resource_id, iri) values (?,?)"))
            {
                for(Entry<String, Integer> entry : newUncategorizedResources.entrySet())
                {
                    insertStatement.setInt(1, entry.getValue());
                    insertStatement.setString(2, entry.getKey());
                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }
        }
    }


    public static void loadDirectory(String path) throws IOException, SQLException
    {
        Model ontologyModel = ModelFactory.createDefaultModel();
        File dir = new File(getPubchemDirectory() + path);

        for(File file : dir.listFiles())
        {
            String lang = file.getName().endsWith(".ttl") ? "TTL" : null;
            Model model = getModel(path + File.separatorChar + file.getName(), lang);
            ontologyModel = ontologyModel.union(model);
            model.close();
        }

        loadClasses(ontologyModel);
        loadProperties(ontologyModel);
        loadIndividuals(ontologyModel);
        loadResourceLabels(ontologyModel);

        loadSuperClasses(ontologyModel);
        loadMissingSuperClasses(ontologyModel);

        loadSuperProperties(ontologyModel);
        loadDomains(ontologyModel);
        loadRanges(ontologyModel);

        loadSomeValuesFromRestriction(ontologyModel);
        loadAllValuesFromRestriction(ontologyModel);
        loadCardinalityRestriction(ontologyModel);
        loadMinCardinalityRestriction(ontologyModel);
        loadMaxCardinalityRestriction(ontologyModel);

        storeUncategorizedResources();

        ontologyModel.close();
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        loadDirectory("ontology");
    }
}
