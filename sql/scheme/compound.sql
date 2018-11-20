create table compound_relations
(
    compound_from    integer not null,
    relation_unit    smallint not null,
    relation_id      smallint not null,
    compound_to      integer not null,
    primary key(compound_from, relation_unit, relation_id, compound_to)
);


create table compound_roles
(
    compound    integer not null,
    role_id     integer not null,
    primary key(compound, role_id)
);


create table compound_biosystems
(
    compound     integer not null,
    biosystem    integer not null,
    primary key(compound, biosystem)
);


create table compound_types
(
    compound     integer not null,
    type_unit    smallint not null,
    type_id      integer not null,
    primary key(compound, type_unit, type_id)
);


create table compound_active_ingredients
(
    compound           integer not null,
    ingredient_unit    smallint not null,
    ingredient_id      integer not null,
    primary key(compound, ingredient_unit, ingredient_id)
);


create table compound_bases
(
    id    integer not null,
    primary key(id)
);
