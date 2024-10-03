package cz.iocb.chemweb.server.sparql.config.matchms;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import java.util.List;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Function;
import cz.iocb.sparql.engine.mapping.classes.DataType;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.mapping.classes.UserLiteralClass;
import cz.iocb.sparql.engine.mapping.extension.FunctionDefinition;
import cz.iocb.sparql.engine.parser.model.IRI;



public abstract class Matchms
{
    public static final String ms = "http://bioinfo.uochb.cas.cz/rdf/v1.0/ms#";
    public static final LiteralClass spectrum = UserLiteralClass.get("pgms.spectrum", "operator(pgms.=)",
            "operator(pgms.!=)", new IRI(ms + "spectrum"));
    public static final DataType spectrumType = new DataType(spectrum, Spectrum::valueOf);


    public static void addPrefixes(SparqlDatabaseConfiguration config)
    {
        config.addPrefix("ms", ms);
    }


    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addDataType(spectrumType);
    }


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
