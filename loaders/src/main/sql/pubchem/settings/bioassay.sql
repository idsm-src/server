create index bioassays__id__varchar on pubchem.bioassays((id::varchar));
create index bioassays__source on pubchem.bioassays(source);
create index bioassays__title on pubchem.bioassays(title);
create index bioassays__title__english on pubchem.bioassays using gin (to_tsvector('english', title));
grant select on pubchem.bioassays to sparql;

--------------------------------------------------------------------------------

create index bioassay_texts__bioassay on pubchem.bioassay_texts(bioassay);
create index bioassay_texts__type_id on pubchem.bioassay_texts(type_id);
create index bioassay_texts__text on pubchem.bioassay_texts using hash (text);
create index bioassay_texts__text__english on pubchem.bioassay_texts using gin (to_tsvector('english', text));
grant select on pubchem.bioassay_texts to sparql;

--------------------------------------------------------------------------------

create index bioassay_stages__stage_id on pubchem.bioassay_stages(stage_id);
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
