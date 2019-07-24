package cz.iocb.pubchem.load;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import cz.iocb.pubchem.load.Ontology.Identifier;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.StreamTableLoader;
import cz.iocb.pubchem.load.common.VoidStreamRDF;



public class Endpoint extends Loader
{
    static private abstract class EndpointStreamTableLoader extends StreamTableLoader
    {
        static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID";

        public EndpointStreamTableLoader(InputStream stream, String sql)
        {
            super(stream, sql);
        }

        void setIDValues(int idx, Node node) throws IOException, SQLException
        {
            String iri = node.getURI();
            int substance;
            int bioassay;
            int measuregroup;

            if(!iri.startsWith(prefix))
                throw new IOException();

            int aid = iri.indexOf("_AID", prefix.length());

            if(aid == -1)
                throw new IOException();

            substance = Integer.parseInt(iri.substring(prefix.length(), aid));

            int grp = iri.indexOf("_", aid + 1);

            if(grp != -1 && iri.indexOf("_PMID") == grp)
            {
                String part = iri.substring(grp + 5);
                bioassay = Integer.parseInt(iri.substring(aid + 4, grp));

                if(part.isEmpty())
                {
                    measuregroup = -2147483647; // magic number
                }
                else
                {
                    measuregroup = -Integer.parseInt(part);

                    if(measuregroup == -2147483647 || measuregroup == 0)
                        throw new IOException();
                }
            }
            else if(grp != -1)
            {
                bioassay = Integer.parseInt(iri.substring(aid + 4, grp));
                measuregroup = Integer.parseInt(iri.substring(grp + 1));

                if(measuregroup == 2147483647)
                    throw new IOException();
            }
            else
            {
                bioassay = Integer.parseInt(iri.substring(aid + 4));
                measuregroup = 2147483647; // magic number
            }

            setValue(idx + 0, substance);
            setValue(idx + 1, bioassay);
            setValue(idx + 2, measuregroup);
        }
    }


    private static void loadOutcomes(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new EndpointStreamTableLoader(stream,
                "insert into endpoint_outcomes(substance, bioassay, measuregroup, outcome_id) values (?,?,?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PubChemAssayOutcome"))
                    throw new IOException();

                Identifier outcome = Ontology.getId(object.getURI());

                if(outcome.unit != Ontology.unitUncategorized)
                    throw new IOException();

                setIDValues(1, subject);
                setValue(4, outcome.id);
            }
        }.load();

        stream.close();
    }


    private static void loadReferences(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new EndpointStreamTableLoader(stream,
                "insert into endpoint_references(substance, bioassay, measuregroup, reference) values (?,?,?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/spar/cito/citesAsDataSource"))
                    throw new IOException();

                setIDValues(1, subject);
                setValue(4, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
            }
        }.load();

        stream.close();
    }


    private static void loadValues(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new EndpointStreamTableLoader(stream,
                "insert into endpoint_measurements(substance, bioassay, measuregroup, type_id, value, label) values (?,?,?,-1,?,'')")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                setIDValues(1, subject);
                setValue(4, getFloat(object));
            }
        }.load();

        stream.close();
    }


    private static void loadTypes(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new EndpointStreamTableLoader(stream,
                "update endpoint_measurements set type_id=? where substance=? and bioassay=? and measuregroup=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                    throw new IOException();

                Identifier type = Ontology.getId(object.getURI());

                if(type.unit != Ontology.unitBAO)
                    throw new IOException();

                setValue(1, type.id);
                setIDValues(2, subject);
            }
        }.load();

        stream.close();
    }


    private static Set<String> loadLabels(String file) throws IOException, SQLException
    {
        final Set<String> hasEmptyLabel = new HashSet<String>();
        InputStream stream = getStream(file);

        new EndpointStreamTableLoader(stream,
                "update endpoint_measurements set label=? where substance=? and bioassay=? and measuregroup=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"))
                    throw new IOException();

                String value = getString(object);

                if(value.isEmpty())
                    hasEmptyLabel.add(subject.getURI());

                setValue(1, value);
                setIDValues(2, subject);
            }
        }.load();

        stream.close();
        return hasEmptyLabel;
    }


    private static void checkUnits(String file) throws FileNotFoundException, IOException
    {
        InputStream stream = getStream(file);

        try
        {
            RDFDataMgr.parse(new VoidStreamRDF()
            {
                @Override
                public void triple(Triple triple)
                {
                    String object = triple.getObject().getURI();

                    if(!object.equals("http://purl.obolibrary.org/obo/UO_0000064"))
                        throw new RuntimeException(new IOException());
                }
            }, stream, Lang.TURTLE);
        }
        catch(RuntimeException e)
        {
            if(e.getCause() instanceof IOException)
                throw(IOException) e.getCause();
            else
                throw e;
        }

        stream.close();
    }


    public static void loadDirectory(String path) throws IOException, SQLException
    {
        File dir = new File(getPubchemDirectory() + path);

        loadValues("RDF/endpoint/pc_endpoint_value.ttl.gz");
        loadTypes("RDF/endpoint/pc_endpoint_type.ttl.gz");
        Set<String> hasEmptyLabel = loadLabels("RDF/endpoint/pc_endpoint_label.ttl.gz");

        Arrays.asList(dir.listFiles()).parallelStream().map(f -> f.getName()).forEach(name -> {
            try
            {
                if(name.startsWith("pc_endpoint_outcome"))
                    loadOutcomes(path + File.separatorChar + name);
                else if(name.startsWith("pc_endpoint_unit"))
                    checkUnits(path + File.separatorChar + name);
                else if(name.startsWith("pc_endpoint2reference"))
                    loadReferences(path + File.separatorChar + name);
                else if(name.startsWith("pc_endpoint2substance"))
                    System.out.println("ignore " + path + File.separator + name);
                else if(!name.equals("pc_endpoint_value.ttl.gz") && !name.equals("pc_endpoint_label.ttl.gz")
                        && !name.equals("pc_endpoint_type.ttl.gz"))
                    System.out.println("unsupported " + path + File.separator + name);
            }
            catch(IOException | SQLException e)
            {
                System.err.println("exception for " + name);
                e.printStackTrace();
                System.exit(1);
            }
        });


        // check ...
        try(Connection connection = getConnection())
        {
            try(PreparedStatement statement = connection
                    .prepareStatement("select * from endpoint_measurements where type_id=-1"))
            {
                try(ResultSet result = statement.executeQuery())
                {
                    while(result.next())
                    {
                        throw new IOException();
                    }
                }
            }
        }

        try(Connection connection = getConnection())
        {
            try(PreparedStatement statement = connection
                    .prepareStatement("select count(*) from endpoint_measurements where label=''"))
            {
                try(ResultSet result = statement.executeQuery())
                {
                    while(result.next())
                    {
                        if(result.getInt(1) != hasEmptyLabel.size())
                            throw new IOException();
                    }
                }
            }
        }
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        loadDirectory("RDF/endpoint");
    }
}
