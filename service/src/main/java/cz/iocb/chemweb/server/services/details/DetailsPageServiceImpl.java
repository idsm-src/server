package cz.iocb.chemweb.server.services.details;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.iocb.chemweb.server.services.GWTRemoteServiceServlet;
import cz.iocb.chemweb.server.velocity.EscapeIriDirective;
import cz.iocb.chemweb.server.velocity.SparqlDirective;
import cz.iocb.chemweb.server.velocity.UrlDirective;
import cz.iocb.chemweb.shared.services.details.DetailsPageService;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.request.IriNode;



public class DetailsPageServiceImpl extends GWTRemoteServiceServlet implements DetailsPageService
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DetailsPageServiceImpl.class);
    private static final Map<Pattern, String> templates = new HashMap<Pattern, String>();

    private SparqlDatabaseConfiguration dbConfig;
    private VelocityEngine ve;


    static
    {
        String pubchem = "http://rdf\\.ncbi\\.nlm\\.nih\\.gov/pubchem/";
        String chembl = "http://rdf\\.ebi\\.ac\\.uk/resource/chembl/";
        String mona = "https://idsm\\.elixir-czech\\.cz/rdf/mona/";
        String isdb = "https://idsm\\.elixir-czech\\.cz/rdf/isdb/";

        String drugbank = "http://wifo5-04\\.informatik\\.uni-mannheim\\.de/drugbank/";
        String wikidata = "http://www\\.wikidata\\.org/";
        String obolibrary = "http://purl\\.obolibrary\\.org/";

        addTemplate(pubchem + "bioassay/AID([0-9]+)", "pubchem/Bioassay.vm");
        addTemplate(pubchem + "compound/CID([0-9]+)", "pubchem/Compound.vm");
        addTemplate(pubchem + "conserveddomain/PSSMID([0-9]+)", "pubchem/ConservedDomain.vm");
        addTemplate(pubchem + "gene/GID([0-9]+)", "pubchem/Gene.vm");
        addTemplate(pubchem + "pathway/PWID([0-9]+)", "pubchem/Pathway.vm");
        addTemplate(pubchem + "protein/ACC.*", "pubchem/Protein.vm");
        addTemplate(pubchem + "reference/([0-9]+)", "pubchem/Reference.vm");
        addTemplate(pubchem + "substance/SID([0-9]+)", "pubchem/Substance.vm");

        addTemplate(chembl + "molecule/CHEMBL([0-9]+)", "chembl/Substance.vm");

        addTemplate(mona + "(.*)_CMPD", "mona/Compound.vm");
        addTemplate(mona + "(.*)_EXP", "mona/Experiment.vm");
        addTemplate(mona + "bnid([0-9]+)_library", "mona/Library.vm");
        addTemplate(mona + "(.*)_MS", "mona/Spectrum.vm");
        addTemplate(mona + "bnid([0-9]+)_submitter", "mona/Submitter.vm");

        addTemplate(isdb + "([A-Z]{14})_CMPD", "isdb/Compound.vm");
        addTemplate(isdb + "([A-Z]{14}-[NP])_EXP", "isdb/Experiment.vm");
        addTemplate(isdb + "library/isdb", "isdb/Library.vm");
        addTemplate(isdb + "([A-Z]{14}-[NP])_MS", "isdb/Spectrum.vm");

        addTemplate(drugbank + "resource/drugs/DB[0-9]{5}", "drugbank/Compound.vm");

        addTemplate(wikidata + "entity/Q([0-9]+)", "wikidata/Compound.vm");

        addTemplate(obolibrary + "obo/CHEBI_([0-9]+)", "chebi/Class.vm");
    }


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String resourceName = config.getInitParameter("resource");

        if(resourceName == null || resourceName.isEmpty())
            throw new ServletException("Resource name is not set");

        try
        {
            Context context = (Context) (new InitialContext()).lookup("java:comp/env");
            dbConfig = (SparqlDatabaseConfiguration) context.lookup(resourceName);
        }
        catch(NamingException e)
        {
            throw new ServletException(e);
        }

        Properties properties = new Properties();
        properties.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        properties.put("file.resource.loader.path", config.getServletContext().getRealPath("/templates"));
        properties.put("userdirective",
                "cz.iocb.chemweb.server.velocity.SparqlDirective,"
                        + "cz.iocb.chemweb.server.velocity.EscapeHtmlDirective,"
                        + "cz.iocb.chemweb.server.velocity.EscapeIriDirective,"
                        + "cz.iocb.chemweb.server.velocity.UrlDirective");

        ve = new VelocityEngine(properties);
        ve.setApplicationAttribute(SparqlDirective.SPARQL_CONFIG, dbConfig);
        ve.setApplicationAttribute(EscapeIriDirective.IRI_PREFIXES_CONFIG, dbConfig.getPrefixes());

        super.init(config);
    }


    @Override
    public String details(String iriText)
    {
        try
        {
            String vmfile = getTempleteFileName(iriText);

            long time = System.currentTimeMillis();
            URI uri = new URI(iriText);

            StringWriter writer = new StringWriter();
            Template template = ve.getTemplate(vmfile);

            VelocityContext context = new VelocityContext();
            context.put("urlContext", UrlDirective.Context.DETAILS);
            context.put("entity", new IriNode(uri.toString()));

            template.merge(context, writer);
            writer.close();

            time = System.currentTimeMillis() - time;
            logger.info(uri + " " + time / 1000.0 + "s");

            return writer.toString();
        }
        catch(URISyntaxException e)
        {
            // TODO:
            return "";
        }
        catch(IOException e)
        {
            // TODO:
            return "";
        }
    }


    private static void addTemplate(String regexp, String file)
    {
        templates.put(Pattern.compile(regexp), "page/" + file);
    }


    private static String getTempleteFileName(String value)
    {
        for(Entry<Pattern, String> e : templates.entrySet())
            if(e.getKey().matcher(value).matches())
                return e.getValue();

        return "page/unknown.vm";
    }
}
