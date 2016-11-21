log_enable(2);

--============================================================================--

create table endpoint_outcomes__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


insert into endpoint_outcomes__reftable(id, iri) values (0, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#active');
insert into endpoint_outcomes__reftable(id, iri) values (1, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inactive');
insert into endpoint_outcomes__reftable(id, iri) values (2, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inconclusive');
insert into endpoint_outcomes__reftable(id, iri) values (3, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#unspecified');
insert into endpoint_outcomes__reftable(id, iri) values (4, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#probe');
grant select on endpoint_outcomes__reftable to "SPARQL";

--------------------------------------------------------------------------------

create table endpoint_bases
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    outcome         smallint not null,
    primary key(substance, bioassay, measuregroup)
);


insert into endpoint_bases(substance, bioassay, measuregroup, outcome)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d', 0)[0] as substance,
    coalesce(
        sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d', 0),
        sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%U', 0)
    )[1] as bioassay,
    coalesce(
        -2147483647 + 0 * sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d', 2)[1],
        -1 * sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%d', 2)[2],
        sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_PMID%d', 2)[2],
        0
    ) as measuregroup,
    rt.id as outcome
from (
    sparql select ?S ?E (str(str(?O)) as ?O) from pubchem:endpoint where
    {
        ?E obo:IAO_0000136 ?S .
        ?E vocab:PubChemAssayOutcome ?O .
    }
) as tbl
inner join endpoint_outcomes__reftable as rt on rt.iri=tbl.o
where sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID493040', 0) is null;


create index endpoint_bases__substance on endpoint_bases(substance);
create index endpoint_bases__bioassay on endpoint_bases(bioassay);
create index endpoint_bases__bioassay_measuregroup on endpoint_bases(bioassay, measuregroup);
create bitmap index endpoint_bases__outcome on endpoint_bases(outcome);
grant select on endpoint_bases to "SPARQL";

--============================================================================--

create table endpoint_measurements
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    type            smallint not null,
    value           float not null,
    label           varchar not null,
    primary key(substance, bioassay, measuregroup)
);


insert into endpoint_measurements(substance, bioassay, measuregroup, type, value, label)
select
    sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_%U', 0)[0] as substance,
    coalesce(
        sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d', 0),
        sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%U', 0)
    )[1] as bioassay,
    coalesce(
        -2147483647 + 0 * sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d', 2)[1],
        -1 * sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%d', 2)[2],
        sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_PMID%d', 2)[2],
        0
    ) as measuregroup,
    sprintf_inverse(T, 'http://www.bioassayontology.org/bao#BAO_%07d', 0)[0] as type,
    V as value,
    L as label
from (
    sparql select ?E ?V ?L ?T from pubchem:endpoint where
    {
        ?E sio:has-value ?V.
        ?E rdfs:label ?L.
        ?E rdf:type ?T.
    }
) as tbl
where sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID493040', 0) is null;


create index endpoint_measurements__substance on endpoint_measurements(substance);
create index endpoint_measurements__bioassay on endpoint_measurements(bioassay);
create index endpoint_measurements__bioassay_measuregroup on endpoint_measurements(bioassay, measuregroup);
create bitmap index bendpoint_measurements__type on endpoint_measurements(type);
grant select on endpoint_measurements to "SPARQL";

--============================================================================--

create table endpoint_references
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    reference        integer not null,
    primary key(substance, bioassay, measuregroup, reference)
);


insert into endpoint_references(substance, bioassay, measuregroup, reference)
select
    sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_%U', 0)[0] as substance,
    coalesce(
        sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d', 0),
        sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%U', 0)
    )[1] as bioassay,
    coalesce(
        -2147483647 + 0 * sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d', 2)[1],
        -1 * sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%d', 2)[2],
        sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_PMID%d', 2)[2],
        0
    ) as measuregroup,
    sprintf_inverse(R, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference
from (
    sparql select ?E ?R from pubchem:endpoint where
    {
        ?E cito:citesAsDataSource ?R.
    }
) as tbl
where sprintf_inverse(E, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID493040', 0) is null;


create index endpoint_references__substance on endpoint_references(substance);
create index endpoint_references__bioassay on endpoint_references(bioassay);
create index endpoint_references__bioassay_measuregroup on endpoint_references(bioassay, measuregroup);
create index endpoint_references__substance_bioassay_measuregroup on endpoint_references(substance, bioassay, measuregroup);
create index endpoint_references__reference on endpoint_references(reference);
grant select on endpoint_references TO "SPARQL";
