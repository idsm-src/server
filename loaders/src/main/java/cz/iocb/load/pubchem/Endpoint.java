package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
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
    public static class IntQuaterpletSet extends SqlSet<EndpointID>
    {
        @Override
        public EndpointID get(ResultSet result) throws SQLException
        {
            return new EndpointID(result.getInt(1), result.getInt(2), result.getInt(3), result.getInt(4));
        }

        @Override
        public void set(PreparedStatement statement, EndpointID value) throws SQLException
        {
            statement.setInt(1, value.substance);
            statement.setInt(2, value.bioassay);
            statement.setInt(3, value.measuregroup);
            statement.setInt(4, value.value);
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


    @SuppressWarnings("serial")
    private static class IntQuaterpletIntMap extends SqlMap<EndpointID, Integer>
    {
        @Override
        public EndpointID getKey(ResultSet result) throws SQLException
        {
            return new EndpointID(result.getInt(1), result.getInt(2), result.getInt(3), result.getInt(4));
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(5);
        }

        @Override
        public void set(PreparedStatement statement, EndpointID key, Integer value) throws SQLException
        {
            statement.setInt(1, key.substance);
            statement.setInt(2, key.bioassay);
            statement.setInt(3, key.measuregroup);
            statement.setInt(4, key.value);
            statement.setInt(5, value);
        }
    }


    @SuppressWarnings("serial")
    private static class IntQuaterpletFloatMap extends SqlMap<EndpointID, Float>
    {
        @Override
        public EndpointID getKey(ResultSet result) throws SQLException
        {
            return new EndpointID(result.getInt(1), result.getInt(2), result.getInt(3), result.getInt(4));
        }

        @Override
        public Float getValue(ResultSet result) throws SQLException
        {
            return result.getFloat(5);
        }

        @Override
        public void set(PreparedStatement statement, EndpointID key, Float value) throws SQLException
        {
            statement.setInt(1, key.substance);
            statement.setInt(2, key.bioassay);
            statement.setInt(3, key.measuregroup);
            statement.setInt(4, key.value);
            statement.setFloat(5, value);
        }
    }


    @SuppressWarnings("serial")
    private static class IntQuaterpletStringMap extends SqlMap<EndpointID, String>
    {
        @Override
        public EndpointID getKey(ResultSet result) throws SQLException
        {
            return new EndpointID(result.getInt(1), result.getInt(2), result.getInt(3), result.getInt(4));
        }

        @Override
        public String getValue(ResultSet result) throws SQLException
        {
            return result.getString(5);
        }

        @Override
        public void set(PreparedStatement statement, EndpointID key, String value) throws SQLException
        {
            statement.setInt(1, key.substance);
            statement.setInt(2, key.bioassay);
            statement.setInt(3, key.measuregroup);
            statement.setInt(4, key.value);
            statement.setString(5, value);
        }
    }


    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID";
    static final int prefixLength = prefix.length();

    private static final IntQuaterpletSet keepEndpoints = new IntQuaterpletSet();
    private static final IntQuaterpletSet newEndpoints = new IntQuaterpletSet();
    private static final IntQuaterpletSet oldEndpoints = new IntQuaterpletSet();

    private static final IntQuaterpletSet oldMeasurements = new IntQuaterpletSet();


    private static void loadBases() throws IOException, SQLException
    {
        load("select substance,bioassay,measuregroup,value from pubchem.endpoint_bases", oldEndpoints);
        load("select substance,bioassay,measuregroup,value from pubchem.endpoint_measurements", oldMeasurements);
    }


    private static void loadOutcomes() throws IOException, SQLException
    {
        IntQuaterpletIntMap keepOutcomes = new IntQuaterpletIntMap();
        IntQuaterpletIntMap newOutcomes = new IntQuaterpletIntMap();
        IntQuaterpletIntMap oldOutcomes = new IntQuaterpletIntMap();

        load("select substance,bioassay,measuregroup,value,outcome_id from pubchem.endpoint_bases "
                + "where outcome_id is not null", oldOutcomes);

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

                        EndpointID endpoint = parseEndpoint(subject, true);
                        Pair<Integer, Integer> outcome = Ontology.getId(object.getURI());

                        if(outcome.getOne() != Ontology.unitUncategorized)
                            throw new IOException();

                        synchronized(newOutcomes)
                        {
                            if(outcome.getTwo().equals(oldOutcomes.remove(endpoint)))
                            {
                                keepOutcomes.put(endpoint, outcome.getTwo());
                            }
                            else
                            {
                                Integer keep = keepOutcomes.get(endpoint);

                                if(outcome.getTwo().equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Integer put = newOutcomes.put(endpoint, outcome.getTwo());

                                if(put != null && !outcome.getTwo().equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.endpoint_bases set outcome_id=null "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and outcome_id=?", oldOutcomes);
        store("insert into pubchem.endpoint_bases(substance,bioassay,measuregroup,value,outcome_id) values(?,?,?,?,?) "
                + "on conflict(substance,bioassay,measuregroup,value) do update set outcome_id=EXCLUDED.outcome_id",
                newOutcomes);
    }


    private static void loadTypes() throws IOException, SQLException
    {
        IntQuaterpletIntMap keepTypes = new IntQuaterpletIntMap();
        IntQuaterpletIntMap newTypes = new IntQuaterpletIntMap();
        IntQuaterpletIntMap oldTypes = new IntQuaterpletIntMap();

        load("select substance,bioassay,measuregroup,value,endpoint_type_id from pubchem.endpoint_measurements "
                + "where endpoint_type_id is not null", oldTypes);

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

                        EndpointID endpoint = parseEndpoint(subject, false);
                        Pair<Integer, Integer> type = Ontology.getId(object.getURI());

                        if(type.getOne() != Ontology.unitBAO)
                            throw new IOException();

                        synchronized(newTypes)
                        {
                            oldMeasurements.remove(endpoint);

                            if(type.getTwo().equals(oldTypes.remove(endpoint)))
                            {
                                keepTypes.put(endpoint, type.getTwo());
                            }
                            else
                            {
                                Integer keep = keepTypes.get(endpoint);

                                if(type.getTwo().equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Integer put = newTypes.put(endpoint, type.getTwo());

                                if(put != null && !type.getTwo().equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.endpoint_measurements set endpoint_type_id=null "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and endpoint_type_id=?", oldTypes);
        store("insert into pubchem.endpoint_measurements(substance,bioassay,measuregroup,value,endpoint_type_id) "
                + "values(?,?,?,?,?) "
                + "on conflict(substance,bioassay,measuregroup,value) do update set endpoint_type_id=EXCLUDED.endpoint_type_id",
                newTypes);
    }


    private static void loadLabels() throws IOException, SQLException
    {
        IntQuaterpletStringMap keepLabels = new IntQuaterpletStringMap();
        IntQuaterpletStringMap newLabels = new IntQuaterpletStringMap();
        IntQuaterpletStringMap oldLabels = new IntQuaterpletStringMap();

        load("select substance,bioassay,measuregroup,value,label from pubchem.endpoint_measurements where "
                + "label is not null", oldLabels);

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
                    EndpointID endpoint = parseEndpoint(subject, false);

                    oldMeasurements.remove(endpoint);

                    if(label.equals(oldLabels.remove(endpoint)))
                    {
                        keepLabels.put(endpoint, label);
                    }
                    else
                    {
                        String keep = keepLabels.get(endpoint);

                        if(label.equals(keep))
                            return;
                        else if(keep != null)
                            throw new IOException();

                        String put = newLabels.put(endpoint, label);

                        if(put != null && !label.equals(put))
                            throw new IOException();
                    }
                }
            }.load(stream);
        }

        store("update pubchem.endpoint_measurements set label=null "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and label=?", oldLabels);
        store("insert into pubchem.endpoint_measurements(substance,bioassay,measuregroup,value,label) "
                + "values(?,?,?,?,?) "
                + "on conflict(substance,bioassay,measuregroup,value) do update set label=EXCLUDED.label", newLabels);
    }


    private static void loadMeasuredValues() throws IOException, SQLException
    {
        IntQuaterpletFloatMap keepMeasuredValues = new IntQuaterpletFloatMap();
        IntQuaterpletFloatMap newMeasuredValues = new IntQuaterpletFloatMap();
        IntQuaterpletFloatMap oldMeasuredValues = new IntQuaterpletFloatMap();

        IntQuaterpletStringMap keepQualifiers = new IntQuaterpletStringMap();
        IntQuaterpletStringMap newQualifiers = new IntQuaterpletStringMap();
        IntQuaterpletStringMap oldQualifiers = new IntQuaterpletStringMap();

        load("select substance,bioassay,measuregroup,value,measurement from pubchem.endpoint_measurements where "
                + "measurement is not null", oldMeasuredValues);

        load("select substance,bioassay,measuregroup,value,qualifier from pubchem.endpoint_measurements where "
                + "qualifier is not null", oldQualifiers);

        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_value.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    EndpointID endpoint = parseEndpoint(subject, false);

                    if(predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
                    {
                        Float measurement = getFloatFromDecimal(object);

                        oldMeasurements.remove(endpoint);

                        if(measurement.equals(oldMeasuredValues.remove(endpoint)))
                        {
                            keepMeasuredValues.put(endpoint, measurement);
                        }
                        else
                        {
                            Float keep = keepMeasuredValues.get(endpoint);

                            if(measurement.equals(keep))
                                return;
                            else if(keep != null)
                                throw new IOException();

                            Float put = newMeasuredValues.put(endpoint, measurement);

                            if(put != null && !measurement.equals(put))
                                throw new IOException();
                        }
                    }
                    else if(predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#hasQualifier"))
                    {
                        String qualifier = getString(object);

                        oldMeasurements.remove(endpoint);

                        if(qualifier.equals(oldQualifiers.remove(endpoint)))
                        {
                            keepQualifiers.put(endpoint, qualifier);
                        }
                        else
                        {
                            String keep = keepQualifiers.get(endpoint);

                            if(qualifier.equals(keep))
                                return;
                            else if(keep != null)
                                throw new IOException();

                            String put = newQualifiers.put(endpoint, qualifier);

                            if(put != null && !qualifier.equals(put))
                                throw new IOException();
                        }

                    }
                    else
                    {
                        throw new IOException();
                    }
                }
            }.load(stream);
        }

        store("update pubchem.endpoint_measurements set label=null "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and measurement=?",
                oldMeasuredValues);
        store("insert into pubchem.endpoint_measurements(substance,bioassay,measuregroup,value,measurement) values(?,?,?,?,?) "
                + "on conflict(substance,bioassay,measuregroup,value) do update set measurement=EXCLUDED.measurement",
                newMeasuredValues);

        store("update pubchem.endpoint_measurements set qualifier=null "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and qualifier=?", oldQualifiers);
        store("insert into pubchem.endpoint_measurements(substance,bioassay,measuregroup,value,qualifier) values(?,?,?,?,?) "
                + "on conflict(substance,bioassay,measuregroup,value) do update set qualifier=EXCLUDED.qualifier",
                newQualifiers);
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
                        EndpointID endpoint = parseEndpoint(subject, false);

                        Pair<EndpointID, Integer> pair = Pair.getPair(endpoint, referenceID);

                        if(oldReferences.remove(pair))
                            keepReferences.add(pair);
                        else if(!keepReferences.contains(pair))
                            newReferences.add(pair);
                    }
                    else if(object.getURI().startsWith(Patent.prefix))
                    {
                        Integer patentID = Patent.getPatentID(object.getURI());
                        EndpointID endpoint = parseEndpoint(subject, false);

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

        loadBases();
        loadOutcomes();
        loadTypes();
        loadLabels();
        loadMeasuredValues();
        loadReferences();
        checkUnits();
        checkSubstances();

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish endpoints ...");

        store("delete from pubchem.endpoint_measurements "
                + "where substance=? and bioassay=? and measuregroup=? and value=?", oldMeasurements);

        store("delete from pubchem.endpoint_bases where substance=? and bioassay=? and measuregroup=? and value=?",
                oldEndpoints);
        store("insert into pubchem.endpoint_bases(substance,bioassay,measuregroup,value) values(?,?,?,?)",
                newEndpoints);

        System.out.println();
    }


    static void addEndpointID(EndpointID endpoint, boolean forceKeep)
    {
        synchronized(newEndpoints)
        {
            if(newEndpoints.contains(endpoint))
            {
                if(forceKeep)
                {
                    newEndpoints.remove(endpoint);
                    keepEndpoints.add(endpoint);
                }
            }
            else if(!keepEndpoints.contains(endpoint))
            {
                if(!oldEndpoints.remove(endpoint) && !forceKeep)
                    newEndpoints.add(endpoint);
                else
                    keepEndpoints.add(endpoint);
            }
        }
    }


    private static EndpointID parseEndpoint(Node node, boolean forceKeep) throws IOException
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

        addEndpointID(endpoint, forceKeep);
        Substance.addSubstanceID(substance);
        Measuregroup.addMeasuregroupID(bioassay, measuregroup);
        Measuregroup.addMeasuregroupSubstance(bioassay, measuregroup, substance);
        Bioassay.addBioassayID(bioassay);

        return endpoint;
    }
}
