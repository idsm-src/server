insert into endpoint_bases(substance, bioassay, measuregroup)
select distinct substance, bioassay, measuregroup from endpoint_outcomes;

create index endpoint_bases__substance on endpoint_bases(substance);
create index endpoint_bases__bioassay on endpoint_bases(bioassay);
create index endpoint_bases__bioassay_measuregroup on endpoint_bases(bioassay, measuregroup);
grant select on endpoint_bases to sparql;

--------------------------------------------------------------------------------

create index endpoint_outcomes__substance on endpoint_outcomes(substance);
create index endpoint_outcomes__bioassay on endpoint_outcomes(bioassay);
create index endpoint_outcomes__bioassay_measuregroup on endpoint_outcomes(bioassay, measuregroup);
create index endpoint_outcomes__substance_bioassay_measuregroup on endpoint_outcomes(substance, bioassay, measuregroup);
create index endpoint_outcomes__outcome on endpoint_outcomes(outcome_id);
grant select on endpoint_outcomes to sparql;

--------------------------------------------------------------------------------

create index endpoint_measurements__substance on endpoint_measurements(substance);
create index endpoint_measurements__bioassay on endpoint_measurements(bioassay);
create index endpoint_measurements__bioassay_measuregroup on endpoint_measurements(bioassay, measuregroup);
create index endpoint_measurements__type on endpoint_measurements(type_id);
grant select on endpoint_measurements to sparql;

--------------------------------------------------------------------------------

create index endpoint_references__substance on endpoint_references(substance);
create index endpoint_references__bioassay on endpoint_references(bioassay);
create index endpoint_references__bioassay_measuregroup on endpoint_references(bioassay, measuregroup);
create index endpoint_references__substance_bioassay_measuregroup on endpoint_references(substance, bioassay, measuregroup);
create index endpoint_references__reference on endpoint_references(reference);
grant select on endpoint_references TO sparql;
