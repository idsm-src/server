package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.stringKey;
import static cz.iocb.load.common.EntityTable.typed;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getDate;
import static cz.iocb.load.common.TripleStreamProcessor.getIntFromInteger;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



class PatentCpc extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/patentcpc/";
    static final int prefixLength = prefix.length();

    private static final String cpc = "http://data.epo.org/linked-data/def/cpc/";

    private static final Map<String, String> types = Map.of(cpc + "Section", "SECTION", cpc + "SubSection",
            "SUBSECTION", cpc + "Class", "CLASS", cpc + "SubClass", "SUBCLASS", cpc + "MainGroup", "MAINGROUP",
            cpc + "SubGroup", "SUBGROUP");

    private static final EntityTable<String> cpcs = new EntityTable<>("pubchem.patentcpcs", stringKey("id"), null,
            typed("type", "pubchem.patentcpc_type_type"), integer("level"), uniqueVarchar("symbol"), varchar("title"),
            varchar("concordant_ipc"));
    private static final MissingEntities<String> missingCpcs = new MissingEntities<>("patent CPC", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", cpc + "level", cpc + "symbol", dcterms + "title",
                dcterms + "modified", skos + "broader", cpc + "concordantIPC");
        dispatcher.checkTypes(all(), vocab + "PatentCPC", cpc + "Section", cpc + "SubSection", cpc + "Class",
                cpc + "SubClass", cpc + "MainGroup", cpc + "SubGroup");
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(vocab + "PatentCPC", (subject, object) -> {
            String code = getStringID(subject, prefix);

            cpcs.reference(code);
            missingCpcs.described(code);
        });
    }


    private static void loadTypes(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        for(Entry<String, String> type : types.entrySet())
        {
            dispatcher.onType(type.getKey(), (subject, object) -> {
                String code = getCpcID(subject.getURI());

                cpcs.set(code, "type", type.getValue());
            });
        }
    }


    private static void loadLevels(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(cpc + "level", (subject, object) -> {
            String code = getCpcID(subject.getURI());
            Integer level = getIntFromInteger(object);

            cpcs.set(code, "level", level);
        });
    }


    private static void loadSymbols(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(cpc + "symbol", (subject, object) -> {
            String code = getCpcID(subject.getURI());
            String symbol = getString(object);

            cpcs.set(code, "symbol", symbol);
        });
    }


    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            String code = getCpcID(subject.getURI());
            String title = getString(object);

            cpcs.set(code, "title", title);
        });
    }


    private static void loadModifiedDates(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        StringPairSet keepDates = new StringPairSet();
        StringPairSet newDates = new StringPairSet();
        StringPairSet oldDates = new StringPairSet();

        load("select patentcpc,date::varchar from pubchem.patentcpc_modified_dates", oldDates);

        dispatcher.on(dcterms + "modified", (subject, object) -> {
            String code = getCpcID(subject.getURI());
            String date = getDate(object);

            Pair<String, String> pair = Pair.getPair(code, date);

            if(oldDates.remove(pair))
                keepDates.add(pair);
            else if(!keepDates.contains(pair))
                newDates.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.patentcpc_modified_dates where patentcpc=? and date=?::date", oldDates);
            store("insert into pubchem.patentcpc_modified_dates(patentcpc,date) values(?,?::date)", newDates);
        });
    }


    private static void loadBroaders(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        StringPairSet keepBroaders = new StringPairSet();
        StringPairSet newBroaders = new StringPairSet();
        StringPairSet oldBroaders = new StringPairSet();

        load("select patentcpc,broader from pubchem.patentcpc_broaders", oldBroaders);

        dispatcher.on(skos + "broader", (subject, object) -> {
            String code = getCpcID(subject.getURI());
            String broader = getCpcID(object.getURI());

            Pair<String, String> pair = Pair.getPair(code, broader);

            if(oldBroaders.remove(pair))
                keepBroaders.add(pair);
            else if(!keepBroaders.contains(pair))
                newBroaders.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.patentcpc_broaders where patentcpc=? and broader=?", oldBroaders);
            store("insert into pubchem.patentcpc_broaders(patentcpc,broader) values(?,?)", newBroaders);
        });
    }


    private static void loadConcordantIpcs(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(cpc + "concordantIPC", (subject, object) -> {
            String code = getCpcID(subject.getURI());
            String ipc = PatentIpc.getIpcID(object.getURI());

            cpcs.set(code, "concordant_ipc", ipc);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load patent CPCs ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadTypes(dispatcher);
        loadLevels(dispatcher);
        loadSymbols(dispatcher);
        loadTitles(dispatcher);
        loadModifiedDates(dispatcher);
        loadBroaders(dispatcher);
        loadConcordantIpcs(dispatcher);

        dispatcher.load("pubchem/RDF/patent/cpc/pc_patentcpc.ttl.gz");
        missingCpcs.settle();
        dispatcher.finish();

        cpcs.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish patent CPCs ...");

        cpcs.store();

        System.out.println();
    }


    static String getCpcID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        String code = value.substring(prefixLength);

        if(cpcs.reference(code))
            missingCpcs.referenced(code);

        return code;
    }
}
