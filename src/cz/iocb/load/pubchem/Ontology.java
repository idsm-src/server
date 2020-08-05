package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Ontology extends Updater
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


    protected static abstract class OntologyQueryResultProcessor extends QueryResultProcessor
    {
        protected final PreparedStatement statement;
        protected int count = 0;

        protected OntologyQueryResultProcessor(String sparql, PreparedStatement statement)
        {
            super(sparql);
            this.statement = statement;
        }

        protected Identifier getId(String name)
        {
            return Ontology.getId(solution.getResource(name));
        }

        protected void setValue(int idx, Identifier identifier) throws SQLException
        {
            statement.setShort(idx, identifier.unit);
            statement.setInt(idx + 1, identifier.id);
        }

        protected void addBatch() throws SQLException
        {
            statement.addBatch();

            if(++count % batchSize == 0)
                statement.executeBatch();
        }

        @Override
        public void load(Model model) throws IOException, SQLException
        {
            super.load(model);

            if(count % batchSize != 0)
                statement.executeBatch();
        }
    }


    private static final List<Unit> units = new ArrayList<Unit>();
    private static final HashMap<String, Integer> blankResourceIds = new HashMap<String, Integer>();
    private static final HashMap<String, Integer> uncategorizedResources = new HashMap<String, Integer>();

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



    public static void init() throws SQLException
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

        uncategorizedResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#active", 0);
        uncategorizedResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inactive", 1);
        uncategorizedResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inconclusive", 2);
        uncategorizedResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#unspecified", 3);
        uncategorizedResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#probe", 4);
        uncategorizedResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#is_active_ingredient_of", 5);
        uncategorizedResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent", 6);
        uncategorizedResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#FDAApprovedDrugs", 7);
        uncategorizedResources.put("http://purl.org/spar/fabio/ReviewArticle", 8);
        uncategorizedResources.put("http://purl.org/spar/fabio/JournalArticle", 9);
        uncategorizedResources.put("http://www.biopax.org/release/biopax-level3.owl#SmallMolecule", 10);
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

        return new Identifier(unitUncategorized, id);
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
            }

            return new Identifier(unitUncategorized, id);
        }
    }


    private static void loadClasses(Model model) throws IOException, SQLException
    {
        connection.createStatement().execute("delete from ontology_resource_classes");

        try(PreparedStatement statement = connection
                .prepareStatement("insert into ontology_resource_classes(class_unit, class_id) values (?,?)"))
        {
            new OntologyQueryResultProcessor(loadQuery("ontology/classes.sparql"), statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    setValue(1, getId("iri"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadProperties(Model model) throws IOException, SQLException
    {
        connection.createStatement().execute("delete from ontology_resource_properties");

        try(PreparedStatement statement = connection
                .prepareStatement("insert into ontology_resource_properties(property_unit, property_id) values (?,?)"))
        {
            new OntologyQueryResultProcessor(loadQuery("ontology/properties.sparql"), statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    setValue(1, getId("iri"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadIndividuals(Model model) throws IOException, SQLException
    {
        connection.createStatement().execute("delete from ontology_resource_individuals");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_individuals(individual_unit, individual_id) values (?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery("?iri rdf:type owl:NamedIndividual"), statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    setValue(1, getId("iri"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadResourceLabels(Model model) throws IOException, SQLException
    {
        connection.createStatement().execute("delete from ontology_resource_labels");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_labels(resource_unit, resource_id, label) values (?,?,?)"))
        {
            new OntologyQueryResultProcessor(loadQuery("ontology/labels.sparql"), statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    setValue(1, getId("iri"));
                    statement.setString(3, getString("label"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadSuperClasses(Model model) throws IOException, SQLException
    {
        connection.createStatement().execute("delete from ontology_resource_superclasses");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_superclasses(class_unit, class_id, superclass_unit, superclass_id) values (?,?,?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery("?class rdfs:subClassOf ?superclass"), statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    setValue(1, getId("class"));
                    setValue(3, getId("superclass"));
                    addBatch();
                }
            }.load(model);

            new OntologyQueryResultProcessor(loadQuery("ontology/superclasses.sparql"), statement)
            {
                Identifier thing = Ontology.getId("http://www.w3.org/2002/07/owl#Thing");

                @Override
                public void parse() throws SQLException, IOException
                {
                    setValue(1, getId("class"));
                    setValue(3, thing);
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadSuperProperties(Model model) throws IOException, SQLException
    {
        connection.createStatement().execute("delete from ontology_resource_superproperties");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_superproperties(property_unit, property_id, superproperty_unit, superproperty_id) values (?,?,?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery("?property rdfs:subPropertyOf ?superproperty"), statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    setValue(1, getId("property"));
                    setValue(3, getId("superproperty"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadDomains(Model model) throws SQLException, IOException
    {
        connection.createStatement().execute("delete from ontology_resource_domains");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_domains(property_unit, property_id, domain_unit, domain_id) values (?,?,?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery("?property rdfs:domain ?domain"), statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    setValue(1, getId("property"));
                    setValue(3, getId("domain"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadRanges(Model model) throws SQLException, IOException
    {
        connection.createStatement().execute("delete from ontology_resource_ranges");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_ranges(property_unit, property_id, range_unit, range_id) values (?,?,?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery("?property rdfs:range ?range"), statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    setValue(1, getId("property"));
                    setValue(3, getId("range"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadSomeValuesFromRestriction(Model model) throws SQLException, IOException
    {
        connection.createStatement().execute("delete from ontology_resource_somevaluesfrom_restrictions");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_somevaluesfrom_restrictions(restriction_id, property_unit, property_id, class_unit, class_id) values (?,?,?,?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery(
                    "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:someValuesFrom ?class"),
                    statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    Identifier restriction = getId("restriction");

                    if(restriction.unit != Ontology.unitBlank)
                        throw new IOException();

                    statement.setInt(1, restriction.id);
                    setValue(2, getId("property"));
                    setValue(4, getId("class"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadAllValuesFromRestriction(Model model) throws SQLException, IOException
    {
        connection.createStatement().execute("delete from ontology_resource_allvaluesfrom_restrictions");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_allvaluesfrom_restrictions(restriction_id, property_unit, property_id, class_unit, class_id) values (?,?,?,?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery(
                    "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:allValuesFrom ?class"),
                    statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    Identifier restriction = getId("restriction");

                    if(restriction.unit != Ontology.unitBlank)
                        throw new IOException();

                    statement.setInt(1, restriction.id);
                    setValue(2, getId("property"));
                    setValue(4, getId("class"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadCardinalityRestriction(Model model) throws SQLException, IOException
    {
        connection.createStatement().execute("delete from ontology_resource_cardinality_restrictions");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_cardinality_restrictions(restriction_id, property_unit, property_id, cardinality) values (?,?,?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery(
                    "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:cardinality ?cardinality"),
                    statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    Identifier restriction = getId("restriction");

                    if(restriction.unit != Ontology.unitBlank)
                        throw new IOException();

                    statement.setInt(1, restriction.id);
                    setValue(2, getId("property"));
                    statement.setInt(4, getInt("cardinality"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadMinCardinalityRestriction(Model model) throws SQLException, IOException
    {
        connection.createStatement().execute("delete from ontology_resource_mincardinality_restrictions");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_mincardinality_restrictions(restriction_id, property_unit, property_id, cardinality) values (?,?,?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery(
                    "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:minCardinality ?cardinality"),
                    statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    Identifier restriction = getId("restriction");

                    if(restriction.unit != Ontology.unitBlank)
                        throw new IOException();

                    statement.setInt(1, restriction.id);
                    setValue(2, getId("property"));
                    statement.setInt(4, getInt("cardinality"));
                    addBatch();
                }
            }.load(model);
        }
    }


    private static void loadMaxCardinalityRestriction(Model model) throws SQLException, IOException
    {
        connection.createStatement().execute("delete from ontology_resource_maxcardinality_restrictions");

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into ontology_resource_maxcardinality_restrictions(restriction_id, property_unit, property_id, cardinality) values (?,?,?,?)"))
        {
            new OntologyQueryResultProcessor(patternQuery(
                    "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:maxCardinality ?cardinality"),
                    statement)
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    Identifier restriction = getId("restriction");

                    if(restriction.unit != Ontology.unitBlank)
                        throw new IOException();

                    statement.setInt(1, restriction.id);
                    setValue(2, getId("property"));
                    statement.setInt(4, getInt("cardinality"));
                    addBatch();
                }
            }.load(model);
        }
    }


    static void storeUncategorizedResources() throws SQLException, IOException
    {
        connection.createStatement().execute("delete from ontology_resources__reftable");

        try(PreparedStatement insertStatement = connection
                .prepareStatement("insert into ontology_resources__reftable(resource_id, iri) values (?,?)"))
        {
            for(Entry<String, Integer> entry : uncategorizedResources.entrySet())
            {
                insertStatement.setInt(1, entry.getValue());
                insertStatement.setString(2, entry.getKey());
                insertStatement.addBatch();
            }

            insertStatement.executeBatch();
        }
    }


    public static void load() throws IOException, SQLException
    {
        List<Model> models = Collections.synchronizedList(new ArrayList<Model>(200));

        processFiles("ontology", ".*", file -> {
            String lang = file.endsWith(".ttl") ? "TTL" : null;
            Model model = getModel(file, lang);
            models.add(model);
        });


        Model ontologyModel = ModelFactory.createDefaultModel();

        for(Model model : models)
        {
            ontologyModel = ontologyModel.union(model);
            model.close();
        }


        init();

        loadClasses(ontologyModel);
        loadProperties(ontologyModel);
        loadIndividuals(ontologyModel);
        loadResourceLabels(ontologyModel);

        loadSuperClasses(ontologyModel);

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
}
