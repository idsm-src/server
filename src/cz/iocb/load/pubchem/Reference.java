package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.jena.graph.Node;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Reference extends Updater
{
    private static IntHashSet usedReferences;
    private static IntHashSet newReferences;
    private static IntHashSet oldReferences;

    private static HashMap<String, String> sources = new HashMap<String, String>();


    static
    {
        sources.put("https://www.drugbank.ca/", "DRUGBANK");
        sources.put("https://datacite.org/", "DATACITE");
        sources.put("http://www.thieme-chemistry.com/", "THIEME_CHEMISTRY");
        sources.put("https://hmdb.ca/", "HMDB");
        sources.put("https://link.springer.com/", "SPRINGER");
        sources.put("https://scigraph.springernature.com/", "SPRINGERNATURE");
        sources.put("https://pubmed.ncbi.nlm.nih.gov/", "PUBMED");
        sources.put("https://www.crossref.org/", "CROSSREF");
        sources.put("https://www.nature.com/natcatal/", "NATURE_NATCATAL");
        sources.put("https://www.nature.com/nature-portfolio/", "NATURE_PORTFOLIO");
        sources.put("https://www.nature.com/natsynth/", "NATURE_NATSYNTH");
        sources.put("https://www.nature.com/nchembio/", "NATURE_NCHEMBIO");
        sources.put("https://www.nature.com/ncomms/", "NATURE_NCOMMS");
        sources.put("https://www.nature.com/nchem/", "NATURE_NCHEM");
    }


    private static void loadBases() throws IOException, SQLException
    {
        usedReferences = new IntHashSet();
        newReferences = new IntHashSet();
        oldReferences = getIntSet("select id from pubchem.reference_bases");

        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = getIntStringMap(
                "select id, title from pubchem.reference_bases where title is not null");

        processFiles("pubchem/RDF/reference", "pc_reference_title_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/title"))
                            throw new IOException();

                        int referenceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/");
                        String title = getString(object);

                        synchronized(newReferences)
                        {
                            if(!oldReferences.remove(referenceID))
                                newReferences.add(referenceID);

                            usedReferences.add(referenceID);
                        }

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
        batch("insert into pubchem.reference_bases(id, title) values (?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadPublications() throws IOException, SQLException
    {
        IntStringMap newPublications = new IntStringMap();
        IntStringMap oldPublications = getIntStringMap(
                "select id, publication from pubchem.reference_bases where publication is not null");

        processFiles("pubchem/RDF/reference", "pc_reference_publication_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/publicationName"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String publication = getString(object);

                        synchronized(newPublications)
                        {
                            if(!publication.equals(oldPublications.remove(referenceID)))
                                newPublications.put(referenceID, publication);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.reference_bases set publication = null where id = ?", oldPublications.keySet());
        batch("insert into pubchem.reference_bases(id, publication) values (?,?) "
                + "on conflict (id) do update set publication=EXCLUDED.publication", newPublications);
    }


    private static void loadCitations() throws IOException, SQLException
    {
        IntStringMap newCitations = new IntStringMap();
        IntStringMap oldCitations = getIntStringMap(
                "select id, citation from pubchem.reference_bases where citation is not null");

        processFiles("pubchem/RDF/reference", "pc_reference_citation_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/bibliographicCitation"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
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
        batch("insert into pubchem.reference_bases(id, citation) values (?,?) "
                + "on conflict (id) do update set citation=EXCLUDED.citation", newCitations);
    }


    private static void loadIssues() throws IOException, SQLException
    {
        IntStringMap newIssues = new IntStringMap();
        IntStringMap oldIssues = getIntStringMap(
                "select id, issue from pubchem.reference_bases where issue is not null");

        processFiles("pubchem/RDF/reference", "pc_reference_issue_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/issueIdentifier"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String issue = getString(object);

                        synchronized(newIssues)
                        {
                            if(!issue.equals(oldIssues.remove(referenceID)))
                                newIssues.put(referenceID, issue);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.reference_bases set issue = null where id = ?", oldIssues.keySet());
        batch("insert into pubchem.reference_bases(id, issue) values (?,?) "
                + "on conflict (id) do update set issue=EXCLUDED.issue", newIssues);
    }


    private static void loadStartingPages() throws IOException, SQLException
    {
        IntStringMap newStartingPages = new IntStringMap();
        IntStringMap oldStartingPages = getIntStringMap(
                "select id, starting_page from pubchem.reference_bases where starting_page is not null");

        processFiles("pubchem/RDF/reference", "pc_reference_startingpage_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/startingPage"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String page = getString(object);

                        synchronized(newStartingPages)
                        {
                            if(!page.equals(oldStartingPages.remove(referenceID)))
                                newStartingPages.put(referenceID, page);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.reference_bases set starting_page = null where id = ?", oldStartingPages.keySet());
        batch("insert into pubchem.reference_bases(id, starting_page) values (?,?) "
                + "on conflict (id) do update set starting_page=EXCLUDED.starting_page", newStartingPages);
    }


    private static void loadEndingPages() throws IOException, SQLException
    {
        IntStringMap newEndingPages = new IntStringMap();
        IntStringMap oldEndingPages = getIntStringMap(
                "select id, ending_page from pubchem.reference_bases where ending_page is not null");

        processFiles("pubchem/RDF/reference", "pc_reference_endingpage_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/endingPage"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String page = getString(object);

                        synchronized(newEndingPages)
                        {
                            if(!page.equals(oldEndingPages.remove(referenceID)))
                                newEndingPages.put(referenceID, page);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.reference_bases set ending_page = null where id = ?", oldEndingPages.keySet());
        batch("insert into pubchem.reference_bases(id, ending_page) values (?,?) "
                + "on conflict (id) do update set ending_page=EXCLUDED.ending_page", newEndingPages);
    }


    private static void loadPageRanges() throws IOException, SQLException
    {
        IntStringMap newPageRanges = new IntStringMap();
        IntStringMap oldPageRanges = getIntStringMap(
                "select id, page_range from pubchem.reference_bases where page_range is not null");

        processFiles("pubchem/RDF/reference", "pc_reference_pagerange_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/pageRange"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String range = getString(object);

                        synchronized(newPageRanges)
                        {
                            if(!range.equals(oldPageRanges.remove(referenceID)))
                                newPageRanges.put(referenceID, range);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.reference_bases set page_range = null where id = ?", oldPageRanges.keySet());
        batch("insert into pubchem.reference_bases(id, page_range) values (?,?) "
                + "on conflict (id) do update set page_range=EXCLUDED.page_range", newPageRanges);
    }


    private static void loadLangs() throws IOException, SQLException
    {
        IntStringMap newLangs = new IntStringMap();
        IntStringMap oldLangs = getIntStringMap("select id, lang from pubchem.reference_bases where lang is not null");

        processFiles("pubchem/RDF/reference", "pc_reference_lang_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/language"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String lang = getString(object);

                        synchronized(newLangs)
                        {
                            if(!lang.equals(oldLangs.remove(referenceID)))
                                newLangs.put(referenceID, lang);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.reference_bases set lang = null where id = ?", oldLangs.keySet());
        batch("insert into pubchem.reference_bases(id, lang) values (?,?) "
                + "on conflict (id) do update set lang=EXCLUDED.lang", newLangs);
    }


    private static void loadDates() throws IOException, SQLException
    {
        IntStringMap newDates = new IntStringMap();
        IntStringMap oldDates = getIntStringMap(
                "select id, dcdate::varchar from pubchem.reference_bases where dcdate is not null");

        processFiles("pubchem/RDF/reference", "pc_reference_date\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/date"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newDates)
                        {
                            if(!date.equals(oldDates.remove(referenceID)))
                                newDates.put(referenceID, date);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.reference_bases set dcdate = null where id = ?", oldDates.keySet());
        batch("insert into pubchem.reference_bases(id, dcdate) values (?,?::date) "
                + "on conflict (id) do update set dcdate=EXCLUDED.dcdate", newDates);
    }


    private static void loadChemicalDiseases() throws IOException, SQLException
    {
        IntStringPairSet newDiscusses = new IntStringPairSet();
        IntStringPairSet oldDiscusses = getIntStringPairSet(
                "select reference, statement from pubchem.reference_discusses");

        processFiles("pubchem/RDF/reference", "pc_reference2chemical_disease_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/spar/cito/discusses"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
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
        IntStringPairSet newSubjects = new IntStringPairSet();
        IntStringPairSet oldSubjects = getIntStringPairSet("select reference, subject from pubchem.reference_subjects");

        IntStringPairSet newAnzsrcSubjects = new IntStringPairSet();
        IntStringPairSet oldAnzsrcSubjects = getIntStringPairSet(
                "select reference, subject from pubchem.reference_anzsrc_subjects");


        processFiles("pubchem/RDF/reference", "pc_reference2meshheading_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/spar/fabio/hasSubjectTerm"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));

                        if(object.getURI().startsWith("http://id.nlm.nih.gov/mesh/"))
                        {
                            String subjectID = getStringID(object, "http://id.nlm.nih.gov/mesh/");
                            IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, subjectID);

                            synchronized(newSubjects)
                            {
                                if(!oldSubjects.remove(pair))
                                    newSubjects.add(pair);
                            }
                        }
                        else
                        {
                            String subjectID = getStringID(object,
                                    "http://purl.org/au-research/vocabulary/anzsrc-for/2008/");
                            IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, subjectID);

                            synchronized(newAnzsrcSubjects)
                            {
                                if(!oldAnzsrcSubjects.remove(pair))
                                    newAnzsrcSubjects.add(pair);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_subjects where reference = ? and subject = ?", oldSubjects);
        batch("insert into pubchem.reference_subjects(reference, subject) values (?,?)", newSubjects);

        batch("delete from pubchem.reference_anzsrc_subjects where reference = ? and subject = ?", oldAnzsrcSubjects);
        batch("insert into pubchem.reference_anzsrc_subjects(reference, subject) values (?,?)", newAnzsrcSubjects);
    }


    private static void loadPrimaryMeshheadings() throws IOException, SQLException
    {
        IntStringPairSet newSubjects = new IntStringPairSet();
        IntStringPairSet oldSubjects = getIntStringPairSet(
                "select reference, subject from pubchem.reference_primary_subjects");

        processFiles("pubchem/RDF/reference", "pc_reference2meshheading_primary_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/spar/fabio/hasPrimarySubjectTerm"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
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


    private static void loadContentTypes() throws IOException, SQLException
    {
        IntStringPairSet newContentTypes = new IntStringPairSet();
        IntStringPairSet oldContentTypes = getIntStringPairSet(
                "select reference, type from pubchem.reference_content_types");

        processFiles("pubchem/RDF/reference", "pc_reference_contenttype\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/contentType"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String type = getString(object);
                        IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, type);

                        synchronized(newContentTypes)
                        {
                            if(!oldContentTypes.remove(pair))
                                newContentTypes.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_content_types where reference = ? and type = ?", oldContentTypes);
        batch("insert into pubchem.reference_content_types(reference, type) values (?,?)", newContentTypes);
    }


    private static void loadIssnNumbers() throws IOException, SQLException
    {
        IntStringPairSet newIssnNumbers = new IntStringPairSet();
        IntStringPairSet oldIssnNumbers = getIntStringPairSet(
                "select reference, issn from pubchem.reference_issn_numbers");

        processFiles("pubchem/RDF/reference", "pc_reference_issn_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/issn"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String issn = getString(object);
                        IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, issn);

                        synchronized(newIssnNumbers)
                        {
                            if(!oldIssnNumbers.remove(pair))
                                newIssnNumbers.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_issn_numbers where reference = ? and issn = ?", oldIssnNumbers);
        batch("insert into pubchem.reference_issn_numbers(reference, issn) values (?,?)", newIssnNumbers);
    }


    private static void loadAuthor() throws IOException, SQLException
    {
        IntPairSet newAuthors = new IntPairSet();
        IntPairSet oldAuthors = getIntPairSet("select reference, author from pubchem.reference_authors");

        processFiles("pubchem/RDF/reference", "pc_reference_author_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/creator"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        int authorID = Author
                                .getAuthorID(getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/author/"));
                        IntIntPair pair = PrimitiveTuples.pair(referenceID, authorID);

                        synchronized(newAuthors)
                        {
                            if(!oldAuthors.remove(pair))
                                newAuthors.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_authors where reference = ? and author = ?", oldAuthors);
        batch("insert into pubchem.reference_authors(reference, author) values (?,?)", newAuthors);
    }


    private static void loadGrant() throws IOException, SQLException
    {
        IntPairSet newGrants = new IntPairSet();
        IntPairSet oldGrants = getIntPairSet("select reference, grantid from pubchem.reference_grants");

        processFiles("pubchem/RDF/reference", "pc_reference_grant_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/cerif/frapo/isSupportedBy"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        int grantID = Grant
                                .getGrantID(getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/"));
                        IntIntPair pair = PrimitiveTuples.pair(referenceID, grantID);

                        synchronized(newGrants)
                        {
                            if(!oldGrants.remove(pair))
                                newGrants.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_grants where reference = ? and grantid = ?", oldGrants);
        batch("insert into pubchem.reference_grants(reference, grantid) values (?,?)", newGrants);
    }


    private static void loadFundingAgency() throws IOException, SQLException
    {
        IntPairSet newOrganizations = new IntPairSet();
        IntPairSet oldOrganizations = getIntPairSet(
                "select reference, organization from pubchem.reference_organizations");

        processFiles("pubchem/RDF/reference", "pc_reference_fundingagency_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/cerif/frapo/hasFundingAgency"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        int organizationID = Organization.getOrganizationID(
                                getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/"));
                        IntIntPair pair = PrimitiveTuples.pair(referenceID, organizationID);

                        synchronized(newOrganizations)
                        {
                            if(!oldOrganizations.remove(pair))
                                newOrganizations.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_organizations where reference = ? and organization = ?", oldOrganizations);
        batch("insert into pubchem.reference_organizations(reference, organization) values (?,?)", newOrganizations);
    }


    private static void loadJournalsAndBooks() throws IOException, SQLException
    {
        IntPairSet newJournals = new IntPairSet();
        IntPairSet oldJournals = getIntPairSet("select reference, journal from pubchem.reference_journals");

        IntPairSet newBooks = new IntPairSet();
        IntPairSet oldBooks = getIntPairSet("select reference, book from pubchem.reference_books");

        IntStringPairSet newIsbnBooks = new IntStringPairSet();
        IntStringPairSet oldIsbnBooks = getIntStringPairSet("select reference, isbn from pubchem.reference_isbn_books");

        IntStringPairSet newIssnJournals = new IntStringPairSet();
        IntStringPairSet oldIssnJournals = getIntStringPairSet(
                "select reference, issn from pubchem.reference_issn_journals");


        processFiles("pubchem/RDF/reference", "pc_reference_journal_book_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/isPartOf"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));


                        if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK"))
                        {
                            int bookID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK");
                            IntIntPair pair = PrimitiveTuples.pair(referenceID, bookID);
                            Book.addBookID(bookID);

                            synchronized(newIsbnBooks)
                            {
                                if(!oldBooks.remove(pair))
                                    newBooks.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/journal/"))
                        {
                            int journalID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/");
                            IntIntPair pair = PrimitiveTuples.pair(referenceID, journalID);
                            Journal.addJournalID(journalID);

                            synchronized(newIssnJournals)
                            {
                                if(!oldJournals.remove(pair))
                                    newJournals.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("https://isbnsearch.org/isbn"))
                        {
                            String isbn = getStringID(object, "https://isbnsearch.org/isbn");
                            IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, isbn);

                            synchronized(newIsbnBooks)
                            {
                                if(!oldIsbnBooks.remove(pair))
                                    newIsbnBooks.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("https://portal.issn.org/resource/ISSN"))
                        {
                            String issn = getStringID(object, "https://portal.issn.org/resource/ISSN");
                            IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, issn);

                            synchronized(newIssnJournals)
                            {
                                if(!oldIssnJournals.remove(pair))
                                    newIssnJournals.add(pair);
                            }
                        }
                        else
                        {
                            throw new IOException();
                        }
                    }
                }.load(stream);
            }
        });


        batch("delete from pubchem.reference_journals where reference = ? and journal = ?", oldJournals);
        batch("insert into pubchem.reference_journals(reference, journal) values (?,?)", newJournals);

        batch("delete from pubchem.reference_books where reference = ? and book = ?", oldBooks);
        batch("insert into pubchem.reference_books(reference, book) values (?,?)", newBooks);

        batch("delete from pubchem.reference_isbn_books where reference = ? and isbn = ?", oldIsbnBooks);
        batch("insert into pubchem.reference_isbn_books(reference, isbn) values (?,?)", newIsbnBooks);

        batch("delete from pubchem.reference_issn_journals where reference = ? and issn = ?", oldIssnJournals);
        batch("insert into pubchem.reference_issn_journals(reference, issn) values (?,?)", newIssnJournals);
    }


    private static void loadTextMinigs() throws IOException, SQLException
    {
        IntPairSet newCompounds = new IntPairSet();
        IntPairSet oldCompounds = getIntPairSet("select reference, compound from pubchem.reference_mined_compounds");

        IntPairSet newDiseases = new IntPairSet();
        IntPairSet oldDiseases = getIntPairSet("select reference, disease from pubchem.reference_mined_diseases");

        IntPairSet newGenes = new IntPairSet();
        IntPairSet oldGenes = getIntPairSet("select reference, gene_symbol from pubchem.reference_mined_genes");

        IntPairSet newEnzymes = new IntPairSet();
        IntPairSet oldEnzymes = getIntPairSet("select reference, enzyme from pubchem.reference_mined_enzymes");


        processFiles("pubchem/RDF/reference", "pc_reference_discusses_by_textming_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals(
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#discussesAsDerivedByTextMining"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));


                        if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"))
                        {
                            int compoundID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                            IntIntPair pair = PrimitiveTuples.pair(referenceID, compoundID);
                            Compound.addCompoundID(compoundID);

                            synchronized(newCompounds)
                            {
                                if(!oldCompounds.remove(pair))
                                    newCompounds.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"))
                        {
                            int diseaseID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");

                            IntIntPair pair = PrimitiveTuples.pair(referenceID, diseaseID);
                            Disease.getDiseaseID(diseaseID);

                            synchronized(newDiseases)
                            {
                                if(!oldDiseases.remove(pair))
                                    newDiseases.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"))
                        {
                            int geneSymbolID = Gene
                                    .getGeneSymbolID(getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"));
                            IntIntPair pair = PrimitiveTuples.pair(referenceID, geneSymbolID);

                            synchronized(newGenes)
                            {
                                if(!oldGenes.remove(pair))
                                    newGenes.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"))
                        {
                            String enzymeName = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_");
                            int enzymeID = Protein.getEnzymeID(enzymeName);
                            IntIntPair pair = PrimitiveTuples.pair(referenceID, enzymeID);

                            synchronized(newEnzymes)
                            {
                                if(!oldEnzymes.remove(pair))
                                    newEnzymes.add(pair);
                            }
                        }
                        else
                        {
                            throw new IOException();
                        }
                    }
                }.load(stream);
            }
        });


        batch("delete from pubchem.reference_mined_compounds where reference = ? and compound = ?", oldCompounds);
        batch("insert into pubchem.reference_mined_compounds(reference, compound) values (?,?)", newCompounds);

        batch("delete from pubchem.reference_mined_diseases where reference = ? and disease = ?", oldDiseases);
        batch("insert into pubchem.reference_mined_diseases(reference, disease) values (?,?)", newDiseases);

        batch("delete from pubchem.reference_mined_genes where reference = ? and gene_symbol = ?", oldGenes);
        batch("insert into pubchem.reference_mined_genes(reference, gene_symbol) values (?,?)", newGenes);

        batch("delete from pubchem.reference_mined_enzymes where reference = ? and enzyme = ?", oldEnzymes);
        batch("insert into pubchem.reference_mined_enzymes(reference, enzyme) values (?,?)", newEnzymes);
    }


    private static void loadIdentifiers() throws IOException, SQLException
    {
        IntStringPairSet newDoiIdentifiers = new IntStringPairSet();
        IntStringPairSet oldDoiIdentifiers = getIntStringPairSet(
                "select reference, doi from pubchem.reference_doi_identifiers");

        IntStringPairSet newPubMedIdentifiers = new IntStringPairSet();
        IntStringPairSet oldPubMedIdentifiers = getIntStringPairSet(
                "select reference, pubmed from pubchem.reference_pubmed_identifiers");


        processFiles("pubchem/RDF/reference", "pc_reference_identifier_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/identifier"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));


                        if(object.getURI().startsWith("https://doi.org/"))
                        {
                            String doi = getStringID(object, "https://doi.org/");
                            IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, doi);

                            synchronized(newDoiIdentifiers)
                            {
                                if(!oldDoiIdentifiers.remove(pair))
                                    newDoiIdentifiers.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("https://pubmed.ncbi.nlm.nih.gov/"))
                        {
                            String pubmed = getStringID(object, "https://pubmed.ncbi.nlm.nih.gov/");
                            IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, pubmed);

                            synchronized(newPubMedIdentifiers)
                            {
                                if(!oldPubMedIdentifiers.remove(pair))
                                    newPubMedIdentifiers.add(pair);
                            }
                        }
                        else
                        {
                            throw new IOException();
                        }
                    }
                }.load(stream);
            }
        });


        batch("delete from pubchem.reference_doi_identifiers where reference = ? and doi = ?", oldDoiIdentifiers);
        batch("insert into pubchem.reference_doi_identifiers(reference, doi) values (?,?)"
                + /* workaround */ " on conflict do nothing", newDoiIdentifiers);

        batch("delete from pubchem.reference_pubmed_identifiers where reference = ? and pubmed = ?",
                oldPubMedIdentifiers);
        batch("insert into pubchem.reference_pubmed_identifiers(reference, pubmed) values (?,?)", newPubMedIdentifiers);
    }


    private static void loadSources() throws IOException, SQLException
    {
        IntStringPairSet newSources = new IntStringPairSet();
        IntStringPairSet oldSources = getIntStringPairSet(
                "select reference, source_type::varchar from pubchem.reference_sources");

        processFiles("pubchem/RDF/reference", "pc_reference_source_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/source"))
                            throw new IOException();

                        int referenceID = Reference
                                .getReferenceID(getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
                        String issn = sources.get(object.getURI());
                        IntObjectPair<String> pair = PrimitiveTuples.pair(referenceID, issn);

                        synchronized(newSources)
                        {
                            if(!oldSources.remove(pair))
                                newSources.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.reference_sources where reference = ? and source_type = ?::pubchem.reference_source_type",
                oldSources);
        batch("insert into pubchem.reference_sources(reference, source_type) values (?,?::pubchem.reference_source_type)",
                newSources);
    }


    static void preload() throws IOException, SQLException
    {
        System.out.println("load references (bases) ...");

        loadBases();

        System.out.println();
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load references ...");

        //loadBases();
        loadPublications();
        loadCitations();
        loadIssues();
        loadStartingPages();
        loadEndingPages();
        loadPageRanges();
        loadLangs();
        loadDates();

        loadChemicalDiseases();
        loadMeshheadings();
        loadPrimaryMeshheadings();
        loadContentTypes();
        loadIssnNumbers();
        loadAuthor();
        loadGrant();
        loadFundingAgency();
        loadTextMinigs();
        loadJournalsAndBooks();
        loadIdentifiers();
        loadSources();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish references ...");

        batch("delete from pubchem.reference_bases where id = ?", oldReferences);
        batch("insert into pubchem.reference_bases(id) values (?)" + " on conflict do nothing", newReferences);

        usedReferences = null;
        newReferences = null;
        oldReferences = null;

        System.out.println();
    }


    static int getReferenceID(int referenceID) throws IOException
    {
        synchronized(newReferences)
        {
            if(!usedReferences.contains(referenceID))
            {
                System.out.println("    add missing reference " + referenceID);

                if(!oldReferences.remove(referenceID))
                    newReferences.add(referenceID);

                usedReferences.add(referenceID);
            }
        }

        return referenceID;
    }
}
