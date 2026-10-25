package cz.iocb.load.pubchem;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitBAO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUncategorized;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.typed;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.is;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.EntityTable.Key;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



/**
 * Loader of the PubChem endpoints, the results of the substances tested in the measuregroups of the bioassays, from the
 * files of {@code pubchem/RDF/endpoint} into the tables {@code pubchem.endpoints} and
 * {@code pubchem.endpoint_measurements} and the tables of their references and patents.
 */
class Endpoint extends Updater
{
    /**
     * Key of an endpoint: the substance, the bioassay, the measuregroup and the value, as the IRI of the endpoint gives
     * them.
     */
    private static final class EndpointID
    {
        /**
         * Id of the substance.
         */
        private final int substance;

        /**
         * Id of the bioassay.
         */
        private final int bioassay;

        /**
         * Number of the measuregroup, as {@link Measuregroup#parseMeasuregroup(Node)} gives it.
         */
        private final int measuregroup;

        /**
         * Number of the value.
         */
        private final int value;


        /**
         * Creates the key.
         *
         * @param substance the id of the substance
         * @param bioassay the id of the bioassay
         * @param measuregroup the number of the measuregroup
         * @param value the number of the value
         */
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


    /**
     * Set of the pairs of an endpoint and an id, read from and written to five columns.
     */
    @SuppressWarnings("serial")
    private static class IntQuaterpletIntSet extends SqlSet<Pair<EndpointID, Integer>>
    {
        /**
         * Creates an empty set.
         */
        private IntQuaterpletIntSet()
        {
        }

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


    /**
     * IRI prefix of the endpoints.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Key of the tables of the endpoints, the columns {@code substance}, {@code bioassay}, {@code measuregroup} and
     * {@code value}.
     */
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

    /**
     * Table of the endpoints and their outcomes.
     */
    private static final EntityTable<EndpointID> endpoints = new EntityTable<>("pubchem.endpoints", key, null,
            integer("outcome_id"));

    /**
     * Table of the measurements of the endpoints: their types, values, qualifiers and labels.
     */
    private static final EntityTable<EndpointID> measurements = new EntityTable<>("pubchem.endpoint_measurements", key,
            null, integer("endpoint_type_id"), typed("measurement", "numeric"), varchar("qualifier"), varchar("label"));


    /**
     * Not instantiable.
     */
    private Endpoint()
    {
    }


    /**
     * Loads the outcomes of the endpoints from the files {@code pc_endpoint_outcome_*.ttl.gz}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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
                        if(!checkPredicate(subject, predicate, object,
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PubChemAssayOutcome"))
                            return;

                        EndpointID endpoint = parseEndpoint(subject);
                        Pair<Integer, Integer> outcome = Ontology.getResourceId(object, unitUncategorized);

                        endpoints.set(endpoint, "outcome_id", outcome.getTwo());
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the types of the endpoints; returns the number of the vocab:Endpoint types.
     *
     * @return the number of the {@code vocab:Endpoint} types
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static long loadTypes() throws IOException, SQLException
    {
        AtomicLong count = new AtomicLong();

        processFiles("pubchem/RDF/endpoint", "pc_endpoint_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            return;

                        EndpointID endpoint = parseEndpoint(subject);

                        if(is(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Endpoint"))
                        {
                            count.incrementAndGet();
                            return;
                        }

                        Pair<Integer, Integer> type = Ontology.getResourceId(object, unitBAO);

                        measurements.set(endpoint, "endpoint_type_id", type.getTwo());
                    }
                }.load(stream);
            }
        });

        return count.get();
    }


    /**
     * Loads the labels of the measurements from {@code pc_endpoint_label.ttl.gz}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadLabels() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_label.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!checkPredicate(subject, predicate, object, "http://www.w3.org/2000/01/rdf-schema#label"))
                        return;

                    String label = getString(object);
                    EndpointID endpoint = parseEndpoint(subject);


                    measurements.set(endpoint, "label", label);
                }
            }.load(stream);
        }
    }


    /**
     * Loads the measured values of the endpoints and their qualifiers from {@code pc_endpoint_value.ttl.gz}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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
                        String measurement = getText(getDecimal(object));


                        measurements.set(endpoint, "measurement", measurement);
                    }
                    else if(predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#hasQualifier"))
                    {
                        String qualifier = getString(object);


                        measurements.set(endpoint, "qualifier", qualifier);

                    }
                    else
                    {
                        unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }
    }


    /**
     * Loads the references and patents that the endpoints cite as their data sources from
     * {@code pc_endpoint2cites_as_data_source.ttl.gz} into the tables {@code pubchem.endpoint_references} and
     * {@code pubchem.endpoint_patents}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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
                    if(!checkPredicate(subject, predicate, object, "http://purl.org/spar/cito/citesAsDataSource"))
                        return;

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
                        unexpectedValue(subject, predicate, object);
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


    /**
     * Checks that the unit of every endpoint in {@code pc_endpoint_unit.ttl.gz} is micromolar.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void checkUnits() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_unit.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!checkPredicate(subject, predicate, object, "http://semanticscience.org/resource/SIO_000221"))
                        return;

                    if(!is(object, "http://purl.obolibrary.org/obo/UO_0000064"))
                        unexpectedValue(subject, predicate, object);
                }
            }.load(stream);
        }
    }


    /**
     * Checks that the substance of an endpoint is the one in its IRI, which is published; returns the number of the
     * triples.
     *
     * @return the number of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static long checkSubstances() throws IOException, SQLException
    {
        AtomicLong count = new AtomicLong();

        processFiles("pubchem/RDF/endpoint", "pc_endpoint2substance_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://purl.obolibrary.org/obo/IAO_0000136"))
                            return;

                        EndpointID endpoint = parseEndpoint(subject);
                        count.incrementAndGet();

                        if(getIntID(object, Substance.prefix) != endpoint.substance)
                            Problems.error("value of " + predicate.getURI() + " not matching the IRI",
                                    text(subject) + " " + text(object));
                    }
                }.load(stream);
            }
        });

        return count.get();
    }


    /**
     * Checks that the measuregroup of an endpoint is the one in its IRI, which is published; returns the number of the
     * triples.
     *
     * @return the number of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static long checkMeasuregroups() throws IOException, SQLException
    {
        AtomicLong count = new AtomicLong();

        processFiles("pubchem/RDF/measuregroup", "pc_measuregroup2endpoint_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://purl.obolibrary.org/obo/OBI_0000299"))
                            return;

                        EndpointID endpoint = parseEndpoint(object);
                        Pair<Integer, Integer> measuregroup = Measuregroup.parseMeasuregroup(subject);
                        count.incrementAndGet();

                        if(!measuregroup.equals(Pair.getPair(endpoint.bioassay, endpoint.measuregroup)))
                            Problems.error("value of " + predicate.getURI() + " not matching the IRI",
                                    text(subject) + " " + text(object));
                    }
                }.load(stream);
            }
        });

        return count.get();
    }


    /**
     * Reports the endpoints without one of the given triples. The source states the triple for every endpoint and
     * states each just once, so the numbers differ whenever an endpoint lacks the triple.
     *
     * @param triples the description of the triples shown in the error
     * @param count the number of the triples
     */
    private static void checkCount(String triples, long count)
    {
        if(count != endpoints.size())
            Problems.error("number of " + triples + " different from the number of the endpoints",
                    count + " triples, " + endpoints.size() + " endpoints");
    }


    /**
     * Loads the endpoints from the files of {@code pubchem/RDF/endpoint} into the tables and stores them at once,
     * because nothing loaded later refers to the endpoints.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load endpoints ...");

        loadOutcomes();
        long types = loadTypes();
        loadLabels();
        loadMeasuredValues();
        loadReferences();
        checkUnits();
        long substances = checkSubstances();
        long measuregroups = checkMeasuregroups();

        checkCount("the types http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Endpoint", types);
        checkCount("the triples of http://purl.obolibrary.org/obo/IAO_0000136", substances);
        checkCount("the triples of http://purl.obolibrary.org/obo/OBI_0000299", measuregroups);

        // nothing loaded later refers to the endpoints, so they are stored at once without keeping their keys
        measurements.store();
        endpoints.store();

        System.out.println();
    }


    /**
     * Ensures that an endpoint has a row.
     *
     * @param endpoint the key of the endpoint
     */
    static void addEndpointID(EndpointID endpoint)
    {
        endpoints.reference(endpoint);
    }


    /**
     * Parses the IRI of an endpoint into its key and references the endpoint, its substance, its measuregroup and its
     * bioassay; the measuregroup is numbered as by {@link Measuregroup#parseMeasuregroup(Node)}.
     *
     * @param node the IRI of the endpoint
     * @return the key of the endpoint
     * @throws IOException if the node is not an IRI of an endpoint
     */
    private static EndpointID parseEndpoint(Node node) throws IOException
    {
        String iri = node.getURI();

        if(!iri.startsWith(prefix))
            throw new DataException("unexpected IRI", iri);

        int aid = iri.indexOf("_AID", prefixLength);
        int val = iri.indexOf("_VALUE", prefixLength);

        if(aid == -1 || val == -1 || val < aid)
            throw new DataException("unexpected IRI", iri);

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
                    throw new DataException("unexpected IRI", iri);
            }
        }
        else if(grp != val)
        {
            measuregroup = Integer.parseInt(iri.substring(grp + 1, val));

            if(measuregroup == 2147483647)
                throw new DataException("unexpected IRI", iri);
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
