log_enable(2);

--============================================================================--

-- workaround: add missing sources
insert into source_bases(iri)
select
    tbl.R as iri
from (
    sparql select distinct (str(str(?R)) as ?R) from pubchem:substance where
    {
        ?S dcterms:source ?R
    }
) as tbl
left join source_bases as rt2 on rt2.iri = tbl.R where rt2.id is null;

--------------------------------------------------------------------------------

create table substance_bases
(
    id           integer not null,
    source       smallint,
    available    datetime,
    modified     datetime,
    compound     integer,
    primary key(id)
);


insert into substance_bases(id, source, available, modified, compound)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as id,
    rt.id as source,
    available,
    modified,
    case
        when C is not null then sprintf_inverse(C, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0]
        else null
    end as compound
from (
    sparql select ?S (str(str(?source)) as ?source) ?available ?modified ?C from pubchem:substance where
    {
        ?S dcterms:available ?available .
        ?S dcterms:source ?source .
        ?S dcterms:modified ?modified .
        optional { ?S sio:CHEMINF_000477 ?C }
    }
) as tbl
inner join source_bases as rt on rt.iri=tbl.source;


-- add missing ...
insert soft substance_bases(id)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as id
from (
    sparql select distinct ?S from pubchem:substance where
    {
        ?S rdf:type [] .
    }
) as tbl;


-- add missing ...
insert soft substance_bases(id)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as id
from (
    sparql select distinct ?S from pubchem:substance where
    {
        ?S obo:BFO_0000056 []
    }
) as tbl;


-- add missing ...
insert soft substance_bases(id)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as id
from (
    sparql select distinct ?S from pubchem:substance where
    {
        ?S cito:isDiscussedBy []
    }
) as tbl;

create index substance_bases__source on substance_bases(source);
create index substance_bases__available on substance_bases(available);
create index substance_bases__modified on substance_bases(modified);
create index substance_bases__compound on substance_bases(compound);
grant select on substance_bases to "SPARQL";

--============================================================================--

create table substance_types
(
    substance    integer not null,
    chebi        integer not null,
    primary key(substance, chebi)
);


insert into substance_types(substance, chebi)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as substance,
    sprintf_inverse(C, 'http://purl.obolibrary.org/obo/CHEBI_%d', 0)[0] as chebi
from (
    sparql select ?S ?C from pubchem:substance where
    {
        ?S rdf:type ?C .
    }
) as tbl;


create index substance_types__substance on substance_types(substance);
create index substance_types__chebi on substance_types(chebi);
grant select on substance_types to "SPARQL";

--============================================================================--

create table substance_chembl_matches
(
    substance    integer not null,
    chembl       integer not null,
    primary key(substance, chembl)
);


insert into substance_chembl_matches(substance, chembl)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as substance,
    sprintf_inverse(C, 'http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL%d', 0)[0] as chembl
from (
    sparql
    select ?S ?C from pubchem:substance where
    {
        ?S skos:exactMatch ?C.
        filter(regex(str(?C), "^http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL[0-9]+$$"))
    }
) as tbl;


create index substance_chembl_matches__substance on substance_chembl_matches(substance);
create index substance_chembl_matches__chembl on substance_chembl_matches(chembl);
grant select on substance_chembl_matches to "SPARQL";

--------------------------------------------------------------------------------

create table substance_schembl_matches
(
    substance    integer not null,
    schembl      integer not null,
    primary key(substance, schembl)
);


insert into substance_schembl_matches(substance, schembl)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as substance,
    sprintf_inverse(C, 'http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL%d', 0)[0] as schembl
from (
    sparql
    select ?S ?C from pubchem:substance where
    {
        ?S skos:exactMatch ?C.
        filter(regex(str(?C), "^http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL[0-9]+$$"))
    }
) as tbl;


create index substance_schembl_matches__substance on substance_schembl_matches(substance);
create index substance_schembl_matches__schembl on substance_schembl_matches(schembl);
grant select on substance_schembl_matches to "SPARQL";

--============================================================================--

create table substance_references
(
    substance    integer not null,
    reference    integer not null,
    primary key(substance, reference)
);


insert into substance_references(substance, reference)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as substance,
    sprintf_inverse(D, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference
from (
    sparql
    select ?S ?D from pubchem:substance where
    {
        ?S cito:isDiscussedBy ?D
    }
) as tbl;


create index substance_references__substance on substance_references(substance);
create index substance_references__reference on substance_references(reference);
grant select on substance_references to "SPARQL";

--============================================================================--

create table substance_pdblinks
(
    substance    integer not null,
    pdblink      char(4) not null,
    primary key(substance, pdblink)
);


insert into substance_pdblinks(substance, pdblink)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as substance,
    sprintf_inverse(L, 'http://rdf.wwpdb.org/pdb/%U', 0)[0] as pdblink
from (
    sparql
    select ?S ?L from pubchem:substance where
    {
        ?S pdbo:link_to_pdb ?L
    }
) as tbl;


create index substance_pdblinks__substance on substance_pdblinks(substance);
create index substance_pdblinks__pdblink on substance_pdblinks(pdblink);
grant select on substance_pdblinks to "SPARQL";
