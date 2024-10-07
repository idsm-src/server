package cz.iocb.chemweb.server.services.query;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.iocb.chemweb.server.services.GWTRemoteServiceServlet;
import cz.iocb.chemweb.server.services.SessionData;
import cz.iocb.chemweb.server.velocity.EscapeIriDirective;
import cz.iocb.chemweb.server.velocity.SparqlDirective;
import cz.iocb.chemweb.server.velocity.UrlDirective;
import cz.iocb.chemweb.shared.services.DatabaseException;
import cz.iocb.chemweb.shared.services.SessionException;
import cz.iocb.chemweb.shared.services.query.DataGridNode;
import cz.iocb.chemweb.shared.services.query.QueryException;
import cz.iocb.chemweb.shared.services.query.QueryResult;
import cz.iocb.chemweb.shared.services.query.QueryService;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.error.TranslateExceptions;
import cz.iocb.sparql.engine.request.BNode;
import cz.iocb.sparql.engine.request.Engine;
import cz.iocb.sparql.engine.request.IriNode;
import cz.iocb.sparql.engine.request.LiteralNode;
import cz.iocb.sparql.engine.request.RdfNode;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.request.Result;



public class QueryServiceImpl extends GWTRemoteServiceServlet implements QueryService
{
    private static class QueryState
    {
        Thread thread;
        Request request;
        QueryResult result;
        Throwable exception;
    }


    private static final long serialVersionUID = 1L;
    private static final long timeout = 15 * 60 * 1000000000l; // 15 minutes
    private static final SessionData<QueryState> sessionData = new SessionData<QueryState>("QuerySessionStorage");
    private static final Logger logger = LoggerFactory.getLogger(QueryServiceImpl.class);
    private static final int cacheSize = 1000000;
    private static final Map<String, Map<RdfNode, String>> nodeHashMaps = new HashMap<String, Map<RdfNode, String>>();
    private static final Map<Pattern, String> templates = new HashMap<Pattern, String>();

    private Map<RdfNode, String> nodeHashMap;
    private SparqlDatabaseConfiguration sparqlConfig;
    private Engine engine;
    private VelocityEngine ve;
    private ExecutorService executorService;


    static
    {
        String pubchem = "http://rdf\\.ncbi\\.nlm\\.nih\\.gov/pubchem/";
        String chembl = "http://rdf\\.ebi\\.ac\\.uk/resource/chembl/";
        String mona = "https://idsm\\.elixir-czech\\.cz/rdf/mona/";
        String isdb = "https://idsm\\.elixir-czech\\.cz/rdf/isdb/";
        String drugbank = "http://wifo5-04\\.informatik\\.uni-mannheim\\.de/drugbank/";
        String wikidata = "http://www\\.wikidata\\.org/";
        String obolibrary = "http://purl\\.obolibrary\\.org/";
        String mesh = "http://id\\.nlm\\.nih\\.gov/mesh/";

        addTemplate(pubchem + "anatomy/ANATOMYID([0-9]+)", "pubchem/Anatomy.vm");
        addTemplate(pubchem + "author/.*", "pubchem/Author.vm");
        addTemplate(pubchem + "bioassay/AID([0-9]+)", "pubchem/Bioassay.vm");
        addTemplate(pubchem + "book/NBK([0-9]+)", "pubchem/Book.vm");
        addTemplate(pubchem + "cell/CELLID([0-9]+)", "pubchem/Cell.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Canonical_SMILES", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Compound_Identifier", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Covalent_Unit_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Defined_Atom_Stereo_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Defined_Bond_Stereo_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Exact_Mass", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Hydrogen_Bond_Acceptor_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Hydrogen_Bond_Donor_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Isomeric_SMILES", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Isotope_Atom_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_IUPAC_InChI", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Molecular_Formula", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Molecular_Weight", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Mono_Isotopic_Weight", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Non-hydrogen_Atom_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Preferred_IUPAC_Name", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Rotatable_Bond_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Structure_Complexity", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Tautomer_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Total_Formal_Charge", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_TPSA", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Undefined_Atom_Stereo_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_Undefined_Bond_Stereo_Count", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_XLogP3-AA", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "descriptor/CID([0-9]+)_XLogP3", "pubchem/CompoundDescriptor.vm");
        addTemplate(pubchem + "compound/CID([0-9]+)", "pubchem/Compound.vm");
        addTemplate(pubchem + "concept/.*", "pubchem/Concept.vm");
        addTemplate(pubchem + "conserveddomain/PSSMID([0-9]+)", "pubchem/ConservedDomain.vm");
        addTemplate(pubchem + "disease/DZID([0-9]+)", "pubchem/Disease.vm");
        addTemplate(pubchem + "endpoint/SID[0-9]+_AID[0-9]+(_(PMID)?[0-9]*)?_VALUE[0-9]+", "pubchem/Endpoint.vm");
        addTemplate(pubchem + "protein/EC_.*", "pubchem/Enzyme.vm");
        addTemplate(pubchem + "gene/GID([0-9]+)", "pubchem/Gene.vm");
        addTemplate(pubchem + "grant/.*", "pubchem/Grant.vm");
        addTemplate(pubchem + "journal/([0-9]+)", "pubchem/Journal.vm");
        addTemplate(pubchem + "measuregroup/AID[0-9]+(_(PMID)?[0-9]*)?", "pubchem/Measuregroup.vm");
        addTemplate(pubchem + "organization/.*", "pubchem/Organization.vm");
        addTemplate(pubchem + "patent/([0-9]+)", "pubchem/Patent.vm");
        addTemplate(pubchem + "pathway/PWID([0-9]+)", "pubchem/Pathway.vm");
        addTemplate(pubchem + "protein/ACC.*", "pubchem/Protein.vm");
        addTemplate(pubchem + "reference/([0-9]+)", "pubchem/Reference.vm");
        addTemplate(pubchem + "source/.*", "pubchem/Source.vm");
        addTemplate(pubchem + "descriptor/SID([0-9]+)_Substance_Version", "pubchem/SubstanceDescriptor.vm");
        addTemplate(pubchem + "substance/SID([0-9]+)", "pubchem/Substance.vm");
        addTemplate(pubchem + "synonym/MD5_.*", "pubchem/Synonym.vm");
        addTemplate(pubchem + "taxonomy/TAXID([0-9]+)", "pubchem/Taxonomy.vm");

        addTemplate(chembl + "assay/CHEMBL([0-9]+)", "chembl/Assay.vm");
        addTemplate(chembl + "binding_site/CHEMBL_BS_([0-9]+)", "chembl/BindingSite.vm");
        addTemplate(chembl + "biocomponent/CHEMBL_BC_([0-9]+)", "chembl/BioComponent.vm");
        addTemplate(chembl + "cell_line/CHEMBL([0-9]+)", "chembl/CellLine.vm");
        addTemplate(chembl + "document/CHEMBL([0-9]+)", "chembl/Document.vm");
        addTemplate(chembl + "drug_indication/CHEMBL_IND_([0-9]+)", "chembl/DrugIndication.vm");
        addTemplate(chembl + "journal/CHEMBL_JRN_([0-9]+)", "chembl/Journal.vm");
        addTemplate(chembl + "drug_mechanism/CHEMBL_MEC_([0-9]+)", "chembl/Mechanism.vm");
        addTemplate(chembl + "protclass/CHEMBL_PC_([0-9]+)", "chembl/ProteinClassification.vm");
        addTemplate(chembl + "source/CHEMBL_SRC_([0-9]+)", "chembl/Source.vm");
        addTemplate(chembl + "molecule/CHEMBL([0-9]+)", "chembl/Substance.vm");
        addTemplate(chembl + "target/CHEMBL([0-9]+)", "chembl/Target.vm");
        addTemplate(chembl + "targetcomponent/CHEMBL_TC_([0-9]+)", "chembl/TargetComponent.vm");

        addTemplate(mona + "(.*)_CMPD", "mona/Compound.vm");
        addTemplate(mona + "bnid([0-9]+)_annotation", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_cas_number", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_collision_energy", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_collision_energy_ramp_end", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_collision_energy_ramp_start", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_exact_mass", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_formula", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_hmdb_id", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_chebi_id", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_chemspider_id", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_inchikey", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_inchi", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_instrument", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_instrument_type", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_kegg_id", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_level", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_lipidmaps_id", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_molfile", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_monoisotopic_mass", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_name", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_precursor_mz", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_precursor_type", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_pubchem_compound_id", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_pubchem_substance_id", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_retention_time", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_smiles", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_splash", "mona/Descriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_tag", "mona/Descriptor.vm");
        addTemplate(mona + "(.*)_EXP", "mona/Experiment.vm");
        addTemplate(mona + "bnid([0-9]+)_library", "mona/Library.vm");
        addTemplate(mona + "bnid([0-9]+)_peak", "mona/Peak.vm");
        addTemplate(mona + "(.*)_MS", "mona/Spectrum.vm");
        addTemplate(mona + "bnid([0-9]+)_submitter", "mona/Submitter.vm");
        addTemplate(mona + "bnid([0-9]+)_ionization", "mona/TypedDescriptor.vm");
        addTemplate(mona + "bnid([0-9]+)_scan", "mona/TypedDescriptor.vm");

        addTemplate(isdb + "([A-Z]{14})_CMPD", "isdb/Compound.vm");
        addTemplate(isdb + "bn([A-Z]{14})_exact_mass", "isdb/Descriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14})_formula", "isdb/Descriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14})_inchi", "isdb/Descriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14}-[NP])_charge_state", "isdb/Descriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14}-[NP])_instrument_type", "isdb/Descriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14}-[NP])_level", "isdb/Descriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14}-[NP])_precursor_mz", "isdb/Descriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14}-[NP])_precursor_type", "isdb/Descriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14}-[NP])_tag", "isdb/Descriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14})_smiles", "isdb/Descriptor.vm");
        addTemplate(isdb + "([A-Z]{14}-[NP])_EXP", "isdb/Experiment.vm");
        addTemplate(isdb + "library/isdb", "isdb/Library.vm");
        addTemplate(isdb + "([A-Z]{14}-[NP])_MS", "isdb/Spectrum.vm");
        addTemplate(isdb + "bn([A-Z]{14}-[NP])_ionization", "isdb/TypedDescriptor.vm");
        addTemplate(isdb + "bn([A-Z]{14}-[NP])_scan", "isdb/TypedDescriptor.vm");

        addTemplate(mesh + "([A-Z][0-9]+(\\.[0-9]+|[A-Z][0-9]+)*)", "mesh/Mesh.vm");

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
            sparqlConfig = (SparqlDatabaseConfiguration) context.lookup(resourceName);

            engine = new Engine(sparqlConfig);


            Properties properties = new Properties();
            properties.put("file.resource.loader.path", config.getServletContext().getRealPath("/templates"));
            properties.put("userdirective",
                    "cz.iocb.chemweb.server.velocity.SparqlDirective,"
                            + "cz.iocb.chemweb.server.velocity.EscapeHtmlDirective,"
                            + "cz.iocb.chemweb.server.velocity.EscapeIriDirective,"
                            + "cz.iocb.chemweb.server.velocity.UrlDirective");

            ve = new VelocityEngine(properties);
            ve.setApplicationAttribute(SparqlDirective.SPARQL_CONFIG, sparqlConfig);
            ve.setApplicationAttribute(EscapeIriDirective.IRI_PREFIXES_CONFIG, sparqlConfig.getPrefixes());
        }
        catch(NamingException e)
        {
            throw new ServletException(e);
        }


        synchronized(QueryServiceImpl.class)
        {
            nodeHashMap = nodeHashMaps.get(resourceName);

            if(nodeHashMap == null)
            {
                nodeHashMap = Collections.synchronizedMap(new LinkedHashMap<RdfNode, String>(cacheSize, 0.75f, true)
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<RdfNode, String> eldest)
                    {
                        return size() > cacheSize;
                    }
                });

                nodeHashMaps.put(resourceName, nodeHashMap);
            }
        }


        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        super.init(config);
    }


    @Override
    public void destroy()
    {
        executorService.shutdown();

        super.destroy();
    }


    @Override
    public long query(String query) throws QueryException, DatabaseException
    {
        return query(query, 0, -1);
    }


    @Override
    public long query(String query, int offset, int limit) throws QueryException, DatabaseException
    {
        final HttpSession httpSession = this.getThreadLocalRequest().getSession(true);
        final QueryState queryState = new QueryState();


        logger.info(query.replaceAll("\n", "\\\\n"));

        queryState.request = engine.getRequest();


        final long id = sessionData.insert(httpSession, queryState);

        queryState.thread = new Thread()
        {
            @Override
            public void run()
            {
                try(Result result = queryState.request.execute(query, offset, limit + 1, timeout))
                {
                    Vector<Future<DataGridNode[]>> futures = new Vector<Future<DataGridNode[]>>(limit + 1);

                    while(result.next())
                    {
                        final RdfNode[] row = result.getRow();

                        futures.add(executorService.submit(() -> {
                            DataGridNode[] stringRow = new DataGridNode[row.length];

                            for(int i = 0; i < row.length; i++)
                            {
                                stringRow[i] = new DataGridNode();

                                if(row[i] != null && row[i].isIri())
                                    stringRow[i].ref = row[i].getValue();


                                String html = nodeHashMap.get(row[i]);

                                if(html != null)
                                {
                                    stringRow[i].html = html;
                                }
                                else
                                {
                                    try
                                    {
                                        String vmfile = getTempleteFileName(row[i]);

                                        if(vmfile != null)
                                        {
                                            VelocityContext context = new VelocityContext();
                                            context.put("urlContext", UrlDirective.Context.NODE);
                                            context.put("entity", row[i]);

                                            Template template = ve.getTemplate(vmfile);
                                            StringWriter writer = new StringWriter();
                                            template.merge(context, writer);

                                            stringRow[i].html = writer.toString();
                                            nodeHashMap.put(row[i], stringRow[i].html);
                                        }
                                        else
                                        {
                                            stringRow[i].html = "null";
                                        }
                                    }
                                    catch(Throwable e)
                                    {
                                        stringRow[i].html = "&lt;template error&gt;";
                                    }
                                }
                            }

                            return stringRow;
                        }));
                    }


                    Vector<DataGridNode[]> items = new Vector<DataGridNode[]>(futures.size());

                    for(Future<DataGridNode[]> future : futures)
                        items.add(future.get());


                    boolean truncated = false;

                    if(limit >= 0 && items.size() > limit)
                    {
                        truncated = true;
                        items.remove(limit);
                    }


                    queryState.result = new QueryResult(result.getHeads(), items, truncated);
                }
                catch(Throwable e)
                {
                    queryState.exception = e;
                }
                finally
                {
                    try
                    {
                        queryState.request.close();
                    }
                    catch(Exception e)
                    {
                    }
                }
            }
        };


        queryState.thread.start();

        return id;
    }


    @Override
    public QueryResult getResult(long queryID) throws SessionException, DatabaseException
    {
        final HttpSession httpSession = this.getThreadLocalRequest().getSession(false);

        if(httpSession == null)
            throw new SessionException("Your server session does not exist.");

        final QueryState queryState = sessionData.get(httpSession, queryID);

        if(queryState == null)
            throw new SessionException("Your server session does not contain the requested query ID.");


        try
        {
            queryState.thread.join();
        }
        catch(InterruptedException e)
        {
            throw new DatabaseException(e); //FIXME: use different exception
        }
        finally
        {
            sessionData.remove(httpSession, queryID);
        }

        if(queryState.exception != null)
        {
            if(queryState.exception instanceof DatabaseException)
                throw(DatabaseException) queryState.exception;
            else
                throw new DatabaseException(queryState.exception); //FIXME: use different exception
        }

        return queryState.result;
    }


    @Override
    public void cancel(long queryID) throws SessionException, DatabaseException
    {
        HttpSession httpSession = this.getThreadLocalRequest().getSession(false);

        if(httpSession == null)
            throw new SessionException("Your server session does not exist.");

        final QueryState queryState = sessionData.getAndRemove(httpSession, queryID);

        if(queryState == null)
            throw new SessionException("Your server session does not contain the requested query ID.");

        try
        {
            queryState.request.cancel();
        }
        catch(SQLException e)
        {
            throw new DatabaseException(e);
        }
    }


    @Override
    public int countOfProperties(String iri) throws DatabaseException
    {
        try(Request request = engine.getRequest())
        {
            String query = "SELECT (count(*) as ?C) WHERE { " + "<" + new URI(iri) + "> ?Property ?Value. }";

            try(Result result = request.execute(query))
            {
                if(!result.next())
                    throw new DatabaseException();

                if(result.getHeads().size() != 1)
                    throw new DatabaseException();

                return Integer.parseInt(result.get(0).getValue());
            }
        }
        catch(URISyntaxException | TranslateExceptions | SQLException e)
        {
            throw new DatabaseException(e);
        }
    }


    private static void addTemplate(String regexp, String file)
    {
        templates.put(Pattern.compile(regexp), "item/" + file);
    }


    private static String getTempleteFileName(RdfNode node)
    {
        switch(node)
        {
            case IriNode iri ->
            {
                String value = iri.getValue();

                for(Entry<Pattern, String> e : templates.entrySet())
                    if(e.getKey().matcher(value).matches())
                        return e.getValue();

                return "item/unknown.vm";
            }
            case LiteralNode literal ->
            {
                return "item/literal.vm";
            }
            case BNode bn ->
            {
                return "item/blanknode.vm";
            }
            default ->
            {
                return null;
            }
        }
    }
}
