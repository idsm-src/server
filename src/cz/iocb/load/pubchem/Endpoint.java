package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.api.tuple.primitive.ObjectFloatPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.IntQuaterplet;
import cz.iocb.load.common.IntTriplet;
import cz.iocb.load.common.IntTripletString;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



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
        usedEndpoints = new IntTripletSet();
        newEndpoints = new IntTripletSet();
        oldEndpoints = getIntTripletSet("select substance, bioassay, measuregroup from pubchem.endpoint_bases");
    }


    private static void loadOutcomes() throws IOException, SQLException
    {
        IntQuaterpletSet newOutcomes = new IntQuaterpletSet();
        IntQuaterpletSet oldOutcomes = getIntQuaterpletSet(
                "select substance, bioassay, measuregroup, outcome_id from pubchem.endpoint_outcomes");


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

        batch("delete from pubchem.endpoint_outcomes where substance = ? and bioassay = ? and measuregroup = ? and outcome_id = ?",
                oldOutcomes);
        batch("insert into pubchem.endpoint_outcomes(substance, bioassay, measuregroup, outcome_id) values (?,?,?,?)",
                newOutcomes);
    }


    private static void loadMeasurementUnits() throws IOException, SQLException
    {
        IntTripletSet newUnits = new IntTripletSet();
        IntTripletSet oldUnits = getIntTripletSet(
                "select substance, bioassay, measuregroup from pubchem.endpoint_measurements");

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

                    IntTriplet quaterplet = parseEndpoint(subject);

                    if(!oldUnits.remove(quaterplet))
                        newUnits.add(quaterplet);
                }
            }.load(stream);
        }

        batch("delete from pubchem.endpoint_measurements where substance = ? and bioassay = ? and measuregroup = ?",
                oldUnits);
        batch("insert into pubchem.endpoint_measurements (substance, bioassay, measuregroup) values (?,?,?)", newUnits);
    }


    private static void loadMeasurementTypes() throws IOException, SQLException
    {
        IntQuaterpletSet newTypes = new IntQuaterpletSet();
        IntQuaterpletSet oldTypes = getIntQuaterpletSet(
                "select substance, bioassay, measuregroup, type_id from pubchem.endpoint_measurement_types");

        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    Identifier type = Ontology.getId(object.getURI());

                    if(type.unit != Ontology.unitBAO)
                        throw new IOException();

                    IntQuaterplet quaterplet = parseEndpoint(subject, type.id);

                    if(!oldTypes.remove(quaterplet))
                        newTypes.add(quaterplet);
                }
            }.load(stream);
        }

        batch("delete from pubchem.endpoint_measurement_types where substance = ? and bioassay = ? and measuregroup = ? and type_id = ?",
                oldTypes);
        batch("insert into pubchem.endpoint_measurement_types (substance, bioassay, measuregroup, type_id) values (?,?,?,?)",
                newTypes);
    }


    private static void loadMeasurementLabels() throws IOException, SQLException
    {
        IntTripletStringSet newValues = new IntTripletStringSet();
        IntTripletStringSet oldValues = getIntTripletStringSet(
                "select substance, bioassay, measuregroup, label from pubchem.endpoint_measurement_labels");

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
                    IntTripletString item = parseEndpoint(subject, label);

                    if(!oldValues.remove(item))
                        newValues.add(item);
                }
            }.load(stream);
        }

        batch("delete from pubchem.endpoint_measurement_labels where substance = ? and bioassay = ? and measuregroup = ? and label = ?",
                oldValues);
        batch("insert into pubchem.endpoint_measurement_labels (substance, bioassay, measuregroup, label) values (?,?,?,?)",
                newValues);
    }


    private static void loadMeasurementValues() throws IOException, SQLException
    {
        IntTripletFloatSet newValues = new IntTripletFloatSet();
        IntTripletFloatSet oldValues = getIntTripletFloatSet(
                "select substance, bioassay, measuregroup, value from pubchem.endpoint_measurement_values");

        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_value.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
                        throw new IOException();

                    IntTriplet triplet = parseEndpoint(subject);
                    float value = getFloat(object);
                    ObjectFloatPair<IntTriplet> pair = PrimitiveTuples.pair(triplet, value);

                    if(!oldValues.remove(pair))
                        newValues.add(pair);
                }
            }.load(stream);
        }

        batch("delete from pubchem.endpoint_measurement_values where substance = ? and bioassay = ? and measuregroup = ? and value = ?",
                oldValues);
        batch("insert into pubchem.endpoint_measurement_values (substance, bioassay, measuregroup, value) values (?,?,?,?)",
                newValues);
    }


    private static void loadReferences() throws IOException, SQLException
    {
        IntQuaterpletSet newReferences = new IntQuaterpletSet();
        IntQuaterpletSet oldReferences = getIntQuaterpletSet(
                "select substance, bioassay, measuregroup, reference from pubchem.endpoint_references");

        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint2reference.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/spar/cito/citesAsDataSource"))
                        throw new IOException();

                    int referenceID = Reference
                            .getReferenceID(getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                    IntQuaterplet quaterplet = parseEndpoint(subject, referenceID);

                    if(!oldReferences.remove(quaterplet))
                        newReferences.add(quaterplet);
                }
            }.load(stream);
        }

        batch("delete from pubchem.endpoint_references where substance = ? and bioassay = ? and measuregroup = ? and reference = ?",
                oldReferences);
        batch("insert into pubchem.endpoint_references(substance, bioassay, measuregroup, reference) values (?,?,?,?)",
                newReferences);
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
                        getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID");

                        if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/IAO_0000136"))
                            throw new IOException();

                        getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
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
        loadMeasurementUnits();
        loadMeasurementTypes();
        loadMeasurementLabels();
        loadMeasurementValues();
        loadReferences();
        checkSubstances();

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish endpoints ...");

        batch("delete from pubchem.endpoint_bases where substance = ? and bioassay = ? and measuregroup = ?",
                oldEndpoints);
        batch("insert into pubchem.endpoint_bases(substance, bioassay, measuregroup) values (?,?,?)"
                + " on conflict do nothing", newEndpoints);

        usedEndpoints = null;
        newEndpoints = null;
        oldEndpoints = null;

        System.out.println();
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
        addEndpointID(substance, bioassay, measuregroup);
        Measuregroup.addMeasuregroupID(bioassay, measuregroup);
        Bioassay.addBioassayID(bioassay);

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


    private static IntTripletString parseEndpoint(Node node, String string) throws IOException
    {
        return parseEndpoint(node,
                (substance, bioassay, measuregroup) -> new IntTripletString(substance, bioassay, measuregroup, string));
    }
}
