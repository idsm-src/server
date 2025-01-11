package cz.iocb.chemweb.server.sparql.config.common;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.util.List;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Function;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;
import cz.iocb.sparql.engine.mapping.extension.FunctionDefinition;



public class Common
{
    public static void addPrefixes(SparqlDatabaseConfiguration config)
    {
        config.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        config.addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        config.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
        config.addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");

        config.addPrefix("sd", "http://www.w3.org/ns/sparql-service-description#");
        config.addPrefix("ent", "http://www.w3.org/ns/entailment/");
        config.addPrefix("format", "http://www.w3.org/ns/formats/");

        config.addPrefix("fulltext", "http://bioinfo.uochb.cas.cz/rdf/v1.0/fulltext#");
    }


    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        // @formatter:off

        config.addIriClass(new IntegerUserIriClass("ncbi:book", "integer", "https://www.ncbi.nlm.nih.gov/books/NBK"));
        config.addIriClass(new IntegerUserIriClass("ncbi:taxonomy", "integer", "https://www.ncbi.nlm.nih.gov/taxonomy/"));
        config.addIriClass(new IntegerUserIriClass("ncbi:pubchem-compound", "integer", "http://pubchem.ncbi.nlm.nih.gov/compound/"));
        config.addIriClass(new IntegerUserIriClass("ncbi:pubchem-substance", "integer", "http://pubchem.ncbi.nlm.nih.gov/substance/"));
        config.addIriClass(new IntegerUserIriClass("ncbi:conserveddomain", "integer", "https://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi?uid="));
        config.addIriClass(new StringUserIriClass("ncbi:journal", "https://www.ncbi.nlm.nih.gov/nlmcatalog/"));
        config.addIriClass(new StringUserIriClass("purl:uniprot", "http://purl.uniprot.org/uniprot/"));
        config.addIriClass(new StringUserIriClass("purl:enzyme", "http://purl.uniprot.org/enzyme/"));

        config.addIriClass(new IntegerUserIriClass("linkedchemistry:chembl", "integer", "http://linkedchemistry.info/chembl/chemblid/CHEMBL"));

        config.addIriClass(new StringUserIriClass("rdf:wwpdb", "http://rdf.wwpdb.org/pdb/", 4));
        config.addIriClass(new StringUserIriClass("rdf:ensembl", "http://rdf.ebi.ac.uk/resource/ensembl/"));

        config.addIriClass(new StringUserIriClass("identifiers:pfam", "http://identifiers.org/pfam/", "PF[0-9]{5}"));
        config.addIriClass(new StringUserIriClass("identifiers:intact", "http://identifiers.org/intact/", "[A-Z0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:obo.go", "http://identifiers.org/obo.go/", "GO:[0-9]{7}"));
        config.addIriClass(new StringUserIriClass("identifiers:pdb", "http://identifiers.org/pdb/", "[0-9][A-Z0-9]{3}"));
        config.addIriClass(new StringUserIriClass("identifiers:interpro", "http://identifiers.org/interpro/", "IPR[0-9]{6}"));
        config.addIriClass(new StringUserIriClass("identifiers:reactome_old", "http://identifiers.org/reactome/", "R-[A-Z]{3}-[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:ec-code", "http://identifiers.org/ec-code/", "((-|[1-9][0-9]*)\\.){3}(-|n?[1-9][0-9]*)"));
        config.addIriClass(new StringUserIriClass("identifiers:lincs.smallmolecule", "http://identifiers.org/lincs.smallmolecule/", "LSM-[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:mesh_old", "http://identifiers.org/mesh/", "[A-Z][0-9]+(\\.[0-9]+|[A-Z][0-9]+)*"));
        config.addIriClass(new StringUserIriClass("identifiers:glytoucan", "http://identifiers.org/glytoucan:", "G[0-9]{5}[A-Z]{2}"));
        config.addIriClass(new StringUserIriClass("identifiers:mesh", "http://identifiers.org/mesh:", "[A-Z][0-9]+(\\.[0-9]+|[A-Z][0-9]+)*"));
        config.addIriClass(new StringUserIriClass("identifiers:pr", "http://identifiers.org/PR:"));
        config.addIriClass(new StringUserIriClass("identifiers:ensembl", "http://identifiers.org/ensembl:"));
        config.addIriClass(new StringUserIriClass("identifiers:kegg", "http://identifiers.org/kegg.genes:"));
        config.addIriClass(new StringUserIriClass("identifiers:bgee", "http://identifiers.org/bgee.gene:"));
        config.addIriClass(new StringUserIriClass("identifiers:pombase", "http://identifiers.org/pombase:"));
        config.addIriClass(new StringUserIriClass("identifiers:zfin", "http://identifiers.org/zfin:ZDB-"));
        config.addIriClass(new StringUserIriClass("identifiers:refseq", "http://identifiers.org/refseq:"));
        config.addIriClass(new StringUserIriClass("identifiers:uniprot", "http://identifiers.org/uniprot:"));
        config.addIriClass(new StringUserIriClass("identifiers:nextprot", "http://identifiers.org/nextprot:NX_"));
        config.addIriClass(new StringUserIriClass("identifiers:col", "http://identifiers.org/col:"));
        config.addIriClass(new StringUserIriClass("identifiers:wikipathway", "http://identifiers.org/wikipathways:", "WP[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:reactome", "http://identifiers.org/reactome:", "R-[A-Z]{3}-[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:panther.pathway", "http://identifiers.org/panther.pathway:", "P[0-9]{5}"));
        config.addIriClass(new StringUserIriClass("identifiers:pharmgkb.pathways", "http://identifiers.org/pharmgkb.pathways:", "PA[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:biocyc", "http://identifiers.org/biocyc:", ".*CYC:.*"));
        config.addIriClass(new StringUserIriClass("identifiers:pid.pathway", "http://identifiers.org/pid.pathway:"));
        config.addIriClass(new StringUserIriClass("identifiers:ncbiprotein", "http://identifiers.org/ncbiprotein:"));
        config.addIriClass(new IntegerUserIriClass("identifiers:wikidata", "integer", "http://identifiers.org/wikidata:Q"));
        config.addIriClass(new IntegerUserIriClass("identifiers:taxonomy", "integer", "http://identifiers.org/taxonomy:"));
        config.addIriClass(new IntegerUserIriClass("identifiers:pubmed", "integer", "http://identifiers.org/pubmed/"));

        config.addIriClass(new IntegerUserIriClass("reference:chebi", "integer", "http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI%3A"));
        config.addIriClass(new IntegerUserIriClass("reference:ncbi-taxonomy", "integer", "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id="));
        config.addIriClass(new IntegerUserIriClass("reference:pubchem-assay", "integer", "http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?aid="));
        config.addIriClass(new StringUserIriClass("reference:life", "http://life.ccs.miami.edu/life/summary?mode=CellLine&source=LINCS&input=", "LCL-[0-9]{4}"));
        config.addIriClass(new StringUserIriClass("reference:pharmgkb-gene", "http://www.pharmgkb.org/gene/", "PA[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("reference:timbal", "http://mordred.bioc.cam.ac.uk/timbal/", "[A-Za-z0-9%()-]+"));
        config.addIriClass(new StringUserIriClass("reference:cgd", "http://research.nhgri.nih.gov/CGD/view/?g=", "[A-Z0-9-]+"));
        config.addIriClass(new StringUserIriClass("reference:uniprot", "http://www.uniprot.org/uniprot/"));
        config.addIriClass(new StringUserIriClass("reference:zinc", "http://zinc15.docking.org/substances/", "ZINC[0-9]{12}"));
        config.addIriClass(new StringUserIriClass("reference:surechembl", "https://www.surechembl.org/chemical/", "SCHEMBL[0-9]+"));
        config.addIriClass(new StringUserIriClass("reference:emolecules", "https://www.emolecules.com/cgi-bin/more?vid=", "[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("reference:mcule", "https://mcule.com/", "MCULE-[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("reference:nikkaji", "http://jglobal.jst.go.jp/en/redirect?Nikkaji_No=", "[A-Z0-9.]+"));
        config.addIriClass(new StringUserIriClass("reference:actor", "http://actor.epa.gov/actor/chemical.xhtml?casrn=", "[1-9][0-9]*-[0-9]{2}-[0-9]"));
        config.addIriClass(new StringUserIriClass("reference:pdbe", "http://www.ebi.ac.uk/pdbe-srv/pdbechem/chemicalCompound/show/", "[A-Z0-9]{1,3}"));
        config.addIriClass(new StringUserIriClass("reference:nmrshiftdb2", "http://nmrshiftdb.org/molecule/", "[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("reference:kegg", "http://www.genome.jp/dbget-bin/www_bget?", "C[0-9]{5}"));
        config.addIriClass(new StringUserIriClass("reference:drugbank", "http://www.drugbank.ca/drugs/", "DB[0-9]{5}"));
        config.addIriClass(new StringUserIriClass("reference:hmdb", "http://www.hmdb.ca/metabolites/", "HMDB[0-9]{7}"));
        config.addIriClass(new StringUserIriClass("reference:iuphar", "http://www.guidetopharmacology.org/GRAC/LigandDisplayForward?ligandId=", "[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("reference:selleck", "http://www.selleckchem.com/products/", "[^/]*", ".html"));
        config.addIriClass(new StringUserIriClass("reference:pharmgkb-drug", "https://www.pharmgkb.org/drug/", "PA[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("reference:expression_atlas", "http://www.ebi.ac.uk/gxa/query?conditionQuery=", ".+"));
        config.addIriClass(new StringUserIriClass("reference:recon", "https://www.vmh.life/#metabolite/", "[^/]+"));
        config.addIriClass(new StringUserIriClass("reference:wikipedia", "http://en.wikipedia.org/wiki/", ".+"));
        config.addIriClass(new StringUserIriClass("reference:fda_srs", "https://precision.fda.gov/uniisearch/srs/unii/", "[A-Z0-9]{10}"));
        config.addIriClass(new StringUserIriClass("reference:pathbank-pathway", "http://pathbank.org/view/", "SMP[0-9]{5,7}"));
        config.addIriClass(new StringUserIriClass("reference:plantcyc-pathway", "https://pmn.plantcyc.org/pathway?", "orgid=[A-Z0-9_]+&id=[-A-Z0-9]+"));
        config.addIriClass(new StringUserIriClass("reference:plantreactome-pathway", "https://plantreactome.gramene.org/content/detail/", "R-OSA-[0-9]{7}"));
        config.addIriClass(new StringUserIriClass("reference:fairdomhub-model", "https://fairdomhub.org/models/", "[0-9]+"));
        config.addIriClass(new StringUserIriClass("reference:lipidmaps-pathway", "https://www.lipidmaps.org/data/IntegratedPathwaysData/SetupIntegratedPathways.pl?imgsize=730&Mode=BMDMATPS11&DataType=", ".*"));

        config.addIriClass(new StringUserIriClass("expasy:enzyme", "https://enzyme.expasy.org/EC/"));
        config.addIriClass(new StringUserIriClass("medlineplus:gene", "https://medlineplus.gov/genetics/gene/"));
        config.addIriClass(new StringUserIriClass("alliancegenome:gene", "https://www.alliancegenome.org/gene/"));

        config.addIriClass(new StringUserIriClass("glygen:protein", "https://glygen.org/protein/"));
        config.addIriClass(new StringUserIriClass("glycosmos:glycoproteins", "https://glycosmos.org/glycoproteins/"));
        config.addIriClass(new StringUserIriClass("alphafold:entry", "https://alphafold.ebi.ac.uk/entry/"));

        config.addIriClass(new IntegerUserIriClass("pfam:family", "integer", "https://pfam.xfam.org/family/PF"));

        config.addIriClass(new StringUserIriClass("orcid:author", "https://orcid.org/"));
        config.addIriClass(new StringUserIriClass("crossref:funder", "https://data.crossref.org/fundingdata/funder/"));

        config.addIriClass(new StringUserIriClass("identifiers:cellosaurus", "http://identifiers.org/cellosaurus:CVCL_"));

        config.addIriClass(new StringUserIriClass("anzsrc:term", "http://purl.org/au-research/vocabulary/anzsrc-for/2008/"));
        config.addIriClass(new StringUserIriClass("identifier:isbn", "https://isbnsearch.org/isbn"));
        config.addIriClass(new StringUserIriClass("identifier:issn", "https://portal.issn.org/resource/ISSN"));
        config.addIriClass(new StringUserIriClass("identifier:doi", "https://doi.org/"));
        config.addIriClass(new StringUserIriClass("identifier:pubmed", "https://pubmed.ncbi.nlm.nih.gov/"));

        config.addIriClass(new IntegerUserIriClass("wikidata:wiki", "integer", "https://www.wikidata.org/wiki/Q"));
        config.addIriClass(new IntegerUserIriClass("wikidata:entity", "integer", "http://www.wikidata.org/entity/Q"));

        config.addIriClass(new StringUserIriClass("pharos:target", "https://pharos.nih.gov/targets/"));
        config.addIriClass(new StringUserIriClass("veupathdb:gene", "https://www.veupathdb.org/gene/"));
        config.addIriClass(new StringUserIriClass("expasy_http:enzyme", "http://enzyme.expasy.org/EC/"));
        config.addIriClass(new StringUserIriClass("wormbase:protein", "https://wormbase.org/db/seq/protein?name=", ";class=Protein"));
        config.addIriClass(new StringUserIriClass("brenda:enzyme", "https://www.brenda-enzymes.org/enzyme.php?ecno="));
        config.addIriClass(new StringUserIriClass("intact:interactor", "https://www.ebi.ac.uk/intact/search?query="));
        config.addIriClass(new StringUserIriClass("interpro:protein", "https://www.ebi.ac.uk/interpro/protein/reviewed/"));
        config.addIriClass(new IntegerUserIriClass("interpro:entry", "integer", "https://www.ebi.ac.uk/interpro/entry/InterPro/IPR", 6));

        // @formatter:on
    }


    public static void addFunctions(SparqlDatabaseConfiguration config)
    {
        String fulltext = config.getPrefixes().get("fulltext");

        FunctionDefinition match = new FunctionDefinition(fulltext + "match", new Function("common", "fulltext_match"),
                xsdBoolean, List.of(FunctionDefinition.stringLiteral, xsdString), false, true);

        config.addFunction(match);
    }
}
