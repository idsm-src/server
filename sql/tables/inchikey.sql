log_enable(2);

--============================================================================--

create table inchikey_bases
(
    id          integer identity,
    inchikey    varchar(27) unique not null,
    primary key(id)
);


insert into inchikey_bases(inchikey)
select
    inchikey
from (
    sparql select distinct ?inchikey from pubchem:inchikey where
    {
        ?S sio:has-value ?inchikey
    }
) as tbl;


grant select on inchikey_bases to "SPARQL";

--============================================================================--

create table inchikey_compounds
(
    inchikey    integer not null,
    compound    integer not null,
    primary key(compound)
);


insert into inchikey_compounds(inchikey, compound)
select
    rt.id as inchikey,
    sprintf_inverse(tbl.C, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as compound
from (
    sparql select ?S ?C from pubchem:inchikey where
    {
        ?S sio:is-attribute-of ?C
    }
) as tbl
inner join inchikey_bases as rt on rt.inchikey = sprintf_inverse(tbl.S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/%U', 0)[0];


create index inchikey_compounds__inchikey on inchikey_compounds(inchikey);
grant select on inchikey_compounds to "SPARQL";

--============================================================================--

create table inchikey_subjects
(
    inchikey    integer not null,
    subject     integer not null,
    primary key(inchikey)
);


insert into inchikey_subjects(inchikey, subject)
select
    rt.id as inchikey,
    sprintf_inverse(tbl.O, 'http://id.nlm.nih.gov/mesh/M%d', 0)[0] as subject
from (
    sparql select ?S ?O from pubchem:inchikey where
    {
        ?S dcterms:subject ?O
    }
) as tbl
inner join inchikey_bases as rt on rt.inchikey = sprintf_inverse(tbl.S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/%U', 0)[0];


create index inchikey_subjects__subject on inchikey_subjects(subject);
grant select on inchikey_subjects to "SPARQL";
