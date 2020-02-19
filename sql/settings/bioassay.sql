create index bioassay_bases__source on bioassay_bases(source);
create index bioassay_bases__title on bioassay_bases using gin (to_tsvector('english', title));
grant select on bioassay_bases to sparql;

--------------------------------------------------------------------------------

create index bioassay_data__bioassay_type on bioassay_data(bioassay, type_id);
create index bioassay_data__bioassay on bioassay_data(bioassay);
create index bioassay_data__type on bioassay_data(type_id);
create index bioassay_data__value on bioassay_data using gin (to_tsvector('english', value));
grant select on bioassay_data to sparql;

--------------------------------------------------------------------------------

insert into bioassay_measuregroups(bioassay, measuregroup)
select distinct bioassay, measuregroup from endpoint_outcomes;

create index bioassay_measuregroups__bioassay on bioassay_measuregroups(bioassay);
grant select on bioassay_measuregroups to sparql;
