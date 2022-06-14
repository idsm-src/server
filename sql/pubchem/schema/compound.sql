create table pubchem.compound_components
(
    compound     integer not null,
    component    integer not null,
    primary key(compound, component)
);


create table pubchem.compound_isotopologues
(
    compound        integer not null,
    isotopologue    integer not null,
    primary key(compound, isotopologue)
);


create table pubchem.compound_parents
(
    compound    integer not null,
    parent      integer not null,
    primary key(compound, parent)
);


create table pubchem.compound_stereoisomers
(
    compound    integer not null,
    isomer      integer not null,
    primary key(compound, isomer)
);


create table pubchem.compound_same_connectivities
(
    compound    integer not null,
    isomer      integer not null,
    primary key(compound, isomer)
);


create table pubchem.compound_roles
(
    compound    integer not null,
    role_id     integer not null,
    primary key(compound, role_id)
);


create table pubchem.compound_types
(
    compound     integer not null,
    type_unit    smallint not null,
    type_id      integer not null,
    primary key(compound, type_unit, type_id)
);


create table pubchem.compound_active_ingredients
(
    compound           integer not null,
    ingredient_unit    smallint not null,
    ingredient_id      integer not null,
    primary key(compound, ingredient_unit, ingredient_id)
);


create table pubchem.compound_titles
(
    compound           integer not null,
    title              varchar not null,
    primary key(compound)
);


create table pubchem.compound_bases
(
    id    integer not null,
    keep  bool not null,
    primary key(id)
);
