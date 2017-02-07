create index orchem_simsearch_parameters_context_id on orchem_simsearch_parameters(context_id);
grant all privileges on orchem_simsearch_parameters to "SPARQL";

--------------------------------------------------------------------------------

create index orchem_simsearch_results_call_id on orchem_simsearch_results(call_id);
grant all privileges on orchem_simsearch_results to "SPARQL";

--------------------------------------------------------------------------------

create index orchem_subsearch_parameters_context_id on orchem_subsearch_parameters(context_id);
grant all privileges on orchem_subsearch_parameters to "SPARQL";

--------------------------------------------------------------------------------

create index orchem_subsearch_results_call_id on orchem_subsearch_results(call_id);
grant all privileges on orchem_subsearch_results to "SPARQL";

--------------------------------------------------------------------------------

create index orchem_smartssearch_parameters_context_id on orchem_smartssearch_parameters(context_id);
grant all privileges on orchem_smartssearch_parameters to "SPARQL";

--------------------------------------------------------------------------------

create index orchem_smartssearch_results_call_id on orchem_smartssearch_results(call_id);
grant all privileges on orchem_smartssearch_results to "SPARQL";
