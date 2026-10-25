package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.date;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static java.util.Locale.ROOT;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem patents ({@code pubchem/RDF/patent}) into the table {@code pubchem.patents}, the tables of
 * their inventors and applicants and the tables of their citations and classifications.
 */
class Patent extends Updater
{
    /**
     * IRI prefix of the patents.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * IRI prefix of the patent inventors.
     */
    static final String inventorPrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/patentinventor/MD5_";

    /**
     * Length of {@link #inventorPrefix}.
     */
    static final int inventorPrefixLength = inventorPrefix.length();

    /**
     * IRI prefix of the patent assignees, which are stored as the applicants.
     */
    static final String assigneePrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/patentassignee/MD5_";

    /**
     * Length of {@link #assigneePrefix}.
     */
    static final int assigneePrefixLength = assigneePrefix.length();

    /**
     * Table of the patents.
     */
    private static final EntityTable<Integer> patents = new EntityTable<>("pubchem.patents", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), uniqueVarchar("title"), uniqueVarchar("abstract"),
            uniqueVarchar("publication_number"), date("filing_date"), date("grant_date"), date("publication_date"),
            date("priority_date"));

    /**
     * Ids of the patents by their IRIs without the prefix.
     */
    private static final StringIntMap patentIDs = new StringIntMap();

    /**
     * Id of the next new patent.
     */
    private static int nextPatentID;

    /**
     * Stored inventors met in the data.
     */
    private static final StringSet keepInventors = new StringSet();

    /**
     * Inventors met in the data that are not stored yet.
     */
    private static final StringSet newInventors = new StringSet();

    /**
     * Stored inventors not met in the data so far; those left are deleted.
     */
    private static final StringSet oldInventors = new StringSet();

    /**
     * Stored assignees met in the data.
     */
    private static final StringSet keepAssignees = new StringSet();

    /**
     * Assignees met in the data that are not stored yet.
     */
    private static final StringSet newAssignees = new StringSet();

    /**
     * Stored assignees not met in the data so far; those left are deleted.
     */
    private static final StringSet oldAssignees = new StringSet();

    /**
     * Patents referenced but not described by the data.
     */
    private static final MissingEntities<String> missingPatents = new MissingEntities<>("patent", false);

    /**
     * Inventors referenced but not described by the data.
     */
    private static final MissingEntities<String> missingInventors = new MissingEntities<>("patent inventor", false);

    /**
     * Assignees referenced but not described by the data.
     */
    private static final MissingEntities<String> missingAssignees = new MissingEntities<>("patent assignee", false);


    /**
     * Not instantiable.
     */
    private Patent()
    {
    }


    /**
     * Loads the ids of the stored patents, inventors and assignees and adds those that the type files describe.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadBases() throws IOException, SQLException
    {
        load("select iri,id from pubchem.patents", patentIDs);
        load("select id from pubchem.inventors", oldInventors);
        load("select id from pubchem.applicants", oldAssignees);

        nextPatentID = patentIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        processFiles("pubchem/RDF/patent", "pc_patent2type_[0-9]+\\.ttl\\.gz", file -> {
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

                        switch(subject.getURI().replaceFirst("[^/_]*$", ""))
                        {
                            case prefix ->
                            {
                                if(!checkType(subject, object, "http://data.epo.org/linked-data/def/patent/Publication",
                                        "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Patent"))
                                    return;

                                addPatent(getStringID(subject, prefix));
                            }

                            case inventorPrefix ->
                            {
                                if(!checkType(subject, object,
                                        "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PatentInventor"))
                                    return;

                                String inventorID = subject.getURI().substring(inventorPrefixLength);

                                synchronized(newInventors)
                                {
                                    if(oldInventors.remove(inventorID))
                                        keepInventors.add(inventorID);
                                    else
                                        newInventors.add(inventorID);
                                }
                            }

                            case assigneePrefix ->
                            {
                                if(!checkType(subject, object,
                                        "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PatentAssignee"))
                                    return;

                                String assigneeID = subject.getURI().substring(assigneePrefixLength);

                                synchronized(newAssignees)
                                {
                                    if(oldAssignees.remove(assigneeID))
                                        keepAssignees.add(assigneeID);
                                    else
                                        newAssignees.add(assigneeID);
                                }
                            }

                            default -> throw new DataException("unexpected IRI", text(subject));
                        }
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the titles of the patents.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTitles() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/patent", "pc_patent2title_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/titleOfInvention"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String title = getString(object);

                        patents.set(patentID, "title", title);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the abstracts of the patents.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadAbstracts() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/patent", "pc_patent2abstract_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/abstract"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String value = getString(object);

                        patents.set(patentID, "abstract", value);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the publication numbers of the patents.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadNumbers() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/patent", "pc_patent2publicationnumber_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/publicationNumber"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String number = getString(object);

                        patents.set(patentID, "publication_number", number);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the filing dates of the patents.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadFilingDates() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/patent", "pc_patent2filingdate_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/filingDate"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String date = getDate(object);

                        patents.set(patentID, "filing_date", date);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the grant dates of the patents.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGrantDates() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/patent", "pc_patent2grantdate_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/grantDate"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String date = getDate(object);

                        patents.set(patentID, "grant_date", date);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the publication dates of the patents.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadPublicationDates() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/patent", "pc_patent2publicationdate_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/publicationDate"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String date = getDate(object);

                        patents.set(patentID, "publication_date", date);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the priority dates of the patents.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadPriorityDates() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/patent", "pc_patent2prioritydate_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#priorityDate"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String date = getDate(object);

                        patents.set(patentID, "priority_date", date);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the patents citing the patents and stores their changes in {@code pubchem.patent_citations}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadCitations() throws IOException, SQLException
    {
        IntPairSet keepCitations = new IntPairSet();
        IntPairSet newCitations = new IntPairSet();
        IntPairSet oldCitations = new IntPairSet();

        load("select patent,citation from pubchem.patent_citations", oldCitations);

        processFiles("pubchem/RDF/patent", "pc_patent2iscitedby_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://purl.org/spar/cito/isCitedBy"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        Integer citationID = getPatentID(object.getURI());

                        Pair<Integer, Integer> pair = Pair.getPair(patentID, citationID);

                        synchronized(newCitations)
                        {
                            if(oldCitations.remove(pair))
                                keepCitations.add(pair);
                            else if(!keepCitations.contains(pair))
                                newCitations.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.patent_citations where patent=? and citation=?", oldCitations);
        store("insert into pubchem.patent_citations(patent,citation) values(?,?)", newCitations);
    }


    /**
     * Loads the additional CPC classifications of the patents and stores their changes in
     * {@code pubchem.patent_cpc_additional_classifications}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadCpcAdditionalClassifications() throws IOException, SQLException
    {
        IntStringSet keepClassifications = new IntStringSet();
        IntStringSet newClassifications = new IntStringSet();
        IntStringSet oldClassifications = new IntStringSet();

        load("select patent,classification from pubchem.patent_cpc_additional_classifications", oldClassifications);

        processFiles("pubchem/RDF/patent", "pc_patent2cpc_additional_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/classificationCPCAdditional"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String classification = PatentCpc.getCpcID(object.getURI());

                        Pair<Integer, String> pair = Pair.getPair(patentID, classification);

                        synchronized(newClassifications)
                        {
                            if(oldClassifications.remove(pair))
                                keepClassifications.add(pair);
                            else if(!keepClassifications.contains(pair))
                                newClassifications.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.patent_cpc_additional_classifications where patent=? and classification=?",
                oldClassifications);
        store("insert into pubchem.patent_cpc_additional_classifications(patent,classification) values(?,?)",
                newClassifications);
    }


    /**
     * Loads the inventive CPC classifications of the patents and stores their changes in
     * {@code pubchem.patent_cpc_inventive_classifications}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadCpcInventiveClassifications() throws IOException, SQLException
    {
        IntStringSet keepClassifications = new IntStringSet();
        IntStringSet newClassifications = new IntStringSet();
        IntStringSet oldClassifications = new IntStringSet();

        load("select patent,classification from pubchem.patent_cpc_inventive_classifications", oldClassifications);

        processFiles("pubchem/RDF/patent", "pc_patent2cpc_inventive_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/classificationCPCInventive"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String classification = PatentCpc.getCpcID(object.getURI());

                        Pair<Integer, String> pair = Pair.getPair(patentID, classification);

                        synchronized(newClassifications)
                        {
                            if(oldClassifications.remove(pair))
                                keepClassifications.add(pair);
                            else if(!keepClassifications.contains(pair))
                                newClassifications.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.patent_cpc_inventive_classifications where patent=? and classification=?",
                oldClassifications);
        store("insert into pubchem.patent_cpc_inventive_classifications(patent,classification) values(?,?)",
                newClassifications);
    }


    /**
     * Loads the additional IPC classifications of the patents and stores their changes in
     * {@code pubchem.patent_ipc_additional_classifications}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadIpcAdditionalClassifications() throws IOException, SQLException
    {
        IntStringSet keepClassifications = new IntStringSet();
        IntStringSet newClassifications = new IntStringSet();
        IntStringSet oldClassifications = new IntStringSet();

        load("select patent,classification from pubchem.patent_ipc_additional_classifications", oldClassifications);

        processFiles("pubchem/RDF/patent", "pc_patent2ipc_additional_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/classificationIPCAdditional"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String classification = PatentIpc.getIpcID(object.getURI());

                        Pair<Integer, String> pair = Pair.getPair(patentID, classification);

                        synchronized(newClassifications)
                        {
                            if(oldClassifications.remove(pair))
                                keepClassifications.add(pair);
                            else if(!keepClassifications.contains(pair))
                                newClassifications.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.patent_ipc_additional_classifications where patent=? and classification=?",
                oldClassifications);
        store("insert into pubchem.patent_ipc_additional_classifications(patent,classification) values(?,?)",
                newClassifications);
    }


    /**
     * Loads the inventive IPC classifications of the patents and stores their changes in
     * {@code pubchem.patent_ipc_inventive_classifications}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadIpcInventiveClassifications() throws IOException, SQLException
    {
        IntStringSet keepClassifications = new IntStringSet();
        IntStringSet newClassifications = new IntStringSet();
        IntStringSet oldClassifications = new IntStringSet();

        load("select patent,classification from pubchem.patent_ipc_inventive_classifications", oldClassifications);

        processFiles("pubchem/RDF/patent", "pc_patent2ipc_inventive_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/classificationIPCInventive"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String classification = PatentIpc.getIpcID(object.getURI());

                        Pair<Integer, String> pair = Pair.getPair(patentID, classification);

                        synchronized(newClassifications)
                        {
                            if(oldClassifications.remove(pair))
                                keepClassifications.add(pair);
                            else if(!keepClassifications.contains(pair))
                                newClassifications.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.patent_ipc_inventive_classifications where patent=? and classification=?",
                oldClassifications);
        store("insert into pubchem.patent_ipc_inventive_classifications(patent,classification) values(?,?)",
                newClassifications);
    }


    /**
     * Loads the inventors of the patents and stores their changes in {@code pubchem.patent_inventors}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadInventors() throws IOException, SQLException
    {
        IntStringSet keepInventors = new IntStringSet();
        IntStringSet newInventors = new IntStringSet();
        IntStringSet oldInventors = new IntStringSet();

        load("select patent,inventor from pubchem.patent_inventors", oldInventors);

        processFiles("pubchem/RDF/patent", "pc_patent2inventorvc_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/inventorVC"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String inventorID = getInventorID(object.getURI(), false);

                        Pair<Integer, String> pair = Pair.getPair(patentID, inventorID);

                        synchronized(newInventors)
                        {
                            if(oldInventors.remove(pair))
                                keepInventors.add(pair);
                            else if(!keepInventors.contains(pair))
                                newInventors.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.patent_inventors where patent=? and inventor=?", oldInventors);
        store("insert into pubchem.patent_inventors(patent,inventor) values(?,?)", newInventors);
    }


    /**
     * Loads the assignees of the patents and stores their changes in {@code pubchem.patent_applicants}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadApplicants() throws IOException, SQLException
    {
        IntStringSet keepApplicants = new IntStringSet();
        IntStringSet newApplicants = new IntStringSet();
        IntStringSet oldApplicants = new IntStringSet();

        load("select patent,applicant from pubchem.patent_applicants", oldApplicants);

        processFiles("pubchem/RDF/patent", "pc_patent2assigneevc_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://data.epo.org/linked-data/def/patent/applicantVC"))
                            return;

                        Integer patentID = getPatentID(subject.getURI());
                        String assigneeID = getAssigneeID(object.getURI(), false);

                        Pair<Integer, String> pair = Pair.getPair(patentID, assigneeID);

                        synchronized(newApplicants)
                        {
                            if(oldApplicants.remove(pair))
                                keepApplicants.add(pair);
                            else if(!keepApplicants.contains(pair))
                                newApplicants.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.patent_applicants where patent=? and applicant=?", oldApplicants);
        store("insert into pubchem.patent_applicants(patent,applicant) values(?,?)", newApplicants);
    }


    /**
     * Loads the names of the inventors and assignees and stores their changes in {@code pubchem.inventors} and
     * {@code pubchem.applicants}; of the conflicting names of one of them the better one is kept, see
     * {@link #isNameBetter(String, String)}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadFormattedNames() throws IOException, SQLException
    {
        StringStringMap keepInventorNames = new StringStringMap();
        StringStringMap newInventorNames = new StringStringMap();
        StringStringMap oldInventorNames = new StringStringMap();
        StringStringMap replacedInventorNames = new StringStringMap();

        StringStringMap keepAssigneeNames = new StringStringMap();
        StringStringMap newAssigneeNames = new StringStringMap();
        StringStringMap oldAssigneeNames = new StringStringMap();
        StringStringMap replacedAssigneeNames = new StringStringMap();


        load("select id,name from pubchem.inventors where name is not null", oldInventorNames);
        load("select id,name from pubchem.applicants where name is not null", oldAssigneeNames);

        processFiles("pubchem/RDF/patent", "pc_patent2vc_fn_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://www.w3.org/2006/vcard/ns#fn"))
                            return;

                        if(subject.getURI().startsWith(inventorPrefix))
                        {
                            String inventorID = getInventorID(subject.getURI(), true);
                            String name = getString(object);

                            synchronized(newInventorNames)
                            {
                                String pre = oldInventorNames.remove(inventorID);

                                if(name.equals(pre))
                                {
                                    keepInventorNames.put(inventorID, name);
                                }
                                else
                                {
                                    if(pre != null)
                                    {
                                        replacedInventorNames.put(inventorID, pre);
                                    }
                                    else if(name.equals(replacedInventorNames.get(inventorID)))
                                    {
                                        keepInventorNames.put(inventorID, name);
                                        newInventorNames.remove(inventorID);
                                        return;
                                    }

                                    String keep = keepInventorNames.get(inventorID);

                                    if(name.equals(keep))
                                        return;

                                    if(keep != null)
                                    {
                                        if(!isNameBetter(name, keep))
                                            return;
                                    }

                                    String put = newInventorNames.put(inventorID, name);

                                    if(put != null && !name.equals(put))
                                    {
                                        if(!isNameBetter(name, put))
                                            newInventorNames.put(inventorID, put);
                                    }
                                }
                            }
                        }
                        else
                        {
                            String assigneeID = getAssigneeID(subject.getURI(), true);
                            String name = getString(object);

                            synchronized(newAssigneeNames)
                            {
                                String pre = oldAssigneeNames.remove(assigneeID);

                                if(name.equals(pre))
                                {
                                    keepAssigneeNames.put(assigneeID, name);
                                }
                                else
                                {
                                    if(pre != null)
                                    {
                                        replacedAssigneeNames.put(assigneeID, pre);
                                    }
                                    else if(name.equals(replacedAssigneeNames.get(assigneeID)))
                                    {
                                        keepAssigneeNames.put(assigneeID, name);
                                        newAssigneeNames.remove(assigneeID);
                                        return;
                                    }

                                    String keep = keepAssigneeNames.get(assigneeID);

                                    if(name.equals(keep))
                                        return;

                                    if(keep != null)
                                    {
                                        if(!isNameBetter(name, keep))
                                            return;
                                    }

                                    String put = newAssigneeNames.put(assigneeID, name);

                                    if(put != null && !name.equals(put))
                                    {
                                        if(!isNameBetter(name, put))
                                            newAssigneeNames.put(assigneeID, put);
                                    }
                                }
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.inventors set name=null where id=? and name=?", oldInventorNames);
        store("insert into pubchem.inventors(id,name) values(?,?) "
                + "on conflict(id) do update set name=EXCLUDED.name", newInventorNames);

        store("update pubchem.applicants set name=null where id=? and name=?", oldAssigneeNames);
        store("insert into pubchem.applicants(id,name) values(?,?) "
                + "on conflict(id) do update set name=EXCLUDED.name", newAssigneeNames);
    }


    /**
     * Decides which of two conflicting names of an inventor or an assignee is kept and reports the conflict as a
     * warning: a name glued to {@code SAMSUNG SDI CO LTD} loses, otherwise the longer, then the not upper-case, and
     * then the lexicographically smaller name wins.
     *
     * @param a the first name
     * @param b the second name
     * @return true if the first name is better
     */
    private static boolean isNameBetter(String a, String b)
    {
        String ua = a.toUpperCase(ROOT);
        String ub = b.toUpperCase(ROOT);

        boolean result = false;

        if(a.matches(".*[^ ]SAMSUNG SDI CO LTD") && !b.matches(".*[^ ]SAMSUNG SDI CO LTD"))
            result = false;
        else if(!a.matches(".*[^ ]SAMSUNG SDI CO LTD") && b.matches(".*[^ ]SAMSUNG SDI CO LTD"))
            result = true;
        else if(a.length() > b.length())
            result = true;
        else if(a.length() < b.length())
            result = false;
        else if(!a.equals(ua) && b.equals(ub))
            result = true;
        else if(a.equals(ua) && !b.equals(ub))
            result = false;
        else if(a.compareTo(b) < 0)
            result = true;
        else
            result = false;

        Problems.warning("conflicting names of an inventor or an assignee",
                "\"" + (result ? a : b) + "\" chosen instead of \"" + (result ? b : a) + "\"");

        return result;
    }


    /**
     * Loads the patents from the PubChemRDF files and flushes their table.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load patents ...");

        loadBases();
        loadTitles();
        loadAbstracts();
        loadNumbers();
        loadFilingDates();
        loadGrantDates();
        loadPublicationDates();
        loadPriorityDates();
        loadCitations();
        loadCpcAdditionalClassifications();
        loadCpcInventiveClassifications();
        loadIpcAdditionalClassifications();
        loadIpcInventiveClassifications();
        loadInventors();
        loadApplicants();
        loadFormattedNames();

        patents.flush();

        System.out.println();
    }


    /**
     * Stores the patents, including those referenced by the parts loaded later, and the changes of the inventors and
     * assignees, and deletes the stored ones that are neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish patents ...");

        patents.store();

        store("delete from pubchem.inventors where id=?", oldInventors);
        store("insert into pubchem.inventors(id) values(?)", newInventors);

        store("delete from pubchem.applicants where id=?", oldAssignees);
        store("insert into pubchem.applicants(id) values(?)", newAssignees);

        System.out.println();
    }


    /**
     * Returns the id of the patent with the given IRI; a patent without a row gets one and is counted as missing.
     *
     * @param value the IRI of the patent
     * @return the id of the patent
     * @throws IOException if the IRI is not an IRI of a patent
     */
    static Integer getPatentID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        String patent = value.substring(prefixLength);

        synchronized(patentIDs)
        {
            Integer patentID = patentIDs.get(patent);

            if(patentID != null && patents.contains(patentID))
                return patentID;

            missingPatents.referenced(patent);

            return addPatent(patent);
        }
    }


    /**
     * Adds the row of a patent, which keeps its id if it has one.
     *
     * @param patent the IRI of the patent without the prefix
     * @return the id of the patent
     * @throws IOException if the row of the patent conflicts with the data
     */
    private static Integer addPatent(String patent) throws IOException
    {
        synchronized(patentIDs)
        {
            Integer patentID = patentIDs.get(patent);

            if(patentID == null)
                patentIDs.put(patent, patentID = nextPatentID++);

            patents.set(patentID, "iri", patent);

            return patentID;
        }
    }


    /**
     * Returns the id of the inventor with the given IRI; an inventor not described by the data is counted as missing.
     *
     * @param value the IRI of the inventor
     * @param forceKeep whether the row of the inventor is written together with its name, so that it must not be
     *            inserted as a new one
     * @return the id (the MD5 hash) of the inventor
     * @throws IOException if the IRI is not an IRI of an inventor
     */
    private static String getInventorID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(inventorPrefix))
            throw new DataException("unexpected IRI", value);

        String inventorID = value.substring(inventorPrefixLength);

        synchronized(newInventors)
        {
            if(newInventors.contains(inventorID))
            {
                if(forceKeep)
                {
                    newInventors.remove(inventorID);
                    keepInventors.add(inventorID);
                }
            }
            else if(!keepInventors.contains(inventorID))
            {
                missingInventors.referenced(inventorID);

                if(!oldInventors.remove(inventorID) && !forceKeep)
                    newInventors.add(inventorID);
                else
                    keepInventors.add(inventorID);
            }
        }

        return inventorID;
    }


    /**
     * Returns the id of the assignee with the given IRI; an assignee not described by the data is counted as missing.
     *
     * @param value the IRI of the assignee
     * @param forceKeep whether the row of the assignee is written together with its name, so that it must not be
     *            inserted as a new one
     * @return the id (the MD5 hash) of the assignee
     * @throws IOException if the IRI is not an IRI of an assignee
     */
    private static String getAssigneeID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(assigneePrefix))
            throw new DataException("unexpected IRI", value);

        String assigneeID = value.substring(assigneePrefixLength);

        synchronized(newAssignees)
        {
            if(newAssignees.contains(assigneeID))
            {
                if(forceKeep)
                {
                    newAssignees.remove(assigneeID);
                    keepAssignees.add(assigneeID);
                }
            }
            else if(!keepAssignees.contains(assigneeID))
            {
                missingAssignees.referenced(assigneeID);

                if(!oldAssignees.remove(assigneeID) && !forceKeep)
                    newAssignees.add(assigneeID);
                else
                    keepAssignees.add(assigneeID);
            }
        }

        return assigneeID;
    }
}
