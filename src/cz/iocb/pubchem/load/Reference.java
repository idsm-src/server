package cz.iocb.pubchem.load;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.pubchem.load.Ontology.Identifier;
import cz.iocb.pubchem.load.common.IntTriplet;
import cz.iocb.pubchem.load.common.TripleStreamProcessor;
import cz.iocb.pubchem.load.common.Updater;



class Reference extends Updater
{
    private static void loadTypes() throws IOException, SQLException
    {
        IntIntHashMap newTypes = new IntIntHashMap(20000000);
        IntIntHashMap oldTypes = getIntIntMap("select id, type_id from reference_bases", 20000000);

        try(InputStream stream = getStream("RDF/reference/pc_reference_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    int referenceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");

                    Identifier type = Ontology.getId(object.getURI());

                    if(type.unit != Ontology.unitUncategorized)
                        throw new IOException();

                    if(type.id != oldTypes.removeKeyIfAbsent(referenceID, NO_VALUE))
                        newTypes.put(referenceID, type.id);
                }
            }.load(stream);
        }

        batch("delete from reference_bases where id = ?", oldTypes.keySet());
        batch("insert into reference_bases(id, type_id) values (?,?) "
                + "on conflict (id) do update set type_id=EXCLUDED.type_id", newTypes);
    }


    private static void loadDates() throws IOException, SQLException
    {
        IntStringMap newDates = new IntStringMap(20000000);
        IntStringMap oldDates = getIntStringMap(
                "select id, dcdate::varchar from reference_bases where dcdate is not null", 20000000);

        try(InputStream stream = getStream("RDF/reference/pc_reference_date.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/dc/terms/date"))
                        throw new IOException();

                    int referenceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");
                    String date = getString(object).replaceFirst("-0[45]:00$", "");

                    if(!date.equals(oldDates.remove(referenceID)))
                        newDates.put(referenceID, date);
                }
            }.load(stream);
        }

        batch("update reference_bases set dcdate = null where id = ?", oldDates.keySet());
        batch("update reference_bases set dcdate = cast(? as date) where id = ?", newDates, Direction.REVERSE);
    }


    private static void loadCitations() throws IOException, SQLException
    {
        IntStringMap newCitations = new IntStringMap(20000000);
        IntStringMap oldCitations = getIntStringMap(
                "select id, citation from reference_bases where citation is not null", 20000000);

        processFiles("RDF/reference", "pc_reference_citation_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/bibliographicCitation"))
                            throw new IOException();

                        int referenceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");
                        String citation = getString(object);

                        synchronized(newCitations)
                        {
                            if(!citation.equals(oldCitations.remove(referenceID)))
                                newCitations.put(referenceID, citation);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update reference_bases set citation = null where id = ?", oldCitations.keySet());
        batch("update reference_bases set citation = ? where id = ?", newCitations, Direction.REVERSE);
    }


    private static void loadTitles() throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap(20000000);
        IntStringMap oldTitles = getIntStringMap("select id, title from reference_bases where title is not null",
                20000000);

        processFiles("RDF/reference", "pc_reference_title_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/title"))
                            throw new IOException();

                        int referenceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");
                        String title = getString(object);

                        synchronized(newTitles)
                        {
                            if(!title.equals(oldTitles.remove(referenceID)))
                                newTitles.put(referenceID, title);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update reference_bases set title = null where id = ?", oldTitles.keySet());
        batch("update reference_bases set title = ? where id = ?", newTitles, Direction.REVERSE);
    }


    private static void loadChemicalDiseases() throws IOException, SQLException
    {
        IntPairSet newDiscusses = new IntPairSet(100000000);
        IntPairSet oldDiscusses = getIntPairSet("select reference, statement from reference_discusses", 100000000);

        processFiles("RDF/reference", "pc_reference2chemical_disease_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/spar/cito/discusses"))
                            throw new IOException();

                        int referenceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");
                        int statementID;

                        if(object.getURI().length() > 27 && object.getURI().charAt(27) == 'M')
                            statementID = getIntID(object, "http://id.nlm.nih.gov/mesh/M");
                        else
                            statementID = -getIntID(object, "http://id.nlm.nih.gov/mesh/C");

                        IntIntPair pair = PrimitiveTuples.pair(referenceID, statementID);

                        synchronized(newDiscusses)
                        {
                            if(!oldDiscusses.remove(pair))
                                newDiscusses.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from reference_discusses where reference = ? and statement = ?", oldDiscusses);
        batch("insert into reference_discusses(reference, statement) values (?,?)", newDiscusses);
    }


    private static void loadMeshheadings() throws IOException, SQLException
    {
        IntTripletSet newTerms = new IntTripletSet(200000000);
        IntTripletSet oldTerms = getIntTripletSet(
                "select reference, descriptor, qualifier from reference_subject_descriptors", 200000000);

        processFiles("RDF/reference", "pc_reference2meshheading_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/spar/fabio/hasSubjectTerm"))
                            throw new IOException();

                        int referenceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");

                        String value = getStringID(object, "http://id.nlm.nih.gov/mesh/D");
                        int idx = value.indexOf('Q');

                        int descriptor = Integer.parseInt(idx == -1 ? value : value.substring(0, idx));
                        int qualifier = idx == -1 ? -1 : Integer.parseInt(value.substring(idx + 1));

                        IntTriplet triplet = new IntTriplet(referenceID, descriptor, qualifier);

                        synchronized(newTerms)
                        {
                            if(!oldTerms.remove(triplet))
                                newTerms.add(triplet);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from reference_subject_descriptors where reference = ? and descriptor = ? and qualifier = ?",
                oldTerms);
        batch("insert into reference_subject_descriptors(reference, descriptor, qualifier) values (?,?,?)", newTerms);
    }


    private static void loadPrimaryMeshheadings() throws IOException, SQLException
    {
        IntTripletSet newTerms = new IntTripletSet(200000000);
        IntTripletSet oldTerms = getIntTripletSet(
                "select reference, descriptor, qualifier from reference_primary_subject_descriptors", 200000000);

        processFiles("RDF/reference", "pc_reference2meshheading_primary_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/spar/fabio/hasPrimarySubjectTerm"))
                            throw new IOException();

                        int referenceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");

                        String value = getStringID(object, "http://id.nlm.nih.gov/mesh/D");
                        int idx = value.indexOf('Q');

                        int descriptor = Integer.parseInt(idx == -1 ? value : value.substring(0, idx));
                        int qualifier = idx == -1 ? -1 : Integer.parseInt(value.substring(idx + 1));

                        IntTriplet triplet = new IntTriplet(referenceID, descriptor, qualifier);

                        synchronized(newTerms)
                        {
                            if(!oldTerms.remove(triplet))
                                newTerms.add(triplet);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from reference_primary_subject_descriptors where reference = ? and descriptor = ? and qualifier = ?",
                oldTerms);
        batch("insert into reference_primary_subject_descriptors(reference, descriptor, qualifier) values (?,?,?)",
                newTerms);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load references ...");

        loadTypes();
        loadDates();
        loadCitations();
        loadTitles();
        loadChemicalDiseases();
        loadMeshheadings();
        loadPrimaryMeshheadings();

        System.out.println();
    }
}
