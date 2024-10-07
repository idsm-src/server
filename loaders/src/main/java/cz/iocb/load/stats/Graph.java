package cz.iocb.load.stats;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import cz.iocb.sparql.engine.mapping.ConstantBlankNodeMapping;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.ConstantLiteralMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedBlankNodeMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedIriMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedLiteralMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.request.Request;



public class Graph
{
    private final Dataset litDataset;
    private final Dataset iriDataset;
    private final Map<Resource, Dataset> iriPredicates;
    private final Map<Resource, Dataset> litPredicates;
    private final Map<Resource, Map<Resource, Dataset>> datatypePredicates;
    private final Map<Set<Resource>, Dataset> classes;


    public Graph()
    {
        this.iriDataset = new Dataset();
        this.litDataset = new Dataset();
        this.iriPredicates = new HashMap<Resource, Dataset>();
        this.litPredicates = new HashMap<Resource, Dataset>();
        this.datatypePredicates = new HashMap<Resource, Map<Resource, Dataset>>();
        this.classes = new HashMap<Set<Resource>, Dataset>();
    }


    void add(Request request, QuadMapping map, Map<IRI, Resource> datatypes) throws SQLException
    {
        if(map.getSubject() instanceof ConstantBlankNodeMapping
                || map.getSubject() instanceof ParametrisedBlankNodeMapping
                || map.getObject() instanceof ConstantBlankNodeMapping
                || map.getObject() instanceof ParametrisedBlankNodeMapping)
            throw new UnsupportedOperationException();


        ConstantIriMapping predicateMapping = (ConstantIriMapping) map.getPredicate();
        Resource predicate = new Resource(predicateMapping.getColumns());

        if(map.getObject() instanceof ConstantLiteralMapping || map.getObject() instanceof ParametrisedLiteralMapping)
        {
            Resource datatype = datatypes.get(((LiteralClass) map.getObject().getResourceClass(null)).getTypeIri());
            addMapping(datatypePredicates, predicate, datatype, map);
            addMapping(litPredicates, predicate, map);
            litDataset.add(map);
        }
        else if(map.getObject() instanceof ConstantIriMapping || map.getObject() instanceof ParametrisedIriMapping)
        {
            addMapping(iriPredicates, predicate, map);
            iriDataset.add(map);
        }

        if(predicateMapping.getIRI().getValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
        {
            Set<Resource> set = getClasses(request, map);
            Set<Resource> key = new HashSet<Resource>(set);
            Dataset dataset = new Dataset(map);

            Iterator<Entry<Set<Resource>, Dataset>> it = classes.entrySet().iterator();

            while(it.hasNext())
            {
                Entry<Set<Resource>, Dataset> entry = it.next();

                Set<Resource> intersection = new HashSet<Resource>(entry.getKey());
                intersection.retainAll(set);

                if(!intersection.isEmpty())
                {
                    key.addAll(entry.getKey());
                    dataset.add(entry.getValue());
                    it.remove();
                }
            }

            classes.put(key, dataset);
        }
    }


    private static Set<Resource> getClasses(Request request, QuadMapping map) throws SQLException
    {
        if(map.getObject() instanceof ConstantIriMapping cmap)
        {
            String iri = cmap.getIRI().getValue();

            for(String prefix : List.of("http://purl.obolibrary.org/obo/CHEMONTID_",
                    "http://purl.bioontology.org/ontology/NDFRT/", "http://purl.bioontology.org/ontology/SNOMEDCT/",
                    "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#", "http://purl.obolibrary.org/obo/UBERON_",
                    "http://purl.obolibrary.org/obo/PR_", "http://purl.obolibrary.org/obo/CHEBI_"))
            {
                if(iri.startsWith(prefix))
                {
                    System.err.println("skip " + iri);
                    return Set.of();
                }
            }

            Resource resource = new Resource(cmap.getColumns());
            return Set.of(resource);
        }
        else
        {
            /*
            SqlIntercode ic = (new Dataset(map)).translate(request, null, "O");
            List<Column> o = ic.getVariables().get("O").getMapping();
            String sql = "SELECT DISTINCT " + o.get(0) + ", " + o.get(1) + " FROM (" + ic.translate(request) + ") as t";

            Set<Resource> result = new HashSet<Resource>();

            try(Connection connection = request.getConfiguration().getConnectionPool().getConnection())
            {
                try(Statement statement = connection.createStatement())
                {
                    try(ResultSet rs = statement.executeQuery(sql))
                    {
                        while(rs.next())
                            result.add(new Resource(rs.getShort(1), rs.getInt(2)));
                    }
                }
            }

            return result;
            */

            return Set.of();
        }
    }


    private static void addMapping(Map<Resource, Dataset> map, Resource iri, QuadMapping mapping)
    {
        Dataset set = map.get(iri);

        if(set == null)
        {
            set = new Dataset();
            map.put(iri, set);
        }

        set.add(mapping);
    }


    private static void addMapping(Map<Resource, Map<Resource, Dataset>> map, Resource major, Resource minor,
            QuadMapping mapping)
    {
        Map<Resource, Dataset> m = map.get(major);

        if(m == null)
        {
            m = new HashMap<Resource, Dataset>();
            map.put(major, m);
        }

        addMapping(m, minor, mapping);
    }


    protected Dataset getIriDataset()
    {
        return iriDataset;
    }


    protected Dataset getLitDataset()
    {
        return litDataset;
    }


    protected Map<Resource, Dataset> getIriPredicates()
    {
        return iriPredicates;
    }


    protected Map<Resource, Dataset> getLitPredicates()
    {
        return litPredicates;
    }


    protected Map<Resource, Map<Resource, Dataset>> getDatatypePredicates()
    {
        return datatypePredicates;
    }


    protected Map<Set<Resource>, Dataset> getClasses()
    {
        return classes;
    }
}
