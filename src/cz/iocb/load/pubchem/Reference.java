package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Reference extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/";
    static final int prefixLength = prefix.length();

    private static final IntSet keepReferences = new IntSet();
    private static final IntSet newReferences = new IntSet();
    private static final IntSet oldReferences = new IntSet();

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
        IntStringMap keepTitles = new IntStringMap();
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select id from pubchem.reference_bases", oldReferences);
        load("select id,title from pubchem.reference_bases where title is not null", oldTitles);

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

                        Integer referenceID = getIntID(subject, prefix);
                        String title = getString(object);

                        synchronized(newReferences)
                        {
                            oldReferences.remove(referenceID);
                            keepReferences.add(referenceID);
                        }

                        synchronized(newTitles)
                        {
                            if(title.equals(oldTitles.remove(referenceID)))
                            {
                                keepTitles.put(referenceID, title);
                            }
                            else
                            {
                                String keep = keepTitles.get(referenceID);

                                if(title.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newTitles.put(referenceID, title);

                                if(put != null && !title.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.reference_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.reference_bases(id,title) values(?,?) "
                + "on conflict(id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadPublications() throws IOException, SQLException
    {
        IntStringMap keepPublications = new IntStringMap();
        IntStringMap newPublications = new IntStringMap();
        IntStringMap oldPublications = new IntStringMap();

        load("select id,publication from pubchem.reference_bases where publication is not null", oldPublications);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI(), true);
                        String publication = getString(object);

                        synchronized(newPublications)
                        {
                            if(publication.equals(oldPublications.remove(referenceID)))
                            {
                                keepPublications.put(referenceID, publication);
                            }
                            else
                            {
                                String keep = keepPublications.get(referenceID);

                                if(publication.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newPublications.put(referenceID, publication);

                                if(put != null && !publication.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.reference_bases set publication=null where id=? and publication=?", oldPublications);
        store("insert into pubchem.reference_bases(id,publication) values(?,?) "
                + "on conflict(id) do update set publication=EXCLUDED.publication", newPublications);
    }


    private static void loadCitations() throws IOException, SQLException
    {
        IntStringMap keepCitations = new IntStringMap();
        IntStringMap newCitations = new IntStringMap();
        IntStringMap oldCitations = new IntStringMap();

        load("select id,citation from pubchem.reference_bases where citation is not null", oldCitations);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI(), true);
                        String citation = getString(object);

                        synchronized(newCitations)
                        {
                            if(citation.equals(oldCitations.remove(referenceID)))
                            {
                                keepCitations.put(referenceID, citation);
                            }
                            else
                            {
                                String keep = keepCitations.get(referenceID);

                                if(citation.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newCitations.put(referenceID, citation);

                                if(put != null && !citation.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.reference_bases set citation=null where id=? and citation=?", oldCitations);
        store("insert into pubchem.reference_bases(id,citation) values(?,?) "
                + "on conflict(id) do update set citation=EXCLUDED.citation", newCitations);
    }


    private static void loadIssues() throws IOException, SQLException
    {
        IntStringMap keepIssues = new IntStringMap();
        IntStringMap newIssues = new IntStringMap();
        IntStringMap oldIssues = new IntStringMap();

        load("select id,issue from pubchem.reference_bases where issue is not null", oldIssues);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI(), true);
                        String issue = getString(object);

                        synchronized(newIssues)
                        {
                            if(issue.equals(oldIssues.remove(referenceID)))
                            {
                                keepIssues.put(referenceID, issue);
                            }
                            else
                            {
                                String keep = keepIssues.get(referenceID);

                                if(issue.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newIssues.put(referenceID, issue);

                                if(put != null && !issue.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.reference_bases set issue=null where id=? and issue=?", oldIssues);
        store("insert into pubchem.reference_bases(id,issue) values(?,?) "
                + "on conflict(id) do update set issue=EXCLUDED.issue", newIssues);
    }


    private static void loadStartingPages() throws IOException, SQLException
    {
        IntStringMap keepStartingPages = new IntStringMap();
        IntStringMap newStartingPages = new IntStringMap();
        IntStringMap oldStartingPages = new IntStringMap();

        load("select id,starting_page from pubchem.reference_bases where starting_page is not null", oldStartingPages);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI(), true);
                        String page = getString(object);

                        synchronized(newStartingPages)
                        {
                            if(page.equals(oldStartingPages.remove(referenceID)))
                            {
                                keepStartingPages.put(referenceID, page);
                            }
                            else
                            {
                                String keep = keepStartingPages.get(referenceID);

                                if(page.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newStartingPages.put(referenceID, page);

                                if(put != null && !page.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.reference_bases set starting_page=null where id=? and starting_page=?", oldStartingPages);
        store("insert into pubchem.reference_bases(id,starting_page) values(?,?) "
                + "on conflict(id) do update set starting_page=EXCLUDED.starting_page", newStartingPages);
    }


    private static void loadEndingPages() throws IOException, SQLException
    {
        IntStringMap keepEndingPages = new IntStringMap();
        IntStringMap newEndingPages = new IntStringMap();
        IntStringMap oldEndingPages = new IntStringMap();

        load("select id,ending_page from pubchem.reference_bases where ending_page is not null", oldEndingPages);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI(), true);
                        String page = getString(object);

                        synchronized(newEndingPages)
                        {
                            if(page.equals(oldEndingPages.remove(referenceID)))
                            {
                                keepEndingPages.put(referenceID, page);
                            }
                            else
                            {
                                String keep = keepEndingPages.get(referenceID);

                                if(page.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newEndingPages.put(referenceID, page);

                                if(put != null && !page.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.reference_bases set ending_page=null where id=? and ending_page=?", oldEndingPages);
        store("insert into pubchem.reference_bases(id,ending_page) values(?,?) "
                + "on conflict(id) do update set ending_page=EXCLUDED.ending_page", newEndingPages);
    }


    private static void loadPageRanges() throws IOException, SQLException
    {
        IntStringMap keepPageRanges = new IntStringMap();
        IntStringMap newPageRanges = new IntStringMap();
        IntStringMap oldPageRanges = new IntStringMap();

        load("select id,page_range from pubchem.reference_bases where page_range is not null", oldPageRanges);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI(), true);
                        String range = getString(object);

                        synchronized(newPageRanges)
                        {
                            if(range.equals(oldPageRanges.remove(referenceID)))
                            {
                                keepPageRanges.put(referenceID, range);
                            }
                            else
                            {
                                String keep = keepPageRanges.get(referenceID);

                                if(range.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newPageRanges.put(referenceID, range);

                                if(put != null && !range.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.reference_bases set page_range=null where id=? and page_range=?", oldPageRanges);
        store("insert into pubchem.reference_bases(id,page_range) values(?,?) "
                + "on conflict(id) do update set page_range=EXCLUDED.page_range", newPageRanges);
    }


    private static void loadLangs() throws IOException, SQLException
    {
        IntStringMap keepLangs = new IntStringMap();
        IntStringMap newLangs = new IntStringMap();
        IntStringMap oldLangs = new IntStringMap();

        load("select id,lang from pubchem.reference_bases where lang is not null", oldLangs);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI(), true);
                        String lang = getString(object);

                        synchronized(newLangs)
                        {
                            if(lang.equals(oldLangs.remove(referenceID)))
                            {
                                keepLangs.put(referenceID, lang);
                            }
                            else
                            {
                                String keep = keepLangs.get(referenceID);

                                if(lang.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newLangs.put(referenceID, lang);

                                if(put != null && !lang.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.reference_bases set lang=null where id=? and lang=?", oldLangs);
        store("insert into pubchem.reference_bases(id,lang) values(?,?) "
                + "on conflict(id) do update set lang=EXCLUDED.lang", newLangs);
    }


    private static void loadDates() throws IOException, SQLException
    {
        IntStringMap keepDates = new IntStringMap();
        IntStringMap newDates = new IntStringMap();
        IntStringMap oldDates = new IntStringMap();

        load("select id,dcdate::varchar from pubchem.reference_bases where dcdate is not null", oldDates);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI(), true);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newDates)
                        {
                            if(date.equals(oldDates.remove(referenceID)))
                            {
                                keepDates.put(referenceID, date);
                            }
                            else
                            {
                                String keep = keepDates.get(referenceID);

                                if(date.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newDates.put(referenceID, date);

                                if(put != null && !date.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.reference_bases set dcdate=null where id=? and dcdate=?::date", oldDates);
        store("insert into pubchem.reference_bases(id,dcdate) values(?,?::date) "
                + "on conflict(id) do update set dcdate=EXCLUDED.dcdate", newDates);
    }


    private static void loadChemicalDiseases() throws IOException, SQLException
    {
        IntStringSet keepDiscusses = new IntStringSet();
        IntStringSet newDiscusses = new IntStringSet();
        IntStringSet oldDiscusses = new IntStringSet();

        load("select reference,statement from pubchem.reference_discusses", oldDiscusses);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());
                        String statementID = getStringID(object, "http://id.nlm.nih.gov/mesh/");

                        Pair<Integer, String> pair = Pair.getPair(referenceID, statementID);

                        synchronized(newDiscusses)
                        {
                            if(oldDiscusses.remove(pair))
                                keepDiscusses.add(pair);
                            else if(!keepDiscusses.contains(pair))
                                newDiscusses.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_discusses where reference=? and statement=?", oldDiscusses);
        store("insert into pubchem.reference_discusses(reference,statement) values(?,?)", newDiscusses);
    }


    private static void loadMeshheadings() throws IOException, SQLException
    {
        IntStringSet keepSubjects = new IntStringSet();
        IntStringSet newSubjects = new IntStringSet();
        IntStringSet oldSubjects = new IntStringSet();

        IntStringSet keepAnzsrcSubjects = new IntStringSet();
        IntStringSet newAnzsrcSubjects = new IntStringSet();
        IntStringSet oldAnzsrcSubjects = new IntStringSet();

        load("select reference,subject from pubchem.reference_subjects", oldSubjects);
        load("select reference,subject from pubchem.reference_anzsrc_subjects", oldAnzsrcSubjects);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());

                        if(object.getURI().startsWith("http://id.nlm.nih.gov/mesh/"))
                        {
                            String subjectID = getStringID(object, "http://id.nlm.nih.gov/mesh/");

                            Pair<Integer, String> pair = Pair.getPair(referenceID, subjectID);

                            synchronized(newSubjects)
                            {
                                if(oldSubjects.remove(pair))
                                    keepSubjects.add(pair);
                                else if(!keepSubjects.contains(pair))
                                    newSubjects.add(pair);
                            }
                        }
                        else
                        {
                            String subjectID = getStringID(object,
                                    "http://purl.org/au-research/vocabulary/anzsrc-for/2008/");

                            Pair<Integer, String> pair = Pair.getPair(referenceID, subjectID);

                            synchronized(newAnzsrcSubjects)
                            {
                                if(oldAnzsrcSubjects.remove(pair))
                                    keepAnzsrcSubjects.add(pair);
                                else if(!keepAnzsrcSubjects.contains(pair))
                                    newAnzsrcSubjects.add(pair);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_subjects where reference=? and subject=?", oldSubjects);
        store("insert into pubchem.reference_subjects(reference,subject) values(?,?)", newSubjects);

        store("delete from pubchem.reference_anzsrc_subjects where reference=? and subject=?", oldAnzsrcSubjects);
        store("insert into pubchem.reference_anzsrc_subjects(reference,subject) values(?,?)", newAnzsrcSubjects);
    }


    private static void loadPrimaryMeshheadings() throws IOException, SQLException
    {
        IntStringSet keepSubjects = new IntStringSet();
        IntStringSet newSubjects = new IntStringSet();
        IntStringSet oldSubjects = new IntStringSet();

        load("select reference,subject from pubchem.reference_primary_subjects", oldSubjects);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());
                        String subjectID = getStringID(object, "http://id.nlm.nih.gov/mesh/");

                        Pair<Integer, String> pair = Pair.getPair(referenceID, subjectID);

                        synchronized(newSubjects)
                        {
                            if(oldSubjects.remove(pair))
                                keepSubjects.add(pair);
                            else if(!keepSubjects.contains(pair))
                                newSubjects.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_primary_subjects where reference=? and subject=?", oldSubjects);
        store("insert into pubchem.reference_primary_subjects(reference,subject) values(?,?)", newSubjects);
    }


    private static void loadContentTypes() throws IOException, SQLException
    {
        IntStringSet keepContentTypes = new IntStringSet();
        IntStringSet newContentTypes = new IntStringSet();
        IntStringSet oldContentTypes = new IntStringSet();

        load("select reference,type from pubchem.reference_content_types", oldContentTypes);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());
                        String type = getString(object);

                        Pair<Integer, String> pair = Pair.getPair(referenceID, type);

                        synchronized(newContentTypes)
                        {
                            if(oldContentTypes.remove(pair))
                                keepContentTypes.add(pair);
                            else if(!keepContentTypes.contains(pair))
                                newContentTypes.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_content_types where reference=? and type=?", oldContentTypes);
        store("insert into pubchem.reference_content_types(reference,type) values(?,?)", newContentTypes);
    }


    private static void loadIssnNumbers() throws IOException, SQLException
    {
        IntStringSet keepIssnNumbers = new IntStringSet();
        IntStringSet newIssnNumbers = new IntStringSet();
        IntStringSet oldIssnNumbers = new IntStringSet();

        load("select reference,issn from pubchem.reference_issn_numbers", oldIssnNumbers);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());
                        String issn = getString(object);

                        Pair<Integer, String> pair = Pair.getPair(referenceID, issn);

                        synchronized(newIssnNumbers)
                        {
                            if(oldIssnNumbers.remove(pair))
                                keepIssnNumbers.add(pair);
                            else if(!keepIssnNumbers.contains(pair))
                                newIssnNumbers.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_issn_numbers where reference=? and issn=?", oldIssnNumbers);
        store("insert into pubchem.reference_issn_numbers(reference,issn) values(?,?)", newIssnNumbers);
    }


    private static void loadAuthor() throws IOException, SQLException
    {
        IntPairSet keepAuthors = new IntPairSet();
        IntPairSet newAuthors = new IntPairSet();
        IntPairSet oldAuthors = new IntPairSet();

        load("select reference,author from pubchem.reference_authors", oldAuthors);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());
                        Integer authorID = Author.getAuthorID(object.getURI());

                        Pair<Integer, Integer> pair = Pair.getPair(referenceID, authorID);

                        synchronized(newAuthors)
                        {
                            if(oldAuthors.remove(pair))
                                keepAuthors.add(pair);
                            else if(!keepAuthors.contains(pair))
                                newAuthors.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_authors where reference=? and author=?", oldAuthors);
        store("insert into pubchem.reference_authors(reference,author) values(?,?)", newAuthors);
    }


    private static void loadGrant() throws IOException, SQLException
    {
        IntPairSet keepGrants = new IntPairSet();
        IntPairSet newGrants = new IntPairSet();
        IntPairSet oldGrants = new IntPairSet();

        load("select reference,grantid from pubchem.reference_grants", oldGrants);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());
                        Integer grantID = Grant.getGrantID(object.getURI());

                        Pair<Integer, Integer> pair = Pair.getPair(referenceID, grantID);

                        synchronized(newGrants)
                        {
                            if(oldGrants.remove(pair))
                                keepGrants.add(pair);
                            else if(!keepGrants.contains(pair))
                                newGrants.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_grants where reference=? and grantid=?", oldGrants);
        store("insert into pubchem.reference_grants(reference,grantid) values(?,?)", newGrants);
    }


    private static void loadFundingAgency() throws IOException, SQLException
    {
        IntPairSet keepOrganizations = new IntPairSet();
        IntPairSet newOrganizations = new IntPairSet();
        IntPairSet oldOrganizations = new IntPairSet();

        load("select reference,organization from pubchem.reference_organizations", oldOrganizations);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());
                        Integer organizationID = Organization.getOrganizationID(object.getURI());

                        Pair<Integer, Integer> pair = Pair.getPair(referenceID, organizationID);

                        synchronized(newOrganizations)
                        {
                            if(oldOrganizations.remove(pair))
                                keepOrganizations.add(pair);
                            else if(!keepOrganizations.contains(pair))
                                newOrganizations.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_organizations where reference=? and organization=?", oldOrganizations);
        store("insert into pubchem.reference_organizations(reference,organization) values(?,?)", newOrganizations);
    }


    private static void loadJournalsAndBooks() throws IOException, SQLException
    {
        IntPairSet keepJournals = new IntPairSet();
        IntPairSet newJournals = new IntPairSet();
        IntPairSet oldJournals = new IntPairSet();

        IntPairSet keepBooks = new IntPairSet();
        IntPairSet newBooks = new IntPairSet();
        IntPairSet oldBooks = new IntPairSet();

        IntStringSet keepIsbnBooks = new IntStringSet();
        IntStringSet newIsbnBooks = new IntStringSet();
        IntStringSet oldIsbnBooks = new IntStringSet();

        IntStringSet keepIssnJournals = new IntStringSet();
        IntStringSet newIssnJournals = new IntStringSet();
        IntStringSet oldIssnJournals = new IntStringSet();

        load("select reference,journal from pubchem.reference_journals", oldJournals);
        load("select reference,book from pubchem.reference_books", oldBooks);
        load("select reference,isbn from pubchem.reference_isbn_books", oldIsbnBooks);
        load("select reference,issn from pubchem.reference_issn_journals", oldIssnJournals);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());

                        if(object.getURI().startsWith(Book.prefix))
                        {
                            Integer bookID = Book.getBookID(object.getURI());

                            Pair<Integer, Integer> pair = Pair.getPair(referenceID, bookID);

                            synchronized(newIsbnBooks)
                            {
                                if(oldBooks.remove(pair))
                                    keepBooks.add(pair);
                                else if(!keepBooks.contains(pair))
                                    newBooks.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith(Journal.prefix))
                        {
                            Integer journalID = Journal.getJournalID(object.getURI());

                            Pair<Integer, Integer> pair = Pair.getPair(referenceID, journalID);

                            synchronized(newIssnJournals)
                            {
                                if(oldJournals.remove(pair))
                                    keepJournals.add(pair);
                                else if(!keepJournals.contains(pair))
                                    newJournals.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("https://isbnsearch.org/isbn"))
                        {
                            String isbn = getStringID(object, "https://isbnsearch.org/isbn");

                            Pair<Integer, String> pair = Pair.getPair(referenceID, isbn);

                            synchronized(newIsbnBooks)
                            {
                                if(oldIsbnBooks.remove(pair))
                                    keepIsbnBooks.add(pair);
                                else if(!keepIsbnBooks.contains(pair))
                                    newIsbnBooks.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("https://portal.issn.org/resource/ISSN"))
                        {
                            String issn = getStringID(object, "https://portal.issn.org/resource/ISSN");

                            Pair<Integer, String> pair = Pair.getPair(referenceID, issn);

                            synchronized(newIssnJournals)
                            {
                                if(oldIssnJournals.remove(pair))
                                    keepIssnJournals.add(pair);
                                else if(!keepIssnJournals.contains(pair))
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

        store("delete from pubchem.reference_journals where reference=? and journal=?", oldJournals);
        store("insert into pubchem.reference_journals(reference,journal) values(?,?)", newJournals);

        store("delete from pubchem.reference_books where reference=? and book=?", oldBooks);
        store("insert into pubchem.reference_books(reference,book) values(?,?)", newBooks);

        store("delete from pubchem.reference_isbn_books where reference=? and isbn=?", oldIsbnBooks);
        store("insert into pubchem.reference_isbn_books(reference,isbn) values(?,?)", newIsbnBooks);

        store("delete from pubchem.reference_issn_journals where reference=? and issn=?", oldIssnJournals);
        store("insert into pubchem.reference_issn_journals(reference,issn) values(?,?)", newIssnJournals);
    }


    private static void loadTextMinigs() throws IOException, SQLException
    {
        IntPairSet keepCompounds = new IntPairSet();
        IntPairSet newCompounds = new IntPairSet();
        IntPairSet oldCompounds = new IntPairSet();

        IntPairSet keepDiseases = new IntPairSet();
        IntPairSet newDiseases = new IntPairSet();
        IntPairSet oldDiseases = new IntPairSet();

        IntPairSet keepGenes = new IntPairSet();
        IntPairSet newGenes = new IntPairSet();
        IntPairSet oldGenes = new IntPairSet();

        IntPairSet keepEnzymes = new IntPairSet();
        IntPairSet newEnzymes = new IntPairSet();
        IntPairSet oldEnzymes = new IntPairSet();

        load("select reference,compound from pubchem.reference_mined_compounds", oldCompounds);
        load("select reference,disease from pubchem.reference_mined_diseases", oldDiseases);
        load("select reference,gene_symbol from pubchem.reference_mined_genes", oldGenes);
        load("select reference,enzyme from pubchem.reference_mined_enzymes", oldEnzymes);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());

                        if(object.getURI().startsWith(Compound.prefix))
                        {
                            Integer compoundID = Compound.getCompoundID(object.getURI());

                            Pair<Integer, Integer> pair = Pair.getPair(referenceID, compoundID);

                            synchronized(newCompounds)
                            {
                                if(oldCompounds.remove(pair))
                                    keepCompounds.add(pair);
                                else if(!keepCompounds.contains(pair))
                                    newCompounds.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith(Disease.prefix))
                        {
                            Integer diseaseID = Disease.getDiseaseID(object.getURI());

                            Pair<Integer, Integer> pair = Pair.getPair(referenceID, diseaseID);

                            synchronized(newDiseases)
                            {
                                if(oldDiseases.remove(pair))
                                    keepDiseases.add(pair);
                                else if(!keepDiseases.contains(pair))
                                    newDiseases.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith(Gene.symbolPrefix))
                        {
                            Integer geneSymbolID = Gene.getGeneSymbolID(object.getURI());

                            Pair<Integer, Integer> pair = Pair.getPair(referenceID, geneSymbolID);

                            synchronized(newGenes)
                            {
                                if(oldGenes.remove(pair))
                                    keepGenes.add(pair);
                                else if(!keepGenes.contains(pair))
                                    newGenes.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith(Protein.enzymePrefix))
                        {
                            Integer enzymeID = Protein.getEnzymeID(object.getURI());

                            Pair<Integer, Integer> pair = Pair.getPair(referenceID, enzymeID);

                            synchronized(newEnzymes)
                            {
                                if(oldEnzymes.remove(pair))
                                    keepEnzymes.add(pair);
                                else if(!keepEnzymes.contains(pair))
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

        store("delete from pubchem.reference_mined_compounds where reference=? and compound=?", oldCompounds);
        store("insert into pubchem.reference_mined_compounds(reference,compound) values(?,?)", newCompounds);

        store("delete from pubchem.reference_mined_diseases where reference=? and disease=?", oldDiseases);
        store("insert into pubchem.reference_mined_diseases(reference,disease) values(?,?)", newDiseases);

        store("delete from pubchem.reference_mined_genes where reference=? and gene_symbol=?", oldGenes);
        store("insert into pubchem.reference_mined_genes(reference,gene_symbol) values(?,?)", newGenes);

        store("delete from pubchem.reference_mined_enzymes where reference=? and enzyme=?", oldEnzymes);
        store("insert into pubchem.reference_mined_enzymes(reference,enzyme) values(?,?)", newEnzymes);
    }


    private static void loadIdentifiers() throws IOException, SQLException
    {
        IntStringSet keepDoiIdentifiers = new IntStringSet();
        IntStringSet newDoiIdentifiers = new IntStringSet();
        IntStringSet oldDoiIdentifiers = new IntStringSet();

        IntStringSet keepPubMedIdentifiers = new IntStringSet();
        IntStringSet newPubMedIdentifiers = new IntStringSet();
        IntStringSet oldPubMedIdentifiers = new IntStringSet();

        load("select reference,doi from pubchem.reference_doi_identifiers", oldDoiIdentifiers);
        load("select reference,pubmed from pubchem.reference_pubmed_identifiers", oldPubMedIdentifiers);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());

                        if(object.getURI().startsWith("https://doi.org/"))
                        {
                            String doi = getStringID(object, "https://doi.org/");

                            Pair<Integer, String> pair = Pair.getPair(referenceID, doi);

                            synchronized(newDoiIdentifiers)
                            {
                                if(oldDoiIdentifiers.remove(pair))
                                    keepDoiIdentifiers.add(pair);
                                else if(!keepDoiIdentifiers.contains(pair))
                                    newDoiIdentifiers.add(pair);
                            }
                        }
                        else if(object.getURI().startsWith("https://pubmed.ncbi.nlm.nih.gov/"))
                        {
                            String pubmed = getStringID(object, "https://pubmed.ncbi.nlm.nih.gov/");

                            Pair<Integer, String> pair = Pair.getPair(referenceID, pubmed);

                            synchronized(newPubMedIdentifiers)
                            {
                                if(oldPubMedIdentifiers.remove(pair))
                                    keepPubMedIdentifiers.add(pair);
                                else if(!keepPubMedIdentifiers.contains(pair))
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

        store("delete from pubchem.reference_doi_identifiers where reference=? and doi=?", oldDoiIdentifiers);
        store("insert into pubchem.reference_doi_identifiers(reference,doi) values(?,?)", newDoiIdentifiers);

        store("delete from pubchem.reference_pubmed_identifiers where reference=? and pubmed=?", oldPubMedIdentifiers);
        store("insert into pubchem.reference_pubmed_identifiers(reference,pubmed) values(?,?)", newPubMedIdentifiers);
    }


    private static void loadSources() throws IOException, SQLException
    {
        IntStringSet keepSources = new IntStringSet();
        IntStringSet newSources = new IntStringSet();
        IntStringSet oldSources = new IntStringSet();

        load("select reference,source_type::varchar from pubchem.reference_sources", oldSources);

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

                        Integer referenceID = Reference.getReferenceID(subject.getURI());
                        String issn = sources.get(object.getURI());

                        Pair<Integer, String> pair = Pair.getPair(referenceID, issn);

                        synchronized(newSources)
                        {
                            if(oldSources.remove(pair))
                                keepSources.add(pair);
                            else if(!keepSources.contains(pair))
                                newSources.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_sources "
                + "where reference=? and source_type=?::pubchem.reference_source_type", oldSources);
        store("insert into pubchem.reference_sources(reference,source_type) values(?,?::pubchem.reference_source_type)",
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

        store("delete from pubchem.reference_bases where id=?", oldReferences);
        store("insert into pubchem.reference_bases(id) values(?)", newReferences);

        System.out.println();
    }


    static Integer getReferenceID(String value) throws IOException
    {
        return getReferenceID(value, false);
    }


    static Integer getReferenceID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer referenceID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newReferences)
        {
            if(newReferences.contains(referenceID))
            {
                if(forceKeep)
                {
                    newReferences.remove(referenceID);
                    keepReferences.add(referenceID);
                }
            }
            else if(!keepReferences.contains(referenceID))
            {
                System.out.println("    add missing reference " + referenceID);

                if(!oldReferences.remove(referenceID) && !forceKeep)
                    newReferences.add(referenceID);
                else
                    keepReferences.add(referenceID);
            }
        }

        return referenceID;
    }
}
