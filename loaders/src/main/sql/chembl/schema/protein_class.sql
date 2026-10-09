create table chembl.protein_classes
(
    id         integer not null,
    chembl_id  varchar,
    label      varchar,
    level      varchar,
    path       varchar,
    parent     integer,
    primary key(id)
);


create table chembl.protein_class_component_descendants
(
    protein_class   integer not null,
    component       integer not null,
    primary key(protein_class, component)
);


create table chembl.protein_class_target_descendants
(
    protein_class   integer not null,
    target          integer not null,
    primary key(protein_class, target)
);
