package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Cooccurrence extends Updater
{
    private static void loadChemicalToChemicalValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.chemical_chemical_cooccurrences", oldValues);

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer subject = Compound.getCompoundID(getIRI("subject"));
                Integer object = Compound.getCompoundID(getIRI("object"));
                Integer value = getInt("value");

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
            }
        }.load(model);

        store("delete from pubchem.chemical_chemical_cooccurrences where subject=? and object=? and value=?",
                oldValues);
        store("insert into pubchem.chemical_chemical_cooccurrences(subject,object,value) values(?,?,?)", newValues);
    }


    private static void loadChemicalToDiseaseValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.chemical_disease_cooccurrences", oldValues);

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("subject").startsWith(Compound.prefix))
                    return;

                Integer subject = Compound.getCompoundID(getIRI("subject"));
                Integer object = Disease.getDiseaseID(getIRI("object"));
                Integer value = getInt("value");

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
            }
        }.load(model);

        store("delete from pubchem.chemical_disease_cooccurrences where subject=? and object=? and value=?", oldValues);
        store("insert into pubchem.chemical_disease_cooccurrences(subject,object,value) values(?,?,?)", newValues);
    }


    private static void loadDiseaseToChemicalValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.disease_chemical_cooccurrences", oldValues);

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("subject").startsWith(Disease.prefix))
                    return;

                Integer subject = Disease.getDiseaseID(getIRI("subject"));
                Integer object = Compound.getCompoundID(getIRI("object"));
                Integer value = getInt("value");

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
            }
        }.load(model);

        store("delete from pubchem.disease_chemical_cooccurrences where subject=? and object=? and value=?", oldValues);
        store("insert into pubchem.disease_chemical_cooccurrences(subject,object,value) values(?,?,?)", newValues);
    }


    private static void loadDiseaseToDiseaseValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.disease_disease_cooccurrences", oldValues);

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer subject = Disease.getDiseaseID(getIRI("subject"));
                Integer object = Disease.getDiseaseID(getIRI("object"));
                Integer value = getInt("value");

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
            }
        }.load(model);

        store("delete from pubchem.disease_disease_cooccurrences where subject=? and object=? and value=?", oldValues);
        store("insert into pubchem.disease_disease_cooccurrences(subject,object,value) values(?,?,?)", newValues);
    }


    private static void loadChemicalToGeneValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.chemical_gene_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.chemical_enzyme_cooccurrences", oldEnzymeValues);

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("subject").startsWith(Compound.prefix))
                    return;

                if(getIRI("object").startsWith(Gene.symbolPrefix))
                {
                    Integer subject = Compound.getCompoundID(getIRI("subject"));
                    Integer object = Gene.getGeneSymbolID(getIRI("object"));
                    Integer value = getInt("value");

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
                else if(getIRI("object").startsWith(Protein.enzymePrefix))
                {
                    Integer subject = Compound.getCompoundID(getIRI("subject"));
                    Integer object = Protein.getEnzymeID(getIRI("object"));
                    Integer value = getInt("value");

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
            }
        }.load(model);

        store("delete from pubchem.chemical_gene_cooccurrences where subject=? and object=? and value=?",
                oldGeneValues);
        store("insert into pubchem.chemical_gene_cooccurrences(subject,object,value) values(?,?,?)", newGeneValues);

        store("delete from pubchem.chemical_enzyme_cooccurrences where subject=? and object=? and value=?",
                oldEnzymeValues);
        store("insert into pubchem.chemical_enzyme_cooccurrences(subject,object,value) values(?,?,?)", newEnzymeValues);
    }


    private static void loadDiseaseToGeneValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.disease_gene_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.disease_enzyme_cooccurrences", oldEnzymeValues);

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("subject").startsWith(Disease.prefix))
                    return;

                if(getIRI("object").startsWith(Gene.symbolPrefix))
                {
                    Integer subject = Disease.getDiseaseID(getIRI("subject"));
                    Integer object = Gene.getGeneSymbolID(getIRI("object"));
                    Integer value = getInt("value");

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
                else if(getIRI("object").startsWith(Protein.enzymePrefix))
                {
                    Integer subject = Disease.getDiseaseID(getIRI("subject"));
                    Integer object = Protein.getEnzymeID(getIRI("object"));
                    Integer value = getInt("value");

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
            }
        }.load(model);


        store("delete from pubchem.disease_gene_cooccurrences where subject=? and object=? and value=?", oldGeneValues);
        store("insert into pubchem.disease_gene_cooccurrences(subject,object,value) values(?,?,?)", newGeneValues);

        store("delete from pubchem.disease_enzyme_cooccurrences where subject=? and object=? and value=?",
                oldEnzymeValues);
        store("insert into pubchem.disease_enzyme_cooccurrences(subject,object,value) values(?,?,?)", newEnzymeValues);
    }


    private static void loadGeneToChemicalValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.gene_chemical_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.enzyme_chemical_cooccurrences", oldEnzymeValues);

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("object").startsWith(Compound.prefix))
                    return;

                if(getIRI("subject").startsWith(Gene.symbolPrefix))
                {
                    Integer subject = Gene.getGeneSymbolID(getIRI("subject"));
                    Integer object = Compound.getCompoundID(getIRI("object"));
                    Integer value = getInt("value");

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
                else if(getIRI("subject").startsWith(Protein.enzymePrefix))
                {
                    Integer subject = Protein.getEnzymeID(getIRI("subject"));
                    Integer object = Compound.getCompoundID(getIRI("object"));
                    Integer value = getInt("value");

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
            }
        }.load(model);

        store("delete from pubchem.gene_chemical_cooccurrences where subject=? and object=? and value=?",
                oldGeneValues);
        store("insert into pubchem.gene_chemical_cooccurrences(subject,object,value) values(?,?,?)", newGeneValues);

        store("delete from pubchem.enzyme_chemical_cooccurrences where subject=? and object=? and value=?",
                oldEnzymeValues);
        store("insert into pubchem.enzyme_chemical_cooccurrences(subject,object,value) values(?,?,?)", newEnzymeValues);
    }


    private static void loadGeneToDiseaseValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap keepGeneValues = new IntPairIntMap();
        IntPairIntMap newGeneValues = new IntPairIntMap();
        IntPairIntMap oldGeneValues = new IntPairIntMap();

        IntPairIntMap keepEnzymeValues = new IntPairIntMap();
        IntPairIntMap newEnzymeValues = new IntPairIntMap();
        IntPairIntMap oldEnzymeValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.gene_disease_cooccurrences", oldGeneValues);
        load("select subject,object,value from pubchem.enzyme_disease_cooccurrences", oldEnzymeValues);

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(!getIRI("object").startsWith(Disease.prefix))
                    return;

                if(getIRI("subject").startsWith(Gene.symbolPrefix))
                {
                    Integer subject = Gene.getGeneSymbolID(getIRI("subject"));
                    Integer object = Disease.getDiseaseID(getIRI("object"));
                    Integer value = getInt("value");

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
                else if(getIRI("subject").startsWith(Protein.enzymePrefix))
                {
                    Integer subject = Protein.getEnzymeID(getIRI("subject"));
                    Integer object = Disease.getDiseaseID(getIRI("object"));
                    Integer value = getInt("value");

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
            }
        }.load(model);

        store("delete from pubchem.gene_disease_cooccurrences where subject=? and object=? and value=?", oldGeneValues);
        store("insert into pubchem.gene_disease_cooccurrences(subject,object,value) values(?,?,?)", newGeneValues);

        store("delete from pubchem.enzyme_disease_cooccurrences where subject=? and object=? and value=?",
                oldEnzymeValues);
        store("insert into pubchem.enzyme_disease_cooccurrences(subject,object,value) values(?,?,?)", newEnzymeValues);
    }


    private static void loadGeneToGeneValues(Model model) throws IOException, SQLException
    {
        IntPairIntMap keepValues = new IntPairIntMap();
        IntPairIntMap newValues = new IntPairIntMap();
        IntPairIntMap oldValues = new IntPairIntMap();

        load("select subject,object,value from pubchem.gene_gene_cooccurrences", oldValues);

        new QueryResultProcessor(patternQuery("[ rdf:subject ?subject; rdf:object ?object; sio:SIO_000300 ?value ]"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer subject = Gene.getGeneSymbolID(getIRI("subject"));
                Integer object = Gene.getGeneSymbolID(getIRI("object"));
                Integer value = getInt("value");

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
            }
        }.load(model);

        store("delete from pubchem.gene_gene_cooccurrences where subject=? and object=? and value=?", oldValues);
        store("insert into pubchem.gene_gene_cooccurrences(subject,object,value) values(?,?,?)", newValues);
    }


    private static void loadChemicalToChemicalCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical_and_chemical_[0-9]+\\.ttl\\.gz", file -> {
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

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical_and_disease_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        //check(model, "pubchem/cooccurrence/check-chemical2disease.sparql");

        loadChemicalToDiseaseValues(model);
        loadDiseaseToChemicalValues(model);

        model.close();
    }


    /*
    private static void loadDiseaseToChemicalCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease_and_chemical_[0-9]+\\.ttl\\.gz", file -> {
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
    */


    private static void loadDiseaseToDiseaseCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease_and_disease_[0-9]+\\.ttl\\.gz", file -> {
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

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_chemical_and_gene_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        //check(model, "pubchem/cooccurrence/check-chemical2gene.sparql");

        loadChemicalToGeneValues(model);
        loadGeneToChemicalValues(model);

        model.close();
    }


    /*
    private static void loadDiseaseToGeneCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_disease_and_gene_[0-9]+\\.ttl\\.gz", file -> {
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
    */


    /*
    private static void loadGeneToChemicalCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_gene_and_chemical_[0-9]+\\.ttl\\.gz", file -> {
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
    */


    private static void loadGeneToDiseaseCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_gene_and_disease_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        //check(model, "pubchem/cooccurrence/check-gene2disease.sparql");

        loadGeneToDiseaseValues(model);
        loadDiseaseToGeneValues(model);

        model.close();
    }


    private static void loadGeneToGeneCooccurrences() throws IOException, SQLException
    {
        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/cooccurrence", "pc_cooccurrence_gene_and_gene_[0-9]+\\.ttl\\.gz", file -> {
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
        //loadDiseaseToChemicalCooccurrences();
        loadDiseaseToDiseaseCooccurrences();
        //loadDiseaseToGeneCooccurrences();
        //loadGeneToChemicalCooccurrences();
        loadGeneToDiseaseCooccurrences();
        loadGeneToGeneCooccurrences();
    }
}
