package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDouble;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInteger;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import static cz.iocb.sparql.engine.mapping.datatypes.BuiltinDatatypes.xsdBooleanIri;
import static cz.iocb.sparql.engine.mapping.datatypes.BuiltinDatatypes.xsdDoubleIri;
import static cz.iocb.sparql.engine.mapping.datatypes.BuiltinDatatypes.xsdIntegerIri;
import static cz.iocb.sparql.engine.mapping.datatypes.BuiltinDatatypes.xsdStringIri;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Function;
import cz.iocb.sparql.engine.database.SqlType;
import cz.iocb.sparql.engine.mapping.classes.EnumUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.mapping.extension.FunctionDefinition;
import cz.iocb.sparql.engine.mapping.extension.ParameterDefinition;
import cz.iocb.sparql.engine.mapping.extension.ProcedureDefinition;
import cz.iocb.sparql.engine.mapping.extension.ResultDefinition;
import cz.iocb.sparql.engine.model.IriNode;
import cz.iocb.sparql.engine.model.expression.LiteralNode;
import cz.iocb.sparql.engine.rdf.Iri;



public abstract class Sachem
{
    private static final IriNode xsdStringTypeNode = new IriNode(xsdStringIri.getValue());
    private static final IriNode xsdIntegerTypeNode = new IriNode(xsdIntegerIri.getValue());
    private static final IriNode xsdBooleanTypeNode = new IriNode(xsdBooleanIri.getValue());
    private static final IriNode xsdDoubleTypeNode = new IriNode(xsdDoubleIri.getValue());


    public static void addPrefixes(SparqlDatabaseConfiguration config)
    {
        config.addPrefix("sachem", "http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#");
    }


    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        String sachem = config.getPrefixes().get("sachem");

        config.addIriClass(new EnumUserIriClass("query_format", VARCHAR, new HashMap<Iri, String>()
        {
            {
                put(new Iri(sachem + "UnspecifiedFormat"), "UNSPECIFIED");
                put(new Iri(sachem + "SMILES"), "SMILES");
                put(new Iri(sachem + "MolFile"), "MOLFILE");
                put(new Iri(sachem + "RGroup"), "RGROUP");
            }
        }));

        config.addIriClass(
                new EnumUserIriClass("search_mode", SqlType.of("sachem.search_mode"), new HashMap<Iri, String>()
                {
                    {
                        put(new Iri(sachem + "substructureSearch"), "SUBSTRUCTURE");
                        put(new Iri(sachem + "exactSearch"), "EXACT");
                    }
                }));

        config.addIriClass(
                new EnumUserIriClass("charge_mode", SqlType.of("sachem.charge_mode"), new HashMap<Iri, String>()
                {
                    {
                        put(new Iri(sachem + "ignoreCharges"), "IGNORE");
                        put(new Iri(sachem + "defaultChargeAsZero"), "DEFAULT_AS_UNCHARGED");
                        put(new Iri(sachem + "defaultChargeAsAny"), "DEFAULT_AS_ANY");
                    }
                }));

        config.addIriClass(
                new EnumUserIriClass("isotope_mode", SqlType.of("sachem.isotope_mode"), new HashMap<Iri, String>()
                {
                    {
                        put(new Iri(sachem + "ignoreIsotopes"), "IGNORE");
                        put(new Iri(sachem + "defaultIsotopeAsStandard"), "DEFAULT_AS_STANDARD");
                        put(new Iri(sachem + "defaultIsotopeAsAny"), "DEFAULT_AS_ANY");
                    }
                }));

        config.addIriClass(
                new EnumUserIriClass("radical_mode", SqlType.of("sachem.radical_mode"), new HashMap<Iri, String>()
                {
                    {
                        put(new Iri(sachem + "ignoreSpinMultiplicity"), "IGNORE");
                        put(new Iri(sachem + "defaultSpinMultiplicityAsZero"), "DEFAULT_AS_STANDARD");
                        put(new Iri(sachem + "defaultSpinMultiplicityAsAny"), "DEFAULT_AS_ANY");
                    }
                }));

        config.addIriClass(
                new EnumUserIriClass("stereo_mode", SqlType.of("sachem.stereo_mode"), new HashMap<Iri, String>()
                {
                    {
                        put(new Iri(sachem + "ignoreStereo"), "IGNORE");
                        put(new Iri(sachem + "strictStereo"), "STRICT");
                    }
                }));

        config.addIriClass(new EnumUserIriClass("aromaticity_mode", SqlType.of("sachem.aromaticity_mode"),
                new HashMap<Iri, String>()
                {
                    {
                        put(new Iri(sachem + "aromaticityFromQuery"), "PRESERVE");
                        put(new Iri(sachem + "aromaticityDetect"), "DETECT");
                        put(new Iri(sachem + "aromaticityDetectIfMissing"), "AUTO");
                    }
                }));

        config.addIriClass(
                new EnumUserIriClass("tautomer_mode", SqlType.of("sachem.tautomer_mode"), new HashMap<Iri, String>()
                {
                    {
                        put(new Iri(sachem + "ignoreTautomers"), "IGNORE");
                        put(new Iri(sachem + "inchiTautomers"), "INCHI");
                    }
                }));
    }


    public static void addProcedures(SparqlDatabaseConfiguration config, String index, String compoundClass,
            List<Column> compoundFields)
    {
        addProcedures(config, index, "idsm", compoundClass, compoundFields);
    }


    public static void addProcedures(SparqlDatabaseConfiguration config, String index, String schema,
            String compoundClass, List<Column> compoundFields)
    {
        String sachem = config.getPrefixes().get("sachem");
        UserIriClass compound = config.getIriClass(compoundClass);


        /* sachem:exactSearch */
        ProcedureDefinition exactsearch = new ProcedureDefinition(sachem + "exactSearch",
                new Function(schema, "substructure_search_stub"));

        exactsearch
                .addParameter(new ParameterDefinition("#index", xsdString, new LiteralNode(index, xsdStringTypeNode)));
        exactsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        exactsearch.addParameter(new ParameterDefinition(sachem + "searchMode", config.getIriClass("search_mode"),
                new IriNode(sachem + "exactSearch")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "chargeMode", config.getIriClass("charge_mode"),
                new IriNode(sachem + "defaultChargeAsZero")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "isotopeMode", config.getIriClass("isotope_mode"),
                new IriNode(sachem + "defaultIsotopeAsStandard")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "radicalMode", config.getIriClass("radical_mode"),
                new IriNode(sachem + "defaultSpinMultiplicityAsZero")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "stereoMode", config.getIriClass("stereo_mode"),
                new IriNode(sachem + "strictStereo")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IriNode(sachem + "ignoreTautomers")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        exactsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        exactsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));
        exactsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new LiteralNode("0", xsdIntegerTypeNode)));

        exactsearch.addResult(new ResultDefinition(null, compound, compoundFields));
        config.addProcedure(exactsearch);


        /* sachem:substructureSearch */
        ProcedureDefinition subsearch = new ProcedureDefinition(sachem + "substructureSearch",
                new Function(schema, "substructure_search_stub"));

        subsearch.addParameter(new ParameterDefinition("#index", xsdString, new LiteralNode(index, xsdStringTypeNode)));
        subsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        subsearch.addParameter(new ParameterDefinition(sachem + "searchMode", config.getIriClass("search_mode"),
                new IriNode(sachem + "substructureSearch")));
        subsearch.addParameter(new ParameterDefinition(sachem + "chargeMode", config.getIriClass("charge_mode"),
                new IriNode(sachem + "defaultChargeAsAny")));
        subsearch.addParameter(new ParameterDefinition(sachem + "isotopeMode", config.getIriClass("isotope_mode"),
                new IriNode(sachem + "ignoreIsotopes")));
        subsearch.addParameter(new ParameterDefinition(sachem + "radicalMode", config.getIriClass("radical_mode"),
                new IriNode(sachem + "ignoreSpinMultiplicity")));
        subsearch.addParameter(new ParameterDefinition(sachem + "stereoMode", config.getIriClass("stereo_mode"),
                new IriNode(sachem + "ignoreStereo")));
        subsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        subsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IriNode(sachem + "ignoreTautomers")));
        subsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        subsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        subsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));
        subsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new LiteralNode("0", xsdIntegerTypeNode)));

        subsearch.addResult(new ResultDefinition(null, compound, compoundFields));
        config.addProcedure(subsearch);


        /* sachem:scoredSubstructureSearch */
        ProcedureDefinition scoredsubsearch = new ProcedureDefinition(sachem + "scoredSubstructureSearch",
                new Function(schema, "substructure_search_stub"));

        scoredsubsearch
                .addParameter(new ParameterDefinition("#index", xsdString, new LiteralNode(index, xsdStringTypeNode)));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "searchMode", config.getIriClass("search_mode"),
                new IriNode(sachem + "substructureSearch")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "chargeMode", config.getIriClass("charge_mode"),
                new IriNode(sachem + "defaultChargeAsAny")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "isotopeMode", config.getIriClass("isotope_mode"),
                new IriNode(sachem + "ignoreIsotopes")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "radicalMode", config.getIriClass("radical_mode"),
                new IriNode(sachem + "ignoreSpinMultiplicity")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "stereoMode", config.getIriClass("stereo_mode"),
                new IriNode(sachem + "ignoreStereo")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode",
                config.getIriClass("tautomer_mode"), new IriNode(sachem + "ignoreTautomers")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        scoredsubsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        scoredsubsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new LiteralNode("0", xsdIntegerTypeNode)));

        scoredsubsearch.addResult(new ResultDefinition(sachem + "compound", compound, compoundFields));
        scoredsubsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(scoredsubsearch);


        /* sachem:similaritySearch */
        ProcedureDefinition simsearch = new ProcedureDefinition(sachem + "similaritySearch",
                new Function(schema, "similarity_search_stub"));

        simsearch.addParameter(new ParameterDefinition("#index", xsdString, new LiteralNode(index, xsdStringTypeNode)));
        simsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simsearch.addParameter(
                new ParameterDefinition(sachem + "cutoff", xsdDouble, new LiteralNode("0.8", xsdDoubleTypeNode)));
        simsearch.addParameter(new ParameterDefinition(sachem + "similarityRadius", xsdInteger,
                new LiteralNode("1", xsdIntegerTypeNode)));
        simsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        simsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IriNode(sachem + "ignoreTautomers")));
        simsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        simsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        simsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));

        simsearch.addResult(new ResultDefinition(sachem + "compound", compound, compoundFields));
        simsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(simsearch);


        /* sachem:similarCompoundSearch */
        ProcedureDefinition simcmpsearch = new ProcedureDefinition(sachem + "similarCompoundSearch",
                new Function(schema, "similarity_search_stub"));

        simcmpsearch
                .addParameter(new ParameterDefinition("#index", xsdString, new LiteralNode(index, xsdStringTypeNode)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simcmpsearch.addParameter(
                new ParameterDefinition(sachem + "cutoff", xsdDouble, new LiteralNode("0.8", xsdDoubleTypeNode)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "similarityRadius", xsdInteger,
                new LiteralNode("1", xsdIntegerTypeNode)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IriNode(sachem + "ignoreTautomers")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        simcmpsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        simcmpsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));

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
                new IriNode(sachem + "exactSearch")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "chargeMode", config.getIriClass("charge_mode"),
                new IriNode(sachem + "defaultChargeAsZero")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "isotopeMode", config.getIriClass("isotope_mode"),
                new IriNode(sachem + "defaultIsotopeAsStandard")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "radicalMode", config.getIriClass("radical_mode"),
                new IriNode(sachem + "defaultSpinMultiplicityAsZero")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "stereoMode", config.getIriClass("stereo_mode"),
                new IriNode(sachem + "strictStereo")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IriNode(sachem + "ignoreTautomers")));
        exactsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        exactsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        exactsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));
        exactsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new LiteralNode("0", xsdIntegerTypeNode)));

        exactsearch.addResult(new ResultDefinition(null, compoundMapping));
        config.addProcedure(exactsearch);


        /* sachem:substructureSearch */
        ProcedureDefinition subsearch = new ProcedureDefinition(sachem + "substructureSearch",
                new Function(schema, "substructure_search_all"));

        subsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        subsearch.addParameter(new ParameterDefinition(sachem + "searchMode", config.getIriClass("search_mode"),
                new IriNode(sachem + "substructureSearch")));
        subsearch.addParameter(new ParameterDefinition(sachem + "chargeMode", config.getIriClass("charge_mode"),
                new IriNode(sachem + "defaultChargeAsAny")));
        subsearch.addParameter(new ParameterDefinition(sachem + "isotopeMode", config.getIriClass("isotope_mode"),
                new IriNode(sachem + "ignoreIsotopes")));
        subsearch.addParameter(new ParameterDefinition(sachem + "radicalMode", config.getIriClass("radical_mode"),
                new IriNode(sachem + "ignoreSpinMultiplicity")));
        subsearch.addParameter(new ParameterDefinition(sachem + "stereoMode", config.getIriClass("stereo_mode"),
                new IriNode(sachem + "ignoreStereo")));
        subsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        subsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IriNode(sachem + "ignoreTautomers")));
        subsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        subsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        subsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));
        subsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new LiteralNode("0", xsdIntegerTypeNode)));

        subsearch.addResult(new ResultDefinition(null, compoundMapping));
        config.addProcedure(subsearch);


        /* sachem:scoredSubstructureSearch */
        ProcedureDefinition scoredsubsearch = new ProcedureDefinition(sachem + "scoredSubstructureSearch",
                new Function(schema, "substructure_search_all"));

        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "searchMode", config.getIriClass("search_mode"),
                new IriNode(sachem + "substructureSearch")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "chargeMode", config.getIriClass("charge_mode"),
                new IriNode(sachem + "defaultChargeAsAny")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "isotopeMode", config.getIriClass("isotope_mode"),
                new IriNode(sachem + "ignoreIsotopes")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "radicalMode", config.getIriClass("radical_mode"),
                new IriNode(sachem + "ignoreSpinMultiplicity")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "stereoMode", config.getIriClass("stereo_mode"),
                new IriNode(sachem + "ignoreStereo")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode",
                config.getIriClass("tautomer_mode"), new IriNode(sachem + "ignoreTautomers")));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        scoredsubsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        scoredsubsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));
        scoredsubsearch.addParameter(new ParameterDefinition(sachem + "internalMatchingLimit", xsdInteger,
                new LiteralNode("0", xsdIntegerTypeNode)));

        scoredsubsearch.addResult(new ResultDefinition(sachem + "compound", compoundMapping));
        scoredsubsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(scoredsubsearch);


        /* sachem:similaritySearch */
        ProcedureDefinition simsearch = new ProcedureDefinition(sachem + "similaritySearch",
                new Function(schema, "similarity_search_all"));

        simsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simsearch.addParameter(
                new ParameterDefinition(sachem + "cutoff", xsdDouble, new LiteralNode("0.8", xsdDoubleTypeNode)));
        simsearch.addParameter(new ParameterDefinition(sachem + "similarityRadius", xsdInteger,
                new LiteralNode("1", xsdIntegerTypeNode)));
        simsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        simsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IriNode(sachem + "ignoreTautomers")));
        simsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        simsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        simsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));

        simsearch.addResult(new ResultDefinition(sachem + "compound", compoundMapping));
        simsearch.addResult(new ResultDefinition(sachem + "score", xsdDouble, "score"));
        config.addProcedure(simsearch);


        /* sachem:similarCompoundSearch */
        ProcedureDefinition simcmpsearch = new ProcedureDefinition(sachem + "similarCompoundSearch",
                new Function(schema, "similarity_search_all"));

        simcmpsearch.addParameter(new ParameterDefinition(sachem + "query", xsdString, null));
        simcmpsearch.addParameter(
                new ParameterDefinition(sachem + "cutoff", xsdDouble, new LiteralNode("0.8", xsdDoubleTypeNode)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "similarityRadius", xsdInteger,
                new LiteralNode("1", xsdIntegerTypeNode)));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "aromaticityMode",
                config.getIriClass("aromaticity_mode"), new IriNode(sachem + "aromaticityDetect")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "tautomerMode", config.getIriClass("tautomer_mode"),
                new IriNode(sachem + "ignoreTautomers")));
        simcmpsearch.addParameter(new ParameterDefinition(sachem + "queryFormat", config.getIriClass("query_format"),
                new IriNode(sachem + "UnspecifiedFormat")));
        simcmpsearch.addParameter(
                new ParameterDefinition(sachem + "topn", xsdInteger, new LiteralNode("-1", xsdIntegerTypeNode)));
        simcmpsearch.addParameter(
                new ParameterDefinition("#sort", xsdBoolean, new LiteralNode("false", xsdBooleanTypeNode)));

        simcmpsearch.addResult(new ResultDefinition(null, compoundMapping));
        config.addProcedure(simcmpsearch);
    }


    public static void addFunctions(SparqlDatabaseConfiguration config)
    {
        String sachem = config.getPrefixes().get("sachem");

        FunctionDefinition similarity = new FunctionDefinition(sachem + "similarity",
                new Function("idsm", "similarity_stub"), xsdDouble,
                List.of(xsdString, xsdString, xsdInteger, config.getIriClass("aromaticity_mode")), 2, false, true);

        config.addFunction(similarity);
    }
}
