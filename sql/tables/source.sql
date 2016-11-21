log_enable(2);

--============================================================================--

create table source_bases
(
    id       smallint identity,
    iri      varchar not null,
    title    varchar,
    primary key(id),
    unique(iri)
);


insert into source_bases(iri, title)
select
    iri,
    title
from (
    sparql select ?iri ?title from pubchem:source where
    {
        ?iri rdf:type dcterms:Dataset .

        optional { ?iri dcterms:title ?title }
    }
) as tbl;


grant select on source_bases to "SPARQL";

--============================================================================--

create table source_subjects__reftable
(
    id     smallint identity,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


insert into source_subjects__reftable(iri)
select
    iri
from (
    sparql select distinct ?iri from pubchem:source where
    {
        ?source dcterms:subject ?iri
    }
) as tbl;


grant select on source_subjects__reftable to "SPARQL";

--------------------------------------------------------------------------------

create table source_subjects
(
    source     smallint not null,
    subject    smallint not null,
    primary key(source, subject)
);


insert into source_subjects(source, subject)
select
    rt1.id as source,
    rt2.id as subject
from (
    sparql select (str(str(?source)) as ?source) (str(str(?subject)) as ?subject) from pubchem:source where
    {
        ?source dcterms:subject ?subject
    }
) as tbl
inner join source_bases as rt1 on rt1.iri=tbl.source
inner join source_subjects__reftable as rt2 on rt2.iri=tbl.subject;


create index source_subjects__source  on source_subjects(source);
create index source_subjects__subject on source_subjects(subject);
grant select on source_subjects to "SPARQL";

--============================================================================--

create table source_alternatives
(
    __             smallint identity,
    source         smallint not null,
    alternative    varchar not null,
    primary key(__)
);


insert into source_alternatives(source, alternative)
select
    rt.id as source,
    tbl.alternative as alternative
from (
    sparql select (str(str(?source)) as ?source) ?alternative from pubchem:source where
    {
        ?source dcterms:alternative ?alternative
    }
) as tbl
inner join source_bases as rt on rt.iri=tbl.source;


create index source_alternatives__source  on source_alternatives(source);
create index source_alternatives__alternative on source_alternatives(alternative);
grant select on source_alternatives to "SPARQL";
