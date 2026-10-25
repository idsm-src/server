package cz.iocb.load.pubchem;

import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.text;
import static cz.iocb.load.common.TripleStreamProcessor.getIntFromInteger;
import static cz.iocb.load.pubchem.PubChemRDF.edam;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.sio;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.TripleDispatcher.Action;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem cooccurrences of compounds, diseases, gene symbols and enzymes in the literature from the files
 * of {@code pubchem/RDF/cooccurrence} into the tables {@code pubchem.*_cooccurrences}, one per pair of kinds.
 */
public class Cooccurrence extends Updater
{
    /**
     * Assembles the cooccurrences from their rdf:subject, rdf:object and sio:SIO_000300 triples, which the files hold
     * far apart, and passes every complete cooccurrence to the handlers. Until then the compounds and diseases are kept
     * as numbers, to save memory; a cooccurrence that never gets all its parts is ignored, as a join would ignore it.
     */
    private static class Statements
    {
        /**
         * A handler of the complete cooccurrences.
         */
        @FunctionalInterface
        static interface Handler
        {
            /**
             * Handles a complete cooccurrence.
             *
             * @param subjectIri the IRI of the subject of the cooccurrence
             * @param objectIri the IRI of the object of the cooccurrence
             * @param value the value of the cooccurrence
             * @throws IOException if the data are invalid or an I/O error occurs
             * @throws SQLException if a database error occurs
             */
            void handle(String subjectIri, String objectIri, Integer value) throws IOException, SQLException;
        }


        /**
         * IRI prefix of the cooccurrences.
         */
        private static final String statementPrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/";

        /**
         * IRI prefixes of the entities whose numbers are kept instead of their IRIs, indexed as in the encoded values.
         */
        private static final String[] numberedPrefixes = { Compound.prefix, Disease.prefix };

        /**
         * Parts of the incomplete cooccurrences (subject, object and value) by the IRIs of the cooccurrences without
         * the prefix.
         */
        private final HashMap<String, Object[]> parts = new HashMap<>();

        /**
         * Handlers of the complete cooccurrences.
         */
        private final List<Handler> handlers = new ArrayList<>();

        /**
         * Dispatcher of the triples.
         */
        private final TripleDispatcher dispatcher;


        /**
         * Creates the assembler and registers its handlers of the parts of the cooccurrences; the cooccurrences that
         * remain incomplete are reported as warnings after all the files have been read.
         *
         * @param dispatcher the dispatcher of the triples
         */
        Statements(TripleDispatcher dispatcher)
        {
            this.dispatcher = dispatcher;

            dispatcher.on(rdf + "subject", (subject, object) -> add(subject, 0, encode(object.getURI())));
            dispatcher.on(rdf + "object", (subject, object) -> add(subject, 1, encode(object.getURI())));
            dispatcher.on(sio + "SIO_000300", (subject, object) -> add(subject, 2, getIntFromInteger(object)));

            dispatcher.after(() -> {
                for(String key : parts.keySet())
                    Problems.warning("incomplete cooccurrence", key);
            });
        }


        /**
         * Registers a handler of the complete cooccurrences.
         *
         * @param handler the handler
         */
        void on(Handler handler)
        {
            handlers.add(handler);
        }


        /**
         * Registers an action to run once all the files have been read.
         *
         * @param action the action
         */
        void after(Action action)
        {
            dispatcher.after(action);
        }


        /**
         * Keeps the IRI of a compound or a disease as the index of its prefix and its number.
         *
         * @param iri the IRI of the entity
         * @return the encoded IRI, or the IRI itself if it is not one of a numbered entity
         */
        private static Object encode(String iri)
        {
            for(int i = 0; i < numberedPrefixes.length; i++)
            {
                if(iri.startsWith(numberedPrefixes[i]))
                {
                    String number = iri.substring(numberedPrefixes[i].length());

                    if(number.matches("[1-9][0-9]{0,8}"))
                        return (long) i << 32 | Integer.parseInt(number);
                }
            }

            return iri;
        }


        /**
         * Returns the IRI encoded by {@link #encode(String)}.
         *
         * @param value the encoded IRI
         * @return the IRI
         */
        private static String decode(Object value)
        {
            if(value instanceof Long number)
                return numberedPrefixes[(int) (number >> 32)] + (int) (long) number;

            return (String) value;
        }


        /**
         * Adds a part of a cooccurrence and passes the cooccurrence to the handlers once it is complete; the problems
         * of the data that a handler finds are reported.
         *
         * @param statement the cooccurrence
         * @param index the index of the part: 0 for the subject, 1 for the object and 2 for the value
         * @param value the part
         * @throws IOException if the cooccurrence already has another value of the part or an I/O error occurs
         * @throws SQLException if a database error occurs
         */
        private void add(Node statement, int index, Object value) throws IOException, SQLException
        {
            String key = statement.isURI() && statement.getURI().startsWith(statementPrefix) ?
                    statement.getURI().substring(statementPrefix.length()) : statement.toString();

            Object[] row = parts.computeIfAbsent(key, k -> new Object[3]);

            if(row[index] != null && !row[index].equals(value))
                throw new DataException("multiple values of a part of a cooccurrence", text(statement));

            row[index] = value;

            if(row[0] == null || row[1] == null || row[2] == null)
                return;

            parts.remove(key);

            for(Handler handler : handlers)
            {
                try
                {
                    handler.handle(decode(row[0]), decode(row[1]), (Integer) row[2]);
                }
                catch(DataException e)
                {
                    Problems.error(e.getKind(), e.getDetail());
                }
            }
        }
    }


    /**
     * Not instantiable.
     */
    private Cooccurrence()
    {
    }


    /**
     * Registers the checks of the predicates, types and values of the chemical-chemical cooccurrence files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void checkChemicalToChemical(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_001435");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
    }


    /**
     * Registers the checks of the predicates, types and values of the chemical-disease cooccurrence files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void checkChemicalToDisease(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_000993");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
    }


    /**
     * Registers the checks of the predicates, types and values of the disease-disease cooccurrence files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void checkDiseaseToDisease(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_001436");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
    }


    /**
     * Registers the checks of the predicates, types and values of the chemical-gene cooccurrence files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void checkChemicalToGene(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_001257");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
    }


    /**
     * Registers the checks of the predicates, types and values of the disease-gene cooccurrence files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void checkGeneToDisease(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_000983");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
    }


    /**
     * Registers the checks of the predicates, types and values of the gene-gene cooccurrence files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void checkGeneToGene(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_001437");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
    }


    /**
     * Registers the handler of the cooccurrences of compounds with compounds and the action that stores their changes
     * in {@code pubchem.compound_compound_cooccurrences}.
     *
     * @param statements the assembler of the cooccurrences
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadChemicalToChemicalValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.compound_compound_cooccurrences", oldValues);

        statements.on((subjectIri, objectIri, value) -> {
            Integer subject = Compound.getCompoundID(subjectIri);
            Integer object = Compound.getCompoundID(objectIri);

            Pair<Integer, Integer> pair = Pair.getPair(subject, object);

            if(value.equals(oldValues.remove(pair)))
            {
                keepValues.put(pair, value);
            }
            else
            {
                Integer keep = keepValues.get(pair);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + keep + ", " + value);

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + put + ", " + value);
            }
        });

        statements.after(() -> {
            store("delete from pubchem.compound_compound_cooccurrences where subject=? and object=? and value=?",
                    oldValues);
            store("insert into pubchem.compound_compound_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


    /**
     * Registers the handler of the cooccurrences of compounds with diseases and the action that stores their changes in
     * {@code pubchem.compound_disease_cooccurrences}.
     *
     * @param statements the assembler of the cooccurrences
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadChemicalToDiseaseValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.compound_disease_cooccurrences", oldValues);

        statements.on((subjectIri, objectIri, value) -> {
            // workaround
            if(!subjectIri.startsWith(Compound.prefix))
                return;

            Integer subject = Compound.getCompoundID(subjectIri);
            Integer object = Disease.getDiseaseID(objectIri);

            Pair<Integer, Integer> pair = Pair.getPair(subject, object);

            if(value.equals(oldValues.remove(pair)))
            {
                keepValues.put(pair, value);
            }
            else
            {
                Integer keep = keepValues.get(pair);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + keep + ", " + value);

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + put + ", " + value);
            }
        });

        statements.after(() -> {
            store("delete from pubchem.compound_disease_cooccurrences where subject=? and object=? and value=?",
                    oldValues);
            store("insert into pubchem.compound_disease_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


    /**
     * Registers the handler of the cooccurrences of diseases with compounds and the action that stores their changes in
     * {@code pubchem.disease_compound_cooccurrences}.
     *
     * @param statements the assembler of the cooccurrences
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadDiseaseToChemicalValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.disease_compound_cooccurrences", oldValues);

        statements.on((subjectIri, objectIri, value) -> {
            // workaround
            if(!subjectIri.startsWith(Disease.prefix))
                return;

            Integer subject = Disease.getDiseaseID(subjectIri);
            Integer object = Compound.getCompoundID(objectIri);

            Pair<Integer, Integer> pair = Pair.getPair(subject, object);

            if(value.equals(oldValues.remove(pair)))
            {
                keepValues.put(pair, value);
            }
            else
            {
                Integer keep = keepValues.get(pair);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + keep + ", " + value);

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + put + ", " + value);
            }
        });

        statements.after(() -> {
            store("delete from pubchem.disease_compound_cooccurrences where subject=? and object=? and value=?",
                    oldValues);
            store("insert into pubchem.disease_compound_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


    /**
     * Registers the handler of the cooccurrences of diseases with diseases and the action that stores their changes in
     * {@code pubchem.disease_disease_cooccurrences}.
     *
     * @param statements the assembler of the cooccurrences
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadDiseaseToDiseaseValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.disease_disease_cooccurrences", oldValues);

        statements.on((subjectIri, objectIri, value) -> {
            Integer subject = Disease.getDiseaseID(subjectIri);
            Integer object = Disease.getDiseaseID(objectIri);

            Pair<Integer, Integer> pair = Pair.getPair(subject, object);

            if(value.equals(oldValues.remove(pair)))
            {
                keepValues.put(pair, value);
            }
            else
            {
                Integer keep = keepValues.get(pair);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + keep + ", " + value);

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + put + ", " + value);
            }
        });

        statements.after(() -> {
            store("delete from pubchem.disease_disease_cooccurrences where subject=? and object=? and value=?",
                    oldValues);
            store("insert into pubchem.disease_disease_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


    /**
     * Registers the handler of the cooccurrences of compounds with gene symbols and enzymes and the action that stores
     * their changes in {@code pubchem.compound_genesymbol_cooccurrences} and
     * {@code pubchem.compound_enzyme_cooccurrences}.
     *
     * @param statements the assembler of the cooccurrences
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadChemicalToGeneValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.compound_genesymbol_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.compound_enzyme_cooccurrences", oldEnzymeValues);

        statements.on((subjectIri, objectIri, value) -> {
            // workaround
            if(!subjectIri.startsWith(Compound.prefix))
                return;

            if(objectIri.startsWith(Gene.symbolPrefix))
            {
                Integer subject = Compound.getCompoundID(subjectIri);
                Integer object = Gene.getGeneSymbolID(objectIri);

                Pair<Integer, Integer> pair = Pair.getPair(subject, object);

                if(value.equals(oldGeneValues.remove(pair)))
                {
                    keepGeneValues.put(pair, value);
                }
                else
                {
                    Integer keep = keepGeneValues.get(pair);

                    if(value.equals(keep))
                        return;
                    else if(keep != null)
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + keep + ", " + value);

                    Integer put = newGeneValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + put + ", " + value);
                }
            }
            else if(objectIri.startsWith(Protein.enzymePrefix))
            {
                Integer subject = Compound.getCompoundID(subjectIri);
                Integer object = Protein.getEnzymeID(objectIri);

                Pair<Integer, Integer> pair = Pair.getPair(subject, object);

                if(value.equals(oldEnzymeValues.remove(pair)))
                {
                    keepEnzymeValues.put(pair, value);
                }
                else
                {
                    Integer keep = keepEnzymeValues.get(pair);

                    if(value.equals(keep))
                        return;
                    else if(keep != null)
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + keep + ", " + value);

                    Integer put = newEnzymeValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + put + ", " + value);
                }
            }
            else
            {
                throw new DataException("unexpected participant of a cooccurrence", subjectIri + " " + objectIri);
            }
        });

        statements.after(() -> {
            store("delete from pubchem.compound_genesymbol_cooccurrences where subject=? and object=? and value=?",
                    oldGeneValues);
            store("insert into pubchem.compound_genesymbol_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newGeneValues);

            store("delete from pubchem.compound_enzyme_cooccurrences where subject=? and object=? and value=?",
                    oldEnzymeValues);
            store("insert into pubchem.compound_enzyme_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newEnzymeValues);
        });
    }


    /**
     * Registers the handler of the cooccurrences of diseases with gene symbols and enzymes and the action that stores
     * their changes in {@code pubchem.disease_genesymbol_cooccurrences} and
     * {@code pubchem.disease_enzyme_cooccurrences}.
     *
     * @param statements the assembler of the cooccurrences
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadDiseaseToGeneValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.disease_genesymbol_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.disease_enzyme_cooccurrences", oldEnzymeValues);

        statements.on((subjectIri, objectIri, value) -> {
            // workaround
            if(!subjectIri.startsWith(Disease.prefix))
                return;

            if(objectIri.startsWith(Gene.symbolPrefix))
            {
                Integer subject = Disease.getDiseaseID(subjectIri);
                Integer object = Gene.getGeneSymbolID(objectIri);

                Pair<Integer, Integer> pair = Pair.getPair(subject, object);

                if(value.equals(oldGeneValues.remove(pair)))
                {
                    keepGeneValues.put(pair, value);
                }
                else
                {
                    Integer keep = keepGeneValues.get(pair);

                    if(value.equals(keep))
                        return;
                    else if(keep != null)
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + keep + ", " + value);

                    Integer put = newGeneValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + put + ", " + value);
                }
            }
            else if(objectIri.startsWith(Protein.enzymePrefix))
            {
                Integer subject = Disease.getDiseaseID(subjectIri);
                Integer object = Protein.getEnzymeID(objectIri);

                Pair<Integer, Integer> pair = Pair.getPair(subject, object);

                if(value.equals(oldEnzymeValues.remove(pair)))
                {
                    keepEnzymeValues.put(pair, value);
                }
                else
                {
                    Integer keep = keepEnzymeValues.get(pair);

                    if(value.equals(keep))
                        return;
                    else if(keep != null)
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + keep + ", " + value);

                    Integer put = newEnzymeValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + put + ", " + value);
                }
            }
            else
            {
                throw new DataException("unexpected participant of a cooccurrence", subjectIri + " " + objectIri);
            }
        });

        statements.after(() -> {
            store("delete from pubchem.disease_genesymbol_cooccurrences where subject=? and object=? and value=?",
                    oldGeneValues);
            store("insert into pubchem.disease_genesymbol_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newGeneValues);

            store("delete from pubchem.disease_enzyme_cooccurrences where subject=? and object=? and value=?",
                    oldEnzymeValues);
            store("insert into pubchem.disease_enzyme_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newEnzymeValues);
        });
    }


    /**
     * Registers the handler of the cooccurrences of gene symbols and enzymes with compounds and the action that stores
     * their changes in {@code pubchem.genesymbol_compound_cooccurrences} and
     * {@code pubchem.enzyme_compound_cooccurrences}.
     *
     * @param statements the assembler of the cooccurrences
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGeneToChemicalValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.genesymbol_compound_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.enzyme_compound_cooccurrences", oldEnzymeValues);

        statements.on((subjectIri, objectIri, value) -> {
            // workaround
            if(!objectIri.startsWith(Compound.prefix))
                return;

            if(subjectIri.startsWith(Gene.symbolPrefix))
            {
                Integer subject = Gene.getGeneSymbolID(subjectIri);
                Integer object = Compound.getCompoundID(objectIri);

                Pair<Integer, Integer> pair = Pair.getPair(subject, object);

                if(value.equals(oldGeneValues.remove(pair)))
                {
                    keepGeneValues.put(pair, value);
                }
                else
                {
                    Integer keep = keepGeneValues.get(pair);

                    if(value.equals(keep))
                        return;
                    else if(keep != null)
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + keep + ", " + value);

                    Integer put = newGeneValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + put + ", " + value);
                }
            }
            else if(subjectIri.startsWith(Protein.enzymePrefix))
            {
                Integer subject = Protein.getEnzymeID(subjectIri);
                Integer object = Compound.getCompoundID(objectIri);

                Pair<Integer, Integer> pair = Pair.getPair(subject, object);

                if(value.equals(oldEnzymeValues.remove(pair)))
                {
                    keepEnzymeValues.put(pair, value);
                }
                else
                {
                    Integer keep = keepEnzymeValues.get(pair);

                    if(value.equals(keep))
                        return;
                    else if(keep != null)
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + keep + ", " + value);

                    Integer put = newEnzymeValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + put + ", " + value);
                }
            }
            else
            {
                throw new DataException("unexpected participant of a cooccurrence", subjectIri + " " + objectIri);
            }
        });

        statements.after(() -> {
            store("delete from pubchem.genesymbol_compound_cooccurrences where subject=? and object=? and value=?",
                    oldGeneValues);
            store("insert into pubchem.genesymbol_compound_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newGeneValues);

            store("delete from pubchem.enzyme_compound_cooccurrences where subject=? and object=? and value=?",
                    oldEnzymeValues);
            store("insert into pubchem.enzyme_compound_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newEnzymeValues);
        });
    }


    /**
     * Registers the handler of the cooccurrences of gene symbols and enzymes with diseases and the action that stores
     * their changes in {@code pubchem.genesymbol_disease_cooccurrences} and
     * {@code pubchem.enzyme_disease_cooccurrences}.
     *
     * @param statements the assembler of the cooccurrences
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGeneToDiseaseValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.genesymbol_disease_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.enzyme_disease_cooccurrences", oldEnzymeValues);

        statements.on((subjectIri, objectIri, value) -> {
            // workaround
            if(!objectIri.startsWith(Disease.prefix))
                return;

            if(subjectIri.startsWith(Gene.symbolPrefix))
            {
                Integer subject = Gene.getGeneSymbolID(subjectIri);
                Integer object = Disease.getDiseaseID(objectIri);

                Pair<Integer, Integer> pair = Pair.getPair(subject, object);

                if(value.equals(oldGeneValues.remove(pair)))
                {
                    keepGeneValues.put(pair, value);
                }
                else
                {
                    Integer keep = keepGeneValues.get(pair);

                    if(value.equals(keep))
                        return;
                    else if(keep != null)
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + keep + ", " + value);

                    Integer put = newGeneValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + put + ", " + value);
                }
            }
            else if(subjectIri.startsWith(Protein.enzymePrefix))
            {
                Integer subject = Protein.getEnzymeID(subjectIri);
                Integer object = Disease.getDiseaseID(objectIri);

                Pair<Integer, Integer> pair = Pair.getPair(subject, object);

                if(value.equals(oldEnzymeValues.remove(pair)))
                {
                    keepEnzymeValues.put(pair, value);
                }
                else
                {
                    Integer keep = keepEnzymeValues.get(pair);

                    if(value.equals(keep))
                        return;
                    else if(keep != null)
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + keep + ", " + value);

                    Integer put = newEnzymeValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new DataException("multiple values of a cooccurrence",
                                subjectIri + " " + objectIri + ": " + put + ", " + value);
                }
            }
            else
            {
                throw new DataException("unexpected participant of a cooccurrence", subjectIri + " " + objectIri);
            }
        });

        statements.after(() -> {
            store("delete from pubchem.genesymbol_disease_cooccurrences where subject=? and object=? and value=?",
                    oldGeneValues);
            store("insert into pubchem.genesymbol_disease_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newGeneValues);

            store("delete from pubchem.enzyme_disease_cooccurrences where subject=? and object=? and value=?",
                    oldEnzymeValues);
            store("insert into pubchem.enzyme_disease_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newEnzymeValues);
        });
    }


    /**
     * Registers the handler of the cooccurrences of gene symbols with gene symbols and the action that stores their
     * changes in {@code pubchem.genesymbol_genesymbol_cooccurrences}.
     *
     * @param statements the assembler of the cooccurrences
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGeneToGeneValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.genesymbol_genesymbol_cooccurrences", oldValues);

        statements.on((subjectIri, objectIri, value) -> {
            Integer subject = Gene.getGeneSymbolID(subjectIri);
            Integer object = Gene.getGeneSymbolID(objectIri);

            Pair<Integer, Integer> pair = Pair.getPair(subject, object);

            if(value.equals(oldValues.remove(pair)))
            {
                keepValues.put(pair, value);
            }
            else
            {
                Integer keep = keepValues.get(pair);

                if(value.equals(keep))
                    return;
                else if(keep != null)
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + keep + ", " + value);

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new DataException("multiple values of a cooccurrence",
                            subjectIri + " " + objectIri + ": " + put + ", " + value);
            }
        });

        statements.after(() -> {
            store("delete from pubchem.genesymbol_genesymbol_cooccurrences where subject=? and object=? and value=?",
                    oldValues);
            store("insert into pubchem.genesymbol_genesymbol_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


    /**
     * Loads the chemical-chemical cooccurrences from the files
     * {@code pubchem/RDF/cooccurrence/pc_cooccurrence_chemical_chemical_*.ttl.gz} into the tables.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadChemicalToChemicalCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        checkChemicalToChemical(dispatcher);

        loadChemicalToChemicalValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical_chemical_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    /**
     * Loads the chemical-disease cooccurrences from the files
     * {@code pubchem/RDF/cooccurrence/pc_cooccurrence_chemical_disease_*.ttl.gz} into the tables.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadChemicalToDiseaseCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        checkChemicalToDisease(dispatcher);

        loadChemicalToDiseaseValues(statements);
        loadDiseaseToChemicalValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical_disease_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    /**
     * Loads the disease-disease cooccurrences from the files
     * {@code pubchem/RDF/cooccurrence/pc_cooccurrence_disease_disease_*.ttl.gz} into the tables.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadDiseaseToDiseaseCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        checkDiseaseToDisease(dispatcher);

        loadDiseaseToDiseaseValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease_disease_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    /**
     * Loads the chemical-gene cooccurrences from the files
     * {@code pubchem/RDF/cooccurrence/pc_cooccurrence_chemical_gene_*.ttl.gz} into the tables.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadChemicalToGeneCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        checkChemicalToGene(dispatcher);

        loadChemicalToGeneValues(statements);
        loadGeneToChemicalValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical_gene_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    /**
     * Loads the disease-gene cooccurrences from the files
     * {@code pubchem/RDF/cooccurrence/pc_cooccurrence_disease_gene_*.ttl.gz} into the tables.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadDiseaseToGeneCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        checkGeneToDisease(dispatcher);

        loadGeneToDiseaseValues(statements);
        loadDiseaseToGeneValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease_gene_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    /**
     * Loads the gene-gene cooccurrences from the files
     * {@code pubchem/RDF/cooccurrence/pc_cooccurrence_gene_gene_*.ttl.gz} into the tables.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGeneToGeneCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        checkGeneToGene(dispatcher);

        loadGeneToGeneValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_gene_gene_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    /**
     * Loads all the cooccurrences from the files of {@code pubchem/RDF/cooccurrence} into the tables.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load cooccurrences ...");

        loadChemicalToChemicalCooccurrences();
        loadChemicalToDiseaseCooccurrences();
        loadChemicalToGeneCooccurrences();
        loadDiseaseToDiseaseCooccurrences();
        loadDiseaseToGeneCooccurrences();
        loadGeneToGeneCooccurrences();
    }
}
