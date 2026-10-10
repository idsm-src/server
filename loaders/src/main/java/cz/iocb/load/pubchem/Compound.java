package cz.iocb.load.pubchem;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUncategorized;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.SdfReader;
import cz.iocb.load.common.StructureTable;
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
    private static final MissingEntities<Integer> missingCompounds = new MissingEntities<>("compound", false);


    private static void loadBases() throws IOException, SQLException
    {
        load("select id from pubchem.compounds", oldCompounds);
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
                    if(!checkPredicate(subject, predicate, object,
                            "http://semanticscience.org/resource/CHEMINF_000480"))
                        return;

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
                    if(!checkPredicate(subject, predicate, object,
                            "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#is_active_ingredient_of"))
                        return;

                    Integer compoundID = getCompoundID(subject.getURI(), false);
                    Pair<Integer, Integer> ingredient = Ontology.getResourceId(object, null);

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
                    if(!checkPredicate(subject, predicate, object,
                            "http://semanticscience.org/resource/CHEMINF_000455"))
                        return;

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
                    if(!checkPredicate(subject, predicate, object,
                            "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent"))
                        return;

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
                    if(!checkPredicate(subject, predicate, object,
                            "http://semanticscience.org/resource/CHEMINF_000462"))
                        return;

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
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/CHEMINF_000461"))
                            return;

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
                    if(!checkPredicate(subject, predicate, object, "http://purl.obolibrary.org/obo/RO_0000087"))
                        return;

                    // workaround
                    if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CIDNULL"))
                    {
                        Problems.warning("ignored triple of an invalid subject in " + predicate.getURI(),
                                text(subject) + " " + text(object));
                        return;
                    }

                    Integer compoundID = getCompoundID(subject.getURI(), false);

                    Pair<Integer, Integer> role = Ontology.getResourceId(object, unitUncategorized);

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

        processFiles("pubchem/RDF/compound/general", "pc_compound_type_[0-9]+\\.ttl\\.gz", file -> {
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

                        if(object.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Compound"))
                            return;

                        Integer compoundID = getCompoundID(subject.getURI(), false);
                        Pair<Integer, Integer> type = Ontology.getResourceId(object, null);

                        Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(compoundID, type);

                        synchronized(newTypes)
                        {
                            if(oldTypes.remove(pair))
                                keepTypes.add(pair);
                            else if(!keepTypes.contains(pair))
                                newTypes.add(pair);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.compound_types where compound=? and type_unit=? and type_id=?", oldTypes);
        store("insert into pubchem.compound_types(compound,type_unit,type_id) values(?,?,?)", newTypes);
    }


    private static void loadLabels() throws IOException, SQLException
    {
        IntStringMap keepLabels = new IntStringMap();
        IntStringMap newLabels = new IntStringMap();
        IntStringMap oldLabels = new IntStringMap();

        load("select compound,label from pubchem.compound_labels", oldLabels);

        processFiles("pubchem/RDF/compound/general", "pc_compound_label_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://www.w3.org/2004/02/skos/core#prefLabel"))
                            return;

                        Integer compoundID = getCompoundID(subject.getURI(), false);
                        String label = getString(object);

                        synchronized(newLabels)
                        {
                            if(label.equals(oldLabels.remove(compoundID)))
                            {
                                keepLabels.put(compoundID, label);
                            }
                            else
                            {
                                String keep = keepLabels.get(compoundID);

                                if(label.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new DataException("multiple values of pubchem.compound_labels.label",
                                            compoundID + ": " + keep + ", " + label);

                                String put = newLabels.put(compoundID, label);

                                if(put != null && !label.equals(put))
                                    throw new DataException("multiple values of pubchem.compound_labels.label",
                                            compoundID + ": " + put + ", " + label);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.compound_labels where compound=? and label=?", oldLabels);
        store("insert into pubchem.compound_labels(compound,label) values(?,?) "
                + "on conflict(compound) do update set label=EXCLUDED.label", newLabels);
    }


    private static void loadCloseMatches() throws IOException, SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        IntPairSet keepWikidataMatches = new IntPairSet();
        IntPairSet newWikidataMatches = new IntPairSet();
        IntPairSet oldWikidataMatches = new IntPairSet();

        load("select compound,match_unit,match_id from pubchem.compound_matches", oldMatches);
        load("select compound,match from pubchem.compound_wikidata_matches", oldWikidataMatches);

        processFiles("pubchem/RDF/compound/general", "pc_compound_seealso_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://www.w3.org/2000/01/rdf-schema#seeAlso"))
                            return;

                        Integer compoundID = getCompoundID(subject.getURI(), false);

                        if(object.getURI().startsWith("http://www.wikidata.org/entity/Q"))
                        {
                            Integer match = getIntID(object, "http://www.wikidata.org/entity/Q");

                            Pair<Integer, Integer> pair = Pair.getPair(compoundID, match);

                            synchronized(newWikidataMatches)
                            {
                                if(oldWikidataMatches.remove(pair))
                                    keepWikidataMatches.add(pair);
                                else if(!keepWikidataMatches.contains(pair))
                                    newWikidataMatches.add(pair);
                            }
                        }
                        else
                        {
                            Pair<Integer, Integer> match = Ontology.getResourceId(object, null);
                            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(compoundID, match);

                            synchronized(newMatches)
                            {
                                if(oldMatches.remove(pair))
                                    keepMatches.add(pair);
                                else if(!keepMatches.contains(pair))
                                    newMatches.add(pair);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.compound_matches where compound=? and match_unit=? and match_id=?", oldMatches);
        store("insert into pubchem.compound_matches(compound,match_unit,match_id) values(?,?,?)", newMatches);

        store("delete from pubchem.compound_wikidata_matches where compound=? and match=?", oldWikidataMatches);
        store("insert into pubchem.compound_wikidata_matches(compound,match) values(?,?)", newWikidataMatches);
    }


    /*
     * Checks that the molfile files cover the identifiers contiguously from the first one, so that an incomplete dump
     * does not delete the structures it lacks. No molfile file at all is reported by processFiles().
     */
    private static void checkMolfileFiles(String path, String pattern)
    {
        String[] files = new File(baseDirectory + path).list((dir, file) -> file.matches(pattern));

        if(files == null)
            return;

        Arrays.sort(files);
        long next = 1;

        for(String file : files)
        {
            long from = Long.parseLong(file.substring(9, 18));
            long to = Long.parseLong(file.substring(19, 28));

            if(from != next || to < from)
                Problems.error("unexpected range of a molfile file",
                        path + "/" + file + " (expected the range from " + next + ")");

            next = to + 1;
        }
    }


    /*
     * Loads the structures of the compounds from the dump of all molfiles; a compound known only from the dump gets
     * its row as well.
     */
    private static void loadMolfiles() throws IOException, SQLException
    {
        String path = "pubchem/Compound/CURRENT-Full/SDF";
        String pattern = "Compound_[0-9]{9}_[0-9]{9}\\.sdf\\.gz";

        checkMolfileFiles(path, pattern);

        StructureTable molfiles = new StructureTable("pubchem.compound_molfiles", "compound", "molfile");
        molfiles.load();

        processFiles(path, pattern, file -> {
            try(BufferedReader reader = getReader(file))
            {
                SdfReader.read(file, reader, "PUBCHEM_COMPOUND_CID", (id, molfile) -> {
                    int compoundID = Integer.parseInt(id);

                    addCompoundID(compoundID, false);
                    molfiles.put(compoundID, molfile);
                });
            }
        });

        molfiles.store();
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
                        getIntID(subject, prefix);

                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000008"))
                            return;

                        getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID");
                    }
                }.load(stream);
            }
        });
    }


    private static void checkIdentifiers() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/compound/general", "pc_compound_identifier_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        Integer compoundID = getIntID(subject, prefix);

                        if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/identifier"))
                            return;

                        if(compoundID != Integer.parseInt(getLexicalForm(object)))
                            Problems.error("value of " + predicate.getURI() + " not matching the IRI",
                                    text(subject) + " " + text(object));
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
        loadLabels();
        loadCloseMatches();
        loadMolfiles();
        checkDescriptors();
        checkIdentifiers();

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish compounds ...");

        store("delete from pubchem.compounds where id=?", oldCompounds);
        store("insert into pubchem.compounds(id) values(?)", newCompounds);

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
                    missingCompounds.referenced(compoundID);

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
            throw new DataException("unexpected IRI", value);

        Integer compoundID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newCompounds)
        {
            if(!keepCompounds.contains(compoundID) && !newCompounds.contains(compoundID))
            {
                if(verbose)
                    missingCompounds.referenced(compoundID);

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
