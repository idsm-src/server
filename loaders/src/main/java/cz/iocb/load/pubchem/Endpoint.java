package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
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
    private static class IntQuaterpletIntFloatPairMap extends SqlMap<EndpointID, Pair<Integer, Float>>
    {
        @Override
        public EndpointID getKey(ResultSet result) throws SQLException
        {
            return new EndpointID(result.getInt(1), result.getInt(2), result.getInt(3), result.getInt(4));
        }

        @Override
        public Pair<Integer, Float> getValue(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(5), result.getFloat(6));
        }

        @Override
        public void set(PreparedStatement statement, EndpointID key, Pair<Integer, Float> value) throws SQLException
        {
            statement.setInt(1, key.substance);
            statement.setInt(2, key.bioassay);
            statement.setInt(3, key.measuregroup);
            statement.setInt(4, key.value);
            statement.setInt(5, value.getOne());
            statement.setFloat(6, value.getTwo());
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
        // workaround
        HashSet<Pair<EndpointID, Integer>> wrongTypes = new HashSet<Pair<EndpointID, Integer>>();
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 1, 1), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 2, 2), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 3, 3), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 4, 4), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 5, 5), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 6, 6), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 7, 7), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 8, 33), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 9, 34), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 10, 35), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 11, 36), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 12, 37), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 13, 38), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 14, 39), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 15, 40), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 16, 41), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 17, 42), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 18, 43), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 19, 44), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 20, 45), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 21, 46), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 22, 47), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 23, 48), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 24, 49), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 25, 50), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 1, 1), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 2, 2), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 3, 3), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 4, 4), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 5, 5), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 6, 6), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(87544119, 1801, 7, 7), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 8, 33), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 9, 34), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 10, 35), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 11, 36), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 12, 37), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 13, 38), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 14, 39), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 15, 40), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 16, 41), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 17, 42), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 18, 43), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 19, 44), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 20, 45), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 21, 46), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 22, 47), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 23, 48), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 24, 49), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103911214, 1801, 25, 50), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(96021160, 1880, 1, 1), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(96021160, 1880, 2, 2), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(96021160, 1880, 3, 3), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(110923218, 1880, 4, 14), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(110923218, 1880, 5, 15), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(110923218, 1880, 6, 16), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(110923218, 1880, 7, 17), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(110923218, 1880, 8, 18), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(110923218, 1880, 9, 19), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(110923218, 1880, 10, 20), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(57288063, 2049, 1, 41), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288063, 2049, 2, 42), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288064, 2049, 1, 25), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288064, 2049, 2, 26), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288068, 2049, 1, 33), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288068, 2049, 2, 34), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288069, 2049, 1, 37), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288069, 2049, 2, 38), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288070, 2049, 1, 29), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288070, 2049, 2, 30), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85261423, 2049, 1, 45), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85261423, 2049, 2, 46), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85261424, 2049, 1, 49), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85261424, 2049, 2, 50), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85261425, 2049, 1, 53), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85261425, 2049, 2, 54), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85261426, 2049, 1, 57), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85261426, 2049, 2, 58), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87326009, 2049, 1, 9), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87326009, 2049, 2, 10), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87326010, 2049, 1, 1), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87326010, 2049, 2, 2), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87326011, 2049, 1, 13), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87326011, 2049, 2, 14), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87334054, 2049, 3, 7), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(87350366, 2049, 1, 17), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87350366, 2049, 2, 18), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87350367, 2049, 1, 21), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87350367, 2049, 2, 22), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(4257091, 2067, 1, 50), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(7972299, 2067, 1, 1), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(7972299, 2067, 2, 2), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(7972299, 2067, 3, 3), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(57288001, 2067, 1, 8), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(57288001, 2067, 2, 9), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(57288001, 2067, 3, 10), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(57288002, 2067, 1, 22), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(57288002, 2067, 3, 24), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(57288005, 2067, 1, 29), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(57288005, 2067, 3, 31), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(57288006, 2067, 1, 36), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(57288006, 2067, 3, 38), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(87241484, 2067, 2, 58), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(87241484, 2067, 3, 59), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(87350363, 2067, 1, 43), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(87350363, 2067, 3, 45), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(87350364, 2067, 1, 15), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(87350364, 2067, 3, 17), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(57287832, 2078, 1, 33), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57287832, 2078, 2, 34), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57287833, 2078, 1, 45), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57287833, 2078, 2, 46), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57287838, 2078, 1, 57), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57287838, 2078, 2, 58), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57287843, 2078, 1, 61), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57287843, 2078, 2, 62), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288032, 2078, 1, 41), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288032, 2078, 2, 42), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288035, 2078, 1, 73), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288035, 2078, 2, 74), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288036, 2078, 1, 65), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288036, 2078, 2, 66), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288037, 2078, 1, 49), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288037, 2078, 2, 50), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288038, 2078, 1, 37), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288038, 2078, 2, 38), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288040, 2078, 1, 29), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288040, 2078, 2, 30), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288041, 2078, 1, 69), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288041, 2078, 2, 70), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288042, 2078, 1, 53), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288042, 2078, 2, 54), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288075, 2078, 1, 25), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(57288075, 2078, 2, 26), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(81080221, 2078, 1, 17), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(81080221, 2078, 2, 18), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(81080222, 2078, 1, 13), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(81080222, 2078, 2, 14), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(81080223, 2078, 1, 9), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(81080223, 2078, 2, 10), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85145925, 2078, 1, 21), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85145925, 2078, 2, 22), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85281103, 2078, 1, 77), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85281103, 2078, 2, 78), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85281104, 2078, 1, 81), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(85281104, 2078, 2, 82), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87326012, 2078, 1, 1), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87326012, 2078, 2, 2), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(87349854, 2078, 3, 7), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257295, 2117, 1, 13), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257295, 2117, 2, 14), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257295, 2117, 3, 15), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257295, 2117, 4, 16), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257296, 2117, 1, 18), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257296, 2117, 2, 19), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257296, 2117, 3, 20), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257296, 2117, 4, 21), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257297, 2117, 1, 23), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257297, 2117, 2, 24), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257297, 2117, 3, 25), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257297, 2117, 4, 26), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257298, 2117, 1, 7), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257298, 2117, 2, 8), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257298, 2117, 3, 9), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257298, 2117, 4, 10), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257298, 2117, 6, 12), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257300, 2117, 1, 28), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257300, 2117, 2, 29), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257300, 2117, 3, 30), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257300, 2117, 4, 31), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257301, 2117, 1, 1), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257301, 2117, 2, 2), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85257301, 2117, 3, 3), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257301, 2117, 4, 4), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85257301, 2117, 6, 6), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(85856282, 2128, 1, 1), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85856282, 2128, 2, 2), 192));
        wrongTypes.add(Pair.getPair(new EndpointID(85856282, 2128, 3, 3), 192));
        wrongTypes.add(Pair.getPair(new EndpointID(85856282, 2128, 4, 4), 192));
        wrongTypes.add(Pair.getPair(new EndpointID(85856281, 2317, 1, 1), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(85856281, 2317, 2, 2), 192));
        wrongTypes.add(Pair.getPair(new EndpointID(85856281, 2317, 3, 3), 192));
        wrongTypes.add(Pair.getPair(new EndpointID(85856281, 2317, 4, 4), 192));
        wrongTypes.add(Pair.getPair(new EndpointID(103913572, 492969, 1, 1), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(103913572, 492969, 2, 2), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(103913572, 492969, 3, 3), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(125269096, 504420, 1, 23), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269096, 504420, 2, 24), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269096, 504420, 3, 25), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269096, 504420, 4, 26), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269096, 504420, 5, 27), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(125269096, 504420, 6, 28), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(125269120, 504420, 10, 10), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269120, 504420, 11, 11), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269120, 504420, 5, 5), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(125269120, 504420, 6, 6), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(125269120, 504420, 7, 7), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269120, 504420, 8, 8), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269120, 504420, 9, 9), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269138, 504420, 1, 12), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269138, 504420, 2, 13), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269138, 504420, 3, 14), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269138, 504420, 4, 15), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(125269138, 504420, 5, 16), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(125269138, 504420, 6, 17), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(124360653, 540309, 1, 29), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(124360653, 540309, 2, 30), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(124360653, 540309, 3, 31), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(124360653, 540309, 4, 32), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(124360653, 540309, 5, 33), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(124360653, 540309, 6, 34), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(124360653, 540309, 7, 35), 187));
        wrongTypes.add(Pair.getPair(new EndpointID(117695958, 624279, 1, 1), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(117695958, 624279, 2, 2), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(117695958, 624279, 3, 3), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(117695958, 624279, 4, 4), 190));
        wrongTypes.add(Pair.getPair(new EndpointID(117695958, 624279, 5, 5), 188));
        wrongTypes.add(Pair.getPair(new EndpointID(3716460, 652181, 1, 1), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(3716460, 652181, 2, 2), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(3716460, 652181, 3, 3), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(7973466, 652181, 1, 10), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(7973466, 652181, 3, 12), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(17415857, 652181, 1, 4), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(17415857, 652181, 2, 5), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(17415857, 652181, 3, 6), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(17432643, 652181, 1, 13), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(17432643, 652181, 3, 15), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(26657680, 652181, 3, 24), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(26671645, 652181, 3, 30), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(26730174, 652181, 3, 21), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(26730315, 652181, 3, 27), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(49669817, 652181, 1, 7), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(49669817, 652181, 2, 8), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(49669817, 652181, 3, 9), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(49680441, 652181, 1, 16), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(49680441, 652181, 2, 17), 34));
        wrongTypes.add(Pair.getPair(new EndpointID(49680441, 652181, 3, 18), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(85267040, 652181, 3, 32), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(85267041, 652181, 3, 34), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(85267042, 652181, 3, 36), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(85267043, 652181, 3, 38), 2162));
        wrongTypes.add(Pair.getPair(new EndpointID(85267044, 652181, 3, 40), 2162));

        IntQuaterpletIntMap keepTypes = new IntQuaterpletIntMap();
        IntQuaterpletIntMap newTypes = new IntQuaterpletIntMap();
        IntQuaterpletIntMap oldTypes = new IntQuaterpletIntMap();

        load("select substance,bioassay,measuregroup,value,endpoint_type_id from pubchem.endpoint_measurements "
                + "where endpoint_type_id is not null", oldTypes);

        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    EndpointID endpoint = parseEndpoint(subject, false);
                    Pair<Integer, Integer> type = Ontology.getId(object.getURI());

                    if(type.getOne() != Ontology.unitBAO)
                        throw new IOException();

                    if(wrongTypes.contains(Pair.getPair(endpoint, type.getTwo())))
                        return;

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
            }.load(stream);
        }

        store("update pubchem.endpoint_measurements set endpoint_type_id=null "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and endpoint_type_id=?", oldTypes);
        store("insert into pubchem.endpoint_measurements(substance,bioassay,measuregroup,value,endpoint_type_id) "
                + "values(?,?,?,?,?) "
                + "on conflict(substance,bioassay,measuregroup,value) do update set endpoint_type_id=EXCLUDED.endpoint_type_id",
                newTypes);
    }


    private static void loadLabels() throws IOException, SQLException
    {
        // workaround
        HashSet<Pair<EndpointID, String>> wrongLabels = new HashSet<Pair<EndpointID, String>>();
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 1, 1), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 2, 2), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 3, 3), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 4, 4), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 5, 5), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 6, 6), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 7, 7), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 8, 33), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 9, 34), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 10, 35), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 11, 36), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 12, 37), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 13, 38), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 14, 39), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 15, 40), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 16, 41), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 17, 42), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 18, 43), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 19, 44), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 20, 45), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 21, 46), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 22, 47), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 23, 48), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 24, 49), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 25, 50), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 1, 1), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 2, 2), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 3, 3), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 4, 4), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 5, 5), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 6, 6), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87544119, 1801, 7, 7), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 8, 33), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 9, 34), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 10, 35), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 11, 36), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 12, 37), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 13, 38), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 14, 39), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 15, 40), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 16, 41), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 17, 42), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 18, 43), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 19, 44), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 20, 45), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 21, 46), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 22, 47), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 23, 48), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 24, 49), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103911214, 1801, 25, 50), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(96021160, 1880, 1, 1), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(96021160, 1880, 2, 2), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(96021160, 1880, 3, 3), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(110923218, 1880, 4, 14), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(110923218, 1880, 5, 15), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(110923218, 1880, 6, 16), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(110923218, 1880, 7, 17), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(110923218, 1880, 8, 18), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(110923218, 1880, 9, 19), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(110923218, 1880, 10, 20), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288063, 2049, 1, 41), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288063, 2049, 2, 42), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288064, 2049, 1, 25), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288064, 2049, 2, 26), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288068, 2049, 1, 33), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288068, 2049, 2, 34), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288069, 2049, 1, 37), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288069, 2049, 2, 38), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288070, 2049, 1, 29), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288070, 2049, 2, 30), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85261423, 2049, 1, 45), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85261423, 2049, 2, 46), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85261424, 2049, 1, 49), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85261424, 2049, 2, 50), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85261425, 2049, 1, 53), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85261425, 2049, 2, 54), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85261426, 2049, 1, 57), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85261426, 2049, 2, 58), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87326009, 2049, 1, 9), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87326009, 2049, 2, 10), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87326010, 2049, 1, 1), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87326010, 2049, 2, 2), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87326011, 2049, 1, 13), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87326011, 2049, 2, 14), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87334054, 2049, 3, 7), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87350366, 2049, 1, 17), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87350366, 2049, 2, 18), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87350367, 2049, 1, 21), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87350367, 2049, 2, 22), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(4257091, 2067, 1, 50), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(7972299, 2067, 1, 1), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(7972299, 2067, 2, 2), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(7972299, 2067, 3, 3), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(57288001, 2067, 1, 8), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(57288001, 2067, 2, 9), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288001, 2067, 3, 10), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(57288002, 2067, 1, 22), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(57288002, 2067, 3, 24), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(57288005, 2067, 1, 29), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(57288005, 2067, 3, 31), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(57288006, 2067, 1, 36), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(57288006, 2067, 3, 38), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(87241484, 2067, 2, 58), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87241484, 2067, 3, 59), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(87350363, 2067, 1, 43), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(87350363, 2067, 3, 45), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(87350364, 2067, 1, 15), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(87350364, 2067, 3, 17), "Ki "));
        wrongLabels.add(Pair.getPair(new EndpointID(57287832, 2078, 1, 33), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57287832, 2078, 2, 34), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57287833, 2078, 1, 45), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57287833, 2078, 2, 46), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57287838, 2078, 1, 57), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57287838, 2078, 2, 58), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57287843, 2078, 1, 61), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57287843, 2078, 2, 62), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288032, 2078, 1, 41), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288032, 2078, 2, 42), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288035, 2078, 1, 73), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288035, 2078, 2, 74), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288036, 2078, 1, 65), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288036, 2078, 2, 66), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288037, 2078, 1, 49), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288037, 2078, 2, 50), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288038, 2078, 1, 37), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288038, 2078, 2, 38), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288040, 2078, 1, 29), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288040, 2078, 2, 30), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288041, 2078, 1, 69), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288041, 2078, 2, 70), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288042, 2078, 1, 53), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288042, 2078, 2, 54), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288075, 2078, 1, 25), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(57288075, 2078, 2, 26), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(81080221, 2078, 1, 17), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(81080221, 2078, 2, 18), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(81080222, 2078, 1, 13), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(81080222, 2078, 2, 14), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(81080223, 2078, 1, 9), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(81080223, 2078, 2, 10), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85145925, 2078, 1, 21), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85145925, 2078, 2, 22), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85281103, 2078, 1, 77), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85281103, 2078, 2, 78), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85281104, 2078, 1, 81), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85281104, 2078, 2, 82), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87326012, 2078, 1, 1), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87326012, 2078, 2, 2), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(87349854, 2078, 3, 7), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257295, 2117, 1, 13), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257295, 2117, 2, 14), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257295, 2117, 3, 15), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257295, 2117, 4, 16), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257296, 2117, 1, 18), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257296, 2117, 2, 19), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257296, 2117, 3, 20), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257296, 2117, 4, 21), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257297, 2117, 1, 23), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257297, 2117, 2, 24), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257297, 2117, 3, 25), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257297, 2117, 4, 26), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257298, 2117, 1, 7), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257298, 2117, 2, 8), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257298, 2117, 3, 9), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257298, 2117, 4, 10), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257298, 2117, 6, 12), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257300, 2117, 1, 28), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257300, 2117, 2, 29), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257300, 2117, 3, 30), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257300, 2117, 4, 31), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257301, 2117, 1, 1), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257301, 2117, 2, 2), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257301, 2117, 3, 3), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257301, 2117, 4, 4), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85257301, 2117, 6, 6), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85856282, 2128, 1, 1), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85856282, 2128, 2, 2), "Ki"));
        wrongLabels.add(Pair.getPair(new EndpointID(85856282, 2128, 3, 3), "Ki"));
        wrongLabels.add(Pair.getPair(new EndpointID(85856282, 2128, 4, 4), "Ki"));
        wrongLabels.add(Pair.getPair(new EndpointID(85856281, 2317, 1, 1), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(85856281, 2317, 2, 2), "Ki"));
        wrongLabels.add(Pair.getPair(new EndpointID(85856281, 2317, 3, 3), "Ki"));
        wrongLabels.add(Pair.getPair(new EndpointID(85856281, 2317, 4, 4), "Ki"));
        wrongLabels.add(Pair.getPair(new EndpointID(103913572, 492969, 1, 1), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103913572, 492969, 2, 2), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(103913572, 492969, 3, 3), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269096, 504420, 1, 23), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269096, 504420, 2, 24), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269096, 504420, 3, 25), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269096, 504420, 4, 26), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269096, 504420, 5, 27), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269096, 504420, 6, 28), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269120, 504420, 10, 10), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269120, 504420, 11, 11), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269120, 504420, 5, 5), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269120, 504420, 6, 6), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269120, 504420, 7, 7), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269120, 504420, 8, 8), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269120, 504420, 9, 9), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269138, 504420, 1, 12), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269138, 504420, 2, 13), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269138, 504420, 3, 14), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269138, 504420, 4, 15), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269138, 504420, 5, 16), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(125269138, 504420, 6, 17), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(124360653, 540309, 1, 29), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(124360653, 540309, 2, 30), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(124360653, 540309, 3, 31), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(124360653, 540309, 4, 32), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(124360653, 540309, 5, 33), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(124360653, 540309, 6, 34), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(124360653, 540309, 7, 35), "CC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(117695958, 624279, 1, 1), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(117695958, 624279, 2, 2), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(117695958, 624279, 3, 3), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(117695958, 624279, 4, 4), "IC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(117695958, 624279, 5, 5), "EC50"));
        wrongLabels.add(Pair.getPair(new EndpointID(3716460, 652181, 1, 1), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(3716460, 652181, 2, 2), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(3716460, 652181, 3, 3), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(7973466, 652181, 1, 10), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(7973466, 652181, 3, 12), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(17415857, 652181, 1, 4), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(17415857, 652181, 2, 5), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(17415857, 652181, 3, 6), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(17432643, 652181, 1, 13), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(17432643, 652181, 3, 15), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(26657680, 652181, 3, 24), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(26671645, 652181, 3, 30), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(26730174, 652181, 3, 21), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(26730315, 652181, 3, 27), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(49669817, 652181, 1, 7), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(49669817, 652181, 2, 8), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(49669817, 652181, 3, 9), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(49680441, 652181, 1, 16), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(49680441, 652181, 2, 17), "Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(49680441, 652181, 3, 18), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(85267040, 652181, 3, 32), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(85267041, 652181, 3, 34), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(85267042, 652181, 3, 36), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(85267043, 652181, 3, 38), "Average Kd"));
        wrongLabels.add(Pair.getPair(new EndpointID(85267044, 652181, 3, 40), "Average Kd"));

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

                    if(wrongLabels.contains(Pair.getPair(endpoint, label)))
                        return;

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
        IntQuaterpletIntFloatPairMap keepMeasuredValues = new IntQuaterpletIntFloatPairMap();
        IntQuaterpletIntFloatPairMap newMeasuredValues = new IntQuaterpletIntFloatPairMap();
        IntQuaterpletIntFloatPairMap oldMeasuredValues = new IntQuaterpletIntFloatPairMap();

        load("select substance,bioassay,measuregroup,value,measurement_type_id,measurement from pubchem.endpoint_measurements "
                + "where measurement_type_id is not null and measurement is not null", oldMeasuredValues);

        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint_value.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    EndpointID endpoint = parseEndpoint(subject, false);
                    Integer type = getIntID(predicate, "http://semanticscience.org/resource/SIO_");
                    Float measurement = getFloatFromDecimal(object);
                    Pair<Integer, Float> pair = Pair.getPair(type, measurement);

                    if(type != 300 && type != 738 && type != 734 && type != 735 && type != 733 && type != 699)
                        throw new IOException();

                    oldMeasurements.remove(endpoint);

                    if(pair.equals(oldMeasuredValues.remove(endpoint)))
                    {
                        keepMeasuredValues.put(endpoint, pair);
                    }
                    else
                    {
                        Pair<Integer, Float> keep = keepMeasuredValues.get(endpoint);

                        if(pair.equals(keep))
                            return;
                        else if(keep != null)
                            throw new IOException();

                        Pair<Integer, Float> put = newMeasuredValues.put(endpoint, pair);

                        if(put != null && !pair.equals(put))
                            throw new IOException();
                    }
                }
            }.load(stream);
        }

        store("update pubchem.endpoint_measurements set label=null "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and measurement_type_id=? and measurement=?",
                oldMeasuredValues);
        store("insert into pubchem.endpoint_measurements(substance,bioassay,measuregroup,value,measurement_type_id,measurement) "
                + "values(?,?,?,?,?,?) "
                + "on conflict(substance,bioassay,measuregroup,value) do update set measurement_type_id=EXCLUDED.measurement_type_id, "
                + "measurement=EXCLUDED.measurement", newMeasuredValues);
    }


    private static void loadReferences() throws IOException, SQLException
    {
        IntQuaterpletIntSet keepReferences = new IntQuaterpletIntSet();
        IntQuaterpletIntSet newReferences = new IntQuaterpletIntSet();
        IntQuaterpletIntSet oldReferences = new IntQuaterpletIntSet();

        load("select substance,bioassay,measuregroup,value,reference from pubchem.endpoint_references", oldReferences);

        try(InputStream stream = getTtlStream("pubchem/RDF/endpoint/pc_endpoint2reference.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/spar/cito/citesAsDataSource"))
                        throw new IOException();

                    Integer referenceID = Reference.getReferenceID(object.getURI());
                    EndpointID endpoint = parseEndpoint(subject, false);

                    Pair<EndpointID, Integer> pair = Pair.getPair(endpoint, referenceID);

                    if(oldReferences.remove(pair))
                        keepReferences.add(pair);
                    else if(!keepReferences.contains(pair))
                        newReferences.add(pair);
                }
            }.load(stream);
        }

        store("delete from pubchem.endpoint_references "
                + "where substance=? and bioassay=? and measuregroup=? and value=? and reference=?", oldReferences);
        store("insert into pubchem.endpoint_references(substance,bioassay,measuregroup,value,reference) "
                + "values(?,?,?,?,?)", newReferences);
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
