create table chembl.drug_indications
(
    id            integer not null,
    chembl_id     varchar,
    molecule      integer,
    mesh          varchar,
    mesh_heading  varchar,
    efo_unit      smallint,
    efo_id        integer,
    efo_name      varchar,
    phase         integer,
    primary key(id)
);
