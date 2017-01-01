log_enable(2);

--============================================================================--

create table class_bases
(
    id     integer identity,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


insert into class_bases(iri)
select distinct
    iri
from (
    sparql select ?iri from pubchem:ontology where
    {
        { ?iri rdf:type owl:Class }
        union
        { ?iri rdfs:subClassOf ?S }
        union
        { ?C rdfs:subClassOf ?iri }
        union
        { ?S rdfs:domain ?iri }
        union
        { ?S rdfs:range ?iri }
    }
) as tbl;


grant select on class_bases to "SPARQL";

--============================================================================--

create table class_labels
(
    __       integer identity,
    class    integer not null,
    label    nvarchar not null,
    primary key(__)
);


insert into class_labels(class, label)
select
    rt.id as class,
    label
from (
    sparql select (str(str(?C)) as ?C) ?label from pubchem:ontology where
    {
        ?C rdfs:label ?label .
    }
) as tbl
inner join class_bases as rt on rt.iri=tbl.c;


create index class_labels__class on class_labels(class);
create index class_labels__label on class_labels(label);
grant select on class_labels to "SPARQL";

--============================================================================--

create table class_subclasses
(
    class       integer not null,
    subclass    integer not null,
    primary key(class, subclass)
);


insert into class_subclasses(class, subclass)
select
    rt1.id as class,
    rt2.id as subclass
from (
    sparql select (str(str(?C)) as ?C) (str(str(?S)) as ?S) from pubchem:ontology where
    {
        ?C rdfs:subClassOf ?S .
    }
) as tbl
inner join class_bases as rt1 on rt1.iri=tbl.c
inner join class_bases as rt2 on rt2.iri=tbl.s;


create index class_subclasses__class on class_subclasses(class);
create index class_subclasses__subclass on class_subclasses(subclass);
grant select on class_subclasses to "SPARQL";

--============================================================================--

create table property_bases
(
    id     integer identity,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


insert into property_bases(iri)
select distinct
    iri
from (
    sparql select ?iri from pubchem:ontology where
    {
        { ?iri rdf:type rdf:Property }
        union
        { ?P rdfs:subPropertyOf ?iri }
        union
        { ?iri rdfs:subPropertyOf ?S }
        union
        { ?iri rdfs:domain ?D }
        union
        { ?iri rdfs:range ?P }
    }
) as tbl;


grant select on property_bases to "SPARQL";

--============================================================================--

create table property_labels
(
    __          integer identity,
    property    integer not null,
    label       nvarchar not null,
    primary key(__)
);


insert into property_labels(property, label)
select
    rt.id as property,
    label
from (
    sparql select (str(str(?C)) as ?C) ?label from pubchem:ontology where
    {
        ?C rdfs:label ?label .
    }
) as tbl
inner join property_bases as rt on rt.iri=tbl.c;


create index property_labels__property on property_labels(property);
create index property_labels__label on property_labels(label);
grant select on property_labels to "SPARQL";

--============================================================================--

create table property_subproperties
(
    property       integer not null,
    subproperty    integer not null,
    primary key(property, subproperty)
);


insert into property_subproperties(property, subproperty)
select
    rt1.id as property,
    rt2.id as subproperty
from (
    sparql select (str(str(?P)) as ?P) (str(str(?S)) as ?S) from pubchem:ontology where
    {
        ?P rdfs:subPropertyOf ?S .
    }
) as tbl
inner join property_bases as rt1 on rt1.iri=tbl.p
inner join property_bases as rt2 on rt2.iri=tbl.s;


create index property_subproperties__property on property_subproperties(property);
create index property_subproperties__subproperty on property_subproperties(subproperty);
grant select on property_subproperties to "SPARQL";

--============================================================================--

create table property_domains
(
    property    integer not null,
    domain      integer not null,
    primary key(property, domain)
);


insert into property_domains(property, domain)
select
    rt1.id as property,
    rt2.id as domain
from (
    sparql select (str(str(?P)) as ?P) (str(str(?C)) as ?C) from pubchem:ontology where
    {
        ?P rdfs:domain ?C .
    }
) as tbl
inner join property_bases as rt1 on rt1.iri=tbl.p
inner join class_bases as rt2 on rt2.iri=tbl.c;


create index property_domains__property on property_domains(property);
create index property_domains__domain on property_domains(domain);
grant select on property_domains to "SPARQL";

--============================================================================--

create table property_ranges
(
    property    integer not null,
    range      integer not null,
    primary key(property, range)
);


insert into property_ranges(property, range)
select
    rt1.id as property,
    rt2.id as range
from (
    sparql select (str(str(?P)) as ?P) (str(str(?C)) as ?C) from pubchem:ontology where
    {
        ?P rdfs:range ?C .
    }
) as tbl
inner join property_bases as rt1 on rt1.iri=tbl.p
inner join class_bases as rt2 on rt2.iri=tbl.c;


create index property_ranges__property on property_ranges(property);
create index property_ranges__range on property_ranges(range);
grant select on property_ranges to "SPARQL";
