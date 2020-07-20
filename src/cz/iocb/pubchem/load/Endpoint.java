package cz.iocb.pubchem.load;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.pubchem.load.Ontology.Identifier;
import cz.iocb.pubchem.load.common.IntQuaterplet;
import cz.iocb.pubchem.load.common.IntTriplet;
import cz.iocb.pubchem.load.common.TripleStreamProcessor;
import cz.iocb.pubchem.load.common.Updater;



class Endpoint extends Updater
{
    private interface SetFunction<T>
    {
        T set(int substance, int bioassay, int measuregroup);
    }


    private static final String endpointPrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID";
    private static final int endpointPrefixLength = endpointPrefix.length();


    private static IntTripletSet usedEndpoints;
    private static IntTripletSet newEndpoints;
    private static IntTripletSet oldEndpoints;


    private static void loadBases() throws IOException, SQLException
    {
        usedEndpoints = new IntTripletSet(250000000);
        newEndpoints = new IntTripletSet(250000000);
        oldEndpoints = getIntTripletSet("select substance, bioassay, measuregroup from endpoint_bases", 250000000);
    }


    private static void loadOutcomes() throws IOException, SQLException
    {
        IntQuaterpletSet newOutcomes = new IntQuaterpletSet(250000000);
        IntQuaterpletSet oldOutcomes = getIntQuaterpletSet(
                "select substance, bioassay, measuregroup, outcome_id from endpoint_outcomes", 250000000);


        processFiles("RDF/endpoint", "pc_endpoint_outcome_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI()
                                .equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PubChemAssayOutcome"))
                            throw new IOException();

                        Identifier outcome = Ontology.getId(object.getURI());

                        if(outcome.unit != Ontology.unitUncategorized)
                            throw new IOException();

                        IntQuaterplet quaterplet = parseEndpoint(subject, outcome.id);

                        synchronized(newOutcomes)
                        {
                            if(!oldOutcomes.remove(quaterplet))
                                newOutcomes.add(quaterplet);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from endpoint_outcomes where substance = ? and bioassay = ? and measuregroup = ? and outcome_id = ?",
                oldOutcomes);
        batch("insert into endpoint_outcomes(substance, bioassay, measuregroup, outcome_id) values (?,?,?,?)",
                newOutcomes);
    }


    private static void loadMeasurements() throws IOException, SQLException
    {
        IntTripletSet usedMeasurements = new IntTripletSet(10000000);
        IntTripletSet newMeasurements = new IntTripletSet(10000000);
        IntTripletSet oldMeasurements = getIntTripletSet(
                "select substance, bioassay, measuregroup from endpoint_measurements", 10000000);


        IntTripletIntMap newTypes = new IntTripletIntMap(10000000);
        IntTripletIntMap oldTypes = getIntTripletIntMap(
                "select substance, bioassay, measuregroup, type_id from endpoint_measurements", 10000000);

        try(InputStream stream = getStream("RDF/endpoint/pc_endpoint_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    IntTriplet triplet = parseEndpoint(subject);
                    int typeID = getIntID(object, "http://www.bioassayontology.org/bao#BAO_");

                    if(usedMeasurements.add(triplet) && !oldMeasurements.remove(triplet))
                        newMeasurements.add(triplet);

                    if(typeID != oldTypes.removeKeyIfAbsent(triplet, NO_VALUE))
                        newTypes.put(triplet, typeID);
                }
            }.load(stream);
        }


        IntTripletFloatMap newValues = new IntTripletFloatMap(10000000);
        IntTripletFloatMap oldValues = getIntTripletFloatMap(
                "select substance, bioassay, measuregroup, value from endpoint_measurements", 10000000);

        try(InputStream stream = getStream("RDF/endpoint/pc_endpoint_value.ttl.gz"))
        {
            new TripleStreamProcessor()
            {

                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                        throw new IOException();

                    IntTriplet triplet = parseEndpoint(subject);
                    float value = getFloat(object);

                    if(usedMeasurements.add(triplet) && !oldMeasurements.remove(triplet))
                        newMeasurements.add(triplet);

                    if(value != oldValues.removeKeyIfAbsent(triplet, Float.NaN) || value == Float.NaN)
                        newValues.put(triplet, value);
                }
            }.load(stream);
        }


        IntTripletStringMap newLabels = new IntTripletStringMap(10000000);
        IntTripletStringMap oldLabels = getIntTripletStringMap(
                "select substance, bioassay, measuregroup, label from endpoint_measurements", 10000000);

        try(InputStream stream = getStream("RDF/endpoint/pc_endpoint_label.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"))
                        throw new IOException();

                    IntTriplet triplet = parseEndpoint(subject);
                    String label = getString(object);

                    if(usedMeasurements.add(triplet) && !oldMeasurements.remove(triplet))
                        newMeasurements.add(triplet);

                    if(!label.equals(oldLabels.remove(triplet)))
                        newLabels.put(triplet, label);
                }
            }.load(stream);
        }


        oldMeasurements.forEach(key -> {
            oldTypes.remove(key);
            oldValues.remove(key);
            oldLabels.remove(key);
        });

        if(!oldTypes.isEmpty() || !oldValues.isEmpty() || !oldLabels.isEmpty())
            throw new IOException();


        batch("delete from endpoint_measurements where substance = ? and bioassay = ? and measuregroup = ?",
                oldMeasurements);

        batch("insert into endpoint_measurements(substance, bioassay, measuregroup, type_id, value, label) values (?,?,?,?,?,?)",
                newMeasurements, (PreparedStatement statement, IntTriplet endpoint) -> {
                    statement.setInt(1, endpoint.getOne());
                    statement.setInt(2, endpoint.getTwo());
                    statement.setInt(3, endpoint.getThree());
                    statement.setInt(4, newTypes.getOrThrow(endpoint));
                    statement.setFloat(5, newValues.getOrThrow(endpoint));
                    statement.setString(6, newLabels.remove(endpoint));
                    newTypes.remove(endpoint);
                    newValues.remove(endpoint);
                });


        batch("update endpoint_measurements set type_id = ? where substance = ? and bioassay = ? and measuregroup = ?",
                newTypes, Direction.REVERSE);

        batch("update endpoint_measurements set value = ? where substance = ? and bioassay = ? and measuregroup = ?",
                newValues, Direction.REVERSE);

        batch("update endpoint_measurements set label = ? where substance = ? and bioassay = ? and measuregroup = ?",
                newLabels, Direction.REVERSE);
    }


    private static void loadReferences() throws IOException, SQLException
    {
        IntQuaterpletSet newReferences = new IntQuaterpletSet(20000000);
        IntQuaterpletSet oldReferences = getIntQuaterpletSet(
                "select substance, bioassay, measuregroup, reference from endpoint_references", 20000000);

        try(InputStream stream = getStream("RDF/endpoint/pc_endpoint2reference.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/spar/cito/citesAsDataSource"))
                        throw new IOException();

                    int referenceID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");
                    IntQuaterplet quaterplet = parseEndpoint(subject, referenceID);

                    synchronized(newReferences)
                    {
                        if(!oldReferences.remove(quaterplet))
                            newReferences.add(quaterplet);
                    }
                }
            }.load(stream);
        }

        batch("delete from endpoint_references where substance = ? and bioassay = ? and measuregroup = ? and reference = ?",
                oldReferences);
        batch("insert into endpoint_references(substance, bioassay, measuregroup, reference) values (?,?,?,?)",
                newReferences);
    }


    private static void checkUnits() throws IOException, SQLException
    {
        try(InputStream stream = getStream("RDF/endpoint/pc_endpoint_unit.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!object.getURI().equals("http://purl.obolibrary.org/obo/UO_0000064"))
                        throw new RuntimeException(new IOException());
                }
            }.load(stream);
        }
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load endpoints ...");

        loadBases();
        loadOutcomes();
        loadMeasurements();
        loadReferences();
        checkUnits();

        System.out.println();
    }


    static void finish() throws SQLException
    {
        batch("delete from endpoint_bases where substance = ? and bioassay = ? and measuregroup = ?", oldEndpoints);
        batch("insert into endpoint_bases(substance, bioassay, measuregroup) values (?,?,?)", newEndpoints);

        usedEndpoints = null;
        newEndpoints = null;
        oldEndpoints = null;
    }


    static void addEndpointID(int substance, int bioassay, int measuregroup)
    {
        IntTriplet triplet = new IntTriplet(substance, bioassay, measuregroup);

        synchronized(newEndpoints)
        {
            if(usedEndpoints.add(triplet) && !oldEndpoints.remove(triplet))
                newEndpoints.add(triplet);
        }
    }


    private static <T> T parseEndpoint(Node node, SetFunction<T> function) throws IOException
    {
        int substance;
        int bioassay;
        int measuregroup;

        String iri = node.getURI();

        if(!iri.startsWith(endpointPrefix))
            throw new IOException();

        int aid = iri.indexOf("_AID", endpointPrefixLength);

        if(aid == -1)
            throw new IOException();

        substance = Integer.parseInt(iri.substring(endpointPrefixLength, aid));

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

        Substance.addSubstanceID(substance);
        Measuregroup.addMeasuregroupID(bioassay, measuregroup);
        addEndpointID(substance, bioassay, measuregroup);

        return function.set(substance, bioassay, measuregroup);
    }


    private static IntQuaterplet parseEndpoint(Node node, int last) throws IOException
    {
        return parseEndpoint(node,
                (substance, bioassay, measuregroup) -> new IntQuaterplet(substance, bioassay, measuregroup, last));
    }


    private static IntTriplet parseEndpoint(Node node) throws IOException
    {
        return parseEndpoint(node,
                (substance, bioassay, measuregroup) -> new IntTriplet(substance, bioassay, measuregroup));
    }
}
