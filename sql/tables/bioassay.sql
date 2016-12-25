log_enable(2);

--============================================================================--

create table bioassay_bases
(
    id        integer not null,
    source    smallint not null,
    title     nvarchar not null,
    primary key(id)
);


create index bioassay_bases__source on bioassay_bases(source);
grant select on bioassay_bases to "SPARQL";

--============================================================================--

create table bioassay_data
(
    __          integer identity,
    bioassay    integer not null,
    type        smallint not null,
    value       long nvarchar not null,
    primary key(__)
);


create index bioassay_data__bioassay_type on bioassay_data(bioassay, type);
create index bioassay_data__bioassay on bioassay_data(bioassay);
create bitmap index bioassay_data__type on bioassay_data(type);
grant select on bioassay_data to "SPARQL";
