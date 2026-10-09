create table chembl.mechanisms
(
    id            integer not null,
    chembl_id     varchar,
    molecule      integer,
    target        integer,
    binding_site  integer,
    description   varchar,
    action_type   varchar,
    primary key(id)
);
