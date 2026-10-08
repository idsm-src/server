package cz.iocb.load.pubchem;

import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getIntFromInteger;
import static cz.iocb.load.pubchem.PubChemRDF.edam;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.sio;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import static cz.iocb.load.pubchem.PubChemRDF.xsd;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.TripleDispatcher.Action;
import cz.iocb.load.common.Updater;



public class Cooccurrence extends Updater
{
    /*
     * Assembles the cooccurrences from their rdf:subject, rdf:object and sio:SIO_000300 triples, which the files hold
     * far apart, and passes every complete cooccurrence to the handlers. Until then the compounds and diseases are kept
     * as numbers, to save memory; a cooccurrence that never gets all its parts is ignored, as a join would ignore it.
     */
    private static class Statements
    {
        @FunctionalInterface
        static interface Handler
        {
            void handle(String subjectIri, String objectIri, Integer value) throws IOException, SQLException;
        }


        private static final String statementPrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/";
        private static final String[] numberedPrefixes = { Compound.prefix, Disease.prefix };

        private final HashMap<String, Object[]> parts = new HashMap<>();
        private final List<Handler> handlers = new ArrayList<>();
        private final TripleDispatcher dispatcher;


        Statements(TripleDispatcher dispatcher)
        {
            this.dispatcher = dispatcher;

            dispatcher.on(rdf + "subject", (subject, object) -> add(subject, 0, encode(object.getURI())));
            dispatcher.on(rdf + "object", (subject, object) -> add(subject, 1, encode(object.getURI())));
            dispatcher.on(sio + "SIO_000300", (subject, object) -> add(subject, 2, getIntFromInteger(object)));

            dispatcher.after(() -> {
                if(!parts.isEmpty())
                    System.out.println("    ignore " + parts.size() + " incomplete cooccurrences");
            });
        }


        void on(Handler handler)
        {
            handlers.add(handler);
        }


        void after(Action action)
        {
            dispatcher.after(action);
        }


        /*
         * Keeps the IRI of a compound or a disease as the index of its prefix and its number.
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


        private static String decode(Object value)
        {
            if(value instanceof Long number)
                return numberedPrefixes[(int) (number >> 32)] + (int) (long) number;

            return (String) value;
        }


        private void add(Node statement, int index, Object value) throws IOException, SQLException
        {
            String key = statement.isURI() && statement.getURI().startsWith(statementPrefix) ?
                    statement.getURI().substring(statementPrefix.length()) : statement.toString();

            Object[] row = parts.computeIfAbsent(key, k -> new Object[3]);

            if(row[index] != null && !row[index].equals(value))
                throw new IOException("multiple values of a part of cooccurrence " + statement);

            row[index] = value;

            if(row[0] == null || row[1] == null || row[2] == null)
                return;

            parts.remove(key);

            for(Handler handler : handlers)
                handler.handle(decode(row[0]), decode(row[1]), (Integer) row[2]);
        }
    }


    private static void checkChemicalToChemical(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_001435");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
        dispatcher.checkDatatype(sio + "SIO_000300", xsd + "integer");
    }


    @SuppressWarnings("unused")
    private static void checkChemicalToDisease(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_000993");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
        dispatcher.checkDatatype(sio + "SIO_000300", xsd + "integer");
    }


    private static void checkDiseaseToDisease(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_001436");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
        dispatcher.checkDatatype(sio + "SIO_000300", xsd + "integer");
    }


    @SuppressWarnings("unused")
    private static void checkChemicalToGene(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_001257");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
        dispatcher.checkDatatype(sio + "SIO_000300", xsd + "integer");
    }


    @SuppressWarnings("unused")
    private static void checkGeneToDisease(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_000983");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
        dispatcher.checkDatatype(sio + "SIO_000300", xsd + "integer");
    }


    private static void checkGeneToGene(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdf + "subject", rdf + "object", sio + "SIO_000300",
                sio + "SIO_001157");
        dispatcher.checkTypes(all(), vocab + "Cooccurrence", sio + "SIO_001437");
        dispatcher.checkValues(sio + "SIO_001157", edam + "operation_0306");
        dispatcher.checkDatatype(sio + "SIO_000300", xsd + "integer");
    }


    private static void loadChemicalToChemicalValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.chemical_chemical_cooccurrences", oldValues);

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
                    throw new IOException();

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        statements.after(() -> {
            store("delete from pubchem.chemical_chemical_cooccurrences where subject=? and object=? and value=?",
                    oldValues);
            store("insert into pubchem.chemical_chemical_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


    private static void loadChemicalToDiseaseValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.chemical_disease_cooccurrences", oldValues);

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
                    throw new IOException();

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        statements.after(() -> {
            store("delete from pubchem.chemical_disease_cooccurrences where subject=? and object=? and value=?",
                    oldValues);
            store("insert into pubchem.chemical_disease_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


    private static void loadDiseaseToChemicalValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.disease_chemical_cooccurrences", oldValues);

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
                    throw new IOException();

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        statements.after(() -> {
            store("delete from pubchem.disease_chemical_cooccurrences where subject=? and object=? and value=?",
                    oldValues);
            store("insert into pubchem.disease_chemical_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


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
                    throw new IOException();

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        statements.after(() -> {
            store("delete from pubchem.disease_disease_cooccurrences where subject=? and object=? and value=?",
                    oldValues);
            store("insert into pubchem.disease_disease_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


    private static void loadChemicalToGeneValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.chemical_gene_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.chemical_enzyme_cooccurrences", oldEnzymeValues);

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
                        throw new IOException();

                    Integer put = newGeneValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new IOException();
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
                        throw new IOException();

                    Integer put = newEnzymeValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new IOException();
                }
            }
            else
            {
                throw new IOException();
            }
        });

        statements.after(() -> {
            store("delete from pubchem.chemical_gene_cooccurrences where subject=? and object=? and value=?",
                    oldGeneValues);
            store("insert into pubchem.chemical_gene_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newGeneValues);

            store("delete from pubchem.chemical_enzyme_cooccurrences where subject=? and object=? and value=?",
                    oldEnzymeValues);
            store("insert into pubchem.chemical_enzyme_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newEnzymeValues);
        });
    }


    private static void loadDiseaseToGeneValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.disease_gene_cooccurrences", oldGeneValues);
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
                        throw new IOException();

                    Integer put = newGeneValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new IOException();
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
                        throw new IOException();

                    Integer put = newEnzymeValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new IOException();
                }
            }
            else
            {
                throw new IOException();
            }
        });

        statements.after(() -> {
            store("delete from pubchem.disease_gene_cooccurrences where subject=? and object=? and value=?",
                    oldGeneValues);
            store("insert into pubchem.disease_gene_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newGeneValues);

            store("delete from pubchem.disease_enzyme_cooccurrences where subject=? and object=? and value=?",
                    oldEnzymeValues);
            store("insert into pubchem.disease_enzyme_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newEnzymeValues);
        });
    }


    private static void loadGeneToChemicalValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.gene_chemical_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.enzyme_chemical_cooccurrences", oldEnzymeValues);

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
                        throw new IOException();

                    Integer put = newGeneValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new IOException();
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
                        throw new IOException();

                    Integer put = newEnzymeValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new IOException();
                }
            }
            else
            {
                throw new IOException();
            }
        });

        statements.after(() -> {
            store("delete from pubchem.gene_chemical_cooccurrences where subject=? and object=? and value=?",
                    oldGeneValues);
            store("insert into pubchem.gene_chemical_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newGeneValues);

            store("delete from pubchem.enzyme_chemical_cooccurrences where subject=? and object=? and value=?",
                    oldEnzymeValues);
            store("insert into pubchem.enzyme_chemical_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newEnzymeValues);
        });
    }


    private static void loadGeneToDiseaseValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.gene_disease_cooccurrences", oldGeneValues);
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
                        throw new IOException();

                    Integer put = newGeneValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new IOException();
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
                        throw new IOException();

                    Integer put = newEnzymeValues.put(pair, value);

                    if(put != null && !value.equals(put))
                        throw new IOException();
                }
            }
            else
            {
                throw new IOException();
            }
        });

        statements.after(() -> {
            store("delete from pubchem.gene_disease_cooccurrences where subject=? and object=? and value=?",
                    oldGeneValues);
            store("insert into pubchem.gene_disease_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newGeneValues);

            store("delete from pubchem.enzyme_disease_cooccurrences where subject=? and object=? and value=?",
                    oldEnzymeValues);
            store("insert into pubchem.enzyme_disease_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newEnzymeValues);
        });
    }


    private static void loadGeneToGeneValues(Statements statements) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.gene_gene_cooccurrences", oldValues);

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
                    throw new IOException();

                Integer put = newValues.put(pair, value);

                if(put != null && !value.equals(put))
                    throw new IOException();
            }
        });

        statements.after(() -> {
            store("delete from pubchem.gene_gene_cooccurrences where subject=? and object=? and value=?", oldValues);
            store("insert into pubchem.gene_gene_cooccurrences(subject,object,value) values(?,?,?) "
                    + "on conflict(subject,object) do update set value=EXCLUDED.value", newValues);
        });
    }


    private static void loadChemicalToChemicalCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        checkChemicalToChemical(dispatcher);

        loadChemicalToChemicalValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical_chemical_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    private static void loadChemicalToDiseaseCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        //checkChemicalToDisease(dispatcher);

        loadChemicalToDiseaseValues(statements);
        loadDiseaseToChemicalValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical_disease_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    private static void loadDiseaseToDiseaseCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        checkDiseaseToDisease(dispatcher);

        loadDiseaseToDiseaseValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease_disease_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    private static void loadChemicalToGeneCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        //checkChemicalToGene(dispatcher);

        loadChemicalToGeneValues(statements);
        loadGeneToChemicalValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical_gene_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    private static void loadDiseaseToGeneCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        //checkGeneToDisease(dispatcher);

        loadGeneToDiseaseValues(statements);
        loadDiseaseToGeneValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease_gene_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


    private static void loadGeneToGeneCooccurrences() throws IOException, SQLException
    {
        TripleDispatcher dispatcher = new TripleDispatcher();
        Statements statements = new Statements(dispatcher);

        checkGeneToGene(dispatcher);

        loadGeneToGeneValues(statements);

        dispatcher.load("pubchem/RDF/cooccurrence", "pc_cooccurrence_gene_gene_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();
    }


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
