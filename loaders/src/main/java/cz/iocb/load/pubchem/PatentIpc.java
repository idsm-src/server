package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.stringKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.owl;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.rdfs;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



class PatentIpc extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/patentipc/";
    static final int prefixLength = prefix.length();

    private static final String epoPrefix = "http://data.epo.org/linked-data/def/ipc/";
    private static final String wipoPrefix = "http://www.wipo.int/classifications/ipc/ipcpub/?notion=scheme&symbol=";
    private static final String imagePrefix = "https://pubchem.ncbi.nlm.nih.gov/images/ipc/";

    private static final EntityTable<String> ipcs = new EntityTable<>("pubchem.patentipcs", stringKey("id"), null,
            uniqueVarchar("label"), varchar("broader"), uniqueVarchar("epo_id"), uniqueVarchar("wipo_id"),
            varchar("image"));
    private static final MissingEntities<String> missingIpcs = new MissingEntities<>("patent IPC", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", rdfs + "label", dcterms + "title", skos + "broader",
                owl + "sameAs");
        dispatcher.checkTypes(all(), vocab + "PatentIPC");
        dispatcher.checkPrefixes(all(), owl + "sameAs", epoPrefix, wipoPrefix, imagePrefix);
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(vocab + "PatentIPC", (subject, object) -> {
            String code = getStringID(subject, prefix);

            ipcs.reference(code);
            missingIpcs.described(code);
        });
    }


    private static void loadLabels(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(rdfs + "label", (subject, object) -> {
            String code = getIpcID(subject.getURI());
            String label = getString(object);

            ipcs.set(code, "label", label);
        });
    }


    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        StringPairSet keepTitles = new StringPairSet();
        StringPairSet newTitles = new StringPairSet();
        StringPairSet oldTitles = new StringPairSet();

        load("select patentipc,title from pubchem.patentipc_titles", oldTitles);

        dispatcher.on(dcterms + "title", (subject, object) -> {
            String code = getIpcID(subject.getURI());
            String title = getString(object);

            Pair<String, String> pair = Pair.getPair(code, title);

            if(oldTitles.remove(pair))
                keepTitles.add(pair);
            else if(!keepTitles.contains(pair))
                newTitles.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.patentipc_titles where patentipc=? and title=?", oldTitles);
            store("insert into pubchem.patentipc_titles(patentipc,title) values(?,?)", newTitles);
        });
    }


    private static void loadBroaders(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(skos + "broader", (subject, object) -> {
            String code = getIpcID(subject.getURI());
            String broader = getIpcID(object.getURI());

            ipcs.set(code, "broader", broader);
        });
    }


    private static void loadSameAsReferences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(owl + "sameAs", (subject, object) -> {
            String code = getIpcID(subject.getURI());
            String iri = object.getURI();

            // the other values are checked by check()
            if(iri.startsWith(epoPrefix))
                ipcs.set(code, "epo_id", iri.substring(epoPrefix.length()));
            else if(iri.startsWith(wipoPrefix))
                ipcs.set(code, "wipo_id", iri.substring(wipoPrefix.length()));
            else if(iri.startsWith(imagePrefix))
                ipcs.set(code, "image", iri.substring(imagePrefix.length()));
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load patent IPCs ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadLabels(dispatcher);
        loadTitles(dispatcher);
        loadBroaders(dispatcher);
        loadSameAsReferences(dispatcher);

        dispatcher.load("pubchem/RDF/patent/ipc/pc_patentipc.ttl.gz");
        missingIpcs.settle();
        dispatcher.finish();

        ipcs.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish patent IPCs ...");

        ipcs.store();

        System.out.println();
    }


    static String getIpcID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        String code = value.substring(prefixLength);

        if(ipcs.reference(code))
            missingIpcs.referenced(code);

        return code;
    }
}
