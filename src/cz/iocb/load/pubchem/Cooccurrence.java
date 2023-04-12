package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Cooccurrence extends Updater
{
    private static void loadChemicalToChemicalValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = getIntPairIntMap(
                "select subject, object, value from pubchem.chemical_chemical_cooccurrences");

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                int subject = getIntID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                int object = getIntID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                int value = getInt("value");

                IntIntPair pair = PrimitiveTuples.pair(subject, object);
                Compound.addCompoundID(subject);
                Compound.addCompoundID(object);

                if(value != oldValues.removeKeyIfAbsent(pair, NO_VALUE))
                    newValues.put(pair, value);
            }
        }.load(model);

        batch("delete from pubchem.chemical_chemical_cooccurrences where subject = ? and object = ?",
                oldValues.keySet(), (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.chemical_chemical_cooccurrences(subject, object, value) values (?,?,?)", newValues);
    }


    private static void loadChemicalToDiseaseValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = getIntPairIntMap(
                "select subject, object, value from pubchem.chemical_disease_cooccurrences");

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("subject").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"))
                    return;

                int subject = getIntID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                int object = getIntID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");
                int value = getInt("value");

                IntIntPair pair = PrimitiveTuples.pair(subject, object);
                Compound.addCompoundID(subject);
                Disease.getDiseaseID(object);

                if(value != oldValues.removeKeyIfAbsent(pair, NO_VALUE))
                    newValues.put(pair, value);
            }
        }.load(model);

        batch("delete from pubchem.chemical_disease_cooccurrences where subject = ? and object = ?", oldValues.keySet(),
                (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.chemical_disease_cooccurrences(subject, object, value) values (?,?,?)", newValues);
    }


    private static void loadDiseaseToChemicalValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = getIntPairIntMap(
                "select subject, object, value from pubchem.disease_chemical_cooccurrences");

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("subject").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"))
                    return;

                int subject = getIntID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");
                int object = getIntID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                int value = getInt("value");

                IntIntPair pair = PrimitiveTuples.pair(subject, object);
                Disease.getDiseaseID(subject);
                Compound.addCompoundID(object);

                if(value != oldValues.removeKeyIfAbsent(pair, NO_VALUE))
                    newValues.put(pair, value);
            }
        }.load(model);

        batch("delete from pubchem.disease_chemical_cooccurrences where subject = ? and object = ?", oldValues.keySet(),
                (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.disease_chemical_cooccurrences(subject, object, value) values (?,?,?)", newValues);
    }


    private static void loadDiseaseToDiseaseValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = getIntPairIntMap(
                "select subject, object, value from pubchem.disease_disease_cooccurrences");

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                int subject = getIntID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");
                int object = getIntID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");
                int value = getInt("value");

                IntIntPair pair = PrimitiveTuples.pair(subject, object);
                Disease.getDiseaseID(object);
                Disease.getDiseaseID(subject);

                if(value != oldValues.removeKeyIfAbsent(pair, NO_VALUE))
                    newValues.put(pair, value);
            }
        }.load(model);

        batch("delete from pubchem.disease_disease_cooccurrences where subject = ? and object = ?", oldValues.keySet(),
                (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.disease_disease_cooccurrences(subject, object, value) values (?,?,?)", newValues);
    }


    private static void loadChemicalToGeneValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = getIntPairIntMap(
                "select subject, object, value from pubchem.chemical_gene_cooccurrences");

        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = getIntPairIntMap(
                "select subject, object, value from pubchem.chemical_enzyme_cooccurrences");

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("subject").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"))
                    return;

                if(getIRI("object").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"))
                {
                    int subject = getIntID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    int object = Gene
                            .getGeneSymbolID(getStringID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"));
                    int value = getInt("value");

                    IntIntPair pair = PrimitiveTuples.pair(subject, object);
                    Compound.addCompoundID(subject);

                    if(value != oldGeneValues.removeKeyIfAbsent(pair, NO_VALUE))
                        newGeneValues.put(pair, value);
                }
                else if(getIRI("object").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"))
                {
                    int subject = getIntID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    int object = Protein
                            .getEnzymeID(getStringID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"));
                    int value = getInt("value");

                    IntIntPair pair = PrimitiveTuples.pair(subject, object);
                    Compound.addCompoundID(subject);

                    if(value != oldEnzymeValues.removeKeyIfAbsent(pair, NO_VALUE))
                        newEnzymeValues.put(pair, value);
                }
                else
                {
                    throw new IOException();
                }
            }
        }.load(model);

        batch("delete from pubchem.chemical_gene_cooccurrences where subject = ? and object = ?",
                oldGeneValues.keySet(), (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.chemical_gene_cooccurrences(subject, object, value) values (?,?,?)", newGeneValues);

        batch("delete from pubchem.chemical_enzyme_cooccurrences where subject = ? and object = ?",
                oldEnzymeValues.keySet(), (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.chemical_enzyme_cooccurrences(subject, object, value) values (?,?,?)",
                newEnzymeValues);
    }


    private static void loadDiseaseToGeneValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = getIntPairIntMap(
                "select subject, object, value from pubchem.disease_gene_cooccurrences");

        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = getIntPairIntMap(
                "select subject, object, value from pubchem.disease_enzyme_cooccurrences");

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("subject").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"))
                    return;

                if(getIRI("object").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"))
                {
                    int subject = getIntID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");
                    int object = Gene
                            .getGeneSymbolID(getStringID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"));
                    int value = getInt("value");

                    IntIntPair pair = PrimitiveTuples.pair(subject, object);
                    Disease.getDiseaseID(subject);

                    if(value != oldGeneValues.removeKeyIfAbsent(pair, NO_VALUE))
                        newGeneValues.put(pair, value);
                }
                else if(getIRI("object").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"))
                {
                    int subject = getIntID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");
                    int object = Protein
                            .getEnzymeID(getStringID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"));
                    int value = getInt("value");

                    IntIntPair pair = PrimitiveTuples.pair(subject, object);
                    Disease.getDiseaseID(subject);

                    if(value != oldEnzymeValues.removeKeyIfAbsent(pair, NO_VALUE))
                        newEnzymeValues.put(pair, value);
                }
                else
                {
                    throw new IOException();
                }
            }
        }.load(model);


        batch("delete from pubchem.disease_gene_cooccurrences where subject = ? and object = ?", oldGeneValues.keySet(),
                (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.disease_gene_cooccurrences(subject, object, value) values (?,?,?)", newGeneValues);

        batch("delete from pubchem.disease_enzyme_cooccurrences where subject = ? and object = ?",
                oldEnzymeValues.keySet(), (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.disease_enzyme_cooccurrences(subject, object, value) values (?,?,?)",
                newEnzymeValues);
    }


    private static void loadGeneToChemicalValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = getIntPairIntMap(
                "select subject, object, value from pubchem.gene_chemical_cooccurrences");

        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = getIntPairIntMap(
                "select subject, object, value from pubchem.enzyme_chemical_cooccurrences");

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("object").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"))
                    return;

                if(getIRI("subject").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"))
                {
                    int subject = Gene
                            .getGeneSymbolID(getStringID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"));
                    int object = getIntID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    int value = getInt("value");

                    IntIntPair pair = PrimitiveTuples.pair(subject, object);
                    Compound.addCompoundID(object);

                    if(value != oldGeneValues.removeKeyIfAbsent(pair, NO_VALUE))
                        newGeneValues.put(pair, value);
                }
                else if(getIRI("subject").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"))
                {
                    int subject = Protein
                            .getEnzymeID(getStringID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"));
                    int object = getIntID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");
                    int value = getInt("value");

                    IntIntPair pair = PrimitiveTuples.pair(subject, object);
                    Compound.addCompoundID(object);

                    if(value != oldEnzymeValues.removeKeyIfAbsent(pair, NO_VALUE))
                        newEnzymeValues.put(pair, value);
                }
                else
                {
                    throw new IOException();
                }
            }
        }.load(model);

        batch("delete from pubchem.gene_chemical_cooccurrences where subject = ? and object = ?",
                oldGeneValues.keySet(), (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.gene_chemical_cooccurrences(subject, object, value) values (?,?,?)", newGeneValues);

        batch("delete from pubchem.enzyme_chemical_cooccurrences where subject = ? and object = ?",
                oldEnzymeValues.keySet(), (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.enzyme_chemical_cooccurrences(subject, object, value) values (?,?,?)",
                newEnzymeValues);
    }


    private static void loadGeneToDiseaseValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = getIntPairIntMap(
                "select subject, object, value from pubchem.gene_disease_cooccurrences");

        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = getIntPairIntMap(
                "select subject, object, value from pubchem.enzyme_disease_cooccurrences");

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("object").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"))
                    return;

                if(getIRI("subject").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"))
                {
                    int subject = Gene
                            .getGeneSymbolID(getStringID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"));
                    int object = getIntID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");
                    int value = getInt("value");

                    IntIntPair pair = PrimitiveTuples.pair(subject, object);
                    Disease.getDiseaseID(object);

                    if(value != oldGeneValues.removeKeyIfAbsent(pair, NO_VALUE))
                        newGeneValues.put(pair, value);
                }
                else if(getIRI("subject").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"))
                {
                    int subject = Protein
                            .getEnzymeID(getStringID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"));
                    int object = getIntID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");
                    int value = getInt("value");

                    IntIntPair pair = PrimitiveTuples.pair(subject, object);
                    Disease.getDiseaseID(object);

                    if(value != oldEnzymeValues.removeKeyIfAbsent(pair, NO_VALUE))
                        newEnzymeValues.put(pair, value);
                }
                else
                {
                    throw new IOException();
                }
            }
        }.load(model);

        batch("delete from pubchem.gene_disease_cooccurrences where subject = ? and object = ?", oldGeneValues.keySet(),
                (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.gene_disease_cooccurrences(subject, object, value) values (?,?,?)", newGeneValues);

        batch("delete from pubchem.enzyme_disease_cooccurrences where subject = ? and object = ?",
                oldEnzymeValues.keySet(), (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.enzyme_disease_cooccurrences(subject, object, value) values (?,?,?)",
                newEnzymeValues);
    }


    private static void loadGeneToGeneValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = getIntPairIntMap(
                "select subject, object, value from pubchem.gene_gene_cooccurrences");

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                int subject = Gene.getGeneSymbolID(getStringID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"));
                int object = Gene.getGeneSymbolID(getStringID("object", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"));
                int value = getInt("value");

                IntIntPair pair = PrimitiveTuples.pair(subject, object);

                if(value != oldValues.removeKeyIfAbsent(pair, NO_VALUE))
                    newValues.put(pair, value);
            }
        }.load(model);

        batch("delete from pubchem.gene_gene_cooccurrences where subject = ? and object = ?", oldValues.keySet(),
                (PreparedStatement statement, IntIntPair pair) -> {
                    statement.setInt(1, pair.getOne());
                    statement.setInt(2, pair.getTwo());
                });
        batch("insert into pubchem.gene_gene_cooccurrences(subject, object, value) values (?,?,?)", newValues);
    }


    private static void loadChemicalToChemicalCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical2chemical_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/cooccurrence/check-chemical2chemical.sparql");

        loadChemicalToChemicalValues(model);

        model.close();
    }


    private static void loadChemicalToDiseaseCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical2disease_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/cooccurrence/check-chemical2disease.sparql");

        loadChemicalToDiseaseValues(model);

        model.close();
    }


    private static void loadDiseaseToChemicalCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease2chemical_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/cooccurrence/check-disease2chemical.sparql");

        loadDiseaseToChemicalValues(model);

        model.close();
    }


    private static void loadDiseaseToDiseaseCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease2disease_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/cooccurrence/check-disease2disease.sparql");

        loadDiseaseToDiseaseValues(model);

        model.close();
    }


    private static void loadChemicalToGeneCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical2gene_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/cooccurrence/check-chemical2gene.sparql");

        loadChemicalToGeneValues(model);

        model.close();
    }


    private static void loadDiseaseToGeneCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease2gene_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/cooccurrence/check-disease2gene.sparql");

        loadDiseaseToGeneValues(model);

        model.close();
    }


    private static void loadGeneToChemicalCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_gene2chemical_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/cooccurrence/check-gene2chemical.sparql");

        loadGeneToChemicalValues(model);

        model.close();
    }


    private static void loadGeneToDiseaseCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_gene2disease_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/cooccurrence/check-gene2disease.sparql");

        loadGeneToDiseaseValues(model);

        model.close();
    }


    private static void loadGeneToGeneCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_gene2gene_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/cooccurrence/check-gene2gene.sparql");

        loadGeneToGeneValues(model);

        model.close();
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load cooccurrences ...");

        loadChemicalToChemicalCooccurrences();
        loadChemicalToDiseaseCooccurrences();
        loadChemicalToGeneCooccurrences();
        loadDiseaseToChemicalCooccurrences();
        loadDiseaseToDiseaseCooccurrences();
        loadDiseaseToGeneCooccurrences();
        loadGeneToChemicalCooccurrences();
        loadGeneToDiseaseCooccurrences();
        loadGeneToGeneCooccurrences();
    }
}
