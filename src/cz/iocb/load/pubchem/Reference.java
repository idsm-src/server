package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



class Reference extends Updater
{
    private static void loadTypes() throws IOException, SQLException
    {
        IntIntHashMap newTypes = new IntIntHashMap(20000000);
        IntIntHashMap oldTypes = getIntIntMap("select id, type_id from pubchem.reference_bases", 20000000);

        try(InputStream stream = getStream("pubchem/RDF/reference/pc_reference_type.ttl.gz"))
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

        batch("delete from pubchem.reference_bases where id = ?", oldTypes.keySet());
        batch("insert into pubchem.reference_bases(id, type_id) values (?,?) "
                + "on conflict (id) do update set type_id=EXCLUDED.type_id", newTypes);
    }


    private static void loadDates() throws IOException, SQLException
    {
        IntStringMap newDates = new IntStringMap(20000000);
        IntStringMap oldDates = getIntStringMap(
                "select id, dcdate::varchar from pubchem.reference_bases where dcdate is not null", 20000000);

        try(InputStream stream = getStream("pubchem/RDF/reference/pc_reference_date.ttl.gz"))
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

        batch("update pubchem.reference_bases set dcdate = null where id = ?", oldDates.keySet());
        batch("update pubchem.reference_bases set dcdate = cast(? as date) where id = ?", newDates, Direction.REVERSE);
    }


    private static void loadCitations() throws IOException, SQLException
    {
        IntStringMap newCitations = new IntStringMap(20000000);
        IntStringMap oldCitations = getIntStringMap(
                "select id, citation from pubchem.reference_bases where citation is not null", 20000000);

        processFiles("pubchem/RDF/reference", "pc_reference_citation_[0-9]+\\.ttl\\.gz", file -> {
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

        batch("update pubchem.reference_bases set citation = null where id = ?", oldCitations.keySet());
        batch("update pubchem.reference_bases set citation = ? where id = ?", newCitations, Direction.REVERSE);
    }


    private static void loadTitles() throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap(20000000);
        IntStringMap oldTitles = getIntStringMap(
                "select id, title from pubchem.reference_bases where title is not null", 20000000);

        processFiles("pubchem/RDF/reference", "pc_reference_title_[0-9]+\\.ttl\\.gz", file -> {
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

        batch("update pubchem.reference_bases set title = null where id = ?", oldTitles.keySet());
        batch("update pubchem.reference_bases set title = ? where id = ?", newTitles, Direction.REVERSE);
    }


    private static void loadChemicalDiseases() throws IOException, SQLException
    {
        IntStringPairSet newDiscusses = new IntStringPairSet(100000000);
        IntStringPairSet oldDiscusses = getIntStringPairSet(
                "select reference, statement from pubchem.reference_discusses", 100000000);

        processFiles("pubchem/RDF/reference", "pc_reference2chemical_disease_[0-9]+\\.ttl\\.gz", file -> {
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
                        String statementID = getStringID(object, "http://id.nlm.nih.gov/mesh/");
                        IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, statementID);

                        synchronized(newDiscusses)
                        {
                            if(!oldDiscusses.remove(pair))
                                newDiscusses.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_discusses where reference = ? and statement = ?", oldDiscusses);
        batch("insert into pubchem.reference_discusses(reference, statement) values (?,?)", newDiscusses);
    }


    private static void loadMeshheadings() throws IOException, SQLException
    {
        IntStringPairSet newSubjects = new IntStringPairSet(200000000);
        IntStringPairSet oldSubjects = getIntStringPairSet("select reference, subject from pubchem.reference_subjects",
                200000000);

        processFiles("pubchem/RDF/reference", "pc_reference2meshheading_[0-9]+\\.ttl\\.gz", file -> {
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
                        String subjectID = getStringID(object, "http://id.nlm.nih.gov/mesh/");
                        IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, subjectID);

                        synchronized(newSubjects)
                        {
                            if(!oldSubjects.remove(pair))
                                newSubjects.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_subjects where reference = ? and subject = ?", oldSubjects);
        batch("insert into pubchem.reference_subjects(reference, subject) values (?,?)", newSubjects);
    }


    private static void loadPrimaryMeshheadings() throws IOException, SQLException
    {
        IntStringPairSet newSubjects = new IntStringPairSet(200000000);
        IntStringPairSet oldSubjects = getIntStringPairSet(
                "select reference, subject from pubchem.reference_primary_subjects", 200000000);

        processFiles("pubchem/RDF/reference", "pc_reference2meshheading_primary_[0-9]+\\.ttl\\.gz", file -> {
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
                        String subjectID = getStringID(object, "http://id.nlm.nih.gov/mesh/");
                        IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, subjectID);

                        synchronized(newSubjects)
                        {
                            if(!oldSubjects.remove(pair))
                                newSubjects.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_primary_subjects where reference = ? and subject = ?", oldSubjects);
        batch("insert into pubchem.reference_primary_subjects(reference, subject) values (?,?)", newSubjects);
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
