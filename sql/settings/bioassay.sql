create index bioassay_bases__source on bioassay_bases(source);
grant select on bioassay_bases to "SPARQL";

--------------------------------------------------------------------------------

create index bioassay_data__bioassay_type on bioassay_data(bioassay, type);
create index bioassay_data__bioassay on bioassay_data(bioassay);
create bitmap index bioassay_data__type on bioassay_data(type);
grant select on bioassay_data to "SPARQL";

--------------------------------------------------------------------------------

insert into bioassay_measuregroups(bioassay, measuregroup)
select distinct bioassay, measuregroup from endpoint_bases;

create index bioassay_measuregroups__bioassay on bioassay_measuregroups(bioassay);
grant select on bioassay_measuregroups to "SPARQL";
