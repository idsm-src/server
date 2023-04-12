package cz.iocb.load.chebi;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



public class ChEBI extends Updater
{
    private static final class Restriction
    {
        final int chebiID;
        final int valueRestrictionID;
        final short propertyUnit;
        final int propertyID;

        public Restriction(int chebiID, int valueRestrictionID, short propertyUnit, int propertyID)
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
        final short propertyUnit;
        final int propertyID;
        final String target;
        final Integer typeID;
        final String reference;
        final String source;

        public Axiom(int chebiID, short propertyUnit, int propertyID, String target, Integer typeID, String reference,
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


    private static int nextRestrictionID;
    private static int nextAxiomID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        IntHashSet newValues = new IntHashSet();
        IntHashSet oldValues = getIntSet("select id from chebi.classes");

        new QueryResultProcessor(patternQuery("?chebi rdf:type owl:Class. "
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");

                if(!oldValues.remove(chebiID))
                    newValues.add(chebiID);
            }
        }.load(model);

        batch("delete from chebi.classes where id = ?", oldValues);
        batch("insert into chebi.classes(id) values(?)", newValues);
    }


    private static void loadParents(Model model) throws IOException, SQLException
    {
        IntPairSet newValues = new IntPairSet();
        IntPairSet oldValues = getIntPairSet("select chebi, parent from chebi.parents");

        new QueryResultProcessor(patternQuery("?chebi rdfs:subClassOf ?parent."
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"
                + "filter(strstarts(str(?parent), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");
                int parentID = getIntID("parent", "http://purl.obolibrary.org/obo/CHEBI_");

                IntIntPair pair = PrimitiveTuples.pair(chebiID, parentID);

                if(!oldValues.remove(pair))
                    newValues.add(pair);
            }
        }.load(model);

        batch("delete from chebi.parents where chebi = ? and parent = ?", oldValues);
        batch("insert into chebi.parents(chebi, parent) values(?,?)", newValues);
    }


    private static void loadStars(Model model) throws IOException, SQLException
    {
        IntIntHashMap newValues = new IntIntHashMap();
        IntIntHashMap oldValues = getIntIntMap("select chebi, star from chebi.stars");

        new QueryResultProcessor(patternQuery("?chebi oboInOwl:inSubset ?star"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");
                int star = Integer.parseInt(
                        getStringID("star", "http://purl.obolibrary.org/obo/chebi#").replaceFirst("_STAR", ""));

                if(star != oldValues.removeKeyIfAbsent(chebiID, NO_VALUE))
                    newValues.put(chebiID, star);
            }
        }.load(model);

        batch("delete from chebi.stars where chebi = ?", oldValues.keySet());
        batch("insert into chebi.stars(chebi, star) values(?,?) "
                + "on conflict (chebi) do update set star=EXCLUDED.star", newValues);
    }


    private static void loadReplacements(Model model) throws IOException, SQLException
    {
        IntIntHashMap newValues = new IntIntHashMap();
        IntIntHashMap oldValues = getIntIntMap("select chebi, replacement from chebi.replacements");

        new QueryResultProcessor(patternQuery("?chebi obo:IAO_0100001 ?replacement"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");
                int replacementID = getIntID("replacement", "http://purl.obolibrary.org/obo/CHEBI_");

                if(replacementID != oldValues.removeKeyIfAbsent(chebiID, NO_VALUE))
                    newValues.put(chebiID, replacementID);
            }
        }.load(model);

        batch("delete from chebi.replacements where chebi = ?", oldValues.keySet());
        batch("insert into chebi.replacements(chebi, replacement) values(?,?) "
                + "on conflict (chebi) do update set replacement=EXCLUDED.replacement", newValues);
    }


    private static void loadObsolescenceReasons(Model model) throws IOException, SQLException
    {
        IntIntHashMap newValues = new IntIntHashMap();
        IntIntHashMap oldValues = getIntIntMap("select chebi, reason from chebi.obsolescence_reasons");

        new QueryResultProcessor(patternQuery("?chebi obo:IAO_0000231 ?reason"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");
                int reasonID = getIntID("reason", "http://purl.obolibrary.org/obo/IAO_");

                if(reasonID != oldValues.removeKeyIfAbsent(chebiID, NO_VALUE))
                    newValues.put(chebiID, reasonID);
            }
        }.load(model);

        batch("delete from chebi.obsolescence_reasons where chebi = ?", oldValues.keySet());
        batch("insert into chebi.obsolescence_reasons(chebi, reason) values(?,?) "
                + "on conflict (chebi) do update set reason=EXCLUDED.reason", newValues);
    }


    private static void loadRestrictions(Model model) throws IOException, SQLException
    {
        nextRestrictionID = getIntValue("select coalesce(max(id)+1,0) from chebi.restrictions");

        HashSet<Restriction> newValues = new HashSet<Restriction>();
        ObjectIntHashMap<Restriction> oldValues = getObjectIntMap(
                "select id, chebi, value_restriction, property_unit, property_id from chebi.restrictions",
                r -> new Restriction(r.getInt(2), r.getInt(3), r.getShort(4), r.getInt(5)));

        new QueryResultProcessor(patternQuery("?chebi rdfs:subClassOf [ rdf:type owl:Restriction; "
                + "owl:onProperty ?property; owl:someValuesFrom ?values ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");
                int valueRestrictionID = getIntID("values", "http://purl.obolibrary.org/obo/CHEBI_");
                Identifier property = Ontology.getId(getIRI("property"));

                Restriction restriction = new Restriction(chebiID, valueRestrictionID, property.unit, property.id);

                if(oldValues.removeKeyIfAbsent(restriction, NO_VALUE) == NO_VALUE)
                    newValues.add(restriction);
            }
        }.load(model);

        batch("delete from chebi.restrictions where id = ?", oldValues.values());
        batch("insert into chebi.restrictions(id, chebi, value_restriction, property_unit, property_id) values (?,?,?,?,?)",
                newValues, (PreparedStatement statement, Restriction restriction) -> {
                    statement.setInt(1, nextRestrictionID++);
                    statement.setInt(2, restriction.chebiID);
                    statement.setInt(3, restriction.valueRestrictionID);
                    statement.setShort(4, restriction.propertyUnit);
                    statement.setInt(5, restriction.propertyID);
                });
    }


    private static void loadAxioms(Model model) throws IOException, SQLException
    {
        nextAxiomID = getIntValue("select coalesce(max(id)+1,0) from chebi.axioms");

        HashSet<Axiom> newValues = new HashSet<Axiom>();
        ObjectIntHashMap<Axiom> oldValues = getObjectIntMap(
                "select id, chebi, property_unit, property_id, target, type_id, reference, source from chebi.axioms",
                r -> new Axiom(r.getInt(2), r.getShort(3), r.getInt(4), r.getString(5), (Integer) r.getObject(6),
                        r.getString(7), r.getString(8)));

        new QueryResultProcessor(patternQuery("?axiom rdf:type owl:Axiom; owl:annotatedProperty ?property;"
                + "owl:annotatedSource ?chebi; owl:annotatedTarget ?target."
                + "optional { ?axiom oboInOwl:hasSynonymType ?type } optional { ?axiom oboInOwl:hasDbXref ?reference }"
                + "optional { ?axiom oboInOwl:source ?source }"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");
                Identifier property = Ontology.getId(getIRI("property"));
                String target = getString("target");
                Identifier type = Ontology.getId(getIRI("type"));
                String reference = getString("reference");
                String source = getString("source");

                if(getIRI("type") != null && type == null || type != null && type.unit != Ontology.unitUncategorized)
                    throw new IOException();

                Axiom axiom = new Axiom(chebiID, property.unit, property.id, target, type == null ? null : type.id,
                        reference, source);

                if(oldValues.removeKeyIfAbsent(axiom, NO_VALUE) == NO_VALUE)
                    newValues.add(axiom);
            }
        }.load(model);

        batch("delete from chebi.axioms where id = ?", oldValues.values());
        batch("insert into chebi.axioms(id, chebi, property_unit, property_id, target, type_id, reference, source) values (?,?,?,?,?,?,?,?)",
                newValues, (PreparedStatement statement, Axiom axiom) -> {
                    statement.setInt(1, nextAxiomID++);
                    statement.setInt(2, axiom.chebiID);
                    statement.setShort(3, axiom.propertyUnit);
                    statement.setInt(4, axiom.propertyID);
                    statement.setString(5, axiom.target);
                    statement.setObject(6, axiom.typeID);
                    statement.setString(7, axiom.reference);
                    statement.setString(8, axiom.source);
                });
    }


    private static void loadMultiStringValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        IntStringPairSet newValues = new IntStringPairSet();
        IntStringPairSet oldValues = getIntStringPairSet("select chebi, " + column + " from chebi." + table);

        new QueryResultProcessor(patternQuery("?chebi " + property + " ?value."
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");
                String value = getString("value");
                IntObjectPair<String> pair = PrimitiveTuples.pair(chebiID, value);

                if(!oldValues.remove(pair))
                    newValues.add(pair);
            }
        }.load(model);

        batch("delete from chebi." + table + " where chebi = ? and " + column + " = ?", oldValues);
        batch("insert into chebi." + table + "(chebi, " + column + ") values(?,?)", newValues);
    }


    private static void loadStringValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        IntStringMap newValues = new IntStringMap();
        IntStringMap oldValues = getIntStringMap("select chebi, " + column + " from chebi." + table);

        new QueryResultProcessor(patternQuery("?chebi " + property + " ?value."
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");
                String value = getString("value");

                if(!value.equals(oldValues.remove(chebiID)))
                    newValues.put(chebiID, value);
            }
        }.load(model);

        batch("delete from chebi." + table + " where chebi = ? and " + column + " = ?", oldValues);
        batch("insert into chebi." + table + "(chebi, " + column + ") values(?,?) on conflict (chebi) do update set "
                + column + "=EXCLUDED." + column, newValues);
    }


    private static void loadBooleanValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        IntIntHashMap newValues = new IntIntHashMap();
        IntIntHashMap oldValues = getIntIntMap("select chebi, " + column + "::integer from chebi." + table);

        new QueryResultProcessor(patternQuery("?chebi " + property + " ?value."
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getIntID("chebi", "http://purl.obolibrary.org/obo/CHEBI_");
                int value = getBoolean("value") ? 1 : 0;

                if(value != oldValues.removeKeyIfAbsent(chebiID, NO_VALUE))
                    newValues.put(chebiID, value);
            }
        }.load(model);

        batch("delete from chebi." + table + " where chebi = ?", oldValues.keySet());
        batch("insert into chebi." + table + "(chebi, " + column + ") values(?,cast(? as boolean)) "
                + "on conflict (chebi) do update set " + column + "=EXCLUDED." + column, newValues);
    }


    static void load() throws IOException, SQLException
    {
        Model model = getModel("chebi/chebi.owl", null);

        loadBases(model);

        loadParents(model);
        loadStars(model);
        loadReplacements(model);
        loadObsolescenceReasons(model);
        loadRestrictions(model);
        loadAxioms(model);

        loadMultiStringValues(model, "oboInOwl:hasDbXref", "references", "reference");
        loadMultiStringValues(model, "oboInOwl:hasRelatedSynonym", "related_synonyms", "synonym");
        loadMultiStringValues(model, "oboInOwl:hasExactSynonym", "exact_synonyms", "synonym");
        loadMultiStringValues(model, "chebi:formula", "formulas", "formula");
        loadMultiStringValues(model, "chebi:mass", "masses", "mass");
        loadMultiStringValues(model, "chebi:monoisotopicmass", "monoisotopic_masses", "mass");
        loadMultiStringValues(model, "oboInOwl:hasAlternativeId", "alternative_identifiers", "identifier");
        loadStringValues(model, "rdfs:label", "labels", "label");
        loadStringValues(model, "oboInOwl:id", "identifiers", "identifier");
        loadStringValues(model, "oboInOwl:hasOBONamespace", "namespaces", "namespace");
        loadStringValues(model, "chebi:charge", "charges", "charge");
        loadStringValues(model, "chebi:smiles", "smiles_codes", "smiles");
        loadStringValues(model, "chebi:inchikey", "inchikeys", "inchikey");
        loadStringValues(model, "chebi:inchi", "inchies", "inchi");
        loadStringValues(model, "obo:IAO_0000115", "definitions", "definition");
        loadBooleanValues(model, "owl:deprecated", "deprecated_flags", "flag");
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();
            Ontology.loadCategories();
            load();
            commit();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
