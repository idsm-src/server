create table reference_types__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


create table reference_bases
(
    id          integer not null,
    type        smallint not null,
    dcdate      datetime,
    title       nvarchar,
    citation    nvarchar,
    primary key(id)
);


create table reference_citations_long
(
    reference    integer not null,
    citation     long nvarchar not null,
    primary key(reference)
);


create table reference_discusses
(
    reference    integer not null,
    statement    integer not null,
    primary key(reference, statement)
);


create table reference_subject_descriptors
(
    reference     integer not null,
    descriptor    integer not null,
    qualifier     integer not null,
    primary key(reference, descriptor, qualifier)
);

--============================================================================--

insert into reference_types__reftable(id, iri) values (0, 'http://purl.org/spar/fabio/ReviewArticle');
insert into reference_types__reftable(id, iri) values (1, 'http://purl.org/spar/fabio/JournalArticle');
