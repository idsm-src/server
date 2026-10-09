package cz.iocb.load.chebi;

import static cz.iocb.load.common.TripleDispatcher.except;
import static cz.iocb.load.common.TripleDispatcher.is;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleStreamProcessor.getBoolean;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getLexicalForm;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.AsyncParser;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource;
import cz.iocb.load.common.BlankNodes;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.SdfReader;
import cz.iocb.load.common.StructureTable;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class ChEBI extends Updater
{
    private static final class Restriction
    {
        final int chebiID;
        final int valueRestrictionID;
        final int propertyUnit;
        final int propertyID;

        public Restriction(int chebiID, int valueRestrictionID, int propertyUnit, int propertyID)
        {
            this.chebiID = chebiID;
            this.valueRestrictionID = valueRestrictionID;
            this.propertyUnit = propertyUnit;
            this.propertyID = propertyID;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(!(obj instanceof Restriction))
                return false;

            Restriction restriction = (Restriction) obj;

            if(chebiID != restriction.chebiID)
                return false;

            if(propertyUnit != restriction.propertyUnit)
                return false;

            if(propertyID != restriction.propertyID)
                return false;

            if(valueRestrictionID != restriction.valueRestrictionID)
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            return Integer.hashCode(chebiID) ^ Integer.hashCode(valueRestrictionID);
        }
    }


    private static final class Axiom
    {
        final int chebiID;
        final int propertyUnit;
        final int propertyID;
        final String target;
        final Integer typeID;
        final String reference;
        final String source;

        public Axiom(int chebiID, int propertyUnit, int propertyID, String target, Integer typeID, String reference,
                String source)
        {
            this.chebiID = chebiID;
            this.propertyUnit = propertyUnit;
            this.propertyID = propertyID;
            this.target = target;
            this.typeID = typeID;
            this.reference = reference;
            this.source = source;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(!(obj instanceof Axiom))
                return false;

            Axiom axiom = (Axiom) obj;

            if(chebiID != axiom.chebiID)
                return false;

            if(propertyUnit != axiom.propertyUnit)
                return false;

            if(propertyID != axiom.propertyID)
                return false;

            if(!target.equals(axiom.target))
                return false;

            if(typeID != axiom.typeID && (typeID == null || !typeID.equals(axiom.typeID)))
                return false;

            if(reference != axiom.reference && (reference == null || !reference.equals(axiom.reference)))
                return false;

            if(source != axiom.source && (source == null || !source.equals(axiom.source)))
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            return Integer.hashCode(chebiID) ^ target.hashCode();
        }
    }


    @SuppressWarnings("serial")
    public static class RestrictionIntMap extends SqlMap<Restriction, Integer>
    {
        @Override
        public Restriction getKey(ResultSet result) throws SQLException
        {
            return new Restriction(result.getInt(1), result.getInt(2), result.getShort(3), result.getInt(4));
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(5);
        }

        @Override
        public void set(PreparedStatement statement, Restriction key, Integer value) throws SQLException
        {
            statement.setInt(1, key.chebiID);
            statement.setInt(2, key.valueRestrictionID);
            statement.setInt(3, key.propertyUnit);
            statement.setInt(4, key.propertyID);
            statement.setInt(5, value);
        }
    }


    @SuppressWarnings("serial")
    public static class AxiomIntMap extends SqlMap<Axiom, Integer>
    {
        @Override
        public Axiom getKey(ResultSet result) throws SQLException
        {
            return new Axiom(result.getInt(1), result.getShort(2), result.getInt(3), result.getString(4),
                    (Integer) result.getObject(5), result.getString(6), result.getString(7));
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(8);
        }

        @Override
        public void set(PreparedStatement statement, Axiom key, Integer value) throws SQLException
        {
            statement.setInt(1, key.chebiID);
            statement.setInt(2, key.propertyUnit);
            statement.setInt(3, key.propertyID);
            statement.setString(4, key.target);
            statement.setObject(5, key.typeID);
            statement.setString(6, key.reference);
            statement.setString(7, key.source);
            statement.setInt(8, value);
        }
    }


    static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
    static final String owl = "http://www.w3.org/2002/07/owl#";
    static final String obo = "http://purl.obolibrary.org/obo/";
    static final String oboInOwl = "http://www.geneontology.org/formats/oboInOwl#";
    static final String chemrof = "https://w3id.org/chemrof/";

    static final String prefix = "http://purl.obolibrary.org/obo/CHEBI_";
    static final int prefixLength = prefix.length();

    private static final String file = "chebi/chebi.owl";

    private static final IntSet keepEntities = new IntSet();
    private static final IntSet newEntities = new IntSet();
    private static final IntSet oldEntities = new IntSet();
    private static final MissingEntities<Integer> missingEntities = new MissingEntities<>("entity", true);


    /*
     * Returns the version of the ontology from its version IRI, which the header of the file states, so that the file
     * is read only up to it.
     */
    private static String getVersion() throws IOException
    {
        try(InputStream input = new FileInputStream(baseDirectory + file))
        {
            IteratorCloseable<Triple> triples = AsyncParser.asyncParseTriples(input, Lang.RDFXML, null);

            try
            {
                while(triples.hasNext())
                {
                    Triple triple = triples.next();

                    if(!is(triple.getSubject(), obo + "chebi.owl")
                            || !triple.getPredicate().getURI().equals(owl + "versionIRI"))
                        continue;

                    String iri = triple.getObject().getURI();

                    if(!iri.matches("http://purl\\.obolibrary\\.org/obo/chebi/[^/]+/chebi\\.owl"))
                        throw new IOException();

                    return iri.replaceFirst("^http://purl\\.obolibrary\\.org/obo/chebi/([^/]+)/chebi\\.owl$", "$1");
                }
            }
            finally
            {
                triples.close();
            }
        }

        throw new IOException("unknown version of the ontology");
    }


    private static void check(TripleDispatcher dispatcher)
    {
        // the subjects other than the ontology and the declarations of its properties, subsets and synonym types
        Predicate<Node> subjects = except(obo + "chebi.owl", obo + "chebi/BRAND_NAME", obo + "chebi/INN",
                obo + "chebi/IUPAC_NAME", obo + "chebi/1_STAR", obo + "chebi/2_STAR", obo + "chebi/3_STAR",
                chemrof + "charge", chemrof + "generalized_empirical_formula", chemrof + "inchi_key_string",
                chemrof + "inchi_string", chemrof + "mass", chemrof + "monoisotopic_mass", chemrof + "smiles_string",
                chemrof + "wurcs_representation", oboInOwl + "SubsetProperty", oboInOwl + "SynonymTypeProperty",
                oboInOwl + "date", oboInOwl + "default-namespace", oboInOwl + "hasAlternativeId",
                oboInOwl + "hasDbXref", oboInOwl + "hasExactSynonym", oboInOwl + "hasOBOFormatVersion",
                oboInOwl + "hasOBONamespace", oboInOwl + "hasRelatedSynonym", oboInOwl + "hasSynonymType",
                oboInOwl + "id", oboInOwl + "inSubset", oboInOwl + "is_cyclic", oboInOwl + "is_transitive",
                oboInOwl + "saved-by", oboInOwl + "shorthand", oboInOwl + "source", obo + "IAO_0000115",
                obo + "IAO_0000231", obo + "IAO_0100001", obo + "BFO_0000051", obo + "RO_0000087", obo + "RO_0018033",
                obo + "RO_0018034", obo + "RO_0018036", obo + "RO_0018037", obo + "RO_0018038", obo + "RO_0018039",
                obo + "RO_0018040", rdfs + "comment", rdfs + "label", owl + "deprecated");

        dispatcher.checkPredicates(subjects, rdf + "type", rdfs + "subClassOf", oboInOwl + "inSubset",
                obo + "IAO_0100001", obo + "IAO_0000231", owl + "onProperty", owl + "someValuesFrom",
                owl + "annotatedProperty", owl + "annotatedSource", owl + "annotatedTarget",
                oboInOwl + "hasSynonymType", oboInOwl + "hasDbXref", oboInOwl + "source",
                oboInOwl + "hasRelatedSynonym", oboInOwl + "hasExactSynonym", oboInOwl + "hasAlternativeId",
                chemrof + "charge", chemrof + "generalized_empirical_formula", chemrof + "inchi_key_string",
                chemrof + "inchi_string", chemrof + "mass", chemrof + "monoisotopic_mass", chemrof + "smiles_string",
                chemrof + "wurcs_representation", rdfs + "label", oboInOwl + "id", oboInOwl + "hasOBONamespace",
                obo + "IAO_0000115", owl + "deprecated");
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        load("select id from chebi.classes", oldEntities);

        dispatcher.onType(owl + "Class", (subject, object) -> {
            if(!startsWith(subject, prefix))
                return;

            Integer chebiID = getIntID(subject, prefix);

            addEntity(chebiID);
            missingEntities.described(chebiID);
        });
    }


    private static void loadParents(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepParents = new IntPairSet();
        IntPairSet newParents = new IntPairSet();
        IntPairSet oldParents = new IntPairSet();

        load("select class,parent from chebi.class_parents", oldParents);

        dispatcher.on(rdfs + "subClassOf", (subject, object) -> {
            if(!startsWith(subject, prefix) || !startsWith(object, prefix))
                return;

            int chebiID = getEntityID(subject.getURI());
            int parentID = getEntityID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(chebiID, parentID);

            if(oldParents.remove(pair))
                keepParents.add(pair);
            else if(!keepParents.contains(pair))
                newParents.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from chebi.class_parents where class=? and parent=?", oldParents);
            store("insert into chebi.class_parents(class,parent) values(?,?)", newParents);
        });
    }


    private static void loadStars(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntMap keepStars = new IntIntMap();
        IntIntMap newStars = new IntIntMap();
        IntIntMap oldStars = new IntIntMap();

        load("select class,star_id from chebi.class_stars", oldStars);

        dispatcher.on(oboInOwl + "inSubset", (subject, object) -> {
            int chebiID = getEntityID(subject.getURI());
            Integer star = Integer.parseInt(getStringID(object, obo + "chebi/").replaceFirst("_STAR", ""));

            if(star.equals(oldStars.remove(chebiID)))
            {
                keepStars.put(chebiID, star);
            }
            else
            {
                Integer keep = keepStars.get(chebiID);

                if(star.equals(keep))
                    return;
                else if(keep != null)
                    throw new IOException();

                Integer put = newStars.put(chebiID, star);

                if(put != null && !star.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi.class_stars where class=? and star_id=?", oldStars);
            store("insert into chebi.class_stars(class,star_id) values(?,?) "
                    + "on conflict(class) do update set star_id=EXCLUDED.star_id", newStars);
        });
    }


    private static void loadReplacements(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntMap keepReplacements = new IntIntMap();
        IntIntMap newReplacements = new IntIntMap();
        IntIntMap oldReplacements = new IntIntMap();

        load("select class,replacement from chebi.class_replacements", oldReplacements);

        dispatcher.on(obo + "IAO_0100001", (subject, object) -> {
            int chebiID = getEntityID(subject.getURI());
            Integer replacementID = getIntID(object, prefix);

            if(replacementID.equals(oldReplacements.remove(chebiID)))
            {
                keepReplacements.put(chebiID, replacementID);
            }
            else
            {
                Integer keep = keepReplacements.get(chebiID);

                if(replacementID.equals(keep))
                    return;
                else if(keep != null)
                    throw new IOException();

                Integer put = newReplacements.put(chebiID, replacementID);

                if(put != null && !replacementID.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi.class_replacements where class=? and replacement=?", oldReplacements);
            store("insert into chebi.class_replacements(class,replacement) values(?,?) "
                    + "on conflict(class) do update set replacement=EXCLUDED.replacement", newReplacements);
        });
    }


    private static void loadObsolescenceReasons(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntMap keepReasons = new IntIntMap();
        IntIntMap newReasons = new IntIntMap();
        IntIntMap oldReasons = new IntIntMap();

        load("select class,reason_id from chebi.class_obsolescence_reasons", oldReasons);

        dispatcher.on(obo + "IAO_0000231", (subject, object) -> {
            int chebiID = getEntityID(subject.getURI());
            Integer reasonID = getIntID(object, obo + "IAO_");

            if(reasonID.equals(oldReasons.remove(chebiID)))
            {
                keepReasons.put(chebiID, reasonID);
            }
            else
            {
                Integer keep = keepReasons.get(chebiID);

                if(reasonID.equals(keep))
                    return;
                else if(keep != null)
                    throw new IOException();

                Integer put = newReasons.put(chebiID, reasonID);

                if(put != null && !reasonID.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi.class_obsolescence_reasons where class=? and reason_id=?", oldReasons);
            store("insert into chebi.class_obsolescence_reasons(class,reason_id) values(?,?) "
                    + "on conflict(class) do update set reason_id=EXCLUDED.reason_id", newReasons);
        });
    }


    /*
     * Loads the existential restrictions that are superclasses of the entities. A restriction is a blank node, so its
     * triples are collected and the restrictions are assembled once the file has been read.
     */
    private static void loadRestrictions(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        HashSet<Restriction> keepRestrictions = new HashSet<>();
        RestrictionIntMap newRestrictions = new RestrictionIntMap();
        RestrictionIntMap oldRestrictions = new RestrictionIntMap();

        load("select class,value_restriction,property_unit,property_id,id from chebi.restrictions", oldRestrictions);

        HashSet<Node> restrictions = new HashSet<>();
        BlankNodes parts = new BlankNodes(dispatcher, owl + "onProperty", owl + "someValuesFrom");
        List<Pair<Node, Node>> superclasses = new ArrayList<>();

        dispatcher.onType(owl + "Restriction", (subject, object) -> {
            if(!subject.isBlank())
                throw new IOException("unexpected restriction " + subject);

            restrictions.add(subject);
        });

        dispatcher.on(rdfs + "subClassOf", (subject, object) -> {
            if(object.isBlank())
                superclasses.add(Pair.getPair(subject, object));
        });

        dispatcher.after(() -> {
            int nextRestrictionID = oldRestrictions.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

            for(Pair<Node, Node> superclass : superclasses)
            {
                Node node = superclass.getTwo();

                if(!restrictions.contains(node))
                    continue;

                for(Node property : parts.values(node, owl + "onProperty"))
                {
                    for(Node values : parts.values(node, owl + "someValuesFrom"))
                    {
                        int chebiID = getEntityID(superclass.getOne().getURI());
                        int valueRestrictionID = getEntityID(values.getURI());

                        Pair<Integer, Integer> propertyID = Ontology.getId(property.getURI());

                        Restriction restriction = new Restriction(chebiID, valueRestrictionID, propertyID.getOne(),
                                propertyID.getTwo());

                        if(oldRestrictions.remove(restriction) != null)
                            keepRestrictions.add(restriction);
                        else if(!keepRestrictions.contains(restriction) && !newRestrictions.containsKey(restriction))
                            newRestrictions.put(restriction, nextRestrictionID++);
                    }
                }
            }

            store("delete from chebi.restrictions "
                    + "where class=? and value_restriction=? and property_unit=? and property_id=? and id=?",
                    oldRestrictions);
            store("insert into chebi.restrictions(class,value_restriction,property_unit,property_id,id) "
                    + "values(?,?,?,?,?)", newRestrictions);
        });
    }


    /*
     * Loads the annotations of the axioms about the entities. An annotated axiom is a blank node, so its triples are
     * collected and the annotations are assembled once the file has been read; the annotation of an axiom with several
     * values of a property is stored once for each combination of them.
     */
    private static void loadAxioms(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        HashSet<Axiom> keepAxioms = new HashSet<>();
        AxiomIntMap newAxioms = new AxiomIntMap();
        AxiomIntMap oldAxioms = new AxiomIntMap();

        load("select class,property_unit,property_id,target,type_id,reference,source,id from chebi.axioms", oldAxioms);

        HashSet<Node> axioms = new HashSet<>();
        BlankNodes parts = new BlankNodes(dispatcher, owl + "annotatedProperty", owl + "annotatedSource",
                owl + "annotatedTarget", oboInOwl + "hasSynonymType", oboInOwl + "hasDbXref", oboInOwl + "source");

        dispatcher.onType(owl + "Axiom", (subject, object) -> {
            if(!subject.isBlank())
                throw new IOException("unexpected axiom " + subject);

            axioms.add(subject);
        });

        dispatcher.after(() -> {
            int nextAxiomID = oldAxioms.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

            for(Node node : axioms)
            {
                for(Axiom axiom : getAxioms(parts, node))
                {
                    if(oldAxioms.remove(axiom) != null)
                        keepAxioms.add(axiom);
                    else if(!keepAxioms.contains(axiom) && !newAxioms.containsKey(axiom))
                        newAxioms.put(axiom, nextAxiomID++);
                }
            }

            store("""
                    delete from chebi.axioms where class=? and property_unit=? and property_id=? and target=? and \
                    coalesce(type_id,-1)=coalesce(?,-1) and coalesce(reference,'')=coalesce(?,'') and \
                    coalesce(source,'')=coalesce(?,'') and id=?""", oldAxioms);
            store("insert into chebi.axioms(class,property_unit,property_id,target,type_id,reference,source,id) "
                    + "values(?,?,?,?,?,?,?,?)", newAxioms);
        });
    }


    /*
     * Returns the annotations of an annotated axiom, one for each combination of the values of its properties; the
     * type, reference and source are optional.
     */
    private static List<Axiom> getAxioms(BlankNodes parts, Node node) throws IOException
    {
        List<Axiom> list = new ArrayList<>();

        for(Node property : parts.values(node, owl + "annotatedProperty"))
            for(Node chebi : parts.values(node, owl + "annotatedSource"))
                for(Node target : parts.values(node, owl + "annotatedTarget"))
                    for(Node type : parts.optionalValues(node, oboInOwl + "hasSynonymType"))
                        for(Node reference : parts.optionalValues(node, oboInOwl + "hasDbXref"))
                            for(Node source : parts.optionalValues(node, oboInOwl + "source"))
                                list.add(getAxiom(chebi, property, target, type, reference, source));

        return list;
    }


    private static Axiom getAxiom(Node chebi, Node property, Node target, Node type, Node reference, Node source)
            throws IOException
    {
        int chebiID = getEntityID(chebi.getURI());
        Pair<Integer, Integer> propertyID = Ontology.getId(property.getURI());
        Pair<Integer, Integer> typeID = type == null ? null : Ontology.getId(type.getURI());

        if(type != null && typeID == null || typeID != null && typeID.getOne() != OntologyResource.unitUncategorized)
            throw new IOException(type.getURI());

        return new Axiom(chebiID, propertyID.getOne(), propertyID.getTwo(), getLexicalForm(target),
                typeID == null ? null : typeID.getTwo(), reference == null ? null : getLexicalForm(reference),
                source == null ? null : getLexicalForm(source));
    }


    private static void loadMultiStringValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        IntStringSet keepValues = new IntStringSet();
        IntStringSet newValues = new IntStringSet();
        IntStringSet oldValues = new IntStringSet();

        load("select class," + column + " from chebi." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            if(!startsWith(subject, prefix))
                return;

            Integer chebiID = getEntityID(subject.getURI());
            String value = getLexicalForm(object);
            Pair<Integer, String> pair = Pair.getPair(chebiID, value);

            if(oldValues.remove(pair))
                keepValues.add(pair);
            else if(!keepValues.contains(pair))
                newValues.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from chebi." + table + " where class=? and " + column + "=?", oldValues);
            store("insert into chebi." + table + "(class," + column + ") values(?,?)", newValues);
        });
    }


    private static void loadStringValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        IntStringMap keepValues = new IntStringMap();
        IntStringMap newValues = new IntStringMap();
        IntStringMap oldValues = new IntStringMap();

        load("select class," + column + " from chebi." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            if(!startsWith(subject, prefix))
                return;

            int chebiID = getEntityID(subject.getURI());
            String value = getLexicalForm(object);

            if(value.equals(oldValues.remove(chebiID)))
            {
                keepValues.put(chebiID, value);
            }
            else
            {
                String keep = keepValues.get(chebiID);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new IOException();

                String put = newValues.put(chebiID, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi." + table + " where class=? and " + column + "=?", oldValues);
            store("insert into chebi." + table + "(class," + column + ") values(?,?) on conflict(class) do update set "
                    + column + "=EXCLUDED." + column, newValues);
        });
    }


    private static void loadBooleanValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        IntIntMap keepValues = new IntIntMap();
        IntIntMap newValues = new IntIntMap();
        IntIntMap oldValues = new IntIntMap();

        load("select class," + column + "::integer from chebi." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            if(!startsWith(subject, prefix))
                return;

            int chebiID = getEntityID(subject.getURI());
            Integer value = getBoolean(object) ? 1 : 0;

            if(value.equals(oldValues.remove(chebiID)))
            {
                keepValues.put(chebiID, value);
            }
            else
            {
                Integer keep = keepValues.get(chebiID);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new IOException();

                Integer put = newValues.put(chebiID, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi." + table + " where class=? and " + column + "=?::boolean", oldValues);
            store("insert into chebi." + table + "(class," + column + ") values(?,?::boolean) "
                    + "on conflict(class) do update set " + column + "=EXCLUDED." + column, newValues);
        });
    }


    private static void finish() throws IOException, SQLException
    {
        store("delete from chebi.classes where id=?", oldEntities);
        store("insert into chebi.classes(id) values(?)", newEntities);
    }


    /*
     * Ensures that the entity has a row; returns whether it has not had one yet.
     */
    private static boolean addEntity(Integer entityID)
    {
        if(newEntities.contains(entityID) || keepEntities.contains(entityID))
            return false;

        if(oldEntities.remove(entityID))
            keepEntities.add(entityID);
        else
            newEntities.add(entityID);

        return true;
    }


    private static Integer getEntityID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        return getEntityID(Integer.parseInt(value.substring(prefixLength)));
    }


    private static Integer getEntityID(Integer entityID)
    {
        if(addEntity(entityID))
            missingEntities.referenced(entityID);

        return entityID;
    }


    /*
     * Loads the structures of the entities from the SDF file of the release.
     */
    private static void loadMolfiles() throws IOException, SQLException
    {
        String name = "chebi.sdf.gz";
        StructureTable molfiles = new StructureTable("chebi.class_molfiles", "class", "molfile");
        molfiles.load();

        try(BufferedReader reader = getReader("chebi/" + name))
        {
            SdfReader.read(name, reader, "ChEBI ID", (id, molfile) -> {
                if(!id.startsWith("CHEBI:"))
                    throw new IOException("unexpected entity " + id);

                int entityID = getEntityID(Integer.parseInt(id.substring(6)));

                molfiles.put(entityID, molfile);
            });
        }

        molfiles.store();
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();
            Ontology.loadCategories();

            String version = getVersion();
            System.out.println("=== load ChEBI version " + version + " ===");
            System.out.println();

            TripleDispatcher dispatcher = new TripleDispatcher();

            check(dispatcher);

            loadBases(dispatcher);

            loadParents(dispatcher);
            loadStars(dispatcher);
            loadReplacements(dispatcher);
            loadObsolescenceReasons(dispatcher);
            loadRestrictions(dispatcher);
            loadAxioms(dispatcher);

            loadMultiStringValues(dispatcher, oboInOwl + "hasDbXref", "class_references", "reference");
            loadMultiStringValues(dispatcher, oboInOwl + "hasRelatedSynonym", "class_related_synonyms", "synonym");
            loadMultiStringValues(dispatcher, oboInOwl + "hasExactSynonym", "class_exact_synonyms", "synonym");
            loadMultiStringValues(dispatcher, chemrof + "generalized_empirical_formula", "class_formulas", "formula");
            loadMultiStringValues(dispatcher, chemrof + "mass", "class_masses", "mass");
            loadMultiStringValues(dispatcher, chemrof + "monoisotopic_mass", "class_monoisotopic_masses", "mass");
            loadMultiStringValues(dispatcher, oboInOwl + "hasAlternativeId", "class_alternative_identifiers",
                    "identifier");
            loadStringValues(dispatcher, rdfs + "label", "class_labels", "label");
            loadStringValues(dispatcher, oboInOwl + "id", "class_identifiers", "identifier");
            loadStringValues(dispatcher, oboInOwl + "hasOBONamespace", "class_namespaces", "namespace");
            loadStringValues(dispatcher, chemrof + "charge", "class_charges", "charge");
            loadStringValues(dispatcher, chemrof + "smiles_string", "class_smileses", "smiles");
            loadStringValues(dispatcher, chemrof + "inchi_key_string", "class_inchikeys", "inchikey");
            loadStringValues(dispatcher, chemrof + "inchi_string", "class_inchis", "inchi");
            loadStringValues(dispatcher, obo + "IAO_0000115", "class_definitions", "definition");
            loadStringValues(dispatcher, chemrof + "wurcs_representation", "class_wurcs_representations", "wurcs");

            loadBooleanValues(dispatcher, owl + "deprecated", "class_deprecated_flags", "flag");

            dispatcher.load(file);
            missingEntities.settle();
            dispatcher.finish();

            loadMolfiles();

            finish();
            MissingEntities.printSummary();

            syncIndex("chebi", true);

            setVersion("ChEBI Ontology", version);
            setCount("ChEBI Entities", newEntities.size() + keepEntities.size());

            updateVersion();
            commit();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
