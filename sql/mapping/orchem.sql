sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.orchem_simsearch_parameters as orchem_simsearch_parameters
    from DB.rdf.orchem_simsearch_results as orchem_simsearch_results
{
    create map-orchem:similaritySearch as graph proc:orchem_similaritySearch option (exclusive)
    {
        iri-proc:call(orchem_simsearch_parameters.call_id)
            proc:context orchem_simsearch_parameters.context_id ;
            orchem:query orchem_simsearch_parameters.query ;
            orchem:queryType orchem_simsearch_parameters.query_type ;
            orchem:topn orchem_simsearch_parameters.topn ;
            orchem:cutoff orchem_simsearch_parameters.cutoff .

        iri-proc:result(orchem_simsearch_results.id)
            proc:call iri-proc:call(orchem_simsearch_results.call_id) ;
            orchem:compound iri:compound(orchem_simsearch_results.compound_id) ;
            orchem:score orchem_simsearch_results.score .
    }.
}.;

--------------------------------------------------------------------------------

sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.orchem_simsearch_parameters as orchem_simsearch_parameters
    from DB.rdf.orchem_simsearch_results as orchem_simsearch_results
{
    create map-orchem:similarCompoundSearch as graph proc:orchem_similarCompoundSearch option (exclusive)
    {
        iri-proc:call(orchem_simsearch_parameters.call_id)
            proc:context orchem_simsearch_parameters.context_id ;
            orchem:query orchem_simsearch_parameters.query ;
            orchem:queryType orchem_simsearch_parameters.query_type ;
            orchem:topn orchem_simsearch_parameters.topn ;
            orchem:cutoff orchem_simsearch_parameters.cutoff .

        iri-proc:result(orchem_simsearch_results.id)
            proc:call iri-proc:call(orchem_simsearch_results.call_id) ;
            proc:result iri:compound(orchem_simsearch_results.compound_id) .
    }.
}.;

--============================================================================--

sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.orchem_subsearch_parameters as orchem_subsearch_parameters
    from DB.rdf.orchem_subsearch_results as orchem_subsearch_results
{
    create map-orchem:substructureSearch as graph proc:orchem_substructureSearch option (exclusive)
    {
        iri-proc:call(orchem_subsearch_parameters.call_id)
            proc:context orchem_subsearch_parameters.context_id ;
            orchem:query orchem_subsearch_parameters.query ;
            orchem:queryType orchem_subsearch_parameters.query_type ;
            orchem:topn orchem_subsearch_parameters.topn ;
            orchem:strictStereo orchem_subsearch_parameters.strict_stereo ;
            orchem:exact orchem_subsearch_parameters.exact ;
            orchem:tautomers orchem_subsearch_parameters.tautomers .

        iri-proc:result(orchem_subsearch_results.id)
            proc:call iri-proc:call(orchem_subsearch_results.call_id) ;
            proc:result iri:compound(orchem_subsearch_results.compound_id) .
    }.
}.;

--============================================================================--

sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.orchem_smartssearch_parameters as orchem_smartssearch_parameters
    from DB.rdf.orchem_smartssearch_results as orchem_smartssearch_results
{
    create map-orchem:smartsSearch as graph proc:orchem_smartsSearch option (exclusive)
    {
        iri-proc:call(orchem_smartssearch_parameters.call_id)
            proc:context orchem_smartssearch_parameters.context_id ;
            orchem:query orchem_smartssearch_parameters.query ;
            orchem:topn orchem_smartssearch_parameters.topn .

        iri-proc:result(orchem_smartssearch_results.id)
            proc:call iri-proc:call(orchem_smartssearch_results.call_id) ;
            proc:result iri:compound(orchem_smartssearch_results.compound_id) .
    }.
}.;
