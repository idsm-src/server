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

        config.addIriClass(new IntegerUserIriClass("ncbi:gene", "integer", "https://www.ncbi.nlm.nih.gov/gene/"));
        config.addIriClass(new IntegerUserIriClass("ncbi:book", "integer", "https://www.ncbi.nlm.nih.gov/books/NBK"));
        config.addIriClass(new IntegerUserIriClass("ncbi:taxonomy", "integer", "https://www.ncbi.nlm.nih.gov/taxonomy/"));
        config.addIriClass(new IntegerUserIriClass("ncbi:pubchem-compound", "integer", "http://pubchem.ncbi.nlm.nih.gov/compound/"));
        config.addIriClass(new IntegerUserIriClass("ncbi:pubchem-substance", "integer", "http://pubchem.ncbi.nlm.nih.gov/substance/"));
        config.addIriClass(new StringUserIriClass("ncbi:protein", "https://www.ncbi.nlm.nih.gov/protein/"));
        config.addIriClass(new StringUserIriClass("ncbi:journal", "https://www.ncbi.nlm.nih.gov/nlmcatalog/"));
        config.addIriClass(new StringUserIriClass("purl:uniprot", "http://purl.uniprot.org/uniprot/"));
        config.addIriClass(new StringUserIriClass("purl:enzyme", "http://purl.uniprot.org/enzyme/"));
        config.addIriClass(new IntegerUserIriClass("purl:taxonomy", "integer", "http://purl.uniprot.org/taxonomy/"));

        config.addIriClass(new IntegerUserIriClass("linkedchemistry:chembl", "integer", "http://linkedchemistry.info/chembl/chemblid/CHEMBL"));

        config.addIriClass(new StringUserIriClass("rdf:wwpdb", "http://rdf.wwpdb.org/pdb/", 4));
        config.addIriClass(new StringUserIriClass("rdf:ensembl", "http://rdf.ebi.ac.uk/resource/ensembl/"));

        config.addIriClass(new IntegerUserIriClass("identifiers:ncbigene", "integer", "https://identifiers.org/ncbigene:"));
        config.addIriClass(new IntegerUserIriClass("identifiers:taxonomy", "integer", "https://identifiers.org/taxonomy:"));
        config.addIriClass(new IntegerUserIriClass("identifiers:pubmed", "integer", "http://identifiers.org/pubmed/"));
        config.addIriClass(new StringUserIriClass("identifiers:pfam", "http://identifiers.org/pfam/", "PF[0-9]{5}"));
        config.addIriClass(new StringUserIriClass("identifiers:intact", "http://identifiers.org/intact/", "[A-Z0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:obo.go", "http://identifiers.org/obo.go/", "GO:[0-9]{7}"));
        config.addIriClass(new StringUserIriClass("identifiers:pdb", "http://identifiers.org/pdb/", "[0-9][A-Z0-9]{3}"));
        config.addIriClass(new StringUserIriClass("identifiers:interpro", "http://identifiers.org/interpro/", "IPR[0-9]{6}"));
        config.addIriClass(new StringUserIriClass("identifiers:reactome_old", "http://identifiers.org/reactome/", "R-[A-Z]{3}-[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:ec-code", "http://identifiers.org/ec-code/", "((-|[1-9][0-9]*)\\.){3}(-|n?[1-9][0-9]*)"));
        config.addIriClass(new StringUserIriClass("identifiers:lincs.smallmolecule", "http://identifiers.org/lincs.smallmolecule/", "LSM-[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:mesh_old", "http://identifiers.org/mesh/", "[A-Z][0-9]+(\\.[0-9]+|[A-Z][0-9]+)*"));
        config.addIriClass(new StringUserIriClass("identifiers:glytoucan", "https://identifiers.org/glytoucan:", "G[0-9]{5}[A-Z]{2}"));
        config.addIriClass(new StringUserIriClass("identifiers:mesh", "https://identifiers.org/mesh:", "[A-Z][0-9]+(\\.[0-9]+|[A-Z][0-9]+)*"));
        config.addIriClass(new IntegerUserIriClass("identifiers:wikidata", "integer", "https://identifiers.org/wikidata:Q"));
        config.addIriClass(new StringUserIriClass("identifiers:ensembl", "https://identifiers.org/ensembl:"));
        config.addIriClass(new StringUserIriClass("identifiers:kegg", "https://identifiers.org/kegg.genes:"));
        config.addIriClass(new StringUserIriClass("identifiers:bgee", "https://identifiers.org/bgee.gene:"));
        config.addIriClass(new StringUserIriClass("identifiers:pombase", "https://identifiers.org/pombase:"));
        config.addIriClass(new StringUserIriClass("identifiers:zfin", "https://identifiers.org/zfin:ZDB-"));
        config.addIriClass(new StringUserIriClass("identifiers:refseq", "https://identifiers.org/refseq:"));
        config.addIriClass(new StringUserIriClass("identifiers:uniprot", "https://identifiers.org/uniprot:"));
        config.addIriClass(new StringUserIriClass("identifiers:nextprot", "https://identifiers.org/nextprot:NX_"));
        config.addIriClass(new StringUserIriClass("identifiers:col", "https://identifiers.org/col:"));
        config.addIriClass(new StringUserIriClass("identifiers:wikipathway", "https://identifiers.org/wikipathways:", "WP[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:reactome", "https://identifiers.org/reactome:", "R-[A-Z]{3}-[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:panther.pathway", "https://identifiers.org/panther.pathway:", "P[0-9]{5}"));
        config.addIriClass(new StringUserIriClass("identifiers:pharmgkb.pathways", "https://identifiers.org/pharmgkb.pathways:", "PA[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("identifiers:biocyc", "https://identifiers.org/biocyc:", ".*CYC:.*"));
        config.addIriClass(new StringUserIriClass("identifiers:pid.pathway", "https://identifiers.org/pid.pathway:"));

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
        config.addIriClass(new StringUserIriClass("reference:reactome-pathway", "https://reactome.org/content/detail/", "R-[A-Z]{3}-[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("reference:biocyc-pathway", "https://biocyc.org/pathway?", "orgid=.*&id=.*"));
        config.addIriClass(new StringUserIriClass("reference:wikipathway-pathway", "https://www.wikipathways.org/pathways/", "WP[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("reference:plantcyc-pathway", "https://pmn.plantcyc.org/pathway?", "orgid=[A-Z0-9_]+&id=[-A-Z0-9]+"));
        config.addIriClass(new StringUserIriClass("reference:pid-pathway", "http://pid.nci.nih.gov/search/pathway_landing.shtml?pathway_id=", ".*"));
        config.addIriClass(new StringUserIriClass("reference:inoh-pathway", "http://www.inoh.org/inohviewer/inohclient.jnlp?id=", ".*"));
        config.addIriClass(new StringUserIriClass("reference:plantreactome-pathway", "https://plantreactome.gramene.org/content/detail/", "R-OSA-[0-9]{7}"));
        config.addIriClass(new StringUserIriClass("reference:pharmgkb-pathway", "https://www.pharmgkb.org/pathway/", "PA[1-9][0-9]*"));
        config.addIriClass(new StringUserIriClass("reference:fairdomhub-model", "https://fairdomhub.org/models/", "[0-9]+"));
        config.addIriClass(new StringUserIriClass("reference:lipidmaps-pathway", "https://www.lipidmaps.org/data/IntegratedPathwaysData/SetupIntegratedPathways.pl?imgsize=730&Mode=BMDMATPS11&DataType=", ".*"));
        config.addIriClass(new StringUserIriClass("reference:pantherdb-pathway", "https://www.pantherdb.org/pathway/pathDetail.do?clsAccession=", "P[0-9]{5}"));

        config.addIriClass(new StringUserIriClass("expasy:enzyme", "https://enzyme.expasy.org/EC/"));
        config.addIriClass(new StringUserIriClass("medlineplus:gene", "https://medlineplus.gov/genetics/gene/"));
        config.addIriClass(new StringUserIriClass("alliancegenome:gene", "https://www.alliancegenome.org/gene/"));
        config.addIriClass(new StringUserIriClass("kegg:entry", "https://www.kegg.jp/entry/"));

        config.addIriClass(new IntegerUserIriClass("chembl:cell_line_report_card", "integer", "https://www.ebi.ac.uk/chembl/cell_line_report_card/CHEMBL"));
        config.addIriClass(new StringUserIriClass("glygen:protein", "http://glygen.org/protein/"));
        config.addIriClass(new StringUserIriClass("glycosmos:glycoproteins", "https://glycosmos.org/glycoproteins/show/uniprot/"));
        config.addIriClass(new StringUserIriClass("alphafold:entry", "https://alphafold.ebi.ac.uk/entry/"));

        config.addIriClass(new IntegerUserIriClass("pfam:family", "integer", "https://pfam.xfam.org/family/PF"));

        config.addIriClass(new StringUserIriClass("orcid:author", "https://orcid.org/"));
        config.addIriClass(new StringUserIriClass("crossref:funder", "https://data.crossref.org/fundingdata/funder/"));

        config.addIriClass(new StringUserIriClass("catalogueoflife:taxon", "https://www.catalogueoflife.org/data/taxon/"));
        config.addIriClass(new IntegerUserIriClass("itis:taxonomy", "integer", "https://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value="));

        config.addIriClass(new StringUserIriClass("expasy:cell", "https://web.expasy.org/cellosaurus/CVCL_"));

        config.addIriClass(new StringUserIriClass("anzsrc:term", "http://purl.org/au-research/vocabulary/anzsrc-for/2008/"));
        config.addIriClass(new StringUserIriClass("identifier:isbn", "https://isbnsearch.org/isbn"));
        config.addIriClass(new StringUserIriClass("identifier:issn", "https://portal.issn.org/resource/ISSN"));
        config.addIriClass(new StringUserIriClass("identifier:doi", "https://doi.org/"));
        config.addIriClass(new StringUserIriClass("identifier:pubmed", "https://pubmed.ncbi.nlm.nih.gov/"));

        config.addIriClass(new IntegerUserIriClass("wikidata:wiki", "integer", "https://www.wikidata.org/wiki/Q"));
        config.addIriClass(new IntegerUserIriClass("wikidata:entity", "integer", "http://www.wikidata.org/entity/Q"));

        config.addIriClass(new StringUserIriClass("pharos:target", "https://pharos.nih.gov/targets/"));
        config.addIriClass(new StringUserIriClass("bgee:gene", "https://www.bgee.org/gene/"));
        config.addIriClass(new StringUserIriClass("pombase:gene", "https://www.pombase.org/gene/"));
        config.addIriClass(new StringUserIriClass("veupathdb:gene", "https://www.veupathdb.org/gene/"));
        config.addIriClass(new StringUserIriClass("zfin:entry", "https://zfin.org/ZDB-"));
        config.addIriClass(new StringUserIriClass("expasy_http:enzyme", "http://enzyme.expasy.org/EC/"));
        config.addIriClass(new StringUserIriClass("proconsortium:entry", "https://proconsortium.org/app/entry/PR:"));
        config.addIriClass(new StringUserIriClass("wormbase:protein", "https://wormbase.org/db/seq/protein?name=", ";class=Protein"));
        config.addIriClass(new StringUserIriClass("brenda:enzyme", "https://www.brenda-enzymes.org/enzyme.php?ecno="));
        config.addIriClass(new StringUserIriClass("intact:interactor", "https://www.ebi.ac.uk/intact/search?query=id:", "#interactors"));
        config.addIriClass(new StringUserIriClass("interpro:protein", "https://www.ebi.ac.uk/interpro/protein/reviewed/"));
        config.addIriClass(new StringUserIriClass("nextprot:entry", "https://www.nextprot.org/entry/NX_"));
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
