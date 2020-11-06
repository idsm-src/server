package cz.iocb.load.pubchem;

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
        usedCompounds = new IntHashSet(200000000);
        newCompounds = new IntHashSet(200000000);
        oldCompounds = getIntSet("select id from pubchem.compound_bases where keep", 200000000);
    }


    private static void loadComponents() throws IOException, SQLException
    {
        IntPairSet newComponents = new IntPairSet(10000000);
        IntPairSet oldComponents = getIntPairSet("select compound, component from pubchem.compound_components",
                10000000);

        try(InputStream stream = getStream("pubchem/RDF/compound/general/pc_compound2component.ttl.gz"))
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
                    addCompoundID(compoundID);
                    addCompoundID(componentID);

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
        IntTripletSet newIngredients = new IntTripletSet(20000);
        IntTripletSet oldIngredients = getIntTripletSet(
                "select compound, ingredient_unit, ingredient_id from pubchem.compound_active_ingredients", 20000);

        try(InputStream stream = getStream("pubchem/RDF/compound/general/pc_compound2drugproduct.ttl.gz"))
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
                    addCompoundID(compoundID);

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
        IntPairSet newIsotopologues = new IntPairSet(5000000);
        IntPairSet oldIsotopologues = getIntPairSet("select compound, isotopologue from pubchem.compound_isotopologues",
                5000000);

        try(InputStream stream = getStream("pubchem/RDF/compound/general/pc_compound2isotopologue.ttl.gz"))
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
                    addCompoundID(compoundID);
                    addCompoundID(isotopologueID);

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
        IntPairSet newParents = new IntPairSet(10000000);
        IntPairSet oldParents = getIntPairSet("select compound, parent from pubchem.compound_parents", 10000000);

        try(InputStream stream = getStream("pubchem/RDF/compound/general/pc_compound2parent.ttl.gz"))
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
                    addCompoundID(compoundID);
                    addCompoundID(parentID);

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
        IntPairSet newIsomers = new IntPairSet(5000000);
        IntPairSet oldIsomers = getIntPairSet("select compound, isomer from pubchem.compound_same_connectivities",
                5000000);

        try(InputStream stream = getStream("pubchem/RDF/compound/general/pc_compound2sameconnectivity.ttl.gz"))
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
                    addCompoundID(compoundID);
                    addCompoundID(isomerID);

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
        IntPairSet newIsomers = new IntPairSet(100000000);
        IntPairSet oldIsomers = getIntPairSet("select compound, isomer from pubchem.compound_stereoisomers", 100000000);

        processFiles("pubchem/RDF/compound/general", "pc_compound2stereoisomer_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
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
                        addCompoundID(compoundID);
                        addCompoundID(isomerID);

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
        IntPairSet newRoles = new IntPairSet(20000);
        IntPairSet oldRoles = getIntPairSet("select compound, role_id from pubchem.compound_roles", 20000);

        try(InputStream stream = getStream("pubchem/RDF/compound/general/pc_compound_role.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/has-role"))
                        throw new IOException();

                    // workaround
                    if(subject.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CIDNULL"))
                        return;

                    int compoundID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                    Identifier role = Ontology.getId(object.getURI());

                    if(role.unit != Ontology.unitUncategorized)
                        throw new IOException();

                    IntIntPair pair = PrimitiveTuples.pair(compoundID, role.id);
                    addCompoundID(compoundID);

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
        IntTripletSet newTypes = new IntTripletSet(200000);
        IntTripletSet oldTypes = getIntTripletSet("select compound, type_unit, type_id from pubchem.compound_types",
                200000);

        try(InputStream stream = getStream("pubchem/RDF/compound/general/pc_compound_type.ttl.gz"))
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
                    addCompoundID(compoundID);

                    if(!oldTypes.remove(triplet))
                        newTypes.add(triplet);
                }
            }.load(stream);
        }

        batch("delete from pubchem.compound_types where compound = ? and type_unit = ? and type_id = ?", oldTypes);
        batch("insert into pubchem.compound_types(compound, type_unit, type_id) values (?,?,?)", newTypes);
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

        System.out.println();
    }


    static void finish() throws SQLException
    {
        batch("delete from pubchem.compound_bases where id = ? and "
                + "not exists (select id from molecules.pubchem where compound_bases.id = molecules.pubchem.id)",
                oldCompounds);

        batch("update pubchem.compound_bases set keep = false where id = ? and "
                + "exists (select id from molecules.pubchem where compound_bases.id = molecules.pubchem.id)",
                oldCompounds);

        batch("insert into pubchem.compound_bases(id,keep) values(?,true) on conflict (id) do update set keep=EXCLUDED.keep",
                newCompounds);

        usedCompounds = null;
        newCompounds = null;
        oldCompounds = null;
    }


    static void addCompoundID(int compoundID)
    {
        synchronized(newCompounds)
        {
            if(usedCompounds.add(compoundID) && !oldCompounds.remove(compoundID))
                newCompounds.add(compoundID);
        }
    }
}
