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



public class OntologyResource extends GenericUserIriClass
{
    static record Unit(short id, String prefix, int valueOffset, int valueLength, String suffix, Pattern pattern)
    {
    }


    private static final String sqlQuery = "select id from ontology.uncategorized_resources where iri = ?";
    private static OntologyResource instance;
    private static List<Unit> units = new ArrayList<>();
    private static Map<Column, Unit> unitMap = new HashMap<>();

    public static final short unitUncategorized = 0;
    public static final short unitBlank = 1;
    public static final short unitPR2 = 100;
    public static final short unitPR1 = 101;
    public static final short unitPR0 = 102;
    public static final short unitBFO = 202;
    public static final short unitBTO = 205;
    public static final short unitStar = 208;
    public static final short unitCHEBI = 209;
    public static final short unitClassyFire = 210;
    public static final short unitCL = 211;
    public static final short unitCLO = 212;
    public static final short unitDOIDColon = 216;
    public static final short unitDOIDColon0 = 217;
    public static final short unitGO = 233;
    public static final short unitHP = 235;
    public static final short unitIAO = 236;
    public static final short unitMONDO = 240;
    public static final short unitNCBITaxon = 245;
    public static final short unitNCIT = 246;
    public static final short unitPR = 260;
    public static final short unitRO = 261;
    public static final short unitUberon = 266;
    public static final short unitUO = 267;
    public static final short unitIdentifiersBTO = 400;
    public static final short unitIdentifiersChEMBL = 401;
    public static final short unitIdentifiersCL = 402;
    public static final short unitIdentifiersCLO = 403;
    public static final short unitIdentifiersCTDGene = 404;
    public static final short unitIdentifiersDOID = 405;
    public static final short unitIdentifiersDOID0 = 406;
    public static final short unitIdentifiersEFO = 407;
    public static final short unitIdentifiersFB = 408;
    public static final short unitIdentifiersHGNC = 409;
    public static final short unitIdentifiersHP = 410;
    public static final short unitIdentifiersHPA = 411;
    public static final short unitIdentifiersIUPHARReceptor = 412;
    public static final short unitIdentifiersKEGGDisease = 413;
    public static final short unitIdentifiersLINCSCell = 414;
    public static final short unitIdentifiersMedGen = 415;
    public static final short unitIdentifiersMedGenCN = 416;
    public static final short unitIdentifiersMGI = 417;
    public static final short unitIdentifiersMIM = 418;
    public static final short unitIdentifiersNANDO = 419;
    public static final short unitIdentifiersNCBIGene = 420;
    public static final short unitIdentifiersNCIT = 421;
    public static final short unitIdentifiersOrphanet = 422;
    public static final short unitIdentifiersPharmGKBDisease = 423;
    public static final short unitIdentifiersPharmGKBGene = 424;
    public static final short unitIdentifiersRGD = 425;
    public static final short unitIdentifiersSGD = 426;
    public static final short unitTaxonomy = 427;
    public static final short unitIdentifiersUberon = 428;
    public static final short unitIdentifiersUMLS = 429;
    public static final short unitIdentifiersXenbaseGene = 430;
    public static final short unitIdentifiersXenbaseGenePage = 431;
    public static final short unitEFOCHEBI = 600;
    public static final short unitEFODOID = 601;
    public static final short unitEFOShort = 602;
    public static final short unitEFO = 603;
    public static final short unitEFOGO = 604;
    public static final short unitEFOMONDO = 605;
    public static final short unitEFONCBITaxon = 606;
    public static final short unitEFOUberon = 608;
    public static final short unitOLSMP = 609;
    public static final short unitChEMBLTissue = 613;
    public static final short unitThesaurus = 801;
    public static final short unitGARD = 805;
    public static final short unitWFO0 = 1000;
    public static final short unitWFO1 = 1001;
    public static final short unitWFO2 = 1002;
    public static final short unitWFO3 = 1003;
    public static final short unitWFO4 = 1004;
    public static final short unitWFO5 = 1005;
    public static final short unitWFO6 = 1006;
    public static final short unitWFO7 = 1007;
    public static final short unitWFO8 = 1008;
    public static final short unitWFO9 = 1009;
    public static final short unitWormBaseGene = 1200;
    public static final short unitWormBaseProteinBM = 1203;
    public static final short unitWormBaseProteinCBP = 1204;
    public static final short unitWormBaseProteinCE = 1205;
    public static final short unitPOWO1 = 1600;
    public static final short unitPOWO2 = 1601;
    public static final short unitPOWO3 = 1602;
    public static final short unitPOWO4 = 1603;
    public static final short unitPOWO5 = 1604;
    public static final short unitGlycosmosGlycoGene = 1800;
    public static final short unitGlycosmosDOID = 1801;
    public static final short unitGlycosmosDOID0 = 1802;
    public static final short unitGlycosmosOrganism = 1803;
    public static final short unitOrphanet = 2000;
    public static final short unitIPNI1 = 2100;
    public static final short unitIPNI2 = 2101;
    public static final short unitIPNI3 = 2102;
    public static final short unitIPNI4 = 2103;
    public static final short unitGlyConnectCellLine = 2300;
    public static final short unitGlyConnectProtein = 2301;
    public static final short unitGlyConnectTaxonomy = 2302;
    public static final short unitNDFRT = 2400;
    public static final short unitSNOMEDCT = 2401;
    public static final short unitCHEMINF = 2600;
    public static final short unitSIO = 2601;
    public static final short unitSangerCellLine = 2900;
    public static final short unitCellModelPassports = 2901;
    public static final short unitGuideToPharmacologyDisease = 3001;
    public static final short unitUniProtTaxonomy = 3900;
    public static final short unitGlycoInfo = 4000;
    public static final short unitBAO = 4100;
    public static final short unitDepMap = 4700;
    public static final short unitEOL = 4800;
    public static final short unitLINCSHMS = 4900;
    public static final short unitOpenTargets = 5200;
    public static final short unitGenCC = 5300;
    public static final short unitCancerRxGene = 5400;
    public static final short unitDrugBankBioEntity = 5500;
    public static final short unitITIS = 5600;

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

    public static final int builtinResourceLimit = 100000;


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


    Unit getUnit(short id)
    {
        return unitMap.get(constant(id, INT2));
    }


    /*
     * Returns the units that have a resource class of their own, in the order of their ids.
     */
    public static List<Short> getClassUnits()
    {
        return unitClassNames.keySet().stream().sorted().toList();
    }


    /*
     * Returns the name of the resource class of the unit; a unit without a class is refused.
     */
    public static String getClassName(short unit)
    {
        String name = unitClassNames.get(unit);

        if(name == null)
            throw new IllegalArgumentException("ontology unit " + unit + " has no resource class");

        return name;
    }


    Unit findUnit(String iri)
    {
        for(Unit unit : units)
            if(unit.pattern.matcher(iri).matches())
                return unit;

        return null;
    }


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


    private static void appendSuffix(StringBuilder builder, Unit unit)
    {
        if(unit.suffix.isEmpty())
            return;

        builder.append(" || '");
        builder.append(unit.suffix.replaceAll("'", "''"));
        builder.append("'");
    }


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


    private static int code(char value)
    {
        return value > '9' ? 10 + value - 'A' : value - '0';
    }


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
