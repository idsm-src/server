package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.StringPair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Patent extends Updater
{
    private static StringIntMap usedPatents;
    private static StringIntMap newPatents;
    private static StringIntMap oldPatents;
    private static int nextPatentID;


    private static void loadBases() throws IOException, SQLException
    {
        usedPatents = new StringIntMap();
        newPatents = new StringIntMap();
        oldPatents = getStringIntMap("select iri, id from pubchem.patent_bases");
        nextPatentID = getIntValue("select coalesce(max(id)+1,0) from pubchem.patent_bases");

        processFiles("pubchem/RDF/patent", "pc_patent2type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            throw new IOException();

                        if(!object.getURI().equals("http://data.epo.org/linked-data/def/patent/Publication"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");

                        synchronized(newPatents)
                        {
                            int patentID = oldPatents.removeKeyIfAbsent(patent, NO_VALUE);

                            if(patentID == NO_VALUE)
                                newPatents.put(patent, patentID = nextPatentID++);

                            usedPatents.put(patent, patentID);
                        }
                    }
                }.load(stream);
            }
        });

        batch("insert into pubchem.patent_bases(iri, id) values (?,?)", newPatents);
        newPatents.clear();
    }


    private static void loadTitles() throws IOException, SQLException
    {
        IntStringPairMap newTitles = new IntStringPairMap();
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.patent_bases where title is not null");

        processFiles("pubchem/RDF/patent", "pc_patent2title_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://data.epo.org/linked-data/def/patent/titleOfInvention"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String value = getString(object);

                        synchronized(newTitles)
                        {
                            if(!value.equals(oldTitles.remove(patentID)))
                                newTitles.put(patentID, Tuples.pair(patent, value));
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.patent_bases set title = null where id = ?", oldTitles.keySet());
        batch("insert into pubchem.patent_bases(id, iri, title) values (?,?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadAbstracts() throws IOException, SQLException
    {
        IntStringPairMap newAbstracts = new IntStringPairMap();
        IntStringMap oldAbstracts = getIntStringMap(
                "select id, abstract from pubchem.patent_bases where abstract is not null");

        processFiles("pubchem/RDF/patent", "pc_patent2abstract_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/abstract"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String value = getString(object);

                        synchronized(newAbstracts)
                        {
                            if(!value.equals(oldAbstracts.remove(patentID)))
                                newAbstracts.put(patentID, Tuples.pair(patent, value));
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.patent_bases set abstract = null where id = ?", oldAbstracts.keySet());
        batch("insert into pubchem.patent_bases(id, iri, abstract) values (?,?,?) "
                + "on conflict (id) do update set abstract=EXCLUDED.abstract", newAbstracts);
    }


    private static void loadNumbers() throws IOException, SQLException
    {
        IntStringPairMap newNumbers = new IntStringPairMap();
        IntStringMap oldNumbers = getIntStringMap(
                "select id, publication_number from pubchem.patent_bases where publication_number is not null");

        processFiles("pubchem/RDF/patent", "pc_patent2publicationnumber_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://data.epo.org/linked-data/def/patent/publicationNumber"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String value = getString(object);

                        synchronized(newNumbers)
                        {
                            if(!value.equals(oldNumbers.remove(patentID)))
                                newNumbers.put(patentID, Tuples.pair(patent, value));
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.patent_bases set publication_number = null where id = ?", oldNumbers.keySet());
        batch("insert into pubchem.patent_bases(id, iri, publication_number) values (?,?,?) "
                + "on conflict (id) do update set publication_number=EXCLUDED.publication_number", newNumbers);
    }


    private static void loadFilingDates() throws IOException, SQLException
    {
        IntStringPairMap newFilingDates = new IntStringPairMap();
        IntStringMap oldFilingDates = getIntStringMap(
                "select id, filing_date::varchar from pubchem.patent_bases where filing_date is not null");

        processFiles("pubchem/RDF/patent", "pc_patent2filingdate_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://data.epo.org/linked-data/def/patent/filingDate"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newFilingDates)
                        {
                            if(!date.equals(oldFilingDates.remove(patentID)))
                                newFilingDates.put(patentID, Tuples.pair(patent, date));
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.patent_bases set filing_date = null where id = ?", oldFilingDates.keySet());
        batch("insert into pubchem.patent_bases(id, iri, filing_date) values (?,?,?::date) "
                + "on conflict (id) do update set filing_date=EXCLUDED.filing_date", newFilingDates);
    }


    private static void loadGrantDates() throws IOException, SQLException
    {
        IntStringPairMap newGrantDates = new IntStringPairMap();
        IntStringMap oldGrantDates = getIntStringMap(
                "select id, grant_date::varchar from pubchem.patent_bases where grant_date is not null");

        processFiles("pubchem/RDF/patent", "pc_patent2grantdate_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        // workaround
                        if(!predicate.getURI().equals("http://data.epo.org/linked-data/def/patent/filingDate"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newGrantDates)
                        {
                            if(!date.equals(oldGrantDates.remove(patentID)))
                                newGrantDates.put(patentID, Tuples.pair(patent, date));
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.patent_bases set grant_date = null where id = ?", oldGrantDates.keySet());
        batch("insert into pubchem.patent_bases(id, iri, grant_date) values (?,?,?::date) "
                + "on conflict (id) do update set grant_date=EXCLUDED.grant_date", newGrantDates);
    }


    private static void loadPublicationDates() throws IOException, SQLException
    {
        IntStringPairMap newPublicationDates = new IntStringPairMap();
        IntStringMap oldPublicationDates = getIntStringMap(
                "select id, publication_date::varchar from pubchem.patent_bases where publication_date is not null");

        processFiles("pubchem/RDF/patent", "pc_patent2publicationdate_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://data.epo.org/linked-data/def/patent/publicationDate"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newPublicationDates)
                        {
                            if(!date.equals(oldPublicationDates.remove(patentID)))
                                newPublicationDates.put(patentID, Tuples.pair(patent, date));
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.patent_bases set publication_date = null where id = ?", oldPublicationDates.keySet());
        batch("insert into pubchem.patent_bases(id, iri, publication_date) values (?,?,?::date) "
                + "on conflict (id) do update set publication_date=EXCLUDED.publication_date", newPublicationDates);
    }


    private static void loadPriorityDates() throws IOException, SQLException
    {
        IntStringPairMap newPriorityDates = new IntStringPairMap();
        IntStringMap oldPriorityDates = getIntStringMap(
                "select id, priority_date::varchar from pubchem.patent_bases where priority_date is not null");

        processFiles("pubchem/RDF/patent", "pc_patent2prioritydate_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#priorityDate"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newPriorityDates)
                        {
                            if(!date.equals(oldPriorityDates.remove(patentID)))
                                newPriorityDates.put(patentID, Tuples.pair(patent, date));
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.patent_bases set priority_date = null where id = ?", oldPriorityDates.keySet());
        batch("insert into pubchem.patent_bases(id, iri, priority_date) values (?,?,?::date) "
                + "on conflict (id) do update set priority_date=EXCLUDED.priority_date", newPriorityDates);
    }


    private static void loadnewCitations() throws IOException, SQLException
    {
        IntPairSet newCitations = new IntPairSet();
        IntPairSet oldCitations = getIntPairSet("select patent, citation from pubchem.patent_citations");

        processFiles("pubchem/RDF/patent", "pc_patent2iscitedby_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/spar/cito/isCitedBy"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        synchronized(newCitations)
                        {
                            String citation = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                            int citationID = usedPatents.getIfAbsent(citation, NO_VALUE);

                            if(citationID == NO_VALUE)
                            {
                                citationID = oldPatents.removeKeyIfAbsent(citation, NO_VALUE);

                                if(citationID == NO_VALUE)
                                    newPatents.put(citation, citationID = nextPatentID++);

                                usedPatents.put(citation, citationID);
                            }

                            IntIntPair pair = PrimitiveTuples.pair(patentID, citationID);

                            if(!oldCitations.remove(pair))
                                newCitations.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.patent_citations where patent = ? and citation = ?", oldCitations);
        batch("insert into pubchem.patent_citations(patent, citation) values (?,?)", newCitations);
    }


    private static void loadCpcAdditionalClassifications() throws IOException, SQLException
    {
        IntStringPairSet newClassifications = new IntStringPairSet();
        IntStringPairSet oldClassifications = getIntStringPairSet(
                "select patent, classification from pubchem.patent_cpc_additional_classifications");

        processFiles("pubchem/RDF/patent", "pc_patent2cpc_additional_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI()
                                .equals("http://data.epo.org/linked-data/def/patent/classificationCPCAdditional"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String classification = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patentcpc/");

                        IntObjectPair<String> pair = PrimitiveTuples.pair(patentID, classification);

                        synchronized(newClassifications)
                        {
                            if(!oldClassifications.remove(pair))
                                newClassifications.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.patent_cpc_additional_classifications where patent = ? and classification = ?",
                oldClassifications);
        batch("insert into pubchem.patent_cpc_additional_classifications(patent, classification) values (?,?)",
                newClassifications);
    }


    private static void loadCpcInventiveClassifications() throws IOException, SQLException
    {
        IntStringPairSet newClassifications = new IntStringPairSet();
        IntStringPairSet oldClassifications = getIntStringPairSet(
                "select patent, classification from pubchem.patent_cpc_inventive_classifications");

        processFiles("pubchem/RDF/patent", "pc_patent2cpc_inventive_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI()
                                .equals("http://data.epo.org/linked-data/def/patent/classificationCPCInventive"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String classification = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patentcpc/");

                        IntObjectPair<String> pair = PrimitiveTuples.pair(patentID, classification);

                        synchronized(newClassifications)
                        {
                            if(!oldClassifications.remove(pair))
                                newClassifications.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.patent_cpc_inventive_classifications where patent = ? and classification = ?",
                oldClassifications);
        batch("insert into pubchem.patent_cpc_inventive_classifications(patent, classification) values (?,?)",
                newClassifications);
    }


    private static void loadIpcAdditionalClassifications() throws IOException, SQLException
    {
        IntStringPairSet newClassifications = new IntStringPairSet();
        IntStringPairSet oldClassifications = getIntStringPairSet(
                "select patent, classification from pubchem.patent_ipc_additional_classifications");

        processFiles("pubchem/RDF/patent", "pc_patent2ipc_additional_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI()
                                .equals("http://data.epo.org/linked-data/def/patent/classificationIPCAdditional"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String classification = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patentipc/");

                        IntObjectPair<String> pair = PrimitiveTuples.pair(patentID, classification);

                        synchronized(newClassifications)
                        {
                            if(!oldClassifications.remove(pair))
                                newClassifications.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.patent_ipc_additional_classifications where patent = ? and classification = ?",
                oldClassifications);
        batch("insert into pubchem.patent_ipc_additional_classifications(patent, classification) values (?,?)",
                newClassifications);
    }


    private static void loadIpcInventiveClassifications() throws IOException, SQLException
    {
        IntStringPairSet newClassifications = new IntStringPairSet();
        IntStringPairSet oldClassifications = getIntStringPairSet(
                "select patent, classification from pubchem.patent_ipc_inventive_classifications");

        processFiles("pubchem/RDF/patent", "pc_patent2ipc_inventive_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI()
                                .equals("http://data.epo.org/linked-data/def/patent/classificationIPCInventive"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String classification = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patentipc/");

                        IntObjectPair<String> pair = PrimitiveTuples.pair(patentID, classification);

                        synchronized(newClassifications)
                        {
                            if(!oldClassifications.remove(pair))
                                newClassifications.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.patent_ipc_inventive_classifications where patent = ? and classification = ?",
                oldClassifications);
        batch("insert into pubchem.patent_ipc_inventive_classifications(patent, classification) values (?,?)",
                newClassifications);
    }


    private static void loadSubstances() throws IOException, SQLException
    {
        IntPairSet newSubstances = new IntPairSet();
        IntPairSet oldSubstances = getIntPairSet("select patent, substance from pubchem.patent_substances");

        processFiles("pubchem/RDF/patent", "pc_patent2isdiscussedby_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/spar/cito/isDiscussedBy"))
                            throw new IOException();

                        String patent = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        int substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                        Substance.addSubstanceID(substanceID);

                        IntIntPair pair = PrimitiveTuples.pair(patentID, substanceID);

                        synchronized(newSubstances)
                        {
                            if(!oldSubstances.remove(pair))
                                newSubstances.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.patent_substances where patent = ? and substance = ?", oldSubstances);
        batch("insert into pubchem.patent_substances(patent, substance) values (?,?)", newSubstances);
    }


    private static void loadInventors() throws IOException, SQLException
    {
        IntStringPairSet newInventors = new IntStringPairSet();
        IntStringPairSet oldInventors = getIntStringPairSet("select patent, inventor from pubchem.patent_inventors");

        processFiles("pubchem/RDF/patent", "pc_patent2inventorvc_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://data.epo.org/linked-data/def/patent/inventorVC"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String inventor = getStringID(object,
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/patentinventor/MD5_");

                        IntObjectPair<String> pair = PrimitiveTuples.pair(patentID, inventor);

                        synchronized(newInventors)
                        {
                            if(!oldInventors.remove(pair))
                                newInventors.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.patent_inventors where patent = ? and inventor = ?", oldInventors);
        batch("insert into pubchem.patent_inventors(patent, inventor) values (?,?)", newInventors);
    }


    private static void loadApplicants() throws IOException, SQLException
    {
        IntStringPairSet newApplicants = new IntStringPairSet();
        IntStringPairSet oldApplicants = getIntStringPairSet("select patent, applicant from pubchem.patent_applicants");

        processFiles("pubchem/RDF/patent", "pc_patent2assigneevc_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://data.epo.org/linked-data/def/patent/applicantVC"))
                            throw new IOException();

                        String patent = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
                        int patentID = getPatentID(patent);
                        String applicant = getStringID(object,
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/patentassignee/MD5_");

                        IntObjectPair<String> pair = PrimitiveTuples.pair(patentID, applicant);

                        synchronized(newApplicants)
                        {
                            if(!oldApplicants.remove(pair))
                                newApplicants.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.patent_applicants where patent = ? and applicant = ?", oldApplicants);
        batch("insert into pubchem.patent_applicants(patent, applicant) values (?,?)", newApplicants);
    }


    private static void loadFormattedNames() throws IOException, SQLException
    {
        StringPairSet newInventorNames = new StringPairSet();
        StringPairSet oldInventorNames = getStringPairSet(
                "select inventor, formatted_name from pubchem.patent_inventor_names");

        StringPairSet newApplicantNames = new StringPairSet();
        StringPairSet oldApplicantNames = getStringPairSet(
                "select applicant, formatted_name from pubchem.patent_applicant_names");

        processFiles("pubchem/RDF/patent", "pc_patent2vc_fn_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://www.w3.org/2006/vcard/ns#fn"))
                            throw new IOException();

                        if(subject.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/patentinventor"))
                        {
                            String inventor = getStringID(subject,
                                    "http://rdf.ncbi.nlm.nih.gov/pubchem/patentinventor/MD5_");
                            String formattedName = getString(object);

                            StringPair pair = new StringPair(inventor, formattedName);

                            synchronized(newInventorNames)
                            {
                                if(!oldInventorNames.remove(pair))
                                    newInventorNames.add(pair);
                            }

                        }
                        else
                        {
                            String applicant = getStringID(subject,
                                    "http://rdf.ncbi.nlm.nih.gov/pubchem/patentassignee/MD5_");
                            String formattedName = getString(object);

                            StringPair pair = new StringPair(applicant, formattedName);

                            synchronized(newApplicantNames)
                            {
                                if(!oldApplicantNames.remove(pair))
                                    newApplicantNames.add(pair);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.patent_inventor_names where inventor = ? and formatted_name = ?", oldInventorNames);
        batch("insert into pubchem.patent_inventor_names(inventor, formatted_name) values (?,?)", newInventorNames);

        batch("delete from pubchem.patent_applicant_names where applicant = ? and formatted_name = ?",
                oldApplicantNames);
        batch("insert into pubchem.patent_applicant_names(applicant, formatted_name) values (?,?)", newApplicantNames);
    }


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
        loadnewCitations();
        loadCpcAdditionalClassifications();
        loadCpcInventiveClassifications();
        loadIpcAdditionalClassifications();
        loadIpcInventiveClassifications();
        loadSubstances();
        loadInventors();
        loadApplicants();
        loadFormattedNames();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish patents ...");

        batch("delete from pubchem.patent_bases where id = ?", oldPatents.values());
        batch("insert into pubchem.patent_bases(iri, id) values (?,?)" + " on conflict do nothing", newPatents);

        usedPatents = null;
        newPatents = null;
        oldPatents = null;

        System.out.println();
    }


    static int getPatentID(String patent) throws IOException
    {
        synchronized(newPatents)
        {
            int patentID = usedPatents.getIfAbsent(patent, NO_VALUE);

            if(patentID == NO_VALUE)
            {
                System.out.println("    add missing patent " + patent);

                if((patentID = oldPatents.removeKeyIfAbsent(patent, NO_VALUE)) == NO_VALUE)
                    newPatents.put(patent, patentID = nextPatentID++);

                usedPatents.put(patent, patentID);
            }

            return patentID;
        }
    }
}
