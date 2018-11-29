create table compound_components
(
    compound     integer not null,
    component    integer not null,
    primary key(compound, component)
);


create table compound_isotopologues
(
    compound        integer not null,
    isotopologue    integer not null,
    primary key(compound, isotopologue)
);


create table compound_parents
(
    compound    integer not null,
    parent      integer not null,
    primary key(compound, parent)
);


create table compound_stereoisomers
(
    compound    integer not null,
    isomer      integer not null,
    primary key(compound, isomer)
);


create table compound_same_connectivities
(
    compound    integer not null,
    isomer      integer not null,
    primary key(compound, isomer)
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
