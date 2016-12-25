DB.DBA.import_jar(null, 'cz.iocb.orchem.OrChemService', 1);
DB.DBA.import_jar(null, 'cz.iocb.orchem.bean.OrChemCompound', 1);

--============================================================================--

create procedure DB.dba.orchem_similaritySearch_store(in context_id integer, in query varchar, in query_type varchar, in cutoff real, in topn integer)
{
    declare results any;
    results := "cz_iocb_orchem_OrChemService"::"simsearch"(query, query_type, cutoff, topn);

    declare call_id integer;
    call_id := sequence_next('simsearch_call_id');

    declare rcount integer;
    rcount := 0;

    insert into DB.rdf.orchem_simsearch_parameters values (call_id, context_id, query, query_type, cutoff, topn);

    foreach ("cz_iocb_orchem_bean_OrChemCompound" x in results) do
    {
        insert into DB.rdf.orchem_simsearch_results(call_id, compound_id, score) values (call_id, x."getId"(), x."getScore"());
        rcount := rcount + 1;
    }

    return rcount;
};


grant execute on DB.dba.orchem_similaritySearch_store to "SPARQL";

--------------------------------------------------------------------------------

create procedure DB.dba.orchem_similarCompoundSearch_store(in context_id integer, in query varchar, in query_type varchar, in cutoff real, in topn integer)
{
    return DB.dba.orchem_similaritySearch_store(context_id, query, query_type, cutoff, topn);
};


grant execute on DB.dba.orchem_similarCompoundSearch_store to "SPARQL";

--============================================================================--

create procedure DB.dba.orchem_substructureSearch_store(in context_id integer, in query varchar, in query_type varchar, in topn integer, in strict_stereo smallint, in exact smallint, in tautomers smallint)
{
    declare results any;
    results := "cz_iocb_orchem_OrChemService"::"subsearch"(query, query_type, topn, strict_stereo, exact, tautomers);

    declare call_id integer;
    call_id := sequence_next('subsearch_call_id');

    declare rcount integer;
    rcount := 0;

    insert into DB.rdf.orchem_subsearch_parameters values (call_id, context_id, query, query_type, topn, strict_stereo, exact, tautomers);

    foreach ("cz_iocb_orchem_bean_OrChemCompound" x in results) do
    {
        insert into DB.rdf.orchem_subsearch_results(call_id, compound_id) values (call_id, x."getId"());
        rcount := rcount + 1;
    }

    return rcount;
};


grant execute on DB.dba.orchem_substructureSearch_store to "SPARQL";

--============================================================================--

create procedure DB.dba.orchem_smartsSearch_store(in context_id integer, in query varchar, in topn integer)
{
    declare results any;
    results := "cz_iocb_orchem_OrChemService"::"smartsSarch"(query, topn);

    declare call_id integer;
    call_id := sequence_next('smartssearch_call_id');

    declare rcount integer;
    rcount := 0;

    insert into DB.rdf.orchem_smartssearch_parameters values (call_id, context_id, query, topn);

    foreach ("cz_iocb_orchem_bean_OrChemCompound" x in results) do
    {
        insert into DB.rdf.orchem_subsearch_results(call_id, compound_id) values (call_id, x."getId"());
        rcount := rcount + 1;
    }

    return rcount;
};


grant execute on DB.dba.orchem_smartsSearch_store to "SPARQL";
