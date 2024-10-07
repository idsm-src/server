create table void.graphs
(
    id              integer not null,
    iri             varchar unique not null,
    classes         bigint not null,
    properties      bigint not null,
    triples         bigint not null,
    subjects        bigint not null,
    objects         bigint not null,
    iri_objects     bigint not null,
    literal_objects bigint not null,
    primary key(id)
);


create table void.class_partitions
(
    graph           integer not null,
    class_unit      smallint not null,
    class_id        integer not null,
    classes         bigint not null,
    properties      bigint not null,
    triples         bigint not null,
    subjects        bigint not null,
    objects         bigint not null,
    iri_objects     bigint not null,
    literal_objects bigint not null,
    primary key(graph, class_unit, class_id)
);


create table void.property_partitions
(
    graph           integer not null,
    property_unit   smallint not null,
    property_id     integer not null,
    triples         bigint not null,
    subjects        bigint not null,
    objects         bigint not null,
    iri_objects     bigint not null,
    literal_objects bigint not null,
    primary key(graph, property_unit, property_id)
);


create table void.class_property_partitions
(
    graph           integer not null,
    class_unit      smallint not null,
    class_id        integer not null,
    property_unit   smallint not null,
    property_id     integer not null,
    triples         bigint not null,
    subjects        bigint not null,
    objects         bigint not null,
    iri_objects     bigint not null,
    literal_objects bigint not null,
    primary key(graph, class_unit, class_id, property_unit, property_id)
);


create table void.linksets
(
    property_graph  integer not null,
    property_unit   smallint not null,
    property_id     integer not null,
    subject_graph   integer not null,
    subject_unit    smallint not null,
    subject_id      integer not null,
    object_graph    integer not null,
    object_unit     smallint not null,
    object_id       integer not null,
    triples         bigint not null,
    subjects        bigint not null,
    objects         bigint not null,
    primary key(property_graph, property_unit, property_id, subject_graph, subject_unit, subject_id, object_graph, object_unit, object_id)
);


create table void.literal_linksets
(
    property_graph  integer not null,
    property_unit   smallint not null,
    property_id     integer not null,
    subject_graph   integer not null,
    subject_unit    smallint not null,
    subject_id      integer not null,
    datatype_unit   smallint not null,
    datatype_id     integer not null,
    triples         bigint not null,
    subjects        bigint not null,
    objects         bigint not null,
    primary key(property_graph, property_unit, property_id, subject_graph, subject_unit, subject_id, datatype_unit, datatype_id)
);
