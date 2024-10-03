package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDouble;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInteger;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinDataTypes.xsdBooleanType;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinDataTypes.xsdDoubleType;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinDataTypes.xsdIntegerType;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinDataTypes.xsdStringType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Function;
import cz.iocb.sparql.engine.mapping.classes.EnumUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.mapping.extension.FunctionDefinition;
import cz.iocb.sparql.engine.mapping.extension.ParameterDefinition;
import cz.iocb.sparql.engine.mapping.extension.ProcedureDefinition;
import cz.iocb.sparql.engine.mapping.extension.ResultDefinition;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.parser.model.expression.Literal;



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

        config.addIriClass(new EnumUserIriClass("query_format", "varchar", new HashMap<IRI, String>()
        {
            {
                put(new IRI(sachem + "UnspecifiedFormat"), "UNSPECIFIED");
                put(new IRI(sachem + "SMILES"), "SMILES");
                put(new IRI(sachem + "MolFile"), "MOLFILE");
                put(new IRI(sachem + "RGroup"), "RGROUP");
            }
        }));

        config.addIriClass(new EnumUserIriClass("search_mode", "sachem.search_mode", new HashMap<IRI, String>()
        {
            {
                put(new IRI(sachem + "substructureSearch"), "SUBSTRUCTURE");
                put(new IRI(sachem + "exactSearch"), "EXACT");
            }
        }));

        config.addIriClass(new EnumUserIriClass("charge_mode", "sachem.charge_mode", new HashMap<IRI, String>()
        {
            {
                put(new IRI(sachem + "ignoreCharges"), "IGNORE");
                put(new IRI(sachem + "defaultChargeAsZero"), "DEFAULT_AS_UNCHARGED");
                put(new IRI(sachem + "defaultChargeAsAny"), "DEFAULT_AS_ANY");
            }
        }));

        config.addIriClass(new EnumUserIriClass("isotope_mode", "sachem.isotope_mode", new HashMap<IRI, String>()
        {
            {
                put(new IRI(sachem + "ignoreIsotopes"), "IGNORE");
                put(new IRI(sachem + "defaultIsotopeAsStandard"), "DEFAULT_AS_STANDARD");
                put(new IRI(sachem + "defaultIsotopeAsAny"), "DEFAULT_AS_ANY");
            }
        }));

        config.addIriClass(new EnumUserIriClass("radical_mode", "sachem.radical_mode", new HashMap<IRI, String>()
        {
            {
                put(new IRI(sachem + "ignoreSpinMultiplicity"), "IGNORE");
                put(new IRI(sachem + "defaultSpinMultiplicityAsZero"), "DEFAULT_AS_STANDARD");
                put(new IRI(sachem + "defaultSpinMultiplicityAsAny"), "DEFAULT_AS_ANY");
            }
        }));

        config.addIriClass(new EnumUserIriClass("stereo_mode", "sachem.stereo_mode", new HashMap<IRI, String>()
        {
            {
                put(new IRI(sachem + "ignoreStereo"), "IGNORE");
                put(new IRI(sachem + "strictStereo"), "STRICT");
            }
        }));

        config.addIriClass(
                new EnumUserIriClass("aromaticity_mode", "sachem.aromaticity_mode", new HashMap<IRI, String>()
                {
                    {
                        put(new IRI(sachem + "aromaticityFromQuery"), "PRESERVE");
                        put(new IRI(sachem + "aromaticityDetect"), "DETECT");
                        put(new IRI(sachem + "aromaticityDetectIfMissing"), "AUTO");
                    }
                }));

        config.addIriClass(new EnumUserIriClass("tautomer_mode", "sachem.tautomer_mode", new HashMap<IRI, String>()
        {
            {
                put(new IRI(sachem + "ignoreTautomers"), "IGNORE");
                put(new IRI(sachem + "inchiTautomers"), "INCHI");
            }
        }));
    }


    public static void addProcedures(SparqlDatabaseConfiguration config, String index, String compoundClass,
            List<Column> compoundFields)
    {
        addProcedures(config, index, "sachem", compoundClass, compoundFields);
    }


    public static void addProcedures(SparqlDatabaseConfiguration config, String index, String schema,
            String compoundClass, List<Column> compoundFields)
    {
        String sachem = config.getPrefixes().get("sachem");
        UserIriClass compound = config.getIriClass(compoundClass);


        /* sachem:exactSearch */
        ProcedureDefinition exactsearch = new ProcedureDefinition(sachem + "exactSearch",
                new Function(schema, "substructure_search_stub"));

        exactsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringType)));
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
                .addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        exactsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));
        exactsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new Literal("0", xsdIntegerType)));

        exactsearch.addResult(new ResultDefinition(null, compound, compoundFields));
        config.addProcedure(exactsearch);


        /* sachem:substructureSearch */
        ProcedureDefinition subsearch = new ProcedureDefinition(sachem + "substructureSearch",
                new Function(schema, "substructure_search_stub"));

        subsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringType)));
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
        subsearch.addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        subsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));
        subsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new Literal("0", xsdIntegerType)));

        subsearch.addResult(new ResultDefinition(null, compound, compoundFields));
        config.addProcedure(subsearch);


        /* sachem:scoredSubstructureSearch */
        ProcedureDefinition scoredsubsearch = new ProcedureDefinition(sachem + "scoredSubstructureSearch",
                new Function(schema, "substructure_search_stub"));

        scoredsubsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringType)));
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
                .addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        scoredsubsearch
                .addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new Literal("0", xsdIntegerType)));

        scoredsubsearch.addResult(new ResultDefinition(sachem + "compound", compound, compoundFields));
        scoredsubsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(scoredsubsearch);


        /* sachem:similaritySearch */
        ProcedureDefinition simsearch = new ProcedureDefinition(sachem + "similaritySearch",
                new Function(schema, "similarity_search_stub"));

        simsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringType)));
        simsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simsearch
                .addParameter(new ParameterDefinition(sachem + "cutoff", xsdDouble, new Literal("0.8", xsdDoubleType)));
        simsearch.addParameter(
                new ParameterDefinition(sachem + "similarityRadius", xsdInteger, new Literal("1", xsdIntegerType)));
        simsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IRI(sachem + "aromaticityDetect")));
        simsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IRI(sachem + "ignoreTautomers")));
        simsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IRI(sachem + "UnspecifiedFormat")));
        simsearch.addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        simsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));

        simsearch.addResult(new ResultDefinition(sachem + "compound", compound, compoundFields));
        simsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(simsearch);


        /* sachem:similarCompoundSearch */
        ProcedureDefinition simcmpsearch = new ProcedureDefinition(sachem + "similarCompoundSearch",
                new Function(schema, "similarity_search_stub"));

        simcmpsearch.addParameter(new ParameterDefinition("#index", xsdString, new Literal(index, xsdStringType)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simcmpsearch
                .addParameter(new ParameterDefinition(sachem + "cutoff", xsdDouble, new Literal("0.8", xsdDoubleType)));
        simcmpsearch.addParameter(
                new ParameterDefinition(sachem + "similarityRadius", xsdInteger, new Literal("1", xsdIntegerType)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IRI(sachem + "aromaticityDetect")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IRI(sachem + "ignoreTautomers")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IRI(sachem + "UnspecifiedFormat")));
        simcmpsearch
                .addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        simcmpsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));

        simcmpsearch.addResult(new ResultDefinition(null, compound, compoundFields));
        config.addProcedure(simcmpsearch);
    }


    public static void addProcedures(SparqlDatabaseConfiguration config, String schema,
            Map<ResourceClass, List<Column>> compoundMapping)
    {
        String sachem = config.getPrefixes().get("sachem");


        /* sachem:exactSearch */
        ProcedureDefinition exactsearch = new ProcedureDefinition(sachem + "exactSearch",
                new Function(schema, "substructure_search_all"));

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
                .addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        exactsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));
        exactsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new Literal("0", xsdIntegerType)));

        exactsearch.addResult(new ResultDefinition(null, compoundMapping));
        config.addProcedure(exactsearch);


        /* sachem:substructureSearch */
        ProcedureDefinition subsearch = new ProcedureDefinition(sachem + "substructureSearch",
                new Function(schema, "substructure_search_all"));

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
        subsearch.addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        subsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));
        subsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new Literal("0", xsdIntegerType)));

        subsearch.addResult(new ResultDefinition(null, compoundMapping));
        config.addProcedure(subsearch);


        /* sachem:scoredSubstructureSearch */
        ProcedureDefinition scoredsubsearch = new ProcedureDefinition(sachem + "scoredSubstructureSearch",
                new Function(schema, "substructure_search_all"));

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
                .addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        scoredsubsearch
                .addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new Literal("0", xsdIntegerType)));

        scoredsubsearch.addResult(new ResultDefinition(sachem + "compound", compoundMapping));
        scoredsubsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(scoredsubsearch);


        /* sachem:similaritySearch */
        ProcedureDefinition simsearch = new ProcedureDefinition(sachem + "similaritySearch",
                new Function(schema, "similarity_search_all"));

        simsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simsearch
                .addParameter(new ParameterDefinition(sachem + "cutoff", xsdDouble, new Literal("0.8", xsdDoubleType)));
        simsearch.addParameter(
                new ParameterDefinition(sachem + "similarityRadius", xsdInteger, new Literal("1", xsdIntegerType)));
        simsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IRI(sachem + "aromaticityDetect")));
        simsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IRI(sachem + "ignoreTautomers")));
        simsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IRI(sachem + "UnspecifiedFormat")));
        simsearch.addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        simsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));

        simsearch.addResult(new ResultDefinition(sachem + "compound", compoundMapping));
        simsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(simsearch);


        /* sachem:similarCompoundSearch */
        ProcedureDefinition simcmpsearch = new ProcedureDefinition(sachem + "similarCompoundSearch",
                new Function(schema, "similarity_search_all"));

        simcmpsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simcmpsearch
                .addParameter(new ParameterDefinition(sachem + "cutoff", xsdDouble, new Literal("0.8", xsdDoubleType)));
        simcmpsearch.addParameter(
                new ParameterDefinition(sachem + "similarityRadius", xsdInteger, new Literal("1", xsdIntegerType)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IRI(sachem + "aromaticityDetect")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IRI(sachem + "ignoreTautomers")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IRI(sachem + "UnspecifiedFormat")));
        simcmpsearch
                .addParameter(new ParameterDefinition(sachem + "topn", xsdInteger, new Literal("-1", xsdIntegerType)));
        simcmpsearch.addParameter(new ParameterDefinition("#sort", xsdBoolean, new Literal("false", xsdBooleanType)));

        simcmpsearch.addResult(new ResultDefinition(null, compoundMapping));
        config.addProcedure(simcmpsearch);
    }


    public static void addFunctions(SparqlDatabaseConfiguration config)
    {
        String sachem = config.getPrefixes().get("sachem");

        FunctionDefinition similarity = new FunctionDefinition(sachem + "similarity",
                new Function("sachem", "similarity_stub"), xsdDouble,
                List.of(xsdString, xsdString, xsdInteger, config.getIriClass("aromaticity_mode")), 2, false, true);

        config.addFunction(similarity);
    }
}
