create index endpoint_bases__substance on endpoint_bases(substance);
create index endpoint_bases__bioassay on endpoint_bases(bioassay);
create index endpoint_bases__bioassay_measuregroup on endpoint_bases(bioassay, measuregroup);
create index endpoint_bases__outcome on endpoint_bases(outcome_id);
grant select on endpoint_bases to "SPARQL";

--------------------------------------------------------------------------------

create index endpoint_measurements__substance on endpoint_measurements(substance);
create index endpoint_measurements__bioassay on endpoint_measurements(bioassay);
create index endpoint_measurements__bioassay_measuregroup on endpoint_measurements(bioassay, measuregroup);
create index endpoint_measurements__type on endpoint_measurements(type_id);
grant select on endpoint_measurements to "SPARQL";

--------------------------------------------------------------------------------

create index endpoint_references__substance on endpoint_references(substance);
create index endpoint_references__bioassay on endpoint_references(bioassay);
create index endpoint_references__bioassay_measuregroup on endpoint_references(bioassay, measuregroup);
create index endpoint_references__substance_bioassay_measuregroup on endpoint_references(substance, bioassay, measuregroup);
create index endpoint_references__reference on endpoint_references(reference);
grant select on endpoint_references TO "SPARQL";

-- workaround
insert into endpoint_references select r.substance, r.bioassay, b.measuregroup, r.reference from endpoint_references r, endpoint_bases b where r.substance = b.substance and r.bioassay = b.bioassay and r.bioassay = 493040 and r.measuregroup = 2147483647;
delete from endpoint_references where bioassay = 493040 and measuregroup = 2147483647;
