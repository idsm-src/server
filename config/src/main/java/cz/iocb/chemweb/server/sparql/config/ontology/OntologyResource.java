package cz.iocb.chemweb.server.sparql.config.ontology;

import static cz.iocb.sparql.engine.database.SqlType.INT2;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.RDFBOX;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.box;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.iri;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.constant;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.string;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.SQLRuntimeException;
import cz.iocb.sparql.engine.database.ValueColumn;
import cz.iocb.sparql.engine.mapping.classes.GenericUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.request.Request;



/**
 * IRI class of the resources of the ontologies, stored as a pair of a unit and an id column. An IRI matching the
 * pattern of a unit of {@code ontology.units} is stored as the unit and the number encoded in the IRI, any other IRI
 * known to the ontologies as the uncategorized unit and its id in {@code ontology.uncategorized_resources}. The class
 * is a singleton created by {@link #get(SparqlDatabaseConfiguration)}; the units with a class of their own are mapped
 * by {@link OntologyUnitResource}.
 */
public class OntologyResource extends GenericUserIriClass
{
    /**
     * Unit of ontology IRIs, a row of {@code ontology.units}.
     *
     * @param id the id of the unit
     * @param prefix the part of the IRIs preceding the value
     * @param valueOffset the one-based position of the value in the IRIs
     * @param valueLength the length the value is zero-padded to, 0 for an unpadded number, negative for a value that is
     *            not a plain number (see {@link OntologyResource#parseId(int, String)})
     * @param suffix the part of the IRIs following the value
     * @param pattern the pattern of the IRIs of the unit
     */
    static record Unit(short id, String prefix, int valueOffset, int valueLength, String suffix, Pattern pattern)
    {
    }


    /**
     * Query of the id of an uncategorized resource by its IRI.
     */
    private static final String sqlQuery = "select id from ontology.uncategorized_resources where iri = ?";

    /**
     * The single instance of the class, created by {@link #get(SparqlDatabaseConfiguration)}.
     */
    private static OntologyResource instance;

    /**
     * Units in the order of their ids.
     */
    private static List<Unit> units = new ArrayList<>();

    /**
     * Units by the constant column of their id.
     */
    private static Map<Column, Unit> unitMap = new HashMap<>();

    /**
     * Unit of the uncategorized resources, the IRIs matching no other unit, identified by their ids in
     * {@code ontology.uncategorized_resources}.
     */
    public static final short unitUncategorized = 0;

    /**
     * Unit of the blank nodes of the ontologies, {@code http://blank/ID_<number>}.
     */
    public static final short unitBlank = 1;

    /**
     * Unit of the PR terms of six-character UniProt accessions, {@code http://purl.obolibrary.org/obo/PR_<accession>}.
     */
    public static final short unitPR2 = 100;

    /**
     * Unit of the PR terms of UniProt isoforms, {@code http://purl.obolibrary.org/obo/PR_<accession>-<isoform>}.
     */
    public static final short unitPR1 = 101;

    /**
     * Unit of the PR terms of ten-character UniProt accessions, {@code http://purl.obolibrary.org/obo/PR_A0A<rest>}.
     */
    public static final short unitPR0 = 102;

    /**
     * Unit of the BFO terms, {@code http://purl.obolibrary.org/obo/BFO_<id>}.
     */
    public static final short unitBFO = 202;

    /**
     * Unit of the BTO terms, {@code http://purl.obolibrary.org/obo/BTO_<id>}.
     */
    public static final short unitBTO = 205;

    /**
     * Unit of the ChEBI star ratings, {@code http://purl.obolibrary.org/obo/chebi/<n>_STAR}.
     */
    public static final short unitStar = 208;

    /**
     * Unit of the ChEBI terms, {@code http://purl.obolibrary.org/obo/CHEBI_<id>}.
     */
    public static final short unitCHEBI = 209;

    /**
     * Unit of the ClassyFire terms, {@code http://purl.obolibrary.org/obo/CHEMONTID_<id>}.
     */
    public static final short unitClassyFire = 210;

    /**
     * Unit of the Cell Ontology terms, {@code http://purl.obolibrary.org/obo/CL_<id>}.
     */
    public static final short unitCL = 211;

    /**
     * Unit of the Cell Line Ontology terms, {@code http://purl.obolibrary.org/obo/CLO_<id>}.
     */
    public static final short unitCLO = 212;

    /**
     * Unit of the DOID terms with an unpadded number, {@code http://purl.obolibrary.org/obo/DOID:<number>}.
     */
    public static final short unitDOIDColon = 216;

    /**
     * Unit of the DOID terms with a zero-padded seven-digit number,
     * {@code http://purl.obolibrary.org/obo/DOID:0<digits>}.
     */
    public static final short unitDOIDColon0 = 217;

    /**
     * Unit of the GO terms, {@code http://purl.obolibrary.org/obo/GO_<id>}.
     */
    public static final short unitGO = 233;

    /**
     * Unit of the HPO terms, {@code http://purl.obolibrary.org/obo/HP_<id>}.
     */
    public static final short unitHP = 235;

    /**
     * Unit of the IAO terms, {@code http://purl.obolibrary.org/obo/IAO_<id>}.
     */
    public static final short unitIAO = 236;

    /**
     * Unit of the MONDO terms, {@code http://purl.obolibrary.org/obo/MONDO_<id>}.
     */
    public static final short unitMONDO = 240;

    /**
     * Unit of the NCBI Taxonomy terms, {@code http://purl.obolibrary.org/obo/NCBITaxon_<id>}.
     */
    public static final short unitNCBITaxon = 245;

    /**
     * Unit of the NCI Thesaurus terms of OBO, {@code http://purl.obolibrary.org/obo/NCIT_C<number>}.
     */
    public static final short unitNCIT = 246;

    /**
     * Unit of the PR terms with a nine-digit number, {@code http://purl.obolibrary.org/obo/PR_<number>}.
     */
    public static final short unitPR = 260;

    /**
     * Unit of the Relation Ontology terms, {@code http://purl.obolibrary.org/obo/RO_<id>}.
     */
    public static final short unitRO = 261;

    /**
     * Unit of the Uberon terms, {@code http://purl.obolibrary.org/obo/UBERON_<id>}.
     */
    public static final short unitUberon = 266;

    /**
     * Unit of the Units of Measurement Ontology terms, {@code http://purl.obolibrary.org/obo/UO_<id>}.
     */
    public static final short unitUO = 267;

    /**
     * Unit of the identifiers.org IRIs of BTO, {@code http://identifiers.org/BTO:<id>}.
     */
    public static final short unitIdentifiersBTO = 400;

    /**
     * Unit of the identifiers.org IRIs of ChEMBL, {@code http://identifiers.org/chembl:CHEMBL<id>}.
     */
    public static final short unitIdentifiersChEMBL = 401;

    /**
     * Unit of the identifiers.org IRIs of the Cell Ontology, {@code http://identifiers.org/CL:<id>}.
     */
    public static final short unitIdentifiersCL = 402;

    /**
     * Unit of the identifiers.org IRIs of the Cell Line Ontology, {@code http://identifiers.org/CLO:<id>}.
     */
    public static final short unitIdentifiersCLO = 403;

    /**
     * Unit of the identifiers.org IRIs of the CTD genes, {@code http://identifiers.org/ctd.gene:<id>}.
     */
    public static final short unitIdentifiersCTDGene = 404;

    /**
     * Unit of the identifiers.org IRIs of DOID with an unpadded number, {@code http://identifiers.org/DOID:<number>}.
     */
    public static final short unitIdentifiersDOID = 405;

    /**
     * Unit of the identifiers.org IRIs of DOID with a zero-padded seven-digit number,
     * {@code http://identifiers.org/DOID:0<digits>}.
     */
    public static final short unitIdentifiersDOID0 = 406;

    /**
     * Unit of the identifiers.org IRIs of EFO, {@code http://identifiers.org/efo:<id>}.
     */
    public static final short unitIdentifiersEFO = 407;

    /**
     * Unit of the identifiers.org IRIs of the FlyBase genes, {@code http://identifiers.org/fb:FBgn<id>}.
     */
    public static final short unitIdentifiersFB = 408;

    /**
     * Unit of the identifiers.org IRIs of HGNC, {@code http://identifiers.org/hgnc:<id>}.
     */
    public static final short unitIdentifiersHGNC = 409;

    /**
     * Unit of the identifiers.org IRIs of HPO, {@code http://identifiers.org/HP:<id>}.
     */
    public static final short unitIdentifiersHP = 410;

    /**
     * Unit of the identifiers.org IRIs of the Human Protein Atlas, {@code http://identifiers.org/hpa:ENSG00<id>}.
     */
    public static final short unitIdentifiersHPA = 411;

    /**
     * Unit of the identifiers.org IRIs of the IUPHAR receptors, {@code http://identifiers.org/iuphar.receptor:<id>}.
     */
    public static final short unitIdentifiersIUPHARReceptor = 412;

    /**
     * Unit of the identifiers.org IRIs of the KEGG diseases, {@code http://identifiers.org/kegg.disease:H<id>}.
     */
    public static final short unitIdentifiersKEGGDisease = 413;

    /**
     * Unit of the identifiers.org IRIs of the LINCS cells, {@code http://identifiers.org/lincs.cell:LCL-<id>}.
     */
    public static final short unitIdentifiersLINCSCell = 414;

    /**
     * Unit of the identifiers.org IRIs of MedGen, {@code http://identifiers.org/medgen:C<id>}.
     */
    public static final short unitIdentifiersMedGen = 415;

    /**
     * Unit of the identifiers.org IRIs of the MedGen CN concepts, {@code http://identifiers.org/medgen:CN<id>}.
     */
    public static final short unitIdentifiersMedGenCN = 416;

    /**
     * Unit of the identifiers.org IRIs of MGI, {@code http://identifiers.org/MGI:<id>}.
     */
    public static final short unitIdentifiersMGI = 417;

    /**
     * Unit of the identifiers.org IRIs of OMIM, {@code http://identifiers.org/mim:<id>}.
     */
    public static final short unitIdentifiersMIM = 418;

    /**
     * Unit of the identifiers.org IRIs of NANDO, {@code http://identifiers.org/NANDO:<id>}.
     */
    public static final short unitIdentifiersNANDO = 419;

    /**
     * Unit of the identifiers.org IRIs of the NCBI genes, {@code http://identifiers.org/ncbigene:<id>}.
     */
    public static final short unitIdentifiersNCBIGene = 420;

    /**
     * Unit of the identifiers.org IRIs of the NCI Thesaurus, {@code http://identifiers.org/ncit:C<id>}.
     */
    public static final short unitIdentifiersNCIT = 421;

    /**
     * Unit of the identifiers.org IRIs of Orphanet, {@code http://identifiers.org/orphanet:<id>}.
     */
    public static final short unitIdentifiersOrphanet = 422;

    /**
     * Unit of the identifiers.org IRIs of the PharmGKB diseases,
     * {@code http://identifiers.org/pharmgkb.disease:PA<id>}.
     */
    public static final short unitIdentifiersPharmGKBDisease = 423;

    /**
     * Unit of the identifiers.org IRIs of the PharmGKB genes, {@code http://identifiers.org/pharmgkb.gene:PA<id>}.
     */
    public static final short unitIdentifiersPharmGKBGene = 424;

    /**
     * Unit of the identifiers.org IRIs of RGD, {@code http://identifiers.org/rgd:<id>}.
     */
    public static final short unitIdentifiersRGD = 425;

    /**
     * Unit of the identifiers.org IRIs of SGD, {@code http://identifiers.org/sgd:S<id>}.
     */
    public static final short unitIdentifiersSGD = 426;

    /**
     * Unit of the identifiers.org IRIs of the NCBI Taxonomy, {@code http://identifiers.org/taxonomy/<id>}.
     */
    public static final short unitTaxonomy = 427;

    /**
     * Unit of the identifiers.org IRIs of Uberon, {@code http://identifiers.org/UBERON:<id>}.
     */
    public static final short unitIdentifiersUberon = 428;

    /**
     * Unit of the identifiers.org IRIs of UMLS, {@code http://identifiers.org/umls:C<id>}.
     */
    public static final short unitIdentifiersUMLS = 429;

    /**
     * Unit of the identifiers.org IRIs of the Xenbase genes, {@code http://identifiers.org/xenbase:XB-GENE-<id>}.
     */
    public static final short unitIdentifiersXenbaseGene = 430;

    /**
     * Unit of the identifiers.org IRIs of the Xenbase gene pages,
     * {@code http://identifiers.org/xenbase:XB-GENEPAGE-<id>}.
     */
    public static final short unitIdentifiersXenbaseGenePage = 431;

    /**
     * Unit of the EFO IRIs of the ChEBI terms, {@code http://www.ebi.ac.uk/efo/CHEBI:<id>}.
     */
    public static final short unitEFOCHEBI = 600;

    /**
     * Unit of the EFO IRIs of the DOID terms, {@code http://www.ebi.ac.uk/efo/DOID:<id>}.
     */
    public static final short unitEFODOID = 601;

    /**
     * Unit of the EFO terms without the underscore, {@code http://www.ebi.ac.uk/efo/EFO<id>}.
     */
    public static final short unitEFOShort = 602;

    /**
     * Unit of the EFO terms, {@code http://www.ebi.ac.uk/efo/EFO_<id>}.
     */
    public static final short unitEFO = 603;

    /**
     * Unit of the EFO IRIs of the GO terms, {@code http://www.ebi.ac.uk/efo/GO:<id>}.
     */
    public static final short unitEFOGO = 604;

    /**
     * Unit of the EFO IRIs of the MONDO terms, {@code http://www.ebi.ac.uk/efo/MONDO:<id>}.
     */
    public static final short unitEFOMONDO = 605;

    /**
     * Unit of the EFO IRIs of the NCBI Taxonomy terms, {@code http://www.ebi.ac.uk/efo/NCBITaxon:<id>}.
     */
    public static final short unitEFONCBITaxon = 606;

    /**
     * Unit of the EFO IRIs of the Uberon terms, {@code http://www.ebi.ac.uk/efo/UBERON:<id>}.
     */
    public static final short unitEFOUberon = 608;

    /**
     * Unit of the OLS IRIs of the Mammalian Phenotype terms,
     * {@code http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MP&termId=MP:<id>}.
     */
    public static final short unitOLSMP = 609;

    /**
     * Unit of the ChEMBL tissue report cards, {@code https://www.ebi.ac.uk/chembl/tissue_report_card/CHEMBL<id>}.
     */
    public static final short unitChEMBLTissue = 613;

    /**
     * Unit of the NCI Thesaurus terms, {@code http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C<number>}.
     */
    public static final short unitThesaurus = 801;

    /**
     * Unit of the GARD diseases, {@code https://rarediseases.info.nih.gov/diseases/<id>/index}.
     */
    public static final short unitGARD = 805;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 0,
     * {@code https://www.worldfloraonline.org/taxon/wfo-0<digits>}.
     */
    public static final short unitWFO0 = 1000;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 1,
     * {@code https://www.worldfloraonline.org/taxon/wfo-1<digits>}.
     */
    public static final short unitWFO1 = 1001;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 2,
     * {@code https://www.worldfloraonline.org/taxon/wfo-2<digits>}.
     */
    public static final short unitWFO2 = 1002;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 3,
     * {@code https://www.worldfloraonline.org/taxon/wfo-3<digits>}.
     */
    public static final short unitWFO3 = 1003;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 4,
     * {@code https://www.worldfloraonline.org/taxon/wfo-4<digits>}.
     */
    public static final short unitWFO4 = 1004;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 5,
     * {@code https://www.worldfloraonline.org/taxon/wfo-5<digits>}.
     */
    public static final short unitWFO5 = 1005;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 6,
     * {@code https://www.worldfloraonline.org/taxon/wfo-6<digits>}.
     */
    public static final short unitWFO6 = 1006;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 7,
     * {@code https://www.worldfloraonline.org/taxon/wfo-7<digits>}.
     */
    public static final short unitWFO7 = 1007;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 8,
     * {@code https://www.worldfloraonline.org/taxon/wfo-8<digits>}.
     */
    public static final short unitWFO8 = 1008;

    /**
     * Unit of the World Flora Online taxa whose ten-digit number begins with 9,
     * {@code https://www.worldfloraonline.org/taxon/wfo-9<digits>}.
     */
    public static final short unitWFO9 = 1009;

    /**
     * Unit of the WormBase genes, {@code http://www.wormbase.org/db/gene/gene?class=Gene;name=WBGene<id>}.
     */
    public static final short unitWormBaseGene = 1200;

    /**
     * Unit of the WormBase proteins of Brugia malayi,
     * {@code https://wormbase.org/db/seq/protein?class=Protein;name=BM<id>}.
     */
    public static final short unitWormBaseProteinBM = 1203;

    /**
     * Unit of the WormBase proteins of Caenorhabditis briggsae,
     * {@code https://wormbase.org/db/seq/protein?class=Protein;name=CBP<id>}.
     */
    public static final short unitWormBaseProteinCBP = 1204;

    /**
     * Unit of the WormBase proteins of Caenorhabditis elegans,
     * {@code https://wormbase.org/db/seq/protein?class=Protein;name=CE<id>}.
     */
    public static final short unitWormBaseProteinCE = 1205;

    /**
     * Unit of the POWO taxa with the version suffix {@code -1}, {@code https://powo.science.kew.org/taxon/<id>-1}.
     */
    public static final short unitPOWO1 = 1600;

    /**
     * Unit of the POWO taxa with the version suffix {@code -2}, {@code https://powo.science.kew.org/taxon/<id>-2}.
     */
    public static final short unitPOWO2 = 1601;

    /**
     * Unit of the POWO taxa with the version suffix {@code -3}, {@code https://powo.science.kew.org/taxon/<id>-3}.
     */
    public static final short unitPOWO3 = 1602;

    /**
     * Unit of the POWO taxa with the version suffix {@code -4}, {@code https://powo.science.kew.org/taxon/<id>-4}.
     */
    public static final short unitPOWO4 = 1603;

    /**
     * Unit of the POWO taxa with the version suffix {@code -5}, {@code https://powo.science.kew.org/taxon/<id>-5}.
     */
    public static final short unitPOWO5 = 1604;

    /**
     * Unit of the GlyCosmos glycogenes, {@code http://glycosmos.org/glycogene/<id>}.
     */
    public static final short unitGlycosmosGlycoGene = 1800;

    /**
     * Unit of the GlyCosmos diseases with an unpadded DOID number,
     * {@code https://glycosmos.org/diseases/DOID:<number>}.
     */
    public static final short unitGlycosmosDOID = 1801;

    /**
     * Unit of the GlyCosmos diseases with a zero-padded seven-digit DOID number,
     * {@code https://glycosmos.org/diseases/DOID:0<digits>}.
     */
    public static final short unitGlycosmosDOID0 = 1802;

    /**
     * Unit of the GlyCosmos organisms, {@code https://glycosmos.org/organisms/show/<id>}.
     */
    public static final short unitGlycosmosOrganism = 1803;

    /**
     * Unit of the Orphanet pages, {@code http://www.orpha.net/consor/cgi-bin/OC_Exp.php?Expert=<id>}.
     */
    public static final short unitOrphanet = 2000;

    /**
     * Unit of the IPNI names with the version suffix {@code -1}, {@code https://www.ipni.org/n/<id>-1}.
     */
    public static final short unitIPNI1 = 2100;

    /**
     * Unit of the IPNI names with the version suffix {@code -2}, {@code https://www.ipni.org/n/<id>-2}.
     */
    public static final short unitIPNI2 = 2101;

    /**
     * Unit of the IPNI names with the version suffix {@code -3}, {@code https://www.ipni.org/n/<id>-3}.
     */
    public static final short unitIPNI3 = 2102;

    /**
     * Unit of the IPNI names with the version suffix {@code -4}, {@code https://www.ipni.org/n/<id>-4}.
     */
    public static final short unitIPNI4 = 2103;

    /**
     * Unit of the GlyConnect cell lines, {@code https://glyconnect.expasy.org/all/cell_lines/<id>}.
     */
    public static final short unitGlyConnectCellLine = 2300;

    /**
     * Unit of the GlyConnect proteins, {@code https://glyconnect.expasy.org/all/proteins/<id>}.
     */
    public static final short unitGlyConnectProtein = 2301;

    /**
     * Unit of the GlyConnect taxonomies, {@code https://glyconnect.expasy.org/all/taxonomies/<id>}.
     */
    public static final short unitGlyConnectTaxonomy = 2302;

    /**
     * Unit of the NDF-RT terms, {@code http://purl.bioontology.org/ontology/NDFRT/N0<id>}.
     */
    public static final short unitNDFRT = 2400;

    /**
     * Unit of the SNOMED CT terms, {@code http://purl.bioontology.org/ontology/SNOMEDCT/<id>}.
     */
    public static final short unitSNOMEDCT = 2401;

    /**
     * Unit of the CHEMINF terms, {@code http://semanticscience.org/resource/CHEMINF_<id>}.
     */
    public static final short unitCHEMINF = 2600;

    /**
     * Unit of the SIO terms, {@code http://semanticscience.org/resource/SIO_<id>}.
     */
    public static final short unitSIO = 2601;

    /**
     * Unit of the COSMIC cell lines, {@code https://cancer.sanger.ac.uk/cell_lines/sample/overview?id=<id>}.
     */
    public static final short unitSangerCellLine = 2900;

    /**
     * Unit of the Cell Model Passports, {@code https://cellmodelpassports.sanger.ac.uk/passports/SIDM<id>}.
     */
    public static final short unitCellModelPassports = 2901;

    /**
     * Unit of the IUPHAR/BPS Guide to Pharmacology diseases,
     * {@code https://www.guidetopharmacology.org/GRAC/DiseaseDisplayForward?diseaseId=<id>}.
     */
    public static final short unitGuideToPharmacologyDisease = 3001;

    /**
     * Unit of the UniProt taxa, {@code http://purl.uniprot.org/taxonomy/<id>}.
     */
    public static final short unitUniProtTaxonomy = 3900;

    /**
     * Unit of the GlycoInfo sources, {@code http://rdf.glycoinfo.org/source/<id>}.
     */
    public static final short unitGlycoInfo = 4000;

    /**
     * Unit of the BAO terms, {@code http://www.bioassayontology.org/bao#BAO_<id>}.
     */
    public static final short unitBAO = 4100;

    /**
     * Unit of the DepMap cell lines, {@code https://depmap.org/portal/cell_line/ACH-<id>}.
     */
    public static final short unitDepMap = 4700;

    /**
     * Unit of the Encyclopedia of Life pages, {@code https://eol.org/pages/<id>}.
     */
    public static final short unitEOL = 4800;

    /**
     * Unit of the HMS LINCS cells, {@code https://lincs.hms.harvard.edu/db/cells/<id>}.
     */
    public static final short unitLINCSHMS = 4900;

    /**
     * Unit of the Open Targets targets, {@code https://platform.opentargets.org/target/ENSG00<id>}.
     */
    public static final short unitOpenTargets = 5200;

    /**
     * Unit of the GenCC genes, {@code https://search.thegencc.org/genes/HGNC:<id>}.
     */
    public static final short unitGenCC = 5300;

    /**
     * Unit of the GDSC cell lines, {@code https://www.cancerrxgene.org/translation/CellLine/<id>}.
     */
    public static final short unitCancerRxGene = 5400;

    /**
     * Unit of the DrugBank biological entities, {@code https://www.drugbank.ca/bio_entities/BE<id>}.
     */
    public static final short unitDrugBankBioEntity = 5500;

    /**
     * Unit of the ITIS taxa,
     * {@code https://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=<id>}.
     */
    public static final short unitITIS = 5600;

    /**
     * Names of the resource classes of the units that have a class of their own ({@link OntologyUnitResource}).
     */
    private static final Map<Short, String> unitClassNames = Map.ofEntries(
            Map.entry(unitUncategorized, "ontology:uncategorized"), Map.entry(unitBlank, "ontology:blank"),
            Map.entry(unitPR2, "ontology:pr2"), Map.entry(unitPR1, "ontology:pr1"), Map.entry(unitPR0, "ontology:pr0"),
            Map.entry(unitBFO, "ontology:bfo"), Map.entry(unitBTO, "ontology:bto"),
            Map.entry(unitStar, "ontology:star"), Map.entry(unitCHEBI, "ontology:chebi"),
            Map.entry(unitClassyFire, "ontology:classyfire"), Map.entry(unitCL, "ontology:cl"),
            Map.entry(unitCLO, "ontology:clo"), Map.entry(unitDOIDColon, "ontology:doid_colon"),
            Map.entry(unitDOIDColon0, "ontology:doid_colon0"), Map.entry(unitGO, "ontology:go"),
            Map.entry(unitHP, "ontology:hp"), Map.entry(unitIAO, "ontology:iao"),
            Map.entry(unitMONDO, "ontology:mondo"), Map.entry(unitNCBITaxon, "ontology:ncbitaxon"),
            Map.entry(unitNCIT, "ontology:ncit"), Map.entry(unitPR, "ontology:pr"), Map.entry(unitRO, "ontology:ro"),
            Map.entry(unitUberon, "ontology:uberon"), Map.entry(unitUO, "ontology:uo"),
            Map.entry(unitIdentifiersBTO, "ontology:identifiers_bto"),
            Map.entry(unitIdentifiersChEMBL, "ontology:identifiers_chembl"),
            Map.entry(unitIdentifiersCL, "ontology:identifiers_cl"),
            Map.entry(unitIdentifiersCLO, "ontology:identifiers_clo"),
            Map.entry(unitIdentifiersCTDGene, "ontology:identifiers_ctd_gene"),
            Map.entry(unitIdentifiersDOID, "ontology:identifiers_doid"),
            Map.entry(unitIdentifiersDOID0, "ontology:identifiers_doid0"),
            Map.entry(unitIdentifiersEFO, "ontology:identifiers_efo"),
            Map.entry(unitIdentifiersFB, "ontology:identifiers_fb"),
            Map.entry(unitIdentifiersHGNC, "ontology:identifiers_hgnc"),
            Map.entry(unitIdentifiersHP, "ontology:identifiers_hp"),
            Map.entry(unitIdentifiersHPA, "ontology:identifiers_hpa"),
            Map.entry(unitIdentifiersIUPHARReceptor, "ontology:identifiers_iuphar_receptor"),
            Map.entry(unitIdentifiersKEGGDisease, "ontology:identifiers_kegg_disease"),
            Map.entry(unitIdentifiersLINCSCell, "ontology:identifiers_lincs_cell"),
            Map.entry(unitIdentifiersMedGen, "ontology:identifiers_medgen"),
            Map.entry(unitIdentifiersMedGenCN, "ontology:identifiers_medgen_cn"),
            Map.entry(unitIdentifiersMGI, "ontology:identifiers_mgi"),
            Map.entry(unitIdentifiersMIM, "ontology:identifiers_mim"),
            Map.entry(unitIdentifiersNANDO, "ontology:identifiers_nando"),
            Map.entry(unitIdentifiersNCBIGene, "ontology:identifiers_ncbigene"),
            Map.entry(unitIdentifiersNCIT, "ontology:identifiers_ncit"),
            Map.entry(unitIdentifiersOrphanet, "ontology:identifiers_orphanet"),
            Map.entry(unitIdentifiersPharmGKBDisease, "ontology:identifiers_pharmgkb_disease"),
            Map.entry(unitIdentifiersPharmGKBGene, "ontology:identifiers_pharmgkb_gene"),
            Map.entry(unitIdentifiersRGD, "ontology:identifiers_rgd"),
            Map.entry(unitIdentifiersSGD, "ontology:identifiers_sgd"), Map.entry(unitTaxonomy, "ontology:taxonomy"),
            Map.entry(unitIdentifiersUberon, "ontology:identifiers_uberon"),
            Map.entry(unitIdentifiersUMLS, "ontology:identifiers_umls"),
            Map.entry(unitIdentifiersXenbaseGene, "ontology:identifiers_xenbase_gene"),
            Map.entry(unitIdentifiersXenbaseGenePage, "ontology:identifiers_xenbase_genepage"),
            Map.entry(unitEFOCHEBI, "ontology:efo_chebi"), Map.entry(unitEFODOID, "ontology:efo_doid"),
            Map.entry(unitEFOShort, "ontology:efo_short"), Map.entry(unitEFO, "ontology:efo"),
            Map.entry(unitEFOGO, "ontology:efo_go"), Map.entry(unitEFOMONDO, "ontology:efo_mondo"),
            Map.entry(unitEFONCBITaxon, "ontology:efo_ncbitaxon"), Map.entry(unitEFOUberon, "ontology:efo_uberon"),
            Map.entry(unitOLSMP, "ontology:ols_mp"), Map.entry(unitChEMBLTissue, "ontology:chembl_tissue"),
            Map.entry(unitThesaurus, "ontology:thesaurus"), Map.entry(unitGARD, "ontology:gard"),
            Map.entry(unitWFO0, "ontology:wfo0"), Map.entry(unitWFO1, "ontology:wfo1"),
            Map.entry(unitWFO2, "ontology:wfo2"), Map.entry(unitWFO3, "ontology:wfo3"),
            Map.entry(unitWFO4, "ontology:wfo4"), Map.entry(unitWFO5, "ontology:wfo5"),
            Map.entry(unitWFO6, "ontology:wfo6"), Map.entry(unitWFO7, "ontology:wfo7"),
            Map.entry(unitWFO8, "ontology:wfo8"), Map.entry(unitWFO9, "ontology:wfo9"),
            Map.entry(unitWormBaseGene, "ontology:wormbase_gene"),
            Map.entry(unitWormBaseProteinBM, "ontology:wormbase_protein_bm"),
            Map.entry(unitWormBaseProteinCBP, "ontology:wormbase_protein_cbp"),
            Map.entry(unitWormBaseProteinCE, "ontology:wormbase_protein_ce"), Map.entry(unitPOWO1, "ontology:powo1"),
            Map.entry(unitPOWO2, "ontology:powo2"), Map.entry(unitPOWO3, "ontology:powo3"),
            Map.entry(unitPOWO4, "ontology:powo4"), Map.entry(unitPOWO5, "ontology:powo5"),
            Map.entry(unitGlycosmosGlycoGene, "ontology:glycosmos_glycogene"),
            Map.entry(unitGlycosmosDOID, "ontology:glycosmos_doid"),
            Map.entry(unitGlycosmosDOID0, "ontology:glycosmos_doid0"),
            Map.entry(unitGlycosmosOrganism, "ontology:glycosmos_organism"),
            Map.entry(unitOrphanet, "ontology:orphanet"), Map.entry(unitIPNI1, "ontology:ipni1"),
            Map.entry(unitIPNI2, "ontology:ipni2"), Map.entry(unitIPNI3, "ontology:ipni3"),
            Map.entry(unitIPNI4, "ontology:ipni4"), Map.entry(unitGlyConnectCellLine, "ontology:glyconnect_cell_line"),
            Map.entry(unitGlyConnectProtein, "ontology:glyconnect_protein"),
            Map.entry(unitGlyConnectTaxonomy, "ontology:glyconnect_taxonomy"), Map.entry(unitNDFRT, "ontology:ndfrt"),
            Map.entry(unitSNOMEDCT, "ontology:snomedct"), Map.entry(unitCHEMINF, "ontology:cheminf"),
            Map.entry(unitSIO, "ontology:sio"), Map.entry(unitSangerCellLine, "ontology:sanger_cell_line"),
            Map.entry(unitCellModelPassports, "ontology:cellmodelpassports"),
            Map.entry(unitGuideToPharmacologyDisease, "ontology:guidetopharmacology_disease"),
            Map.entry(unitUniProtTaxonomy, "ontology:uniprot_taxonomy"), Map.entry(unitGlycoInfo, "ontology:glycoinfo"),
            Map.entry(unitBAO, "ontology:bao"), Map.entry(unitDepMap, "ontology:depmap"),
            Map.entry(unitEOL, "ontology:eol"), Map.entry(unitLINCSHMS, "ontology:lincs_hms"),
            Map.entry(unitOpenTargets, "ontology:opentargets"), Map.entry(unitGenCC, "ontology:gencc"),
            Map.entry(unitCancerRxGene, "ontology:cancerrxgene"),
            Map.entry(unitDrugBankBioEntity, "ontology:drugbank_bio_entity"), Map.entry(unitITIS, "ontology:itis"));

    /**
     * Bound of the ids reserved for the built-in uncategorized resources, which have fixed ids; the ontology loader
     * numbers the other uncategorized resources from it.
     */
    public static final int builtinResourceLimit = 100000;


    /**
     * Creates the class over the units loaded by {@link #get(SparqlDatabaseConfiguration)}.
     */
    private OntologyResource()
    {
        super("ontology:resource", "ontology", "resource", List.of(INT2, INT4),
                units.stream().map(c -> c.pattern.pattern()).collect(Collectors.joining("|")),
                GenericUserIriClass.SqlCheck.IF_NOT_MATCH);
    }


    @Override
    public List<Column> toColumns(Request request, Iri iri)
    {
        String val = iri.getValue();

        assert match(request, iri);

        Unit unit = findUnit(val);

        if(unit == null)
            return List.of(constant(unitUncategorized, INT2), constant(findResourceId(request, val), INT4));

        int id = parseId(unit.id, val.substring(unit.valueOffset - 1, val.length() - unit.suffix.length()));

        return List.of(constant(unit.id, INT2), constant(id, INT4));
    }


    @Override
    public List<Column> toGeneralClass(ResourceClass superClass, List<Column> columns, boolean canBeNull)
    {
        ResourceClass targetClass = superClass.getEffectiveClass();

        if(targetClass.equals(this))
            return columns;

        String code = getInverseCode(columns);

        if(targetClass.equals(box))
            return List.of(new ExpressionColumn("sparql.rdfbox_create_from_iri(" + code + ")", RDFBOX));

        if(targetClass.equals(iri))
            return List.of(new ExpressionColumn(code, VARCHAR));

        throw new IllegalArgumentException();
    }


    @Override
    public List<Column> toOrderColumns(List<Column> columns)
    {
        Unit unit = unitMap.get(columns.get(0));

        if(unit == null || unit.valueLength < 0)
        {
            return super.toOrderColumns(columns);
        }
        else if(unit.valueLength == 0)
        {
            StringBuilder builder = new StringBuilder();

            builder.append("(");
            builder.append(columns.get(1));
            builder.append(")::varchar");
            appendSuffix(builder, unit);

            return List.of(new ExpressionColumn(builder.toString(), VARCHAR));
        }
        else
        {
            StringBuilder builder = new StringBuilder();

            builder.append("lpad((");
            builder.append(columns.get(1));
            builder.append(")::varchar, ");
            builder.append(unit.valueLength);
            builder.append(", '0')::varchar");
            appendSuffix(builder, unit);

            return List.of(new ExpressionColumn(builder.toString(), VARCHAR));
        }
    }


    @Override
    public String getPrefix(List<Column> columns)
    {
        Unit unit = unitMap.get(columns.get(0));

        if(unit == null)
            return "";

        return unit.prefix();
    }


    @Override
    public boolean match(Request request, Iri iri)
    {
        String val = iri.getValue();

        return findUnit(val) != null || findResourceId(request, val) != null;
    }


    /**
     * Returns the unit of the given id.
     *
     * @param id the id of the unit
     * @return the unit, or null if there is none
     */
    Unit getUnit(short id)
    {
        return unitMap.get(constant(id, INT2));
    }


    /**
     * Returns the units that have a resource class of their own, in the order of their ids.
     *
     * @return the ids of the units
     */
    public static List<Short> getClassUnits()
    {
        return unitClassNames.keySet().stream().sorted().toList();
    }


    /**
     * Returns the name of the resource class of the unit; a unit without a class is refused.
     *
     * @param unit the id of the unit
     * @return the name of the class
     * @throws IllegalArgumentException if the unit has no resource class
     */
    public static String getClassName(short unit)
    {
        String name = unitClassNames.get(unit);

        if(name == null)
            throw new IllegalArgumentException("ontology unit " + unit + " has no resource class");

        return name;
    }


    /**
     * Finds the unit whose pattern the IRI matches.
     *
     * @param iri the IRI
     * @return the unit, or null if the IRI matches no unit
     */
    Unit findUnit(String iri)
    {
        for(Unit unit : units)
            if(unit.pattern.matcher(iri).matches())
                return unit;

        return null;
    }


    /**
     * Looks up the id of an uncategorized resource.
     *
     * @param request the current request
     * @param iri the IRI of the resource
     * @return the id of the resource, or null if the IRI is not an uncategorized resource
     */
    Integer findResourceId(Request request, String iri)
    {
        try
        {
            String sql = sqlQuery.replace("?", string(iri));

            try(ResultSet result = request.getStatement().executeQuery(sql))
            {
                return result.next() ? result.getInt(1) : null;
            }
        }
        catch(SQLException e)
        {
            throw new SQLRuntimeException(e);
        }
    }


    /**
     * Returns the SQL code building the IRI from the unit and id columns: from the prefix, the value and the suffix for
     * a constant unit of a numeric value, by the SQL function of the class otherwise.
     *
     * @param columns the unit and id columns
     * @return the SQL expression of the IRI
     */
    private String getInverseCode(List<Column> columns)
    {
        StringBuilder builder = new StringBuilder();
        Unit unit = unitMap.get(columns.get(0));

        if(unit == null || unit.valueLength < 0)
        {
            builder.append(function);
            builder.append("(");

            for(int i = 0; i < getColumnCount(); i++)
            {
                if(i > 0)
                    builder.append(", ");

                builder.append(columns.get(i));
            }

            builder.append(")");
        }
        else if(unit.valueLength == 0)
        {
            builder.append("('");
            builder.append(unit.prefix().replaceAll("'", "''"));
            builder.append("' || (");
            builder.append(columns.get(1));
            builder.append(")::varchar");
            appendSuffix(builder, unit);
            builder.append(")");
        }
        else
        {
            builder.append("('");
            builder.append(unit.prefix().replaceAll("'", "''"));
            builder.append("' || lpad((");
            builder.append(columns.get(1));
            builder.append(")::varchar, ");
            builder.append(unit.valueLength);
            builder.append(", '0')::varchar");
            appendSuffix(builder, unit);
            builder.append(")");
        }

        return builder.toString();
    }


    /**
     * Appends the concatenation of the suffix of the unit, if it has one, to SQL code.
     *
     * @param builder the builder of the code
     * @param unit the unit
     */
    private static void appendSuffix(StringBuilder builder, Unit unit)
    {
        if(unit.suffix.isEmpty())
            return;

        builder.append(" || '");
        builder.append(unit.suffix.replaceAll("'", "''"));
        builder.append("'");
    }


    /**
     * Encodes the value part of an IRI of a unit as the id of the resource: the UniProt accessions of the units
     * {@link #unitPR0}, {@link #unitPR1} and {@link #unitPR2} are packed into an integer, the values of the other units
     * are plain numbers.
     *
     * @param unit the id of the unit
     * @param value the value part of the IRI
     * @return the id of the resource
     */
    public static int parseId(int unit, String value)
    {
        int id = 0;

        if(unit == unitPR0)
        {
            // [0-9][A-Z0-9][0-9][A-Z0-9]{3}[0-9]
            id = value.charAt(0) - '0';
            id = id * 36 + code(value.charAt(1));
            id = id * 10 + value.charAt(2) - '0';
            id = id * 36 + code(value.charAt(3));
            id = id * 36 + code(value.charAt(4));
            id = id * 36 + code(value.charAt(5));
            id = id * 10 + value.charAt(6) - '0';
        }
        else if(unit == unitPR1 || unit == unitPR2)
        {
            // [A-Z][0-9][A-Z0-9]{3}[0-9](-([12])?[0-9])?
            id = value.charAt(0) - 'A';
            id = id * 10 + value.charAt(1) - '0';
            id = id * 36 + code(value.charAt(2));
            id = id * 36 + code(value.charAt(3));
            id = id * 36 + code(value.charAt(4));
            id = id * 10 + value.charAt(5) - '0';

            if(unit == unitPR1)
                id = id * 30 + Integer.parseInt(value.substring(7));
        }
        else
        {
            id = Integer.parseInt(value);
        }

        return id;
    }


    /**
     * Returns the value of a base-36 digit.
     *
     * @param value a digit or an upper-case letter
     * @return the value of the digit
     */
    private static int code(char value)
    {
        return value > '9' ? 10 + value - 'A' : value - '0';
    }


    /**
     * Returns the class, loading the units from {@code ontology.units} on the first call.
     *
     * @param config the configuration providing the connection pool
     * @return the class
     * @throws SQLException if a database error occurs
     */
    public static synchronized OntologyResource get(SparqlDatabaseConfiguration config) throws SQLException
    {
        if(instance != null)
            return instance;

        try(Connection connection = config.getConnectionPool().getConnection())
        {
            connection.setAutoCommit(true);

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet r = statement
                        .executeQuery("select id, prefix, value_offset, value_length, suffix, pattern "
                                + "from ontology.units order by id"))
                {
                    while(r.next())
                    {
                        Unit unit = new Unit(r.getShort(1), r.getString(2), r.getInt(3), r.getInt(4), r.getString(5),
                                Pattern.compile(r.getString(6)));
                        units.add(unit);
                        unitMap.put(new ValueColumn(Short.toString(unit.id), INT2), unit);
                    }
                }
            }
        }

        instance = new OntologyResource();

        return instance;
    }
}
