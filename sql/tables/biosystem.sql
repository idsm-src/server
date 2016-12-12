log_enable(2);

--============================================================================--

-- workaround: add missing sources
insert into source_bases(iri)
select
    tbl.R as iri
from (
    sparql select distinct (str(str(?R)) as ?R) from pubchem:biosystem where
    {
        ?S dcterms:source ?R
    }
) as tbl
left join source_bases as rt2 on rt2.iri = tbl.R where rt2.id is null;

--------------------------------------------------------------------------------

create table biosystem_bases
(
    id          integer not null,
    source      smallint not null,
    title       nvarchar not null,
    organism    integer,
    primary key(id)
);


insert into biosystem_bases(id, source, title, organism)
select
    sprintf_inverse(tbl.S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID%d', 0)[0] as id,
    rt.id as source,
    tbl.title as title,
    case
        when tbl.O is not null then sprintf_inverse(tbl.O, 'http://identifiers.org/taxonomy/%d', 0)[0]
        else null
    end as organism
from (
    sparql select ?S (str(str(?R)) as ?R) ?title ?O from pubchem:biosystem where
    {
        ?S rdf:type bp:Pathway .
        optional { ?S dcterms:source ?R }
        optional { ?S dcterms:title ?title }
        optional { ?S bp:organism ?O }
    }
) as tbl
inner join source_bases as rt on rt.iri=tbl.r;


create bitmap index biosystem_bases__source on biosystem_bases(source);
create index biosystem_bases__organism on biosystem_bases(organism);
grant select on biosystem_bases to "SPARQL";

--============================================================================--

create table biosystem_components
(
    biosystem    integer not null,
    component    integer not null,
    primary key(biosystem, component)
);


insert into biosystem_components(biosystem, component)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID%d', 0)[0] as biosystem,
    sprintf_inverse(C, 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID%d', 0)[0] as component
from (
    sparql select ?S ?C from pubchem:biosystem where
    {
        ?S bp:pathwayComponent ?C
    }
) as tbl;


create index biosystem_components__biosystem on biosystem_components(biosystem);
create index biosystem_components__component on biosystem_components(component);
grant select on biosystem_components to "SPARQL";

--============================================================================--

create table biosystem_references
(
    biosystem    integer not null,
    reference    integer not null,
    primary key(biosystem, reference)
);


insert into biosystem_references(biosystem, reference)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID%d', 0)[0] as biosystem,
    sprintf_inverse(R, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference
from (
    sparql select ?S ?R from pubchem:biosystem where
    {
        ?S cito:isDiscussedBy ?R .
    }
) as tbl;


create index biosystem_references__biosystem on biosystem_references(biosystem);
create index biosystem_references__reference on biosystem_references(reference);
grant select on biosystem_references to "SPARQL";

--============================================================================--

create table biosystem_matches
(
    biosystem      integer not null,
    wikipathway    integer not null,
    primary key(biosystem)
);


insert into biosystem_matches(biosystem, wikipathway)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID%d', 0)[0] as biosystem,
    sprintf_inverse(M, 'http://identifiers.org/wikipathways/WP%d', 0)[0] as wikipathway
from (
    sparql
    select ?S ?M from pubchem:biosystem where
    {
        ?S skos:exactMatch ?M.
    }
) as tbl;


create index biosystem_matches__wikipathway on biosystem_matches(wikipathway);
grant select on biosystem_matches to "SPARQL";
