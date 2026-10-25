package cz.iocb.chemweb.server.sparql.config.matchms;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import java.util.List;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Function;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.mapping.datatypes.UserDatatype;
import cz.iocb.sparql.engine.mapping.extension.FunctionDefinition;



/**
 * Mapping of the mass spectra literals and of the spectrum similarity functions of matchms, evaluated by the
 * {@code pgms} extension.
 */
public abstract class Matchms
{
    /**
     * Namespace of the mass spectra vocabulary.
     */
    public static final String ms = "http://bioinfo.uochb.cas.cz/rdf/v1.0/ms#";

    /**
     * Datatype of the spectra.
     */
    public static final UserDatatype spectrumType = new SpectrumDatatype();

    /**
     * Literal class of the spectra.
     */
    public static final LiteralClass spectrum = spectrumType.getCanonicalLiteralClass();


    /**
     * Not instantiable.
     */
    protected Matchms()
    {
    }


    /**
     * Adds the prefix of the mass spectra vocabulary to the configuration.
     *
     * @param config the configuration
     */
    public static void addPrefixes(SparqlDatabaseConfiguration config)
    {
        config.addPrefix("ms", ms);
    }


    /**
     * Adds the datatype of the spectra to the configuration.
     *
     * @param config the configuration
     */
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addDatatype(spectrumType);
    }


    /**
     * Adds the spectrum similarity functions (greedy, Hungarian and modified cosine scores) and the spectrum
     * normalization to the configuration.
     *
     * @param config the configuration
     */
    public static void addFunctions(SparqlDatabaseConfiguration config)
    {
        FunctionDefinition cosineGreedy = new FunctionDefinition(ms + "cosineGreedy",
                new Function("pgms", "cosine_greedy"), xsdFloat,
                List.of(spectrum, spectrum, xsdFloat, xsdFloat, xsdFloat), 2, false, true);

        config.addFunction(cosineGreedy);


        FunctionDefinition cosineHungarian = new FunctionDefinition(ms + "cosineHungarian",
                new Function("pgms", "cosine_hungarian"), xsdFloat,
                List.of(spectrum, spectrum, xsdFloat, xsdFloat, xsdFloat), 2, false, true);

        config.addFunction(cosineHungarian);


        FunctionDefinition modifiedCosine = new FunctionDefinition(ms + "modifiedCosine",
                new Function("pgms", "cosine_modified"), xsdFloat,
                List.of(spectrum, spectrum, xsdFloat, xsdFloat, xsdFloat, xsdFloat), 3, false, true);

        config.addFunction(modifiedCosine);


        FunctionDefinition normalize = new FunctionDefinition(ms + "normalize",
                new Function("pgms", "spectrum_normalize"), spectrum, List.of(spectrum), false, true);

        config.addFunction(normalize);
    }
}
