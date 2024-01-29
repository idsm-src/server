package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class PubChemRDF extends Updater
{
    private static String getVersion() throws IOException
    {
        String base = "base <http://rdf.ncbi.nlm.nih.gov/pubchem/>";
        String query = prefixes + base + "select * { <void.ttl#PubChemRDF> dcterms:modified ?date }";

        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(baseDirectory + "pubchem/RDF/void.ttl");
        model.read(in, null, "TTL");

        try(QueryExecution qexec = QueryExecutionFactory.create(query, model))
        {
            ResultSet results = qexec.execSelect();
            QuerySolution solution = results.nextSolution();
            return solution.getLiteral("date").getLexicalForm();
        }
        finally
        {
            model.close();
        }
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();

            String version = getVersion();
            System.out.println("=== load pubchem version " + version + " ===");
            System.out.println();


            Ontology.loadCategories();

            Reference.preload(); // required by Cell, Endpoint, Gene, Pathway, Substance, Taxonomy

            Concept.load();
            Source.load(); // require Concept
            Compound.load();
            InchiKey.load(); // require Compound
            Synonym.load(); // require Compound, Concept
            Substance.load(); // require Compound, Source, Synonym

            Author.load();
            Book.load(); // require Author
            Journal.load();
            Organization.load();
            Grant.load(); // require Organization

            Disease.load();
            Taxonomy.load();
            ConservedDomain.load();
            Cell.load(); // require Taxonomy
            Gene.load(); // require Taxonomy
            Protein.load(); // require ConservedDomain, Gene, Taxonomy
            Pathway.load(); // require Compound, Gene, Protein, Source, Taxonomy

            Cooccurrence.load(); // require Compound, Disease, Gene, Protein
            Patent.load(); // require Anatomy, Compound, Gene, Protein, Substance, Taxonomy
            Reference.load(); // require Author, Book, Compound, Disease, Journal, Gene, Grant, Organization, Protein

            Bioassay.load(); // require Patent, Source
            Measuregroup.load(); // require Anatomy, Bioassay, Cell, Gene, Protein, Source, Taxonomy
            Endpoint.load(); // require Bioassay, Measuregroup, Substance

            CompoundDescriptor.load(); // require Compound
            SubstanceDescriptor.load(); // require Substance


            Concept.finish();
            Source.finish();
            Compound.finish();
            Substance.finish();

            Author.finish();
            Book.finish();
            Journal.finish();
            Organization.finish();
            Grant.finish();
            Patent.finish();

            Disease.finish();
            Taxonomy.finish();
            ConservedDomain.finish();
            Cell.finish();
            Gene.finish();
            Protein.finish();
            Pathway.finish();

            Reference.finish();

            Bioassay.finish();
            Measuregroup.finish();
            Endpoint.finish();

            CompoundDescriptor.finish();

            //ConstraintChecker.check();

            setCount("PubChem Substances", Substance.size());
            setCount("PubChem Compounds", Compound.size());
            setCount("PubChem BioAssays", Bioassay.size());

            setVersion("PubChemRDF", version);

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
