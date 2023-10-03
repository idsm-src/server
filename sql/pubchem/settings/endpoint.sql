create index endpoint_bases__substance on pubchem.endpoint_bases(substance);
create index endpoint_bases__bioassay on pubchem.endpoint_bases(bioassay);
create index endpoint_bases__bioassay_measuregroup on pubchem.endpoint_bases(bioassay, measuregroup);
create index endpoint_bases__outcome on pubchem.endpoint_bases(outcome_id);
grant select on pubchem.endpoint_bases to sparql;

--------------------------------------------------------------------------------

create index endpoint_measurements__substance on pubchem.endpoint_measurements(substance);
create index endpoint_measurements__bioassay on pubchem.endpoint_measurements(bioassay);
create index endpoint_measurements__bioassay_measuregroup on pubchem.endpoint_measurements(bioassay, measuregroup);
create index endpoint_measurements__endpoint_type_id on pubchem.endpoint_measurements(endpoint_type_id);
create index endpoint_measurements__measurement_type_id on pubchem.endpoint_measurements(measurement_type_id);
create index endpoint_measurements__measurement on pubchem.endpoint_measurements(measurement);
create index endpoint_measurements__label on pubchem.endpoint_measurements(label);
grant select on pubchem.endpoint_measurements to sparql;

--------------------------------------------------------------------------------

create index endpoint_references__substance on pubchem.endpoint_references(substance);
create index endpoint_references__bioassay on pubchem.endpoint_references(bioassay);
create index endpoint_references__bioassay_measuregroup on pubchem.endpoint_references(bioassay, measuregroup);
create index endpoint_references__substance_bioassay_measuregroup_value on pubchem.endpoint_references(substance, bioassay, measuregroup, value);
create index endpoint_references__reference on pubchem.endpoint_references(reference);
grant select on pubchem.endpoint_references TO sparql;
