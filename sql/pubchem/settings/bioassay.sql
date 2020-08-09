create index bioassay_bases__source on pubchem.bioassay_bases(source);
create index bioassay_bases__title on pubchem.bioassay_bases using gin (to_tsvector('english', title));
grant select on pubchem.bioassay_bases to sparql;

--------------------------------------------------------------------------------

create index bioassay_data__bioassay_type on pubchem.bioassay_data(bioassay, type_id);
create index bioassay_data__bioassay on pubchem.bioassay_data(bioassay);
create index bioassay_data__type on pubchem.bioassay_data(type_id);
create index bioassay_data__value on pubchem.bioassay_data using gin (to_tsvector('english', value));
grant select on pubchem.bioassay_data to sparql;
