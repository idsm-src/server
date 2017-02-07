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


create table orchem_simsearch_results
(
    id             integer identity,
    call_id        integer not null,
    compound_id    integer not null,
    score          real not null,
    primary key(id),
    foreign key(call_id) references orchem_simsearch_parameters(call_id)
);


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


create table orchem_subsearch_results
(
    id             integer identity,
    call_id        integer not null,
    compound_id    integer not null,
    primary key(id),
    foreign key(call_id) references orchem_subsearch_parameters(call_id)
);


create table orchem_smartssearch_parameters
(
    call_id       integer not null,
    context_id    integer not null,
    query         long varchar not null,
    topn          integer not null,
    primary key(call_id)
);


create table orchem_smartssearch_results
(
    id             integer identity,
    call_id        integer not null,
    compound_id    integer not null,
    primary key(id),
    foreign key(call_id) references orchem_subsearch_parameters(call_id)
);
