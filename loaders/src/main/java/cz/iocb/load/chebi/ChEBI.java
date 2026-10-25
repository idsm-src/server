package cz.iocb.load.chebi;

import static cz.iocb.chemweb.server.sparql.config.chebi.Chebi.restrictionPropertyUnits;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUncategorized;
import static cz.iocb.load.common.TripleDispatcher.except;
import static cz.iocb.load.common.TripleDispatcher.is;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleDispatcher.text;
import static cz.iocb.load.common.TripleStreamProcessor.getBoolean;
import static cz.iocb.load.common.TripleStreamProcessor.getDecimal;
import static cz.iocb.load.common.TripleStreamProcessor.getIntFromInteger;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.common.TripleStreamProcessor.getText;
import java.io.BufferedReader;
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
import cz.iocb.load.common.BlankNodes;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.SdfReader;
import cz.iocb.load.common.StructureTable;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



/**
 * Loader of the ChEBI ontology from the OWL file {@code chebi/chebi.owl} and of the structures of its entities from the
 * SDF file {@code chebi/chebi.sdf.gz} into the tables of the schema {@code chebi}; the tables are updated
 * incrementally, only the rows that changed are deleted or inserted.
 */
public class ChEBI extends Updater
{
    /**
     * Existential restriction that is a superclass of an entity.
     */
    private static final class Restriction
    {
        /**
         * Id of the restricted entity.
         */
        final int chebiID;

        /**
         * Id of the entity the values of the property must come from.
         */
        final int valueRestrictionID;

        /**
         * Ontology unit of the property.
         */
        final int propertyUnit;

        /**
         * Id of the property within its unit.
         */
        final int propertyID;

        /**
         * Creates the restriction.
         *
         * @param chebiID the id of the restricted entity
         * @param valueRestrictionID the id of the entity the values of the property must come from
         * @param propertyUnit the ontology unit of the property
         * @param propertyID the id of the property within its unit
         */
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


    /**
     * Annotation of an axiom about an entity, such as the type, the reference and the source of its synonym.
     */
    private static final class Axiom
    {
        /**
         * Id of the annotated entity.
         */
        final int chebiID;

        /**
         * Id of the annotated property among the uncategorized ontology resources.
         */
        final int propertyID;

        /**
         * Value of the annotated property.
         */
        final String target;

        /**
         * Id of the synonym type among the uncategorized ontology resources, or null.
         */
        final Integer typeID;

        /**
         * Database cross-reference, or null.
         */
        final String reference;

        /**
         * Source, or null.
         */
        final String source;

        /**
         * Creates the annotation.
         *
         * @param chebiID the id of the annotated entity
         * @param propertyID the id of the annotated property among the uncategorized ontology resources
         * @param target the value of the annotated property
         * @param typeID the id of the synonym type among the uncategorized ontology resources, or null
         * @param reference the database cross-reference, or null
         * @param source the source, or null
         */
        public Axiom(int chebiID, int propertyID, String target, Integer typeID, String reference, String source)
        {
            this.chebiID = chebiID;
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


    /**
     * Restrictions with their ids, as stored in the table {@code chebi.restrictions}.
     */
    @SuppressWarnings("serial")
    public static class RestrictionIntMap extends SqlMap<Restriction, Integer>
    {
        /**
         * Creates an empty map.
         */
        public RestrictionIntMap()
        {
        }

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


    /**
     * Axiom annotations with their ids, as stored in the table {@code chebi.axioms}.
     */
    @SuppressWarnings("serial")
    public static class AxiomIntMap extends SqlMap<Axiom, Integer>
    {
        /**
         * Creates an empty map.
         */
        public AxiomIntMap()
        {
        }

        @Override
        public Axiom getKey(ResultSet result) throws SQLException
        {
            return new Axiom(result.getInt(1), result.getInt(2), result.getString(3), (Integer) result.getObject(4),
                    result.getString(5), result.getString(6));
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(7);
        }

        @Override
        public void set(PreparedStatement statement, Axiom key, Integer value) throws SQLException
        {
            statement.setInt(1, key.chebiID);
            statement.setInt(2, key.propertyID);
            statement.setString(3, key.target);
            statement.setObject(4, key.typeID);
            statement.setString(5, key.reference);
            statement.setString(6, key.source);
            statement.setInt(7, value);
        }
    }


    /**
     * Namespace of RDF.
     */
    static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /**
     * Namespace of RDF Schema.
     */
    static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";

    /**
     * Namespace of OWL.
     */
    static final String owl = "http://www.w3.org/2002/07/owl#";

    /**
     * Namespace of the OBO ontologies.
     */
    static final String obo = "http://purl.obolibrary.org/obo/";

    /**
     * Namespace of the OBO in OWL vocabulary.
     */
    static final String oboInOwl = "http://www.geneontology.org/formats/oboInOwl#";

    /**
     * Namespace of the Chemical Entity Materials and Reactions Ontological Framework.
     */
    static final String chemrof = "https://w3id.org/chemrof/";

    /**
     * Namespace of the DCMI Metadata Terms.
     */
    static final String dcterms = "http://purl.org/dc/terms/";

    /**
     * Namespace of FOAF.
     */
    static final String foaf = "http://xmlns.com/foaf/0.1/";

    /**
     * IRI prefix of the ChEBI entities.
     */
    static final String prefix = "http://purl.obolibrary.org/obo/CHEBI_";

    /**
     * OWL file of the ontology.
     */
    private static final String file = "chebi/chebi.owl";

    /**
     * Stored entities that the release still contains.
     */
    private static final IntSet keepEntities = new IntSet();

    /**
     * Entities of the release to insert into {@code chebi.classes}.
     */
    private static final IntSet newEntities = new IntSet();

    /**
     * Stored entities not met in the release (yet), deleted at the end.
     */
    private static final IntSet oldEntities = new IntSet();

    /**
     * Entities referenced but not described by the ontology.
     */
    private static final MissingEntities<Integer> missingEntities = new MissingEntities<>("entity", true);


    /**
     * Not instantiable.
     */
    private ChEBI()
    {
    }


    /**
     * Returns the version of the ontology from its version IRI, which the header of the file states, so that the file
     * is read only up to it; null stands for an unknown version.
     *
     * @return the version of the ontology, or null if it is unknown
     * @throws IOException if the file cannot be read
     */
    private static String getVersion() throws IOException
    {
        try(InputStream input = openFile(file))
        {
            if(input == null)
                return null;

            IteratorCloseable<Triple> triples = AsyncParser.asyncParseTriples(input, Lang.RDFXML, null);

            try
            {
                while(triples.hasNext())
                {
                    Triple triple = triples.next();

                    if(!is(triple.getSubject(), obo + "chebi.owl")
                            || !triple.getPredicate().getURI().equals(owl + "versionIRI"))
                        continue;

                    Node object = triple.getObject();
                    String iri = object.isURI() ? object.getURI() : null;

                    if(iri == null || !iri.matches("http://purl\\.obolibrary\\.org/obo/chebi/[^/]+/chebi\\.owl"))
                    {
                        Problems.error("unexpected version IRI", text(object));
                        return null;
                    }

                    return iri.replaceFirst("^http://purl\\.obolibrary\\.org/obo/chebi/([^/]+)/chebi\\.owl$", "$1");
                }
            }
            finally
            {
                triples.close();
            }
        }

        return null;
    }


    /**
     * Registers the checks of the predicates and types of the ontology file.
     *
     * @param dispatcher the dispatcher of the triples
     */
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
                obo + "RO_0018040", rdfs + "comment", rdfs + "label", owl + "deprecated", dcterms + "description",
                dcterms + "license", dcterms + "title", foaf + "homepage");

        dispatcher.checkPredicates(subjects, rdf + "type", rdfs + "subClassOf", oboInOwl + "inSubset",
                obo + "IAO_0100001", obo + "IAO_0000231", owl + "onProperty", owl + "someValuesFrom",
                owl + "annotatedProperty", owl + "annotatedSource", owl + "annotatedTarget",
                oboInOwl + "hasSynonymType", oboInOwl + "hasDbXref", oboInOwl + "source",
                oboInOwl + "hasRelatedSynonym", oboInOwl + "hasExactSynonym", oboInOwl + "hasAlternativeId",
                chemrof + "charge", chemrof + "generalized_empirical_formula", chemrof + "inchi_key_string",
                chemrof + "inchi_string", chemrof + "mass", chemrof + "monoisotopic_mass", chemrof + "smiles_string",
                chemrof + "wurcs_representation", rdfs + "label", oboInOwl + "id", oboInOwl + "hasOBONamespace",
                obo + "IAO_0000115", owl + "deprecated");
        dispatcher.checkTypes(subjects, owl + "Class", owl + "Restriction", owl + "Axiom");
    }


    /**
     * Loads the stored entities and registers the handler that collects the entities declared by the ontology.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler that loads the superclasses of the entities into {@code chebi.class_parents}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler that loads the star ratings of the entities into {@code chebi.class_stars}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadStars(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntMap keepStars = new IntIntMap();
        IntIntMap newStars = new IntIntMap();
        IntIntMap oldStars = new IntIntMap();

        load("select class,star_id from chebi.class_stars", oldStars);

        dispatcher.on(oboInOwl + "inSubset", (subject, object) -> {
            int chebiID = getEntityID(subject.getURI());
            String subset = getStringID(object, obo + "chebi/");

            if(!subset.matches("[1-9]_STAR"))
                throw new DataException("unexpected subset", text(object));

            Integer star = Integer.parseInt(subset.replaceFirst("_STAR", ""));

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
                    throw new DataException("multiple values of chebi.class_stars.star_id",
                            chebiID + ": " + keep + ", " + star);

                Integer put = newStars.put(chebiID, star);

                if(put != null && !star.equals(put))
                    throw new DataException("multiple values of chebi.class_stars.star_id",
                            chebiID + ": " + put + ", " + star);
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi.class_stars where class=? and star_id=?", oldStars);
            store("insert into chebi.class_stars(class,star_id) values(?,?) "
                    + "on conflict(class) do update set star_id=EXCLUDED.star_id", newStars);
        });
    }


    /**
     * Registers the handler that loads the replacements of the obsolete entities into {@code chebi.class_replacements}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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
                    throw new DataException("multiple values of chebi.class_replacements.replacement",
                            chebiID + ": " + keep + ", " + replacementID);

                Integer put = newReplacements.put(chebiID, replacementID);

                if(put != null && !replacementID.equals(put))
                    throw new DataException("multiple values of chebi.class_replacements.replacement",
                            chebiID + ": " + put + ", " + replacementID);
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi.class_replacements where class=? and replacement=?", oldReplacements);
            store("insert into chebi.class_replacements(class,replacement) values(?,?) "
                    + "on conflict(class) do update set replacement=EXCLUDED.replacement", newReplacements);
        });
    }


    /**
     * Registers the handler that loads the reasons of the obsolescence of the entities into
     * {@code chebi.class_obsolescence_reasons}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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
                    throw new DataException("multiple values of chebi.class_obsolescence_reasons.reason_id",
                            chebiID + ": " + keep + ", " + reasonID);

                Integer put = newReasons.put(chebiID, reasonID);

                if(put != null && !reasonID.equals(put))
                    throw new DataException("multiple values of chebi.class_obsolescence_reasons.reason_id",
                            chebiID + ": " + put + ", " + reasonID);
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi.class_obsolescence_reasons where class=? and reason_id=?", oldReasons);
            store("insert into chebi.class_obsolescence_reasons(class,reason_id) values(?,?) "
                    + "on conflict(class) do update set reason_id=EXCLUDED.reason_id", newReasons);
        });
    }


    /**
     * Loads the existential restrictions that are superclasses of the entities. A restriction is a blank node, so its
     * triples are collected and the restrictions are assembled once the file has been read.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
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
                throw new DataException("unexpected restriction", text(subject));

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
                        Restriction restriction;

                        try
                        {
                            int chebiID = getEntityID(text(superclass.getOne()));
                            int valueRestrictionID = getEntityID(text(values));
                            Pair<Integer, Integer> propertyID = Ontology.getResourceId(property,
                                    restrictionPropertyUnits);

                            restriction = new Restriction(chebiID, valueRestrictionID, propertyID.getOne(),
                                    propertyID.getTwo());
                        }
                        catch(DataException e)
                        {
                            Problems.error(e.getKind() + " in " + owl + "Restriction", e.getDetail());
                            continue;
                        }

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


    /**
     * Loads the annotations of the axioms about the entities. An annotated axiom is a blank node, so its triples are
     * collected and the annotations are assembled once the file has been read; the annotation of an axiom with several
     * values of a property is stored once for each combination of them.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadAxioms(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        HashSet<Axiom> keepAxioms = new HashSet<>();
        AxiomIntMap newAxioms = new AxiomIntMap();
        AxiomIntMap oldAxioms = new AxiomIntMap();

        load("select class,property_id,target,type_id,reference,source,id from chebi.axioms", oldAxioms);

        HashSet<Node> axioms = new HashSet<>();
        BlankNodes parts = new BlankNodes(dispatcher, owl + "annotatedProperty", owl + "annotatedSource",
                owl + "annotatedTarget", oboInOwl + "hasSynonymType", oboInOwl + "hasDbXref", oboInOwl + "source");

        dispatcher.onType(owl + "Axiom", (subject, object) -> {
            if(!subject.isBlank())
                throw new DataException("unexpected axiom", text(subject));

            axioms.add(subject);
        });

        dispatcher.after(() -> {
            int nextAxiomID = oldAxioms.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

            for(Node node : axioms)
            {
                List<Axiom> list;

                try
                {
                    list = getAxioms(parts, node);
                }
                catch(DataException e)
                {
                    Problems.error(e.getKind() + " in " + owl + "Axiom", e.getDetail());
                    continue;
                }

                for(Axiom axiom : list)
                {
                    if(oldAxioms.remove(axiom) != null)
                        keepAxioms.add(axiom);
                    else if(!keepAxioms.contains(axiom) && !newAxioms.containsKey(axiom))
                        newAxioms.put(axiom, nextAxiomID++);
                }
            }

            store("""
                    delete from chebi.axioms where class=? and property_id=? and target=? and \
                    coalesce(type_id,-1)=coalesce(?,-1) and coalesce(reference,'')=coalesce(?,'') and \
                    coalesce(source,'')=coalesce(?,'') and id=?""", oldAxioms);
            store("insert into chebi.axioms(class,property_id,target,type_id,reference,source,id) "
                    + "values(?,?,?,?,?,?,?)", newAxioms);
        });
    }


    /**
     * Returns the annotations of an annotated axiom, one for each combination of the values of its properties; the
     * type, reference and source are optional.
     *
     * @param parts the collected triples of the blank nodes
     * @param node the blank node of the axiom
     * @return the annotations of the axiom
     * @throws IOException if a part of the axiom is missing or invalid
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


    /**
     * Creates the annotation of an axiom from the values of its properties.
     *
     * @param chebi the annotated entity
     * @param property the annotated property
     * @param target the value of the annotated property
     * @param type the synonym type, or null
     * @param reference the database cross-reference, or null
     * @param source the source, or null
     * @return the annotation
     * @throws IOException if a value is invalid
     */
    private static Axiom getAxiom(Node chebi, Node property, Node target, Node type, Node reference, Node source)
            throws IOException
    {
        int chebiID = getEntityID(text(chebi));
        Pair<Integer, Integer> propertyID = Ontology.getResourceId(property, unitUncategorized);
        Pair<Integer, Integer> typeID = type == null ? null : Ontology.getResourceId(type, unitUncategorized);

        return new Axiom(chebiID, propertyID.getTwo(), getString(target), typeID == null ? null : typeID.getTwo(),
                reference == null ? null : getString(reference), source == null ? null : getString(source));
    }


    /**
     * Registers the handler that loads the string values of a multivalued property of the entities into a table.
     *
     * @param dispatcher the dispatcher of the triples
     * @param property the IRI of the property
     * @param table the name of the table in the schema {@code chebi}
     * @param column the name of the value column
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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
            String value = getString(object);
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


    /**
     * Registers the handler that loads the string values of a single-valued property of the entities into a table.
     *
     * @param dispatcher the dispatcher of the triples
     * @param property the IRI of the property
     * @param table the name of the table in the schema {@code chebi}
     * @param column the name of the value column
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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
            String value = getString(object);

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
                    throw new DataException("multiple values of chebi." + table + "." + column,
                            chebiID + ": " + keep + ", " + value);

                String put = newValues.put(chebiID, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of chebi." + table + "." + column,
                            chebiID + ": " + put + ", " + value);
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi." + table + " where class=? and " + column + "=?", oldValues);
            store("insert into chebi." + table + "(class," + column + ") values(?,?) on conflict(class) do update set "
                    + column + "=EXCLUDED." + column, newValues);
        });
    }


    /**
     * Registers the handler that loads the decimal values of a multivalued property of the entities into a table.
     *
     * @param dispatcher the dispatcher of the triples
     * @param property the IRI of the property
     * @param table the name of the table in the schema {@code chebi}
     * @param column the name of the value column
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadMultiDecimalValues(TripleDispatcher dispatcher, String property, String table,
            String column) throws IOException, SQLException
    {
        IntStringSet keepValues = new IntStringSet();
        IntStringSet newValues = new IntStringSet();
        IntStringSet oldValues = new IntStringSet();

        load("select class," + column + "::varchar from chebi." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            if(!startsWith(subject, prefix))
                return;

            Integer chebiID = getEntityID(subject.getURI());
            String value = getText(getDecimal(object));
            Pair<Integer, String> pair = Pair.getPair(chebiID, value);

            if(oldValues.remove(pair))
                keepValues.add(pair);
            else if(!keepValues.contains(pair))
                newValues.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from chebi." + table + " where class=? and " + column + "=?::numeric", oldValues);
            store("insert into chebi." + table + "(class," + column + ") values(?,?::numeric)", newValues);
        });
    }


    /**
     * Registers the handler that loads the integer values of a single-valued property of the entities into a table.
     *
     * @param dispatcher the dispatcher of the triples
     * @param property the IRI of the property
     * @param table the name of the table in the schema {@code chebi}
     * @param column the name of the value column
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadIntegerValues(TripleDispatcher dispatcher, String property, String table, String column)
            throws IOException, SQLException
    {
        IntIntMap keepValues = new IntIntMap();
        IntIntMap newValues = new IntIntMap();
        IntIntMap oldValues = new IntIntMap();

        load("select class," + column + " from chebi." + table, oldValues);

        dispatcher.on(property, (subject, object) -> {
            if(!startsWith(subject, prefix))
                return;

            int chebiID = getEntityID(subject.getURI());
            Integer value = getIntFromInteger(object);

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
                    throw new DataException("multiple values of chebi." + table + "." + column,
                            chebiID + ": " + keep + ", " + value);

                Integer put = newValues.put(chebiID, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of chebi." + table + "." + column,
                            chebiID + ": " + put + ", " + value);
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi." + table + " where class=? and " + column + "=?", oldValues);
            store("insert into chebi." + table + "(class," + column + ") values(?,?) "
                    + "on conflict(class) do update set " + column + "=EXCLUDED." + column, newValues);
        });
    }


    /**
     * Registers the handler that loads the boolean values of a single-valued property of the entities into a table.
     *
     * @param dispatcher the dispatcher of the triples
     * @param property the IRI of the property
     * @param table the name of the table in the schema {@code chebi}
     * @param column the name of the value column
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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
                    throw new DataException("multiple values of chebi." + table + "." + column,
                            chebiID + ": " + keep + ", " + value);

                Integer put = newValues.put(chebiID, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of chebi." + table + "." + column,
                            chebiID + ": " + put + ", " + value);
            }
        });

        dispatcher.after(() -> {
            store("delete from chebi." + table + " where class=? and " + column + "=?::boolean", oldValues);
            store("insert into chebi." + table + "(class," + column + ") values(?,?::boolean) "
                    + "on conflict(class) do update set " + column + "=EXCLUDED." + column, newValues);
        });
    }


    /**
     * Stores the collected entities: deletes the entities no longer present and inserts the new ones.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void finish() throws IOException, SQLException
    {
        store("delete from chebi.classes where id=?", oldEntities);
        store("insert into chebi.classes(id) values(?)", newEntities);
    }


    /**
     * Ensures that the entity has a row; returns whether it has not had one yet.
     *
     * @param entityID the id of the entity
     * @return true if the entity has not had a row yet, false otherwise
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


    /**
     * Returns the id of a referenced entity given by its IRI and ensures that the entity has a row.
     *
     * @param value the IRI of the entity
     * @return the id of the entity
     * @throws IOException if the IRI is not an IRI of a ChEBI entity
     */
    private static Integer getEntityID(String value) throws IOException
    {
        return getEntityID(getIntID(value, prefix));
    }


    /**
     * Returns the id of a referenced entity and ensures that the entity has a row.
     *
     * @param entityID the id of the entity
     * @return the id of the entity
     */
    private static Integer getEntityID(Integer entityID)
    {
        if(addEntity(entityID))
            missingEntities.referenced(entityID);

        return entityID;
    }


    /**
     * Loads the structures of the entities from the SDF file of the release.
     *
     * @throws IOException if the file cannot be read or is invalid
     * @throws SQLException if a database error occurs
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
                    throw new DataException("unexpected entity", id);

                int entityID = getEntityID(Integer.parseInt(id.substring(6)));

                molfiles.put(entityID, molfile);
            });
        }

        molfiles.store();
    }


    /**
     * Runs the loader of ChEBI.
     *
     * @param args the command-line arguments (ignored)
     * @throws SQLException if a database error occurs
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init("chebi");
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
            loadMultiDecimalValues(dispatcher, chemrof + "mass", "class_masses", "mass");
            loadMultiDecimalValues(dispatcher, chemrof + "monoisotopic_mass", "class_monoisotopic_masses", "mass");
            loadMultiStringValues(dispatcher, oboInOwl + "hasAlternativeId", "class_alternative_identifiers",
                    "identifier");
            loadStringValues(dispatcher, rdfs + "label", "class_labels", "label");
            loadStringValues(dispatcher, oboInOwl + "id", "class_identifiers", "identifier");
            loadStringValues(dispatcher, oboInOwl + "hasOBONamespace", "class_namespaces", "namespace");
            loadStringValues(dispatcher, chemrof + "smiles_string", "class_smileses", "smiles");
            loadStringValues(dispatcher, chemrof + "inchi_key_string", "class_inchikeys", "inchikey");
            loadStringValues(dispatcher, chemrof + "inchi_string", "class_inchis", "inchi");
            loadStringValues(dispatcher, obo + "IAO_0000115", "class_definitions", "definition");
            loadStringValues(dispatcher, chemrof + "wurcs_representation", "class_wurcs_representations", "wurcs");

            loadIntegerValues(dispatcher, chemrof + "charge", "class_charges", "charge");

            loadBooleanValues(dispatcher, owl + "deprecated", "class_deprecated_flags", "flag");

            dispatcher.load(file);
            missingEntities.settle();
            dispatcher.finish();

            loadMolfiles();

            finish();

            setVersion("ChEBI Ontology", version);
            setCount("ChEBI Entities", newEntities.size() + keepEntities.size());

            updateVersion();

            checkFiles("chebi");
            MissingEntities.printSummary();
            checkProblems();

            syncIndex("chebi", true);
            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
