log_enable(2);

--============================================================================--

create table concept_bases
(
    id         smallint identity,
    iri        varchar not null,
    label      nvarchar,
    scheme     smallint,
    broader    smallint,
    primary key(id),
    unique(iri)
);


insert into concept_bases(iri, label)
select
    iri,
    label
from (
    sparql select ?iri ?label from pubchem:concept where
    {
        ?iri rdf:type ?type.
        optional { ?iri skos:prefLabel ?label }
    }
) as tbl;


insert replacing concept_bases(id, iri, label, scheme)
select
    rt1.id as id,
    rt1.iri as iri,
    rt1.label as label,
    rt2.id as scheme
from (
    sparql select (str(str(?iri)) as ?iri) (str(str(?scheme)) as ?scheme) from pubchem:concept where
    {
        ?iri skos:inScheme ?scheme
    }
) as tbl
inner join concept_bases as rt1 on rt1.iri=tbl.iri
inner join concept_bases as rt2 on rt2.iri=tbl.scheme;


insert replacing concept_bases(id, iri, label, scheme, broader)
select
    rt1.id as id,
    rt1.iri as iri,
    rt1.label as label,
    rt1.scheme as scheme,
    rt2.id as broader
from (
    sparql select (str(str(?iri)) as ?iri) (str(str(?broader)) as ?broader) from pubchem:concept where
    {
        ?iri skos:broader ?broader
        filter(?iri != ?broader)
    }
) as tbl
inner join concept_bases as rt1 on rt1.iri=tbl.iri
inner join concept_bases as rt2 on rt2.iri=tbl.broader;


grant select on concept_bases to "SPARQL";
