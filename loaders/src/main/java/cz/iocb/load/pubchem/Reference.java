package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.date;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Reference extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> references = new EntityTable<>("pubchem.references", intKey("id"), null,
            date("dcdate"), varchar("date"), uniqueVarchar("title"), uniqueVarchar("citation"), varchar("publication"),
            varchar("issue"), varchar("starting_page"), varchar("ending_page"), varchar("page_range"), varchar("lang"));
    private static final MissingEntities<Integer> missingReferences = new MissingEntities<>("reference", false);

    private static HashMap<String, String> sources = new HashMap<>();


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
        processFiles("pubchem/RDF/reference", "pc_reference_title_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/dc/terms/title"))
                            throw new IOException();

                        Integer referenceID = getIntID(subject, prefix);
                        String title = getString(object);

                        references.set(referenceID, "title", title);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadPublications() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/reference", "pc_reference_publication_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/publicationName"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
                        String publication = getString(object);

                        references.set(referenceID, "publication", publication);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadCitations() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/reference", "pc_reference_citation_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/dc/terms/bibliographicCitation"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
                        String citation = getString(object);

                        references.set(referenceID, "citation", citation);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadIssues() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/reference", "pc_reference_issue_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/issueIdentifier"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
                        String issue = getString(object);

                        references.set(referenceID, "issue", issue);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadStartingPages() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/reference", "pc_reference_startingpage_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/startingPage"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
                        String page = getString(object);

                        references.set(referenceID, "starting_page", page);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadEndingPages() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/reference", "pc_reference_endingpage_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/endingPage"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
                        String page = getString(object);

                        references.set(referenceID, "ending_page", page);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadPageRanges() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/reference", "pc_reference_pagerange_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/pageRange"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
                        String range = getString(object);

                        references.set(referenceID, "page_range", range);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadLangs() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/reference", "pc_reference_lang_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/dc/terms/language"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
                        String lang = getString(object);

                        references.set(referenceID, "lang", lang);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadDates() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/reference", "pc_reference_date\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/dc/terms/date"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());

                        switch(object.getLiteral().getDatatype().getURI())
                        {
                            case "http://www.w3.org/2001/XMLSchema#date" ->
                            {
                                String date = getLexicalForm(object).replaceFirst("-0[45]:00$", "");

                                references.set(referenceID, "dcdate", date);
                            }
                            case "http://www.w3.org/2001/XMLSchema#string" ->
                            {
                                String date = getString(object);

                                references.set(referenceID, "date", date);
                            }
                            default ->
                            {
                                throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });
    }


    private static void loadChemicalDiseases() throws IOException, SQLException
    {
        IntStringSet keepDiscusses = new IntStringSet();
        IntStringSet newDiscusses = new IntStringSet();
        IntStringSet oldDiscusses = new IntStringSet();

        load("select reference,heading from pubchem.reference_discussed_headings", oldDiscusses);

        processFiles("pubchem/RDF/reference", "pc_reference2chemical_disease_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/spar/cito/discusses"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
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

        store("delete from pubchem.reference_discussed_headings where reference=? and heading=?", oldDiscusses);
        store("insert into pubchem.reference_discussed_headings(reference,heading) values(?,?)", newDiscusses);
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
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/spar/fabio/hasSubjectTerm"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());

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
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/spar/fabio/hasPrimarySubjectTerm"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
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
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/contentType"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
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
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/issn"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
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


    private static void loadIsbnNumbers() throws IOException, SQLException
    {
        IntStringSet keepIsbnNumbers = new IntStringSet();
        IntStringSet newIsbnNumbers = new IntStringSet();
        IntStringSet oldIsbnNumbers = new IntStringSet();

        load("select reference,isbn from pubchem.reference_isbn_numbers", oldIsbnNumbers);

        processFiles("pubchem/RDF/reference", "pc_reference_isbn_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://prismstandard.org/namespaces/basic/3.0/isbn"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
                        String isbn = getString(object);

                        Pair<Integer, String> pair = Pair.getPair(referenceID, isbn);

                        synchronized(newIsbnNumbers)
                        {
                            if(oldIsbnNumbers.remove(pair))
                                keepIsbnNumbers.add(pair);
                            else if(!keepIsbnNumbers.contains(pair))
                                newIsbnNumbers.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_isbn_numbers where reference=? and isbn=?", oldIsbnNumbers);
        store("insert into pubchem.reference_isbn_numbers(reference,isbn) values(?,?)", newIsbnNumbers);
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
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/dc/terms/creator"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
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

        load("select reference,supporting_grant from pubchem.reference_grants", oldGrants);

        processFiles("pubchem/RDF/reference", "pc_reference_grant_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/cerif/frapo/isSupportedBy"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
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

        store("delete from pubchem.reference_grants where reference=? and supporting_grant=?", oldGrants);
        store("insert into pubchem.reference_grants(reference,supporting_grant) values(?,?)", newGrants);
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
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/cerif/frapo/hasFundingAgency"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
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
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/dc/terms/isPartOf"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());

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
        load("select reference,genesymbol from pubchem.reference_mined_genesymbols", oldGenes);
        load("select reference,enzyme from pubchem.reference_mined_enzymes", oldEnzymes);

        processFiles("pubchem/RDF/reference", "pc_reference_discusses_by_textming_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals(
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#discussesAsDerivedByTextMining"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());

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

        store("delete from pubchem.reference_mined_genesymbols where reference=? and genesymbol=?", oldGenes);
        store("insert into pubchem.reference_mined_genesymbols(reference,genesymbol) values(?,?)", newGenes);

        store("delete from pubchem.reference_mined_enzymes where reference=? and enzyme=?", oldEnzymes);
        store("insert into pubchem.reference_mined_enzymes(reference,enzyme) values(?,?)", newEnzymes);
    }


    private static void loadIdentifiers() throws IOException, SQLException
    {
        IntStringSet keepIdentifiers = new IntStringSet();
        IntStringSet newIdentifiers = new IntStringSet();
        IntStringSet oldIdentifiers = new IntStringSet();

        load("select reference,identifier from pubchem.reference_identifiers", oldIdentifiers);

        processFiles("pubchem/RDF/reference", "pc_reference_identifier_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/dc/terms/identifier"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
                        String identifier = getString(object);

                        Pair<Integer, String> pair = Pair.getPair(referenceID, identifier);

                        synchronized(newIdentifiers)
                        {
                            if(oldIdentifiers.remove(pair))
                                keepIdentifiers.add(pair);
                            else if(!keepIdentifiers.contains(pair))
                                newIdentifiers.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.reference_identifiers where reference=? and identifier=?", oldIdentifiers);
        store("insert into pubchem.reference_identifiers(reference,identifier) values(?,?)", newIdentifiers);
    }


    private static void loadSources() throws IOException, SQLException
    {
        IntStringSet keepSources = new IntStringSet();
        IntStringSet newSources = new IntStringSet();
        IntStringSet oldSources = new IntStringSet();

        load("select reference,source_type::varchar from pubchem.reference_source_types", oldSources);

        processFiles("pubchem/RDF/reference", "pc_reference_source_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                            return;

                        if(!predicate.getURI().equals("http://purl.org/dc/terms/source"))
                            throw new IOException();

                        Integer referenceID = getReferenceID(subject.getURI());
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

        store("delete from pubchem.reference_source_types "
                + "where reference=? and source_type=?::pubchem.reference_source_type", oldSources);
        store("insert into pubchem.reference_source_types(reference,source_type) "
                + "values(?,?::pubchem.reference_source_type)", newSources);
    }


    private static void checkTypes() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream("pubchem/RDF/reference/pc_reference_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                        return;

                    getStringID(subject, prefix);

                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    if(!object.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Reference"))
                        throw new IOException();
                }
            }.load(stream);
        }
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
        loadIsbnNumbers();
        loadAuthor();
        loadGrant();
        loadFundingAgency();
        loadTextMinigs();
        loadJournalsAndBooks();
        loadIdentifiers();
        loadSources();
        checkTypes();

        references.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish references ...");

        references.store();

        System.out.println();
    }


    static Integer getReferenceID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer referenceID = Integer.parseInt(value.substring(prefixLength));

        if(references.reference(referenceID))
            missingReferences.referenced(referenceID);

        return referenceID;
    }
}
