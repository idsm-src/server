package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.real;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.EntityTable.Key;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



class Endpoint extends Updater
{
    private static final class EndpointID
    {
        private final int substance;
        private final int bioassay;
        private final int measuregroup;
        private final int value;


        public EndpointID(int substance, int bioassay, int measuregroup, int value)
        {
            this.substance = substance;
            this.bioassay = bioassay;
            this.measuregroup = measuregroup;
            this.value = value;
        }


        @Override
        public boolean equals(Object obj)
        {
            if(obj == this)
                return true;

            if(obj == null || obj.getClass() != this.getClass())
                return false;

            EndpointID other = (EndpointID) obj;

            return substance == other.substance && bioassay == other.bioassay && measuregroup == other.measuregroup
                    && value == other.value;
        }


        @Override
        public int hashCode()
        {
            return Integer.hashCode(substance) + Integer.hashCode(bioassay) + Integer.hashCode(measuregroup)
                    + Integer.hashCode(value);
        }
    }


    @SuppressWarnings("serial")
    private static class IntQuaterpletIntSet extends SqlSet<Pair<EndpointID, Integer>>
    {
        @Override
        public Pair<EndpointID, Integer> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(new EndpointID(result.getInt(1), result.getInt(2), result.getInt(3), result.getInt(4)),
                    result.getInt(5));
        }

        @Override
        public void set(PreparedStatement statement, Pair<EndpointID, Integer> value) throws SQLException
        {
            statement.setInt(1, value.getOne().substance);
            statement.setInt(2, value.getOne().bioassay);
            statement.setInt(3, value.getOne().measuregroup);
            statement.setInt(4, value.getOne().value);
            statement.setInt(5, value.getTwo());
        }
    }


    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID";
    static final int prefixLength = prefix.length();

    private static final Key<EndpointID> key = new Key<>("substance", "bioassay", "measuregroup", "value")
    {
        @Override
        protected EndpointID read(ResultSet result) throws SQLException
        {
            return new EndpointID(result.getInt(1), result.getInt(2), result.getInt(3), result.getInt(4));
        }

        @Override
        protected void write(PreparedStatement statement, EndpointID endpoint) throws SQLException
        {
            statement.setInt(1, endpoint.substance);
            statement.setInt(2, endpoint.bioassay);
            statement.setInt(3, endpoint.measuregroup);
            statement.setInt(4, endpoint.value);
        }
    };

    private static final EntityTable<EndpointID> endpoints = new EntityTable<>("pubchem.endpoint_bases", key, null,
            integer("outcome_id"));

    private static final EntityTable<EndpointID> measurements = new EntityTable<>("pubchem.endpoint_measurements", key,
            null, integer("endpoint_type_id"), real("measurement"), varchar("qualifier"), varchar("label"));


    private static void loadOutcomes() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/endpoint", "pc_endpoint_outcome_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI()
                                .equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PubChemAssayOutcome"))
                            throw new IOException();

                        EndpointID endpoint = parseEndpoint(subject);
                        Pair<Integer, Integer> outcome = Ontology.getId(object.getURI());

                        if(outcome.getOne() != OntologyResource.unitUncategorized)
                            throw new IOException();

                        endpoints.set(endpoint, "outcome_id", outcome.getTwo());
                    }
                }.load(stream);
            }
        });
    }


    private static void loadTypes() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/endpoint", "pc_endpoint_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            throw new IOException();

                        if(object.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Endpoint"))
                            return;

                        EndpointID endpoint = parseEndpoint(subject);
                        Pair<Integer, Integer> type = Ontology.getId(object.getURI());

                        if(type.getOne() != OntologyResource.unitBAO)
                            throw new IOException();

                        measurements.set(endpoint, "endpoint_type_id", type.getTwo());
                    }
                }.load(stream);
            }
        });
    }


    private static void loadLabels() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_label.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"))
                        throw new IOException();

                    String label = getString(object);
                    EndpointID endpoint = parseEndpoint(subject);


                    measurements.set(endpoint, "label", label);
                }
            }.load(stream);
        }
    }


    private static void loadMeasuredValues() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_value.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    EndpointID endpoint = parseEndpoint(subject);

                    if(predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
                    {
                        Float measurement = getFloatFromDecimal(object);


                        measurements.set(endpoint, "measurement", measurement);
                    }
                    else if(predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#hasQualifier"))
                    {
                        String qualifier = getString(object);


                        measurements.set(endpoint, "qualifier", qualifier);

                    }
                    else
                    {
                        throw new IOException();
                    }
                }
            }.load(stream);
        }
    }


    private static void loadReferences() throws IOException, SQLException
    {
        IntQuaterpletIntSet keepReferences = new IntQuaterpletIntSet();
        IntQuaterpletIntSet newReferences = new IntQuaterpletIntSet();
        IntQuaterpletIntSet oldReferences = new IntQuaterpletIntSet();

        IntQuaterpletIntSet keepPatents = new IntQuaterpletIntSet();
        IntQuaterpletIntSet newPatents = new IntQuaterpletIntSet();
        IntQuaterpletIntSet oldPatents = new IntQuaterpletIntSet();

        load("select substance,bioassay,measuregroup,value,reference from pubchem.endpoint_references", oldReferences);
        load("select substance,bioassay,measuregroup,value,patent from pubchem.endpoint_patents", oldPatents);

        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint2cites_as_data_source.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/spar/cito/citesAsDataSource"))
                        throw new IOException();

                    if(object.getURI().startsWith(Reference.prefix))
                    {
                        Integer referenceID = Reference.getReferenceID(object.getURI());
                        EndpointID endpoint = parseEndpoint(subject);

                        Pair<EndpointID, Integer> pair = Pair.getPair(endpoint, referenceID);

                        if(oldReferences.remove(pair))
                            keepReferences.add(pair);
                        else if(!keepReferences.contains(pair))
                            newReferences.add(pair);
                    }
                    else if(object.getURI().startsWith(Patent.prefix))
                    {
                        Integer patentID = Patent.getPatentID(object.getURI());
                        EndpointID endpoint = parseEndpoint(subject);

                        Pair<EndpointID, Integer> pair = Pair.getPair(endpoint, patentID);

                        if(oldPatents.remove(pair))
                            keepPatents.add(pair);
                        else if(!keepPatents.contains(pair))
                            newPatents.add(pair);
                    }
                    else
                    {
                        throw new IOException();
                    }
                }
            }.load(stream);
        }

        store("delete from pubchem.endpoint_references "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and reference=?", oldReferences);
        store("insert into pubchem.endpoint_references(substance,bioassay,measuregroup,value,reference) "
                + "values(?,?,?,?,?)", newReferences);

        store("delete from pubchem.endpoint_patents "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and patent=?", oldPatents);
        store("insert into pubchem.endpoint_patents(substance,bioassay,measuregroup,value,patent) "
                + "values(?,?,?,?,?)", newPatents);
    }


    private static void checkUnits() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_unit.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000221"))
                        throw new IOException();

                    if(!object.getURI().equals("http://purl.obolibrary.org/obo/UO_0000064"))
                        throw new IOException();
                }
            }.load(stream);
        }
    }


    private static void checkSubstances() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/endpoint", "pc_endpoint2substance_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        getStringID(subject, prefix);

                        if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/IAO_0000136"))
                            throw new IOException();

                        getIntID(object, Substance.prefix);
                    }
                }.load(stream);
            }
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load endpoints ...");

        loadOutcomes();
        loadTypes();
        loadLabels();
        loadMeasuredValues();
        loadReferences();
        checkUnits();
        checkSubstances();

        // nothing loaded later refers to the endpoints, so they are stored at once without keeping their keys
        measurements.store();
        endpoints.store();

        System.out.println();
    }


    static void addEndpointID(EndpointID endpoint)
    {
        endpoints.reference(endpoint);
    }


    private static EndpointID parseEndpoint(Node node) throws IOException
    {
        String iri = node.getURI();

        if(!iri.startsWith(prefix))
            throw new IOException();

        int aid = iri.indexOf("_AID", prefixLength);
        int val = iri.indexOf("_VALUE", prefixLength);

        if(aid == -1 || val == -1 || val < aid)
            throw new IOException();

        int grp = iri.indexOf("_", aid + 1);

        Integer substance = Integer.parseInt(iri.substring(prefixLength, aid));
        Integer bioassay = Integer.parseInt(iri.substring(aid + 4, grp));
        Integer value = Integer.parseInt(iri.substring(val + 6));
        Integer measuregroup;

        if(iri.indexOf("_PMID") == grp)
        {
            String part = iri.substring(grp + 5, val);

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
        else if(grp != val)
        {
            measuregroup = Integer.parseInt(iri.substring(grp + 1, val));

            if(measuregroup == 2147483647)
                throw new IOException();
        }
        else
        {
            measuregroup = 2147483647; // magic number
        }

        EndpointID endpoint = new EndpointID(substance, bioassay, measuregroup, value);

        addEndpointID(endpoint);
        Substance.addSubstanceID(substance);
        Measuregroup.addMeasuregroupID(bioassay, measuregroup);
        Measuregroup.addMeasuregroupSubstance(bioassay, measuregroup, substance);
        Bioassay.addBioassayID(bioassay);

        return endpoint;
    }
}
