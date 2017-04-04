grant select on endpoint_outcomes__reftable to "SPARQL";

--------------------------------------------------------------------------------

create index endpoint_bases__substance on endpoint_bases(substance);
create index endpoint_bases__bioassay on endpoint_bases(bioassay);
create index endpoint_bases__bioassay_measuregroup on endpoint_bases(bioassay, measuregroup);
create index endpoint_bases__outcome on endpoint_bases(outcome);
grant select on endpoint_bases to "SPARQL";

--------------------------------------------------------------------------------

create index endpoint_measurements__substance on endpoint_measurements(substance);
create index endpoint_measurements__bioassay on endpoint_measurements(bioassay);
create index endpoint_measurements__bioassay_measuregroup on endpoint_measurements(bioassay, measuregroup);
create index endpoint_measurements__type on endpoint_measurements(type);
grant select on endpoint_measurements to "SPARQL";

--------------------------------------------------------------------------------

create index endpoint_references__substance on endpoint_references(substance);
create index endpoint_references__bioassay on endpoint_references(bioassay);
create index endpoint_references__bioassay_measuregroup on endpoint_references(bioassay, measuregroup);
create index endpoint_references__substance_bioassay_measuregroup on endpoint_references(substance, bioassay, measuregroup);
create index endpoint_references__reference on endpoint_references(reference);
grant select on endpoint_references TO "SPARQL";
