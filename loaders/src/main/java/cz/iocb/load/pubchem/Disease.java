package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.sio;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Disease extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> diseases = new EntityTable<>("pubchem.disease_bases", intKey("id"), null,
            varchar("label"));
    private static final MissingEntities<Integer> missingDiseases = new MissingEntities<>("disease", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", skos + "prefLabel", skos + "altLabel", skos + "closeMatch",
                skos + "relatedMatch");
        dispatcher.checkTypes(all(), vocab + "Disease", sio + "SIO_010299");
        dispatcher.checkPrefixes(all(), skos + "relatedMatch", "https://uts.nlm.nih.gov/uts/umls/concept/C",
                "http://purl.obolibrary.org/obo/MONDO_", "http://purl.obolibrary.org/obo/HP_",
                "https://omim.org/entry/", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C",
                "https://www.ncbi.nlm.nih.gov/medgen/C", "https://www.ncbi.nlm.nih.gov/medgen/CN",
                "https://rarediseases.info.nih.gov/diseases/",
                "https://www.guidetopharmacology.org/GRAC/DiseaseDisplayForward?diseaseId=",
                "https://www.kegg.jp/entry/H", "http://nanbyodata.jp/ontology/NANDO_", "http://identifiers.org/DOID:",
                "http://identifiers.org/kegg.disease:H", "http://identifiers.org/medgen:CN",
                "http://identifiers.org/medgen:C", "http://identifiers.org/umls:C",
                "http://identifiers.org/pharmgkb.disease:PA", "http://identifiers.org/orphanet:",
                "http://identifiers.org/NANDO:", "http://identifiers.org/ncit:C", "http://identifiers.org/mim:",
                "http://identifiers.org/HP:", "https://www.pharmgkb.org/disease/PA",
                "https://hpo.jax.org/app/browse/term/HP:", "https://www.orpha.net/en/disease/detail/",
                "http://purl.obolibrary.org/obo/DOID:", "https://www.disease-ontology.org/?id=DOID:",
                "https://monarchinitiative.org/disease/MONDO:", "https://glycosmos.org/diseases/DOID:");
        dispatcher.checkPrefixes(all(), skos + "closeMatch", "http://id.nlm.nih.gov/mesh/",
                "http://identifiers.org/mesh:", "https://uts.nlm.nih.gov/uts/umls/concept/C",
                "http://purl.obolibrary.org/obo/MONDO_", "http://purl.obolibrary.org/obo/HP_",
                "https://omim.org/entry/", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C",
                "https://www.ncbi.nlm.nih.gov/medgen/C", "https://www.ncbi.nlm.nih.gov/medgen/CN",
                "https://rarediseases.info.nih.gov/diseases/",
                "https://www.guidetopharmacology.org/GRAC/DiseaseDisplayForward?diseaseId=",
                "https://www.kegg.jp/entry/H", "http://nanbyodata.jp/ontology/NANDO_", "http://identifiers.org/DOID:",
                "http://identifiers.org/kegg.disease:H", "http://identifiers.org/medgen:CN",
                "http://identifiers.org/medgen:C", "http://identifiers.org/umls:C",
                "http://identifiers.org/pharmgkb.disease:PA", "http://identifiers.org/orphanet:",
                "http://identifiers.org/NANDO:", "http://identifiers.org/ncit:C", "http://identifiers.org/mim:",
                "http://identifiers.org/HP:", "https://www.pharmgkb.org/disease/PA",
                "https://hpo.jax.org/app/browse/term/HP:", "https://www.orpha.net/en/disease/detail/",
                "http://purl.obolibrary.org/obo/DOID:", "https://www.disease-ontology.org/?id=DOID:",
                "https://monarchinitiative.org/disease/MONDO:", "https://glycosmos.org/diseases/DOID:");
        dispatcher.checkPaired(skos + "closeMatch", "http://identifiers.org/mesh:", "http://id.nlm.nih.gov/mesh/");
    }


    private static void loadBases(TripleDispatcher dispatcher)
    {
        dispatcher.onType(sio + "SIO_010299", (subject, object) -> {
            Integer diseaseID = getIntID(subject, prefix);

            diseases.reference(diseaseID);
            missingDiseases.described(diseaseID);
        });
    }


    private static void loadLabels(TripleDispatcher dispatcher)
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            diseases.set(getDiseaseID(subject.getURI()), "label", getString(object));
        });
    }


    private static void loadAlternatives(TripleDispatcher dispatcher) throws SQLException
    {
        IntStringSet keepAlternatives = new IntStringSet();
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select disease,alternative from pubchem.disease_alternatives", oldAlternatives);

        dispatcher.on(skos + "altLabel", (subject, object) -> {
            Integer diseaseID = getDiseaseID(subject.getURI());
            String alternative = getString(object);

            Pair<Integer, String> pair = Pair.getPair(diseaseID, alternative);

            if(oldAlternatives.remove(pair))
                keepAlternatives.add(pair);
            else if(!keepAlternatives.contains(pair))
                newAlternatives.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.disease_alternatives where disease=? and alternative=?", oldAlternatives);
            store("insert into pubchem.disease_alternatives(disease,alternative) values(?,?)", newAlternatives);
        });
    }


    private static void loadCloseMatches(TripleDispatcher dispatcher) throws SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select disease,match_unit,match_id from pubchem.disease_matches", oldMatches);

        dispatcher.on(skos + "closeMatch", (subject, object) -> {
            String iri = object.getURI();

            if(iri.startsWith("http://id.nlm.nih.gov/mesh/") || iri.startsWith("http://identifiers.org/mesh:"))
                return;

            // workaround
            if(iri.matches("http://purl\\.obolibrary\\.org/obo/[0-9]*"))
                return;

            // workaround
            iri = iri.replaceFirst("^(https://rarediseases.info.nih.gov/diseases/)0*([0-9]*/index)$", "$1$2");

            Integer diseaseID = getDiseaseID(subject.getURI());
            Pair<Integer, Integer> match = Ontology.getId(iri);

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(diseaseID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.disease_matches where disease=? and match_unit=? and match_id=?", oldMatches);
            store("insert into pubchem.disease_matches(disease,match_unit,match_id) values(?,?,?)", newMatches);
        });
    }


    private static void loadMeshCloseMatches(TripleDispatcher dispatcher) throws SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select disease,match from pubchem.disease_mesh_matches", oldMatches);

        dispatcher.on(skos + "closeMatch", (subject, object) -> {
            if(!object.getURI().startsWith("http://id.nlm.nih.gov/mesh/"))
                return;

            Integer diseaseID = getDiseaseID(subject.getURI());
            String match = getStringID(object, "http://id.nlm.nih.gov/mesh/");

            Pair<Integer, String> pair = Pair.getPair(diseaseID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.disease_mesh_matches where disease=? and match=?", oldMatches);
            store("insert into pubchem.disease_mesh_matches(disease,match) values(?,?)", newMatches);
        });
    }


    private static void loadRelatedMatches(TripleDispatcher dispatcher) throws SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select disease,match_unit,match_id from pubchem.disease_related_matches", oldMatches);

        dispatcher.on(skos + "relatedMatch", (subject, object) -> {
            //NOTE: workaround to prevent loading incorrect references
            if(object.getURI().matches("http://purl\\.obolibrary\\.org/obo/[0-9]+"))
                return;

            Integer diseaseID = getDiseaseID(subject.getURI());
            Pair<Integer, Integer> match = Ontology.getId(object.getURI());

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(diseaseID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.disease_related_matches where disease=? and match_unit=? and match_id=?",
                    oldMatches);
            store("insert into pubchem.disease_related_matches(disease,match_unit,match_id) values(?,?,?)", newMatches);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load diseases ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadLabels(dispatcher);
        loadAlternatives(dispatcher);
        loadCloseMatches(dispatcher);
        loadMeshCloseMatches(dispatcher);
        loadRelatedMatches(dispatcher);

        dispatcher.load("pubchem/RDF/disease/pc_disease.ttl.gz");
        missingDiseases.settle();
        dispatcher.finish();

        diseases.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish diseases ...");

        diseases.store();

        System.out.println();
    }


    static Integer getDiseaseID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer diseaseID = Integer.parseInt(value.substring(prefixLength));

        if(diseases.reference(diseaseID))
            missingDiseases.referenced(diseaseID);

        return diseaseID;
    }
}
