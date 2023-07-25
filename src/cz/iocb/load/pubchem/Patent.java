package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Patent extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/";
    static final int prefixLength = prefix.length();

    private static final StringIntMap keepPatents = new StringIntMap();
    private static final StringIntMap newPatents = new StringIntMap();
    private static final StringIntMap oldPatents = new StringIntMap();
    private static int nextPatentID;


    private static void loadBases() throws IOException, SQLException
    {
        load("select iri,id from pubchem.patent_bases", oldPatents);

        nextPatentID = oldPatents.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

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

                        String patent = getStringID(subject, prefix);

                        synchronized(newPatents)
                        {
                            Integer patentID = keepPatents.get(patent);

                            if(patentID != null)
                                return;

                            patentID = newPatents.get(patent);

                            if(patentID != null)
                                return;

                            if((patentID = oldPatents.remove(patent)) == null)
                                newPatents.put(patent, nextPatentID++);
                            else
                                keepPatents.put(patent, patentID);
                        }
                    }
                }.load(stream);
            }
        });
    }


    private static void loadTitles() throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();
        IntStringPairMap newTitles = new IntStringPairMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select id,title from pubchem.patent_bases where title is not null", oldTitles);

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

                        Integer patentID = getPatentID(subject.getURI(), true);
                        String title = getString(object);

                        synchronized(newTitles)
                        {
                            if(title.equals(oldTitles.remove(patentID)))
                            {
                                keepTitles.put(patentID, title);
                            }
                            else
                            {
                                String keep = keepTitles.get(patentID);

                                Pair<String, String> pair = Pair.getPair(getStringID(subject, prefix), title);

                                if(title.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Pair<String, String> put = newTitles.put(patentID, pair);

                                if(put != null && !title.equals(put.getTwo()))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.patent_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.patent_bases(id,iri,title) values(?,?,?) "
                + "on conflict(id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadAbstracts() throws IOException, SQLException
    {
        IntStringMap keepAbstracts = new IntStringMap();
        IntStringPairMap newAbstracts = new IntStringPairMap();
        IntStringMap oldAbstracts = new IntStringMap();

        load("select id,abstract from pubchem.patent_bases where abstract is not null", oldAbstracts);

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

                        Integer patentID = getPatentID(subject.getURI(), true);
                        String value = getString(object);

                        synchronized(newAbstracts)
                        {
                            if(value.equals(oldAbstracts.remove(patentID)))
                            {
                                keepAbstracts.put(patentID, value);
                            }
                            else
                            {
                                String keep = keepAbstracts.get(patentID);

                                Pair<String, String> pair = Pair.getPair(getStringID(subject, prefix), value);

                                if(value.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Pair<String, String> put = newAbstracts.put(patentID, pair);

                                if(put != null && !value.equals(put.getTwo()))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.patent_bases set abstract=null where id=? and abstract=?", oldAbstracts);
        store("insert into pubchem.patent_bases(id,iri,abstract) values(?,?,?) "
                + "on conflict(id) do update set abstract=EXCLUDED.abstract", newAbstracts);
    }


    private static void loadNumbers() throws IOException, SQLException
    {
        IntStringMap keepNumbers = new IntStringMap();
        IntStringPairMap newNumbers = new IntStringPairMap();
        IntStringMap oldNumbers = new IntStringMap();

        load("select id,publication_number from pubchem.patent_bases where publication_number is not null", oldNumbers);

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

                        Integer patentID = getPatentID(subject.getURI(), true);
                        String number = getString(object);

                        synchronized(newNumbers)
                        {
                            if(number.equals(oldNumbers.remove(patentID)))
                            {
                                keepNumbers.put(patentID, number);
                            }
                            else
                            {
                                String keep = keepNumbers.get(patentID);

                                Pair<String, String> pair = Pair.getPair(getStringID(subject, prefix), number);

                                if(number.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Pair<String, String> put = newNumbers.put(patentID, pair);

                                if(put != null && !number.equals(put.getTwo()))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.patent_bases set publication_number=null where id=? and publication_number=?",
                oldNumbers);
        store("insert into pubchem.patent_bases(id,iri,publication_number) values(?,?,?) "
                + "on conflict(id) do update set publication_number=EXCLUDED.publication_number", newNumbers);
    }


    private static void loadFilingDates() throws IOException, SQLException
    {
        IntStringMap keepFilingDates = new IntStringMap();
        IntStringPairMap newFilingDates = new IntStringPairMap();
        IntStringMap oldFilingDates = new IntStringMap();

        load("select id,filing_date::varchar from pubchem.patent_bases where filing_date is not null", oldFilingDates);

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

                        Integer patentID = getPatentID(subject.getURI(), true);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newFilingDates)
                        {
                            if(date.equals(oldFilingDates.remove(patentID)))
                            {
                                keepFilingDates.put(patentID, date);
                            }
                            else
                            {
                                String keep = keepFilingDates.get(patentID);

                                Pair<String, String> pair = Pair.getPair(getStringID(subject, prefix), date);

                                if(date.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Pair<String, String> put = newFilingDates.put(patentID, pair);

                                if(put != null && !date.equals(put.getTwo()))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.patent_bases set filing_date=null where id=? and filing_date=?::date", oldFilingDates);
        store("insert into pubchem.patent_bases(id,iri,filing_date) values(?,?,?::date) "
                + "on conflict(id) do update set filing_date=EXCLUDED.filing_date", newFilingDates);
    }


    private static void loadGrantDates() throws IOException, SQLException
    {
        IntStringMap keepGrantDates = new IntStringMap();
        IntStringPairMap newGrantDates = new IntStringPairMap();
        IntStringMap oldGrantDates = new IntStringMap();

        load("select id,grant_date::varchar from pubchem.patent_bases where grant_date is not null", oldGrantDates);

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

                        Integer patentID = getPatentID(subject.getURI(), true);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newGrantDates)
                        {
                            if(date.equals(oldGrantDates.remove(patentID)))
                            {
                                keepGrantDates.put(patentID, date);
                            }
                            else
                            {
                                String keep = keepGrantDates.get(patentID);

                                Pair<String, String> pair = Pair.getPair(getStringID(subject, prefix), date);

                                if(date.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Pair<String, String> put = newGrantDates.put(patentID, pair);

                                if(put != null && !date.equals(put.getTwo()))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.patent_bases set grant_date=null where id=? and grant_date=?::date", oldGrantDates);
        store("insert into pubchem.patent_bases(id,iri,grant_date) values(?,?,?::date) "
                + "on conflict(id) do update set grant_date=EXCLUDED.grant_date", newGrantDates);
    }


    private static void loadPublicationDates() throws IOException, SQLException
    {
        IntStringMap keepPublicationDates = new IntStringMap();
        IntStringPairMap newPublicationDates = new IntStringPairMap();
        IntStringMap oldPublicationDates = new IntStringMap();

        load("select id,publication_date::varchar from pubchem.patent_bases where publication_date is not null",
                oldPublicationDates);

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

                        Integer patentID = getPatentID(subject.getURI(), true);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newPublicationDates)
                        {
                            if(date.equals(oldPublicationDates.remove(patentID)))
                            {
                                keepPublicationDates.put(patentID, date);
                            }
                            else
                            {
                                String keep = keepPublicationDates.get(patentID);

                                Pair<String, String> pair = Pair.getPair(getStringID(subject, prefix), date);

                                if(date.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Pair<String, String> put = newPublicationDates.put(patentID, pair);

                                if(put != null && !date.equals(put.getTwo()))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.patent_bases set publication_date=null where id=? and publication_date=?::date",
                oldPublicationDates);
        store("insert into pubchem.patent_bases(id,iri,publication_date) values(?,?,?::date) "
                + "on conflict(id) do update set publication_date=EXCLUDED.publication_date", newPublicationDates);
    }


    private static void loadPriorityDates() throws IOException, SQLException
    {
        IntStringMap keepPriorityDates = new IntStringMap();
        IntStringPairMap newPriorityDates = new IntStringPairMap();
        IntStringMap oldPriorityDates = new IntStringMap();

        load("select id,priority_date::varchar from pubchem.patent_bases where priority_date is not null",
                oldPriorityDates);

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

                        Integer patentID = getPatentID(subject.getURI(), true);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newPriorityDates)
                        {
                            if(date.equals(oldPriorityDates.remove(patentID)))
                            {
                                keepPriorityDates.put(patentID, date);
                            }
                            else
                            {
                                String keep = keepPriorityDates.get(patentID);

                                Pair<String, String> pair = Pair.getPair(getStringID(subject, prefix), date);

                                if(date.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Pair<String, String> put = newPriorityDates.put(patentID, pair);

                                if(put != null && !date.equals(put.getTwo()))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.patent_bases set priority_date=null where id=? and priority_date=?::date",
                oldPriorityDates);
        store("insert into pubchem.patent_bases(id,iri,priority_date) values(?,?,?::date) "
                + "on conflict(id) do update set priority_date=EXCLUDED.priority_date", newPriorityDates);
    }


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
                        if(!predicate.getURI().equals("http://purl.org/spar/cito/isCitedBy"))
                            throw new IOException();

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
                        if(!predicate.getURI()
                                .equals("http://data.epo.org/linked-data/def/patent/classificationCPCAdditional"))
                            throw new IOException();

                        Integer patentID = getPatentID(subject.getURI());
                        String classification = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patentcpc/");

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
                        if(!predicate.getURI()
                                .equals("http://data.epo.org/linked-data/def/patent/classificationCPCInventive"))
                            throw new IOException();

                        Integer patentID = getPatentID(subject.getURI());
                        String classification = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patentcpc/");

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
                        if(!predicate.getURI()
                                .equals("http://data.epo.org/linked-data/def/patent/classificationIPCAdditional"))
                            throw new IOException();

                        Integer patentID = getPatentID(subject.getURI());
                        String classification = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patentipc/");

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
                        if(!predicate.getURI()
                                .equals("http://data.epo.org/linked-data/def/patent/classificationIPCInventive"))
                            throw new IOException();

                        Integer patentID = getPatentID(subject.getURI());
                        String classification = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/patentipc/");

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


    private static void loadSubstances() throws IOException, SQLException
    {
        IntPairSet keepSubstances = new IntPairSet();
        IntPairSet newSubstances = new IntPairSet();
        IntPairSet oldSubstances = new IntPairSet();

        load("select patent,substance from pubchem.patent_substances", oldSubstances);

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

                        Integer patentID = getPatentID(object.getURI());
                        Integer substanceID = Substance.getSubstanceID(subject.getURI());

                        Pair<Integer, Integer> pair = Pair.getPair(patentID, substanceID);

                        synchronized(newSubstances)
                        {
                            if(oldSubstances.remove(pair))
                                keepSubstances.add(pair);
                            else if(!keepSubstances.contains(pair))
                                newSubstances.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.patent_substances where patent=? and substance=?", oldSubstances);
        store("insert into pubchem.patent_substances(patent,substance) values(?,?)", newSubstances);
    }


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
                        if(!predicate.getURI().equals("http://data.epo.org/linked-data/def/patent/inventorVC"))
                            throw new IOException();

                        Integer patentID = getPatentID(subject.getURI());
                        String inventor = getStringID(object,
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/patentinventor/MD5_");

                        Pair<Integer, String> pair = Pair.getPair(patentID, inventor);

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
                        if(!predicate.getURI().equals("http://data.epo.org/linked-data/def/patent/applicantVC"))
                            throw new IOException();

                        Integer patentID = getPatentID(subject.getURI());
                        String applicant = getStringID(object,
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/patentassignee/MD5_");

                        Pair<Integer, String> pair = Pair.getPair(patentID, applicant);

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


    private static void loadFormattedNames() throws IOException, SQLException
    {
        StringPairSet keepInventorNames = new StringPairSet();
        StringPairSet newInventorNames = new StringPairSet();
        StringPairSet oldInventorNames = new StringPairSet();

        StringPairSet keepApplicantNames = new StringPairSet();
        StringPairSet newApplicantNames = new StringPairSet();
        StringPairSet oldApplicantNames = new StringPairSet();

        load("select inventor,formatted_name from pubchem.patent_inventor_names", oldInventorNames);
        load("select applicant,formatted_name from pubchem.patent_applicant_names", oldApplicantNames);

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

                            Pair<String, String> pair = Pair.getPair(inventor, formattedName);

                            synchronized(newInventorNames)
                            {
                                if(oldInventorNames.remove(pair))
                                    keepInventorNames.add(pair);
                                else if(!keepInventorNames.contains(pair))
                                    newInventorNames.add(pair);
                            }
                        }
                        else
                        {
                            String applicant = getStringID(subject,
                                    "http://rdf.ncbi.nlm.nih.gov/pubchem/patentassignee/MD5_");
                            String formattedName = getString(object);

                            Pair<String, String> pair = Pair.getPair(applicant, formattedName);

                            synchronized(newApplicantNames)
                            {
                                if(oldApplicantNames.remove(pair))
                                    keepApplicantNames.add(pair);
                                else if(!keepApplicantNames.contains(pair))
                                    newApplicantNames.add(pair);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.patent_inventor_names where inventor=? and formatted_name=?", oldInventorNames);
        store("insert into pubchem.patent_inventor_names(inventor,formatted_name) values(?,?)", newInventorNames);

        store("delete from pubchem.patent_applicant_names where applicant=? and formatted_name=?", oldApplicantNames);
        store("insert into pubchem.patent_applicant_names(applicant,formatted_name) values(?,?)", newApplicantNames);
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
        loadCitations();
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

        store("delete from pubchem.patent_bases where iri=? and id=?", oldPatents);
        store("insert into pubchem.patent_bases(iri,id) values(?,?)", newPatents);

        System.out.println();
    }


    static Integer getPatentID(String value) throws IOException
    {
        return getPatentID(value, false);
    }


    static Integer getPatentID(String value, boolean keepForce) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        String patent = value.substring(prefixLength);

        synchronized(newPatents)
        {
            Integer patentID = keepPatents.get(patent);

            if(patentID != null)
                return patentID;

            patentID = newPatents.get(patent);

            if(patentID != null)
            {
                if(keepForce)
                {
                    newPatents.remove(patent);
                    keepPatents.put(patent, patentID);
                }

                return patentID;
            }

            System.out.println("    add missing patent " + patent);

            if((patentID = oldPatents.remove(patent)) != null)
                keepPatents.put(patent, patentID);
            else if(keepForce)
                keepPatents.put(patent, patentID = nextPatentID++);
            else
                newPatents.put(patent, patentID = nextPatentID++);

            return patentID;
        }
    }
}
