package cz.iocb.load.pubchem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.IntTriplet;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



class Compound extends Updater
{
    private static IntHashSet usedCompounds;
    private static IntHashSet newCompounds;
    private static IntHashSet oldCompounds;


    private static void loadBases() throws IOException, SQLException
    {
        usedCompounds = new IntHashSet();
        newCompounds = new IntHashSet();
        oldCompounds = getIntSet("select id from pubchem.compound_bases where keep");
    }


    private static void loadComponents() throws IOException, SQLException
    {
        IntPairSet newComponents = new IntPairSet();
        IntPairSet oldComponents = getIntPairSet("select compound, component from pubchem.compound_components");

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound2component.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000480"))
                        throw new IOException();

                    int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    int componentID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                    IntIntPair pair = PrimitiveTuples.pair(compoundID, componentID);
                    addCompoundID(compoundID, false);
                    addCompoundID(componentID, false);

                    if(!oldComponents.remove(pair))
                        newComponents.add(pair);
                }
            }.load(stream);
        }

        batch("delete from pubchem.compound_components where compound = ? and component = ?", oldComponents);
        batch("insert into pubchem.compound_components(compound, component) values (?,?)", newComponents);
    }


    private static void loadDrugproducts() throws IOException, SQLException
    {
        IntTripletSet newIngredients = new IntTripletSet();
        IntTripletSet oldIngredients = getIntTripletSet(
                "select compound, ingredient_unit, ingredient_id from pubchem.compound_active_ingredients");

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

                    int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    Identifier ingredient = Ontology.getId(object.getURI());

                    IntTriplet triplet = new IntTriplet(compoundID, ingredient.unit, ingredient.id);
                    addCompoundID(compoundID, false);

                    if(!oldIngredients.remove(triplet))
                        newIngredients.add(triplet);
                }
            }.load(stream);
        }

        batch("delete from pubchem.compound_active_ingredients where compound = ? and ingredient_unit = ? and ingredient_id = ?",
                oldIngredients);
        batch("insert into pubchem.compound_active_ingredients(compound, ingredient_unit, ingredient_id) values (?,?,?)",
                newIngredients);
    }


    private static void loadIsotopologues() throws IOException, SQLException
    {
        IntPairSet newIsotopologues = new IntPairSet();
        IntPairSet oldIsotopologues = getIntPairSet(
                "select compound, isotopologue from pubchem.compound_isotopologues");

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound2isotopologue.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000455"))
                        throw new IOException();

                    int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    int isotopologueID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                    IntIntPair pair = PrimitiveTuples.pair(compoundID, isotopologueID);
                    addCompoundID(compoundID, false);
                    addCompoundID(isotopologueID, false);

                    if(!oldIsotopologues.remove(pair))
                        newIsotopologues.add(pair);
                }
            }.load(stream);
        }

        batch("delete from pubchem.compound_isotopologues where compound = ? and isotopologue = ?", oldIsotopologues);
        batch("insert into pubchem.compound_isotopologues(compound, isotopologue) values (?,?)", newIsotopologues);
    }


    private static void loadParents() throws IOException, SQLException
    {
        IntPairSet newParents = new IntPairSet();
        IntPairSet oldParents = getIntPairSet("select compound, parent from pubchem.compound_parents");

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound2parent.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent"))
                        throw new IOException();

                    int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    int parentID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                    IntIntPair pair = PrimitiveTuples.pair(compoundID, parentID);
                    addCompoundID(compoundID, false);
                    addCompoundID(parentID, false);

                    if(!oldParents.remove(pair))
                        newParents.add(pair);
                }
            }.load(stream);
        }

        batch("delete from pubchem.compound_parents where compound = ? and parent = ?", oldParents);
        batch("insert into pubchem.compound_parents(compound, parent) values (?,?)", newParents);
    }


    private static void loadSameConnectivities() throws IOException, SQLException
    {
        IntPairSet newIsomers = new IntPairSet();
        IntPairSet oldIsomers = getIntPairSet("select compound, isomer from pubchem.compound_same_connectivities");

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound2sameconnectivity.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000462"))
                        throw new IOException();

                    int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    int isomerID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                    IntIntPair pair = PrimitiveTuples.pair(compoundID, isomerID);
                    addCompoundID(compoundID, false);
                    addCompoundID(isomerID, false);

                    if(!oldIsomers.remove(pair))
                        newIsomers.add(pair);
                }
            }.load(stream);
        }

        batch("delete from pubchem.compound_same_connectivities where compound = ? and isomer = ?", oldIsomers);
        batch("insert into pubchem.compound_same_connectivities(compound, isomer) values (?,?)", newIsomers);
    }


    private static void loadStereoisomers() throws IOException, SQLException
    {
        IntPairSet newIsomers = new IntPairSet();
        IntPairSet oldIsomers = getIntPairSet("select compound, isomer from pubchem.compound_stereoisomers");

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

                        int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                        int isomerID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                        IntIntPair pair = PrimitiveTuples.pair(compoundID, isomerID);
                        addCompoundID(compoundID, false);
                        addCompoundID(isomerID, false);

                        synchronized(newIsomers)
                        {
                            if(!oldIsomers.remove(pair))
                                newIsomers.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.compound_stereoisomers where compound = ? and isomer = ?", oldIsomers);
        batch("insert into pubchem.compound_stereoisomers(compound, isomer) values (?,?)", newIsomers);
    }


    private static void loadRoles() throws IOException, SQLException
    {
        IntPairSet newRoles = new IntPairSet();
        IntPairSet oldRoles = getIntPairSet("select compound, role_id from pubchem.compound_roles");

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

                    int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                    Identifier role = Ontology.getId(object.getURI());

                    if(role.unit != Ontology.unitUncategorized)
                        throw new IOException();

                    IntIntPair pair = PrimitiveTuples.pair(compoundID, role.id);
                    addCompoundID(compoundID, false);

                    if(!oldRoles.remove(pair))
                        newRoles.add(pair);
                }
            }.load(stream);
        }

        batch("delete from pubchem.compound_roles where compound = ? and role_id = ?", oldRoles);
        batch("insert into pubchem.compound_roles(compound, role_id) values (?,?)", newRoles);
    }


    private static void loadTypes() throws IOException, SQLException
    {
        IntTripletSet newTypes = new IntTripletSet();
        IntTripletSet oldTypes = getIntTripletSet("select compound, type_unit, type_id from pubchem.compound_types");

        try(InputStream stream = getTtlStream("pubchem/RDF/compound/general/pc_compound_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    Identifier type = Ontology.getId(object.getURI());

                    IntTriplet triplet = new IntTriplet(compoundID, type.unit, type.id);
                    addCompoundID(compoundID, false);

                    if(!oldTypes.remove(triplet))
                        newTypes.add(triplet);
                }
            }.load(stream);
        }

        batch("delete from pubchem.compound_types where compound = ? and type_unit = ? and type_id = ?", oldTypes);
        batch("insert into pubchem.compound_types(compound, type_unit, type_id) values (?,?,?)", newTypes);
    }


    private static void loadTitle() throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = getIntStringMap("select compound, title from pubchem.compound_titles");

        try(BufferedReader reader = getReader("pubchem/Compound/Extras/CID-Title.gz"))
        {
            for(String line = reader.readLine(); line != null; line = reader.readLine())
            {
                int compoundID = Integer.parseInt(line.replaceFirst("\t.*$", ""));
                String title = line.replaceFirst("^[^\t]+\t", "");

                addCompoundID(compoundID, false);

                if(!title.equals(oldTitles.remove(compoundID)))
                    newTitles.put(compoundID, title);
            }
        }

        batch("delete from pubchem.compound_titles where compound = ?", oldTitles.keySet());
        batch("insert into pubchem.compound_titles(compound, title) values (?,?)"
                + "on conflict (compound) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadCloseMatches() throws IOException, SQLException
    {
        IntPairSet newThesaurusMatches = new IntPairSet();
        IntPairSet oldThesaurusMatches = getIntPairSet(
                "select compound, match from pubchem.compound_thesaurus_matches");

        IntPairSet newWikidataMatches = new IntPairSet();
        IntPairSet oldWikidataMatches = getIntPairSet("select compound, match from pubchem.compound_wikidata_matches");

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

                        int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                        addCompoundID(compoundID, false);

                        if(object.getURI().startsWith("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C"))
                        {
                            int match = getIntID(object, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C");
                            IntIntPair pair = PrimitiveTuples.pair(compoundID, match);

                            synchronized(newThesaurusMatches)
                            {
                                if(!oldThesaurusMatches.remove(pair))
                                    newThesaurusMatches.add(pair);
                            }
                        }
                        else
                        {
                            int match = getIntID(object, "https://www.wikidata.org/wiki/Q");
                            IntIntPair pair = PrimitiveTuples.pair(compoundID, match);

                            synchronized(newWikidataMatches)
                            {
                                if(!oldWikidataMatches.remove(pair))
                                    newWikidataMatches.add(pair);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem.compound_thesaurus_matches where compound = ? and match = ?", oldThesaurusMatches);
        batch("insert into pubchem.compound_thesaurus_matches(compound, match) values (?,?)", newThesaurusMatches);

        batch("delete from pubchem.compound_wikidata_matches where compound = ? and match = ?", oldWikidataMatches);
        batch("insert into pubchem.compound_wikidata_matches(compound, match) values (?,?)", newWikidataMatches);
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
                        getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

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

        batch("delete from pubchem.compound_bases where id = ? and "
                + "not exists (select id from molecules.pubchem where compound_bases.id = molecules.pubchem.id)",
                oldCompounds);

        batch("update pubchem.compound_bases set keep = false where id = ? and "
                + "exists (select id from molecules.pubchem where compound_bases.id = molecules.pubchem.id)",
                oldCompounds);

        batch("insert into pubchem.compound_bases(id,keep) values(?,true)"
                + " on conflict (id) do update set keep=EXCLUDED.keep", newCompounds);

        usedCompounds = null;
        newCompounds = null;
        oldCompounds = null;

        System.out.println();
    }


    static void addCompoundID(int compoundID)
    {
        addCompoundID(compoundID, true);
    }


    private static void addCompoundID(int compoundID, boolean verbose)
    {
        synchronized(newCompounds)
        {
            if(usedCompounds.add(compoundID))
            {
                if(verbose)
                    System.out.println("    add missing compound CID" + compoundID);

                if(!oldCompounds.remove(compoundID))
                    newCompounds.add(compoundID);
            }
        }
    }
}
