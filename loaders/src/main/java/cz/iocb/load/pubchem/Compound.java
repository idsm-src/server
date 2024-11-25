package cz.iocb.load.pubchem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



class Compound extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepCompounds = new IntSet();
    private static final IntSet newCompounds = new IntSet();
    private static final IntSet oldCompounds = new IntSet();


    private static void loadBases() throws IOException, SQLException
    {
        load("select id from pubchem.compound_bases where keep", oldCompounds);
    }


    private static void loadComponents() throws IOException, SQLException
    {
        IntPairSet keepComponents = new IntPairSet();
        IntPairSet newComponents = new IntPairSet();
        IntPairSet oldComponents = new IntPairSet();

        load("select compound,component from pubchem.compound_components", oldComponents);

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound2component.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000480"))
                        throw new IOException();

                    Integer compoundID = getCompoundID(subject.getURI(), false);
                    Integer componentID = getCompoundID(object.getURI(), false);

                    Pair<Integer, Integer> pair = Pair.getPair(compoundID, componentID);

                    if(oldComponents.remove(pair))
                        keepComponents.add(pair);
                    else if(!keepComponents.contains(pair))
                        newComponents.add(pair);
                }
            }.load(stream);
        }

        store("delete from pubchem.compound_components where compound=? and component=?", oldComponents);
        store("insert into pubchem.compound_components(compound,component) values(?,?)", newComponents);
    }


    private static void loadDrugproducts() throws IOException, SQLException
    {
        IntIntPairSet keepIngredients = new IntIntPairSet();
        IntIntPairSet newIngredients = new IntIntPairSet();
        IntIntPairSet oldIngredients = new IntIntPairSet();

        load("select compound,ingredient_unit,ingredient_id from pubchem.compound_active_ingredients", oldIngredients);

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound2drugproduct.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI()
                            .equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#is_active_ingredient_of"))
                        throw new IOException();

                    Integer compoundID = getCompoundID(subject.getURI(), false);
                    Pair<Integer, Integer> ingredient = Ontology.getId(object.getURI());

                    Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(compoundID, ingredient);

                    if(oldIngredients.remove(pair))
                        keepIngredients.add(pair);
                    else if(!keepIngredients.contains(pair))
                        newIngredients.add(pair);
                }
            }.load(stream);
        }

        store("delete from pubchem.compound_active_ingredients where compound=? and ingredient_unit=? and "
                + "ingredient_id=?", oldIngredients);
        store("insert into pubchem.compound_active_ingredients(compound,ingredient_unit,ingredient_id) values(?,?,?)",
                newIngredients);
    }


    private static void loadIsotopologues() throws IOException, SQLException
    {
        IntPairSet keepIsotopologues = new IntPairSet();
        IntPairSet newIsotopologues = new IntPairSet();
        IntPairSet oldIsotopologues = new IntPairSet();

        load("select compound,isotopologue from pubchem.compound_isotopologues", oldIsotopologues);

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound2isotopologue.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000455"))
                        throw new IOException();

                    Integer compoundID = getCompoundID(subject.getURI(), false);
                    Integer isotopologueID = getCompoundID(object.getURI(), false);

                    Pair<Integer, Integer> pair = Pair.getPair(compoundID, isotopologueID);

                    if(oldIsotopologues.remove(pair))
                        keepIsotopologues.add(pair);
                    else if(!keepIsotopologues.contains(pair))
                        newIsotopologues.add(pair);
                }
            }.load(stream);
        }

        store("delete from pubchem.compound_isotopologues where compound=? and isotopologue=?", oldIsotopologues);
        store("insert into pubchem.compound_isotopologues(compound,isotopologue) values(?,?)", newIsotopologues);
    }


    private static void loadParents() throws IOException, SQLException
    {
        IntPairSet keepParents = new IntPairSet();
        IntPairSet newParents = new IntPairSet();
        IntPairSet oldParents = new IntPairSet();

        load("select compound,parent from pubchem.compound_parents", oldParents);

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound2parent.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent"))
                        throw new IOException();

                    Integer compoundID = getCompoundID(subject.getURI(), false);
                    Integer parentID = getCompoundID(object.getURI(), false);

                    Pair<Integer, Integer> pair = Pair.getPair(compoundID, parentID);

                    if(oldParents.remove(pair))
                        keepParents.add(pair);
                    else if(!keepParents.contains(pair))
                        newParents.add(pair);
                }
            }.load(stream);
        }

        store("delete from pubchem.compound_parents where compound=? and parent=?", oldParents);
        store("insert into pubchem.compound_parents(compound,parent) values(?,?)", newParents);
    }


    private static void loadSameConnectivities() throws IOException, SQLException
    {
        IntPairSet keepIsomers = new IntPairSet();
        IntPairSet newIsomers = new IntPairSet();
        IntPairSet oldIsomers = new IntPairSet();

        load("select compound,isomer from pubchem.compound_same_connectivities", oldIsomers);

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound2sameconnectivity.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000462"))
                        throw new IOException();

                    Integer compoundID = getCompoundID(subject.getURI(), false);
                    Integer isomerID = getCompoundID(object.getURI(), false);

                    Pair<Integer, Integer> pair = Pair.getPair(compoundID, isomerID);

                    if(oldIsomers.remove(pair))
                        keepIsomers.add(pair);
                    else if(!keepIsomers.contains(pair))
                        newIsomers.add(pair);
                }
            }.load(stream);
        }

        store("delete from pubchem.compound_same_connectivities where compound=? and isomer=?", oldIsomers);
        store("insert into pubchem.compound_same_connectivities(compound,isomer) values(?,?)", newIsomers);
    }


    private static void loadStereoisomers() throws IOException, SQLException
    {
        IntPairSet keepIsomers = new IntPairSet();
        IntPairSet newIsomers = new IntPairSet();
        IntPairSet oldIsomers = new IntPairSet();

        load("select compound,isomer from pubchem.compound_stereoisomers", oldIsomers);

        processFiles("pubchem/RDF/compound/general", "pc_compound2stereoisomer_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000461"))
                            throw new IOException();

                        Integer compoundID = getCompoundID(subject.getURI(), false);
                        Integer isomerID = getCompoundID(object.getURI(), false);

                        Pair<Integer, Integer> pair = Pair.getPair(compoundID, isomerID);

                        synchronized(newIsomers)
                        {
                            if(oldIsomers.remove(pair))
                                keepIsomers.add(pair);
                            else if(!keepIsomers.contains(pair))
                                newIsomers.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.compound_stereoisomers where compound=? and isomer=?", oldIsomers);
        store("insert into pubchem.compound_stereoisomers(compound,isomer) values(?,?)", newIsomers);
    }


    private static void loadRoles() throws IOException, SQLException
    {
        IntPairSet keepRoles = new IntPairSet();
        IntPairSet newRoles = new IntPairSet();
        IntPairSet oldRoles = new IntPairSet();

        load("select compound,role_id from pubchem.compound_roles", oldRoles);

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound_role.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/RO_0000087"))
                        throw new IOException();

                    // workaround
                    if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CIDNULL"))
                        return;

                    Integer compoundID = getCompoundID(subject.getURI(), false);

                    Pair<Integer, Integer> role = Ontology.getId(object.getURI());

                    if(role.getOne() != Ontology.unitUncategorized)
                        throw new IOException();

                    Pair<Integer, Integer> pair = Pair.getPair(compoundID, role.getTwo());

                    if(oldRoles.remove(pair))
                        keepRoles.add(pair);
                    else if(!keepRoles.contains(pair))
                        newRoles.add(pair);
                }
            }.load(stream);
        }

        store("delete from pubchem.compound_roles where compound=? and role_id=?", oldRoles);
        store("insert into pubchem.compound_roles(compound,role_id) values(?,?)", newRoles);
    }


    private static void loadTypes() throws IOException, SQLException
    {
        IntIntPairSet keepTypes = new IntIntPairSet();
        IntIntPairSet newTypes = new IntIntPairSet();
        IntIntPairSet oldTypes = new IntIntPairSet();

        load("select compound,type_unit,type_id from pubchem.compound_types", oldTypes);

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    Integer compoundID = getCompoundID(subject.getURI(), false);
                    Pair<Integer, Integer> type = Ontology.getId(object.getURI());

                    Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(compoundID, type);

                    if(oldTypes.remove(pair))
                        keepTypes.add(pair);
                    else if(!keepTypes.contains(pair))
                        newTypes.add(pair);
                }
            }.load(stream);
        }

        store("delete from pubchem.compound_types where compound=? and type_unit=? and type_id=?", oldTypes);
        store("insert into pubchem.compound_types(compound,type_unit,type_id) values(?,?,?)", newTypes);
    }


    private static void loadTitle() throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select compound,title from pubchem.compound_titles", oldTitles);

        try(BufferedReader reader = getReader("pubchem/Compound/Extras/CID-Title.gz"))
        {
            for(String line = reader.readLine(); line != null; line = reader.readLine())
            {
                Integer compoundID = Integer.parseInt(line.replaceFirst("\t.*$", ""));
                String title = line.replaceFirst("^[^\t]+\t", "");

                addCompoundID(compoundID, false);

                if(title.equals(oldTitles.remove(compoundID)))
                {
                    keepTitles.put(compoundID, title);
                }
                else
                {
                    String keep = keepTitles.get(compoundID);

                    if(title.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newTitles.put(compoundID, title);

                    if(put != null && !title.equals(put))
                        throw new IOException();
                }
            }
        }

        store("delete from pubchem.compound_titles where compound=? and title=?", oldTitles);
        store("insert into pubchem.compound_titles(compound,title) values(?,?) "
                + "on conflict(compound) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadCloseMatches() throws IOException, SQLException
    {
        IntPairSet keepThesaurusMatches = new IntPairSet();
        IntPairSet newThesaurusMatches = new IntPairSet();
        IntPairSet oldThesaurusMatches = new IntPairSet();

        IntPairSet keepWikidataMatches = new IntPairSet();
        IntPairSet newWikidataMatches = new IntPairSet();
        IntPairSet oldWikidataMatches = new IntPairSet();

        load("select compound,match from pubchem.compound_thesaurus_matches", oldThesaurusMatches);
        load("select compound,match from pubchem.compound_wikidata_matches", oldWikidataMatches);

        processFiles("pubchem/RDF/compound/general", "pc_compound_closematch.ttl[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://www.w3.org/2004/02/skos/core#closeMatch"))
                            throw new IOException();

                        Integer compoundID = getCompoundID(subject.getURI(), false);

                        if(object.getURI().startsWith("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C"))
                        {
                            Integer match = getIntID(object, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C");

                            Pair<Integer, Integer> pair = Pair.getPair(compoundID, match);

                            synchronized(newThesaurusMatches)
                            {
                                if(oldThesaurusMatches.remove(pair))
                                    keepThesaurusMatches.add(pair);
                                else if(!keepThesaurusMatches.contains(pair))
                                    newThesaurusMatches.add(pair);
                            }
                        }
                        else
                        {
                            Integer match = getIntID(object, "https://www.wikidata.org/wiki/Q");

                            Pair<Integer, Integer> pair = Pair.getPair(compoundID, match);

                            synchronized(newWikidataMatches)
                            {
                                if(oldWikidataMatches.remove(pair))
                                    keepWikidataMatches.add(pair);
                                else if(!keepWikidataMatches.contains(pair))
                                    newWikidataMatches.add(pair);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.compound_thesaurus_matches where compound=? and match=?", oldThesaurusMatches);
        store("insert into pubchem.compound_thesaurus_matches(compound,match) values(?,?)", newThesaurusMatches);

        store("delete from pubchem.compound_wikidata_matches where compound=? and match=?", oldWikidataMatches);
        store("insert into pubchem.compound_wikidata_matches(compound,match) values(?,?)", newWikidataMatches);
    }


    private static void checkDescriptors() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/compound/general", "pc_compound2descriptor_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        getCompoundID(subject.getURI(), false);

                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000008"))
                            throw new IOException();

                        getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID");
                    }
                }.load(stream);
            }
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load compounds ...");

        loadBases();
        loadComponents();
        loadDrugproducts();
        loadIsotopologues();
        loadParents();
        loadSameConnectivities();
        loadStereoisomers();
        loadRoles();
        loadTypes();
        loadTitle();
        loadCloseMatches();
        checkDescriptors();

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish compounds ...");

        store("delete from pubchem.compound_bases "
                + "where id=? and not exists (select id from molecules.pubchem where compound_bases.id = pubchem.id)",
                oldCompounds);

        store("update pubchem.compound_bases set keep = false "
                + "where id=? and  exists (select id from molecules.pubchem where compound_bases.id = pubchem.id)",
                oldCompounds);

        store("insert into pubchem.compound_bases(id,keep) values(?,true) "
                + "on conflict(id) do update set keep=EXCLUDED.keep", newCompounds);

        System.out.println();
    }


    static void addCompoundID(Integer compoundID) throws IOException
    {
        addCompoundID(compoundID, true);
    }


    static void addCompoundID(Integer compoundID, boolean verbose) throws IOException
    {
        synchronized(newCompounds)
        {
            if(!keepCompounds.contains(compoundID) && !newCompounds.contains(compoundID))
            {
                if(verbose)
                    System.out.println("    add missing compound CID" + compoundID);

                if(oldCompounds.remove(compoundID))
                    keepCompounds.add(compoundID);
                else
                    newCompounds.add(compoundID);
            }
        }
    }


    static Integer getCompoundID(String value) throws IOException
    {
        return getCompoundID(value, true);
    }


    private static Integer getCompoundID(String value, boolean verbose) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer compoundID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newCompounds)
        {
            if(!keepCompounds.contains(compoundID) && !newCompounds.contains(compoundID))
            {
                if(verbose)
                    System.out.println("    add missing compound CID" + compoundID);

                if(oldCompounds.remove(compoundID))
                    keepCompounds.add(compoundID);
                else
                    newCompounds.add(compoundID);
            }
        }

        return compoundID;
    }


    public static int size()
    {
        return newCompounds.size() + keepCompounds.size();
    }
}
