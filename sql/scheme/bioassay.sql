create table bioassay_bases
(
    id        integer not null,
    source    smallint not null,
    title     nvarchar not null,
    primary key(id)
);


create table bioassay_data
(
    __          integer identity,
    bioassay    integer not null,
    type        smallint not null,
    value       long nvarchar not null,
    primary key(__)
);


create table bioassay_measuregroups
(
    bioassay        integer not null,
    measuregroup    integer not null,
    primary key(bioassay, measuregroup)
);
