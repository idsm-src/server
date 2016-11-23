log_enable(2);

--============================================================================--

create table synonym_bases
(
    id       integer identity,
    md5      varchar(32) unique not null,
    primary key(id)
);


insert into synonym_bases(md5)
select distinct
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_%U', 0)[0] as md5
from (
    sparql select ?S from pubchem:synonym where
    {
        ?S sio:has-value []
    }
) as tbl;


grant select on synonym_bases to "SPARQL";

--============================================================================--

create table synonym_values
(
    __         integer identity,
    synonym    integer not null,
    value      nvarchar not null,
    primary key(__)
);


insert into synonym_values(synonym, value)
select
    rt.id as synonym,
    value
from (
    sparql select ?S ?value from pubchem:synonym where
    {
        ?S sio:has-value ?value
    }
) as tbl
inner join synonym_bases as rt on rt.md5 = sprintf_inverse(tbl.S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_%U', 0)[0];


create index synonym_values__synonym on synonym_values(synonym);
grant select on synonym_values to "SPARQL";

--============================================================================--

create table synonym_types
(
    synonym    integer not null,
    type       integer not null,
    primary key(synonym, type)
);


insert into synonym_types(synonym, type)
select
    rt.id as synonym,
    sprintf_inverse(tbl.T, 'http://semanticscience.org/resource/CHEMINF_%d', 0)[0] as type
from (
    sparql select ?S ?T from pubchem:synonym where
    {
        ?S rdf:type ?T
    }
) as tbl
inner join synonym_bases as rt on rt.md5 = sprintf_inverse(tbl.S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_%U', 0)[0];


create index synonym_types__synonym on synonym_types(synonym);
create bitmap index synonym_types__type on synonym_types(type);
grant select on synonym_types to "SPARQL";

--============================================================================--

create table synonym_compounds
(
    synonym     integer not null,
    compound    integer not null,
    primary key(synonym, compound)
);


insert into synonym_compounds(synonym, compound)
select
    rt.id as synonym,
    sprintf_inverse(tbl.C, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as compound
from (
    sparql select ?S ?C from pubchem:synonym where
    {
        ?S sio:is-attribute-of ?C
    }
) as tbl
inner join synonym_bases as rt on rt.md5 = sprintf_inverse(tbl.S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_%U', 0)[0];


create index synonym_compounds__synonym on synonym_compounds(synonym);
create index synonym_compounds__compound on synonym_compounds(compound);
grant select on synonym_compounds to "SPARQL";

--============================================================================--

create table synonym_mesh_subjects
(
    synonym    integer not null,
    subject    integer not null,
    primary key(synonym, subject)
);


insert into synonym_mesh_subjects(synonym, subject)
select
    synonym,
    subject
from (
    select
        rt.id as synonym,
        sprintf_inverse(tbl.V, 'http://id.nlm.nih.gov/mesh/M%d', 2)[0] as subject
    from (
        sparql select ?S ?V from pubchem:synonym where
        {
            ?S dcterms:subject ?V
        }
    ) as tbl
    inner join synonym_bases as rt on rt.md5 = sprintf_inverse(tbl.S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_%U', 0)[0]
) as t where subject is not null;


create index synonym_mesh_subjects__synonym on synonym_mesh_subjects(synonym);
create index synonym_mesh_subjects__subject on synonym_mesh_subjects(subject);
grant select on synonym_mesh_subjects to "SPARQL";

--============================================================================--

-- workaround: add missing concepts

insert into concept_bases(iri)
select
    tbl.V as iri
from (
    sparql select distinct (str(str(?V)) as ?V) from pubchem:synonym where
    {
        ?S dcterms:subject ?V
        filter(strstarts(str(?V),'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/'))
    }
) as tbl
left join concept_bases as rt2 on rt2.iri = tbl.V where rt2.id is null;

--------------------------------------------------------------------------------

create table synonym_concept_subjects
(
    synonym    integer not null,
    subject    smallint not null,
    primary key(synonym, subject)
);


insert into synonym_concept_subjects(synonym, subject)
select
    rt1.id as synonym,
    rt2.id as subject
from (
    sparql select ?S (str(str(?V)) as ?V) from pubchem:synonym where
    {
        ?S dcterms:subject ?V
    }
) as tbl
inner join synonym_bases as rt1 on rt1.md5 = sprintf_inverse(tbl.S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_%U', 0)[0]
inner join concept_bases as rt2 on rt2.iri = tbl.V;


create index synonym_concept_subjects__synonym on synonym_concept_subjects(synonym);
create index synonym_concept_subjects__subject on synonym_concept_subjects(subject);
grant select on synonym_concept_subjects to "SPARQL";
