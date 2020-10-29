create index bioassay_bases__source on pubchem.bioassay_bases(source);
create index bioassay_bases__title__gin on pubchem.bioassay_bases using gin (to_tsvector('english', title));
grant select on pubchem.bioassay_bases to sparql;

--------------------------------------------------------------------------------

create index bioassay_data__bioassay_type on pubchem.bioassay_data(bioassay, type_id);
create index bioassay_data__bioassay on pubchem.bioassay_data(bioassay);
create index bioassay_data__type on pubchem.bioassay_data(type_id);
create index bioassay_data__value__gin on pubchem.bioassay_data using gin (to_tsvector('english', value));
grant select on pubchem.bioassay_data to sparql;

--------------------------------------------------------------------------------

create index bioassay_chembl_assays__chembl_assay on pubchem.bioassay_chembl_assays(chembl_assay);
grant select on pubchem.bioassay_chembl_assays to sparql;

--------------------------------------------------------------------------------

create index bioassay_chembl_mechanisms__chembl_mechanism on pubchem.bioassay_chembl_mechanisms(chembl_mechanism);
grant select on pubchem.bioassay_chembl_mechanisms to sparql;
