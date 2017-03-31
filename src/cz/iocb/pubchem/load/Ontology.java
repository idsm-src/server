package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.ModelTableLoader;



public class Ontology extends Loader
{
    protected static abstract class OntologyModelTableLoader extends ModelTableLoader
    {
        private Map<String, String> blankClasses = new HashMap<String, String>();
        private Map<String, String> blankProperties = new HashMap<String, String>();

        public OntologyModelTableLoader(Model model, String sparql, String sql)
        {
            super(model, sparql, sql);
        }

        private String getResourceIRI(String name, String blankPrefix, Map<String, String> blankNodes)
        {
            Resource resource = solution.getResource(name);

            if(resource == null)
                return null;

            if(!resource.isAnon())
                return resource.getURI();

            String blankLabel = resource.getId().getLabelString();
            String blankID = blankNodes.get(blankLabel);

            if(blankID == null)
            {
                blankID = blankPrefix + blankNodes.size();
                blankNodes.put(blankLabel, blankID);
            }

            return blankID;
        }

        public String getClassIRI(String name)
        {
            return getResourceIRI(name, "http://blanknodes/class#", blankClasses);
        }

        public String getPropertyIRI(String name)
        {
            return getResourceIRI(name, "http://blanknodes/property#", blankProperties);
        }
    }


    private static Map<String, Short> loadClassBases(Model model) throws IOException, SQLException
    {
        Map<String, Short> map = new HashMap<String, Short>();

        new OntologyModelTableLoader(model, loadQuery("ontology/classes.sparql"),
                "insert into class_bases(id, iri, label) values (?,?,?)")
        {
            short nextID = 0;

            @Override
            public void insert() throws SQLException, IOException
            {
                String iri = getClassIRI("iri");
                map.put(iri, nextID);

                setValue(1, nextID++);
                setValue(2, iri);
                setValue(3, getLiteralValue("label"));
            }
        }.load();

        return map;
    }


    private static void loadSubClasses(Model model, Map<String, Short> classes) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, patternQuery("?class rdfs:subClassOf ?subclass"),
                "insert into class_subclasses(class, subclass) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, classes.get(getClassIRI("class")));
                setValue(2, classes.get(getClassIRI("subclass")));
            }
        }.load();
    }


    private static void loadMissingSubClasses(Model model, Map<String, Short> classes) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, loadQuery("ontology/subclasses.sparql"),
                "insert into class_subclasses(class, subclass) values (?,?)")
        {
            short thing = classes.get("http://www.w3.org/2002/07/owl#Thing");

            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, classes.get(getClassIRI("class")));
                setValue(2, thing);
            }
        }.load();
    }


    private static Map<String, Short> loadPropertyBases(Model model) throws IOException, SQLException
    {
        Map<String, Short> map = new HashMap<String, Short>();

        new OntologyModelTableLoader(model, loadQuery("ontology/properties.sparql"),
                "insert into property_bases(id, iri, label) values (?,?,?)")
        {
            short nextID = 0;

            @Override
            public void insert() throws SQLException, IOException
            {
                String iri = getPropertyIRI("iri");
                map.put(iri, nextID);

                setValue(1, nextID++);
                setValue(2, iri);
                setValue(3, getLiteralValue("label"));
            }
        }.load();

        return map;
    }


    private static void loadSubProperties(Model model, Map<String, Short> properties) throws IOException, SQLException
    {
        new OntologyModelTableLoader(model, patternQuery("?property rdfs:subPropertyOf ?subproperty"),
                "insert into property_subproperties(property, subproperty) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, properties.get(getPropertyIRI("property")));
                setValue(2, properties.get(getPropertyIRI("subproperty")));
            }
        }.load();
    }


    private static void loadDomains(Model model, Map<String, Short> classes, Map<String, Short> properties)
            throws SQLException, IOException
    {
        new OntologyModelTableLoader(model, patternQuery("?property rdfs:domain ?domain"),
                "insert into property_domains(property, domain) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, properties.get(getPropertyIRI("property")));
                setValue(2, classes.get(getClassIRI("domain")));
            }
        }.load();
    }


    private static void loadRanges(Model model, Map<String, Short> classes, Map<String, Short> properties)
            throws SQLException, IOException
    {
        new OntologyModelTableLoader(model, patternQuery("?property rdfs:range ?range"),
                "insert into property_ranges(property, range) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, properties.get(getPropertyIRI("property")));
                setValue(2, classes.get(getClassIRI("range")));
            }
        }.load();
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
        } ;

        Map<String, Short> classes = loadClassBases(ontologyModel);
        loadSubClasses(ontologyModel, classes);
        loadMissingSubClasses(ontologyModel, classes);

        Map<String, Short> properties = loadPropertyBases(ontologyModel);
        loadSubProperties(ontologyModel, properties);

        loadDomains(ontologyModel, classes, properties);
        loadRanges(ontologyModel, classes, properties);

        ontologyModel.close();
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        loadDirectory("ontology");
    }
}
