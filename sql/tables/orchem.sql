create table orchem_simsearch_parameters
(
  call_id       integer not null,
  context_id    integer not null,
  query         long varchar not null,
  query_type    varchar not null,
  cutoff        real not null,
  topn          integer not null,
  primary key(call_id)
);


create index orchem_simsearch_parameters_context_id on orchem_simsearch_parameters(context_id);
grant all privileges on orchem_simsearch_parameters to "SPARQL";

--------------------------------------------------------------------------------

create table orchem_simsearch_results
(
  id             integer identity,
  call_id        integer not null,
  compound_id    integer not null,
  score          real not null,
  primary key(id),
  foreign key(call_id) references orchem_simsearch_parameters(call_id)
);


create index orchem_simsearch_results_call_id on orchem_simsearch_results(call_id);
grant all privileges on orchem_simsearch_results to "SPARQL";

--------------------------------------------------------------------------------

create table orchem_subsearch_parameters
(
  call_id          integer not null,
  context_id       integer not null,
  query            long varchar not null,
  query_type       varchar not null,
  topn             integer not null,
  strict_stereo    smallint not null,
  exact            smallint not null,
  tautomers        smallint not null,
  primary key(call_id)
);


create index orchem_subsearch_parameters_context_id on orchem_subsearch_parameters(context_id);
grant all privileges on orchem_subsearch_parameters to "SPARQL";

--------------------------------------------------------------------------------

create table orchem_subsearch_results
(
  id             integer identity,
  call_id        integer not null,
  compound_id    integer not null,
  primary key(id),
  foreign key(call_id) references orchem_subsearch_parameters(call_id)
);


create index orchem_subsearch_results_call_id on orchem_subsearch_results(call_id);
grant all privileges on orchem_subsearch_results to "SPARQL";

--------------------------------------------------------------------------------

create table orchem_smartssearch_parameters
(
  call_id       integer not null,
  context_id    integer not null,
  query         long varchar not null,
  topn          integer not null,
  primary key(call_id)
);


create index orchem_smartssearch_parameters_context_id on orchem_smartssearch_parameters(context_id);
grant all privileges on orchem_smartssearch_parameters to "SPARQL";

--------------------------------------------------------------------------------

create table orchem_smartssearch_results
(
  id             integer identity,
  call_id        integer not null,
  compound_id    integer not null,
  primary key(id),
  foreign key(call_id) references orchem_subsearch_parameters(call_id)
);


create index orchem_smartssearch_results_call_id on orchem_smartssearch_results(call_id);
grant all privileges on orchem_smartssearch_results to "SPARQL";
