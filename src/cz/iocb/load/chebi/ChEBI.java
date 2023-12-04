package cz.iocb.load.chebi;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
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


    static final String prefix = "http://purl.obolibrary.org/obo/CHEBI_";
    static final int prefixLength = prefix.length();

    private static final IntSet keepEntities = new IntSet();
    private static final IntSet newEntities = new IntSet();
    private static final IntSet oldEntities = new IntSet();


    private static String getVersion(Model model) throws IOException
    {
        String query = prefixes + " select * { <http://purl.obolibrary.org/obo/chebi.owl> owl:versionIRI ?iri }";

        try(QueryExecution qexec = QueryExecutionFactory.create(query, model))
        {
            org.apache.jena.query.ResultSet results = qexec.execSelect();

            QuerySolution solution = results.nextSolution();
            String iri = solution.getResource("iri").getURI();

            if(!iri.matches("http://purl\\.obolibrary\\.org/obo/chebi/[^/]+/chebi\\.owl"))
                throw new IOException();

            return iri.replaceFirst("^http://purl\\.obolibrary\\.org/obo/chebi/([^/]+)/chebi\\.owl$", "$1");
        }
    }


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select id from chebi.classes", oldEntities);

        new QueryResultProcessor(patternQuery("?chebi rdf:type owl:Class. "
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer chebiID = getIntID("chebi", prefix);

                if(oldEntities.remove(chebiID))
                    keepEntities.add(chebiID);
                else
                    newEntities.add(chebiID);
            }
        }.load(model);
    }


    private static void loadParents(Model model) throws IOException, SQLException
    {
        IntPairSet newParents = new IntPairSet();
        IntPairSet oldParents = new IntPairSet();

        load("select chebi,parent from chebi.parents", oldParents);

        new QueryResultProcessor(patternQuery("?chebi rdfs:subClassOf ?parent."
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"
                + "filter(strstarts(str(?parent), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getEntityID(getIRI("chebi"));
                int parentID = getEntityID(getIRI("parent"));

                Pair<Integer, Integer> pair = Pair.getPair(chebiID, parentID);

                if(!oldParents.remove(pair))
                    newParents.add(pair);
            }
        }.load(model);

        store("delete from chebi.parents where chebi=? and parent=?", oldParents);
        store("insert into chebi.parents(chebi,parent) values(?,?)", newParents);
    }


    private static void loadStars(Model model) throws IOException, SQLException
    {
        IntIntMap keepStars = new IntIntMap();
        IntIntMap newStars = new IntIntMap();
        IntIntMap oldStars = new IntIntMap();

        load("select chebi,star from chebi.stars", oldStars);

        new QueryResultProcessor(patternQuery("?chebi oboInOwl:inSubset ?star"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getEntityID(getIRI("chebi"));
                Integer star = Integer.parseInt(
                        getStringID("star", "http://purl.obolibrary.org/obo/chebi#").replaceFirst("_STAR", ""));

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
            }
        }.load(model);

        store("delete from chebi.stars where chebi=? and star=?", oldStars);
        store("insert into chebi.stars(chebi,star) values(?,?) on conflict(chebi) do update set star=EXCLUDED.star",
                newStars);
    }


    private static void loadReplacements(Model model) throws IOException, SQLException
    {
        IntIntMap keepReplacements = new IntIntMap();
        IntIntMap newReplacements = new IntIntMap();
        IntIntMap oldReplacements = new IntIntMap();

        load("select chebi,replacement from chebi.replacements", oldReplacements);

        new QueryResultProcessor(patternQuery("?chebi obo:IAO_0100001 ?replacement"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getEntityID(getIRI("chebi"));
                Integer replacementID = getEntityID(getIRI("replacement"));

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
            }
        }.load(model);

        store("delete from chebi.replacements where chebi=? and replacement=?", oldReplacements);
        store("insert into chebi.replacements(chebi,replacement) values(?,?) "
                + "on conflict(chebi) do update set replacement=EXCLUDED.replacement", newReplacements);
    }


    private static void loadObsolescenceReasons(Model model) throws IOException, SQLException
    {
        IntIntMap keepReasons = new IntIntMap();
        IntIntMap newReasons = new IntIntMap();
        IntIntMap oldReasons = new IntIntMap();

        load("select chebi,reason from chebi.obsolescence_reasons", oldReasons);

        new QueryResultProcessor(patternQuery("?chebi obo:IAO_0000231 ?reason"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getEntityID(getIRI("chebi"));
                Integer reasonID = getIntID("reason", "http://purl.obolibrary.org/obo/IAO_");

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
            }
        }.load(model);

        store("delete from chebi.obsolescence_reasons where chebi=? and reason=?", oldReasons);
        store("insert into chebi.obsolescence_reasons(chebi,reason) values(?,?) "
                + "on conflict(chebi) do update set reason=EXCLUDED.reason", newReasons);
    }


    private static void loadRestrictions(Model model) throws IOException, SQLException
    {
        RestrictionIntMap newRestrictions = new RestrictionIntMap();
        RestrictionIntMap oldRestrictions = new RestrictionIntMap();

        load("select chebi,value_restriction,property_unit,property_id,id from chebi.restrictions", oldRestrictions);

        new QueryResultProcessor(patternQuery("?chebi rdfs:subClassOf [ rdf:type owl:Restriction; "
                + "owl:onProperty ?property; owl:someValuesFrom ?values ]"))
        {
            int nextRestrictionID = oldRestrictions.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

            @Override
            protected void parse() throws IOException
            {
                int chebiID = getEntityID(getIRI("chebi"));
                int valueRestrictionID = getEntityID(getIRI("values"));

                Pair<Integer, Integer> property = Ontology.getId(getIRI("property"));

                Restriction restriction = new Restriction(chebiID, valueRestrictionID, property.getOne(),
                        property.getTwo());

                if(oldRestrictions.remove(restriction) == null)
                    newRestrictions.put(restriction, nextRestrictionID++);
            }
        }.load(model);

        store("delete from chebi.restrictions "
                + "where chebi=? and value_restriction=? and property_unit=? and property_id=? and id=?",
                oldRestrictions);
        store("insert into chebi.restrictions(chebi,value_restriction,property_unit,property_id,id) values(?,?,?,?,?)",
                newRestrictions);
    }


    private static void loadAxioms(Model model) throws IOException, SQLException
    {
        AxiomIntMap newAxioms = new AxiomIntMap();
        AxiomIntMap oldAxioms = new AxiomIntMap();

        load("select chebi,property_unit,property_id,target,type_id,reference,source,id from chebi.axioms", oldAxioms);

        new QueryResultProcessor(patternQuery("?axiom rdf:type owl:Axiom; owl:annotatedProperty ?property;"
                + "owl:annotatedSource ?chebi; owl:annotatedTarget ?target."
                + "optional { ?axiom oboInOwl:hasSynonymType ?type } optional { ?axiom oboInOwl:hasDbXref ?reference }"
                + "optional { ?axiom oboInOwl:source ?source }"))
        {
            int nextAxiomID = oldAxioms.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

            @Override
            protected void parse() throws IOException
            {
                int chebiID = getEntityID(getIRI("chebi"));
                Pair<Integer, Integer> property = Ontology.getId(getIRI("property"));
                String target = getString("target");
                Pair<Integer, Integer> type = Ontology.getId(getIRI("type"));
                String reference = getString("reference");
                String source = getString("source");

                if(getIRI("type") != null && type == null
                        || type != null && type.getOne() != Ontology.unitUncategorized)
                    throw new IOException();

                Axiom axiom = new Axiom(chebiID, property.getOne(), property.getTwo(), target,
                        type == null ? null : type.getTwo(), reference, source);

                if(oldAxioms.remove(axiom) == null)
                    newAxioms.put(axiom, nextAxiomID++);
            }
        }.load(model);

        store("delete from chebi.axioms where chebi=? and property_unit=? and property_id=? and target=? and "
                + "coalesce(type_id,-1)=coalesce(?,-1) and coalesce(reference,'')=coalesce(?,'') and "
                + "coalesce(source,'')=coalesce(?,'') and id=?", oldAxioms);
        store("insert into chebi.axioms(chebi,property_unit,property_id,target,type_id,reference,source,id) "
                + "values(?,?,?,?,?,?,?,?)", newAxioms);
    }


    private static void loadMultiStringValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        IntStringSet newValues = new IntStringSet();
        IntStringSet oldValues = new IntStringSet();

        load("select chebi," + column + " from chebi." + table, oldValues);

        new QueryResultProcessor(patternQuery("?chebi " + property + " ?value."
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer chebiID = getEntityID(getIRI("chebi"));
                String value = getString("value");
                Pair<Integer, String> pair = Pair.getPair(chebiID, value);

                if(!oldValues.remove(pair))
                    newValues.add(pair);
            }
        }.load(model);

        store("delete from chebi." + table + " where chebi=? and " + column + "=?", oldValues);
        store("insert into chebi." + table + "(chebi," + column + ") values(?,?)", newValues);
    }


    private static void loadStringValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        IntStringMap keepValues = new IntStringMap();
        IntStringMap newValues = new IntStringMap();
        IntStringMap oldValues = new IntStringMap();

        load("select chebi," + column + " from chebi." + table, oldValues);

        new QueryResultProcessor(patternQuery("?chebi " + property + " ?value."
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getEntityID(getIRI("chebi"));
                String value = getString("value");

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
            }
        }.load(model);

        store("delete from chebi." + table + " where chebi=? and " + column + "=?", oldValues);
        store("insert into chebi." + table + "(chebi," + column + ") values(?,?) on conflict(chebi) do update set "
                + column + "=EXCLUDED." + column, newValues);
    }


    private static void loadBooleanValues(Model model, String property, String table, String column)
            throws IOException, SQLException
    {
        IntIntMap keepValues = new IntIntMap();
        IntIntMap newValues = new IntIntMap();
        IntIntMap oldValues = new IntIntMap();

        load("select chebi," + column + "::integer from chebi." + table, oldValues);

        new QueryResultProcessor(patternQuery("?chebi " + property + " ?value."
                + "filter(strstarts(str(?chebi), 'http://purl.obolibrary.org/obo/CHEBI_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int chebiID = getEntityID(getIRI("chebi"));
                Integer value = getBoolean("value") ? 1 : 0;

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
            }
        }.load(model);

        store("delete from chebi." + table + " where chebi=? and " + column + "=?::boolean", oldValues);
        store("insert into chebi." + table + "(chebi," + column + ") values(?,?::boolean) "
                + "on conflict(chebi) do update set " + column + "=EXCLUDED." + column, newValues);
    }


    private static void finish() throws IOException, SQLException
    {
        store("delete from chebi.classes where id=?", oldEntities);
        store("insert into chebi.classes(id) values(?)", newEntities);
    }


    private static Integer getEntityID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer entityID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newEntities)
        {
            if(!newEntities.contains(entityID) && !keepEntities.contains(entityID))
            {
                System.out.println("    add missing entity CHEBI_" + entityID);

                if(!oldEntities.remove(entityID))
                    newEntities.add(entityID);
                else
                    keepEntities.add(entityID);
            }
        }

        return entityID;
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();
            Ontology.loadCategories();

            Model model = getModel("chebi/chebi.owl", null);

            String version = getVersion(model);
            System.out.println("=== load ChEBI version " + version + " ===");
            System.out.println();

            check(model, "chebi/check.sparql");

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

            finish();

            setVersion("ChEBI Ontology", version);
            setCount("ChEBI Entities", newEntities.size() + keepEntities.size());

            model.close();
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
