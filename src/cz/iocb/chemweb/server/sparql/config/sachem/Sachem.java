package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdDouble;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdInteger;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdString;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdBooleanIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdDoubleIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdIntegerIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdStringIri;
import java.util.HashMap;
import java.util.List;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.database.Column;
import cz.iocb.chemweb.server.sparql.database.Function;
import cz.iocb.chemweb.server.sparql.mapping.classes.EnumUserIriClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.UserIriClass;
import cz.iocb.chemweb.server.sparql.mapping.extension.ParameterDefinition;
import cz.iocb.chemweb.server.sparql.mapping.extension.ProcedureDefinition;
import cz.iocb.chemweb.server.sparql.mapping.extension.ResultDefinition;
import cz.iocb.chemweb.server.sparql.parser.model.IRI;
import cz.iocb.chemweb.server.sparql.parser.model.expression.Literal;



public abstract class Sachem
{
    public static void addPrefixes(SparqlDatabaseConfiguration config)
    {
        config.addPrefix("sachem", "http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#");
    }


    @SuppressWarnings("serial")
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        String sachem = config.getPrefixes().get("sachem");

        config.addIriClass(new EnumUserIriClass("query_format", "sachem.query_format", new HashMap<String, String>()
        {
            {
                put("UNSPECIFIED", sachem + "UnspecifiedFormat");
                put("SMILES", sachem + "SMILES");
                put("MOLFILE", sachem + "MolFile");
                put("RGROUP", sachem + "RGroup");
            }
        }));

        config.addIriClass(new EnumUserIriClass("search_mode", "sachem.search_mode", new HashMap<String, String>()
        {
            {
                put("SUBSTRUCTURE", sachem + "substructureSearch");
                put("EXACT", sachem + "exactSearch");
            }
        }));

        config.addIriClass(new EnumUserIriClass("charge_mode", "sachem.charge_mode", new HashMap<String, String>()
        {
            {
                put("IGNORE", sachem + "ignoreCharges");
                put("DEFAULT_AS_UNCHARGED", sachem + "defaultChargeAsZero");
                put("DEFAULT_AS_ANY", sachem + "defaultChargeAsAny");
            }
        }));

        config.addIriClass(new EnumUserIriClass("isotope_mode", "sachem.isotope_mode", new HashMap<String, String>()
        {
            {
                put("IGNORE", sachem + "ignoreIsotopes");
                put("DEFAULT_AS_STANDARD", sachem + "defaultIsotopeAsStandard");
                put("DEFAULT_AS_ANY", sachem + "defaultIsotopeAsAny");
            }
        }));

        config.addIriClass(new EnumUserIriClass("radical_mode", "sachem.radical_mode", new HashMap<String, String>()
        {
            {
                put("IGNORE", sachem + "ignoreSpinMultiplicity");
                put("DEFAULT_AS_STANDARD", sachem + "defaultSpinMultiplicityAsZero");
                put("DEFAULT_AS_ANY", sachem + "defaultSpinMultiplicityAsAny");
            }
        }));

        config.addIriClass(new EnumUserIriClass("stereo_mode", "sachem.stereo_mode", new HashMap<String, String>()
        {
            {
                put("IGNORE", sachem + "ignoreStereo");
                put("STRICT", sachem + "strictStereo");
            }
        }));

        config.addIriClass(
                new EnumUserIriClass("aromaticity_mode", "sachem.aromaticity_mode", new HashMap<String, String>()
                {
                    {
                        put("PRESERVE", sachem + "aromaticityFromQuery");
                        put("DETECT", sachem + "aromaticityDetect");
                        put("AUTO", sachem + "aromaticityDetectIfMissing");
                    }
                }));

        config.addIriClass(new EnumUserIriClass("tautomer_mode", "sachem.tautomer_mode", new HashMap<String, String>()
        {
            {
                put("IGNORE", sachem + "ignoreTautomers");
                put("INCHI", sachem + "inchiTautomers");
            }
        }));
    }


    public static void addProcedures(SparqlDatabaseConfiguration config, String index, String compoundClass,
            List<Column> compoundFields)
    {
        String sachem = config.getPrefixes().get("sachem");
        UserIriClass compound = config.getIriClass(compoundClass);


        /* sachem:exactSearch */
        ProcedureDefinition exactsearch = new ProcedureDefinition(sachem + "exactSearch",
                new Function("sachem", "substructure_search_stub"));

        exactsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringIri)));
        exactsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        exactsearch.addParameter(new ParameterDefinition(sachem + "searchMode", config.getIriClass("search_mode"),
                new IRI(sachem + "exactSearch")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "chargeMode", config.getIriClass("charge_mode"),
                new IRI(sachem + "defaultChargeAsZero")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "isotopeMode", config.getIriClass("isotope_mode"),
                new IRI(sachem + "defaultIsotopeAsStandard")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "radicalMode", config.getIriClass("radical_mode"),
                new IRI(sachem + "defaultSpinMultiplicityAsZero")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "stereoMode", config.getIriClass("stereo_mode"),
                new IRI(sachem + "strictStereo")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IRI(sachem + "aromaticityDetect")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IRI(sachem + "ignoreTautomers")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IRI(sachem + "UnspecifiedFormat")));
        exactsearch
                .addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerIri)));
        exactsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanIri)));
        exactsearch.addParameter(
                new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger, new Literal("0", xsdIntegerIri)));

        exactsearch.addResult(new ResultDefinition(null, compound, compoundFields));
        config.addProcedure(exactsearch);


        /* sachem:substructureSearch */
        ProcedureDefinition subsearch = new ProcedureDefinition(sachem + "substructureSearch",
                new Function("sachem", "substructure_search_stub"));

        subsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringIri)));
        subsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        subsearch.addParameter(new ParameterDefinition(sachem + "searchMode", config.getIriClass("search_mode"),
                new IRI(sachem + "substructureSearch")));
        subsearch.addParameter(new ParameterDefinition(sachem + "chargeMode", config.getIriClass("charge_mode"),
                new IRI(sachem + "defaultChargeAsAny")));
        subsearch.addParameter(new ParameterDefinition(sachem + "isotopeMode", config.getIriClass("isotope_mode"),
                new IRI(sachem + "ignoreIsotopes")));
        subsearch.addParameter(new ParameterDefinition(sachem + "radicalMode", config.getIriClass("radical_mode"),
                new IRI(sachem + "ignoreSpinMultiplicity")));
        subsearch.addParameter(new ParameterDefinition(sachem + "stereoMode", config.getIriClass("stereo_mode"),
                new IRI(sachem + "ignoreStereo")));
        subsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IRI(sachem + "aromaticityDetect")));
        subsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IRI(sachem + "ignoreTautomers")));
        subsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IRI(sachem + "UnspecifiedFormat")));
        subsearch.addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerIri)));
        subsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanIri)));
        subsearch.addParameter(
                new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger, new Literal("0", xsdIntegerIri)));

        subsearch.addResult(new ResultDefinition(null, compound, compoundFields));
        config.addProcedure(subsearch);


        /* sachem:scoredSubstructureSearch */
        ProcedureDefinition scoredsubsearch = new ProcedureDefinition(sachem + "scoredSubstructureSearch",
                new Function("sachem", "substructure_search_stub"));

        scoredsubsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringIri)));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "searchMode", config.getIriClass("search_mode"),
                new IRI(sachem + "substructureSearch")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "chargeMode", config.getIriClass("charge_mode"),
                new IRI(sachem + "defaultChargeAsAny")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "isotopeMode", config.getIriClass("isotope_mode"),
                new IRI(sachem + "ignoreIsotopes")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "radicalMode", config.getIriClass("radical_mode"),
                new IRI(sachem + "ignoreSpinMultiplicity")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "stereoMode", config.getIriClass("stereo_mode"),
                new IRI(sachem + "ignoreStereo")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IRI(sachem + "aromaticityDetect")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode",
                config.getIriClass("tautomer_mode"), new IRI(sachem + "ignoreTautomers")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IRI(sachem + "UnspecifiedFormat")));
        scoredsubsearch
                .addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerIri)));
        scoredsubsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanIri)));
        scoredsubsearch.addParameter(
                new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger, new Literal("0", xsdIntegerIri)));

        scoredsubsearch.addResult(new ResultDefinition(sachem + "compound", compound, compoundFields));
        scoredsubsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(scoredsubsearch);


        /* sachem:similaritySearch */
        ProcedureDefinition simsearch = new ProcedureDefinition(sachem + "similaritySearch",
                new Function("sachem", "similarity_search_stub"));

        simsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringIri)));
        simsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simsearch.addParameter(new ParameterDefinition(sachem + "cutoff", xsdDouble, new Literal("0.8", xsdDoubleIri)));
        simsearch.addParameter(
                new ParameterDefinition(sachem + "similarityRadius", xsdInteger, new Literal("1", xsdIntegerIri)));
        simsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IRI(sachem + "aromaticityDetect")));
        simsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IRI(sachem + "ignoreTautomers")));
        simsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IRI(sachem + "UnspecifiedFormat")));
        simsearch.addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerIri)));
        simsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanIri)));

        simsearch.addResult(new ResultDefinition(sachem + "compound", compound, compoundFields));
        simsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(simsearch);


        /* sachem:similarCompoundSearch */
        ProcedureDefinition simcmpsearch = new ProcedureDefinition(sachem + "similarCompoundSearch",
                new Function("sachem", "similarity_search_stub"));

        simcmpsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringIri)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simcmpsearch
                .addParameter(new ParameterDefinition(sachem + "cutoff", xsdDouble, new Literal("0.8", xsdDoubleIri)));
        simcmpsearch.addParameter(
                new ParameterDefinition(sachem + "similarityRadius", xsdInteger, new Literal("1", xsdIntegerIri)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IRI(sachem + "aromaticityDetect")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IRI(sachem + "ignoreTautomers")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IRI(sachem + "UnspecifiedFormat")));
        simcmpsearch
                .addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerIri)));
        simcmpsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanIri)));

        simcmpsearch.addResult(new ResultDefinition(null, compound, compoundFields));
        config.addProcedure(simcmpsearch);
    }
}
