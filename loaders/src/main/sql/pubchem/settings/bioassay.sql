create index bioassay_bases__source on pubchem.bioassay_bases(source);
create index bioassay_bases__title on pubchem.bioassay_bases(title);
create index bioassay_bases__title__english on pubchem.bioassay_bases using gin (to_tsvector('english', title));
grant select on pubchem.bioassay_bases to sparql;

--------------------------------------------------------------------------------

create index bioassay_data__bioassay on pubchem.bioassay_data(bioassay);
create index bioassay_data__type on pubchem.bioassay_data(type_id);
create index bioassay_data__value on pubchem.bioassay_data using hash (value);
create index bioassay_data__value__english on pubchem.bioassay_data using gin (to_tsvector('english', value));
grant select on pubchem.bioassay_data to sparql;

--------------------------------------------------------------------------------

create index bioassay_stages__stage on pubchem.bioassay_stages(stage);
grant select on pubchem.bioassay_stages to sparql;

--------------------------------------------------------------------------------

create index bioassay_confirmatory_assays__bioassay on pubchem.bioassay_confirmatory_assays(bioassay);
create index bioassay_confirmatory_assays__confirmatory_assay on pubchem.bioassay_confirmatory_assays(confirmatory_assay);
grant select on pubchem.bioassay_confirmatory_assays to sparql;

--------------------------------------------------------------------------------

create index bioassay_primary_assays__bioassay on pubchem.bioassay_primary_assays(bioassay);
create index bioassay_primary_assays__primary_assay on pubchem.bioassay_primary_assays(primary_assay);
grant select on pubchem.bioassay_primary_assays to sparql;

--------------------------------------------------------------------------------

create index bioassay_summary_assays__bioassay on pubchem.bioassay_summary_assays(bioassay);
create index bioassay_summary_assays__summary_assay on pubchem.bioassay_summary_assays(summary_assay);
grant select on pubchem.bioassay_summary_assays to sparql;

--------------------------------------------------------------------------------

create index bioassay_chembl_assays__chembl_assay on pubchem.bioassay_chembl_assays(chembl_assay);
grant select on pubchem.bioassay_chembl_assays to sparql;

--------------------------------------------------------------------------------

create index bioassay_chembl_mechanisms__chembl_mechanism on pubchem.bioassay_chembl_mechanisms(chembl_mechanism);
grant select on pubchem.bioassay_chembl_mechanisms to sparql;

--------------------------------------------------------------------------------

create index bioassay_patent_references__bioassay on pubchem.bioassay_patent_references(bioassay);
create index bioassay_patent_references__patent on pubchem.bioassay_patent_references(patent);
grant select on pubchem.bioassay_patent_references to sparql;
