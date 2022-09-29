create index endpoint_bases__substance on pubchem.endpoint_bases(substance);
create index endpoint_bases__bioassay on pubchem.endpoint_bases(bioassay);
create index endpoint_bases__bioassay_measuregroup on pubchem.endpoint_bases(bioassay, measuregroup);
grant select on pubchem.endpoint_bases to sparql;

--------------------------------------------------------------------------------

create index endpoint_outcomes__substance on pubchem.endpoint_outcomes(substance);
create index endpoint_outcomes__bioassay on pubchem.endpoint_outcomes(bioassay);
create index endpoint_outcomes__bioassay_measuregroup on pubchem.endpoint_outcomes(bioassay, measuregroup);
create index endpoint_outcomes__substance_bioassay_measuregroup on pubchem.endpoint_outcomes(substance, bioassay, measuregroup);
create index endpoint_outcomes__outcome on pubchem.endpoint_outcomes(outcome_id);
grant select on pubchem.endpoint_outcomes to sparql;

--------------------------------------------------------------------------------

create index endpoint_measurements__substance on pubchem.endpoint_measurements(substance);
create index endpoint_measurements__bioassay on pubchem.endpoint_measurements(bioassay);
create index endpoint_measurements__bioassay_measuregroup on pubchem.endpoint_measurements(bioassay, measuregroup);
grant select on pubchem.endpoint_measurements to sparql;

--------------------------------------------------------------------------------

create index endpoint_measurement_types__substance on pubchem.endpoint_measurement_types(substance);
create index endpoint_measurement_types__bioassay on pubchem.endpoint_measurement_types(bioassay);
create index endpoint_measurement_types__bioassay_measuregroup on pubchem.endpoint_measurement_types(bioassay, measuregroup);
create index endpoint_measurement_types__substance_bioassay_measuregroup on pubchem.endpoint_outcomes(substance, bioassay, measuregroup);
create index endpoint_measurement_types__type on pubchem.endpoint_measurement_types(type_id);
grant select on pubchem.endpoint_measurement_types to sparql;

--------------------------------------------------------------------------------

create index endpoint_measurement_labels__substance on pubchem.endpoint_measurement_labels(substance);
create index endpoint_measurement_labels__bioassay on pubchem.endpoint_measurement_labels(bioassay);
create index endpoint_measurement_labels__bioassay_measuregroup on pubchem.endpoint_measurement_labels(bioassay, measuregroup);
create index endpoint_measurement_labels__substance_bioassay_measuregroup on pubchem.endpoint_outcomes(substance, bioassay, measuregroup);
create index endpoint_measurement_labels__label on pubchem.endpoint_measurement_labels(label);
grant select on pubchem.endpoint_measurement_labels to sparql;

--------------------------------------------------------------------------------

create index endpoint_measurement_values__substance on pubchem.endpoint_measurement_values(substance);
create index endpoint_measurement_values__bioassay on pubchem.endpoint_measurement_values(bioassay);
create index endpoint_measurement_values__bioassay_measuregroup on pubchem.endpoint_measurement_values(bioassay, measuregroup);
create index endpoint_measurement_values__substance_bioassay_measuregroup on pubchem.endpoint_measurement_values(substance, bioassay, measuregroup);
create index endpoint_measurement_values__value on pubchem.endpoint_measurement_values(value);
grant select on pubchem.endpoint_measurement_values to sparql;

--------------------------------------------------------------------------------

create index endpoint_references__substance on pubchem.endpoint_references(substance);
create index endpoint_references__bioassay on pubchem.endpoint_references(bioassay);
create index endpoint_references__bioassay_measuregroup on pubchem.endpoint_references(bioassay, measuregroup);
create index endpoint_references__substance_bioassay_measuregroup on pubchem.endpoint_references(substance, bioassay, measuregroup);
create index endpoint_references__reference on pubchem.endpoint_references(reference);
grant select on pubchem.endpoint_references TO sparql;
