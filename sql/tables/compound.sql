log_enable(2);

--============================================================================--

create table compound_sdfiles_gzip
(
    id      integer not null,
    sdf_gz  varchar not null,
    primary key(id)
);


create view compound_bases as select id, gz_decompress(sdf_gz) as sdf from compound_sdfiles_gzip;
grant select on compound_bases TO "SPARQL";

--============================================================================--

create table compound_relations__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


insert into compound_relations__reftable(id, iri) values (455, 'http://semanticscience.org/resource/CHEMINF_000455');
insert into compound_relations__reftable(id, iri) values (461, 'http://semanticscience.org/resource/CHEMINF_000461');
insert into compound_relations__reftable(id, iri) values (462, 'http://semanticscience.org/resource/CHEMINF_000462');
insert into compound_relations__reftable(id, iri) values (480, 'http://semanticscience.org/resource/CHEMINF_000480');
insert into compound_relations__reftable(id, iri) values (1024, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent');
grant select on compound_relations__reftable to "SPARQL";

--------------------------------------------------------------------------------

create table compound_relations
(
    compound_from    integer  not null,
    relation         smallint not null,
    compound_to      integer  not null,
    primary key(compound_from, relation, compound_to)
);


insert into compound_relations(compound_from, relation, compound_to)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as compound_from,
    rt.id                                                                          as relation,
    sprintf_inverse(O, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as compound_to
from (
    sparql select ?S (str(str(?P)) as ?P) ?O from pubchem:compound where
    {
        values ?P
        {
            sio:CHEMINF_000480
            sio:CHEMINF_000462
            sio:CHEMINF_000461
            sio:CHEMINF_000455
            vocab:has_parent
        }

        ?S ?P ?O
    }
) as tbl
inner join compound_relations__reftable as rt on rt.iri=tbl.p;


create index compound_relations__compound_from   on compound_relations(compound_from);
create bitmap index compound_relations__relation on compound_relations(relation);
create index compound_relations__compound_to     on compound_relations(compound_to);
grant select on compound_relations to "SPARQL";

--============================================================================--

create table compound_roles__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


insert into compound_roles__reftable(id, iri) values (0, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#FDAApprovedDrugs');
grant select on compound_roles__reftable to "SPARQL";

--------------------------------------------------------------------------------

create table compound_roles
(
    compound    integer not null,
    roleid      smallint not null,
    primary key(compound, roleid)
);


insert into compound_roles(compound, roleid)
select
    cast(sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as integer) as compound,
    rt.id as roleid
from (
    sparql select ?S (str(?O) as ?O) from pubchem:compound where
    {
        ?S obo:has-role ?O
    }
) as tbl
inner join compound_roles__reftable as rt on rt.iri=tbl.o;


create index compound_roles__compound      on compound_roles(compound);
create bitmap index compound_roles__roleid on compound_roles(roleid);
grant select on compound_roles to "SPARQL";

--============================================================================--

create table compound_biosystems
(
    compound     integer not null,
    biosystem    integer not null,
    primary key(compound, biosystem)
);


insert into compound_biosystems(compound, biosystem)
select
    cast(sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as integer) as compound,
    cast(sprintf_inverse(O, 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID%d', 0)[0] as integer) as biosystem
from (
    sparql select ?S ?O from pubchem:compound where
    {
        ?S obo:BFO_0000056 ?O
    }
) as tbl;


create index compound_biosystems__compound  on compound_biosystems(compound);
create index compound_biosystems__biosystem on compound_biosystems(biosystem);
grant select on compound_biosystems to "SPARQL";

--============================================================================--

create table compound_type_units__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


insert into compound_type_units__reftable(id, iri) values (0, 'http://purl.obolibrary.org/obo/CHEBI_');
insert into compound_type_units__reftable(id, iri) values (1, 'http://purl.bioontology.org/ontology/SNOMEDCT/');
insert into compound_type_units__reftable(id, iri) values (2, 'http://purl.bioontology.org/ontology/NDFRT/N');
insert into compound_type_units__reftable(id, iri) values (3, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C');
insert into compound_type_units__reftable(id, iri) values (4, 'http://www.biopax.org/release/biopax-level3.owl#SmallMolecule');
grant select on compound_type_units__reftable to "SPARQL";

--------------------------------------------------------------------------------

create table compound_types
(
    compound    integer not null,
    unit        smallint not null,
    type        integer not null,
    primary key(compound, unit, type)
);


insert into compound_types(compound, unit, type)
select
    cast(sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as integer) as compound,
    0 as unit,
    cast(sprintf_inverse(O, 'http://purl.obolibrary.org/obo/CHEBI_%d', 0)[0] as integer) as type
from (
    sparql select ?S ?O from pubchem:compound where
    {
        ?S rdf:type ?O .
        filter( strstarts(str(?O), "http://purl.obolibrary.org/obo/CHEBI_"))
    }
) as tbl;


insert into compound_types(compound, unit, type)
select
    cast(sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as integer) as compound,
    1 as unit,
    cast(sprintf_inverse(O, 'http://purl.bioontology.org/ontology/SNOMEDCT/%d', 0)[0] as integer) as type
from (
    sparql select ?S ?O from pubchem:compound where
    {
        ?S rdf:type ?O .
        filter( strstarts(str(?O), "http://purl.bioontology.org/ontology/SNOMEDCT/"))
    }
) as tbl;


insert into compound_types(compound, unit, type)
select
    cast(sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as integer) as compound,
    2 as unit,
    cast(sprintf_inverse(O, 'http://purl.bioontology.org/ontology/NDFRT/N%d', 0)[0] as integer) as type
from (
    sparql select ?S ?O from pubchem:compound where
    {
        ?S rdf:type ?O .
        filter( strstarts(str(?O), "http://purl.bioontology.org/ontology/NDFRT/N"))
    }
) as tbl;


insert into compound_types(compound, unit, type)
select
    cast(sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as integer) as compound,
    3 as unit,
    cast(sprintf_inverse(O, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C%d', 0)[0] as integer) as type
from (
    sparql select ?S ?O from pubchem:compound where
    {
        ?S rdf:type ?O .
        filter( strstarts(str(?O), "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C"))
    }
) as tbl;


insert into compound_types(compound, unit, type)
select
    cast(sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d', 0)[0] as integer) as compound,
    4 as unit,
    -1 as type
from (
    sparql select ?S from pubchem:compound where
    {
        ?S rdf:type bp:SmallMolecule .
    }
) as tbl;


create index compound_types__compound     on compound_types(compound);
create bitmap index compound_types__unit  on compound_types(unit);
create index compound_types__unit_type    on compound_types(unit, type);
grant select on compound_types to "SPARQL";
