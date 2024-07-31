alter table chembl_tmp.molecule_dictionary add column id integer not null default -1;
update chembl_tmp.molecule_dictionary set id = replace(chembl_id, 'CHEMBL', '')::integer;
alter table chembl_tmp.molecule_dictionary alter column id drop default;

alter table chembl_tmp.molecule_dictionary alter column chebi_par_id type integer;
alter table chembl_tmp.molecule_dictionary alter column max_phase type float4;

update chembl_tmp.molecule_dictionary set molecule_type = 'Unknown' where molecule_type is null;
alter table chembl_tmp.molecule_dictionary alter column molecule_type set not null;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_synonyms add column molecule_id integer not null default -1;
update chembl_tmp.molecule_synonyms set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.molecule_synonyms.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.molecule_synonyms alter column molecule_id drop default;

alter table chembl_tmp.molecule_synonyms alter column molsyn_id type integer;

alter table chembl_tmp.molecule_synonyms alter column synonyms set not null;

--------------------------------------------------------------------------------

alter table chembl_tmp.biotherapeutics add column molecule_id integer not null default -1;
update chembl_tmp.biotherapeutics set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.biotherapeutics.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.biotherapeutics alter column molecule_id drop default;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_atc_classification add column molecule_id integer not null default -1;
update chembl_tmp.molecule_atc_classification set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.molecule_atc_classification.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.molecule_atc_classification alter column molecule_id drop default;

alter table chembl_tmp.molecule_atc_classification alter column mol_atc_id type integer;

--------------------------------------------------------------------------------

alter table chembl_tmp.biotherapeutic_components add column molecule_id integer not null default -1;
update chembl_tmp.biotherapeutic_components set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.biotherapeutic_components.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.biotherapeutic_components alter column molecule_id drop default;

alter table chembl_tmp.biotherapeutic_components alter column biocomp_id type integer;
alter table chembl_tmp.biotherapeutic_components alter column component_id type integer;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_hrac_classification add column molecule_id integer not null default -1;
update chembl_tmp.molecule_hrac_classification set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.molecule_hrac_classification.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.molecule_hrac_classification alter column molecule_id drop default;

alter table chembl_tmp.molecule_hrac_classification alter column mol_hrac_id type integer;
alter table chembl_tmp.molecule_hrac_classification alter column hrac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl_tmp.hrac_classification alter column hrac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_irac_classification add column molecule_id integer not null default -1;
update chembl_tmp.molecule_irac_classification set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.molecule_irac_classification.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.molecule_irac_classification alter column molecule_id drop default;

alter table chembl_tmp.molecule_irac_classification alter column mol_irac_id type integer;
alter table chembl_tmp.molecule_irac_classification alter column irac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl_tmp.irac_classification alter column irac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_frac_classification add column molecule_id integer not null default -1;
update chembl_tmp.molecule_frac_classification set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.molecule_frac_classification.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.molecule_frac_classification alter column molecule_id drop default;

alter table chembl_tmp.molecule_frac_classification alter column mol_frac_id type integer;
alter table chembl_tmp.molecule_frac_classification alter column frac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl_tmp.frac_classification alter column frac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl_tmp.compound_records add column molecule_id integer not null default -1;
update chembl_tmp.compound_records set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.compound_records.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.compound_records alter column molecule_id drop default;

alter table chembl_tmp.compound_records add column document_id integer not null default -1;
update chembl_tmp.compound_records set document_id = replace(chembl_tmp.docs.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.docs where chembl_tmp.compound_records.doc_id = chembl_tmp.docs.doc_id;
alter table chembl_tmp.compound_records alter column document_id drop default;

alter table chembl_tmp.compound_records alter column record_id type integer;

--------------------------------------------------------------------------------

alter table chembl_tmp.compound_properties add column molecule_id integer not null default -1;
update chembl_tmp.compound_properties set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.compound_properties.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.compound_properties alter column molecule_id drop default;

alter table chembl_tmp.compound_properties alter column cx_most_apka type float8;
alter table chembl_tmp.compound_properties alter column cx_most_bpka type float8;
alter table chembl_tmp.compound_properties alter column cx_logd type float8;
alter table chembl_tmp.compound_properties alter column cx_logp type float8;
alter table chembl_tmp.compound_properties alter column alogp type float8;
alter table chembl_tmp.compound_properties alter column aromatic_rings type float8;
alter table chembl_tmp.compound_properties alter column hba type float8;
alter table chembl_tmp.compound_properties alter column hbd type float8;
alter table chembl_tmp.compound_properties alter column heavy_atoms type float8;
alter table chembl_tmp.compound_properties alter column num_ro5_violations type float8;
alter table chembl_tmp.compound_properties alter column psa type float8;
alter table chembl_tmp.compound_properties alter column qed_weighted type float8;
alter table chembl_tmp.compound_properties alter column rtb type float8;
alter table chembl_tmp.compound_properties alter column mw_freebase type float8;
alter table chembl_tmp.compound_properties alter column mw_monoisotopic type float8;
alter table chembl_tmp.compound_properties alter column full_mwt type float8;

alter table chembl_tmp.compound_properties alter column full_mwt set not null;
alter table chembl_tmp.compound_properties alter column full_molformula set not null;

--------------------------------------------------------------------------------

alter table chembl_tmp.compound_structures add column molecule_id integer not null default -1;
update chembl_tmp.compound_structures set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.compound_structures.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.compound_structures alter column molecule_id drop default;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_hierarchy add column molecule_id integer not null default -1;
update chembl_tmp.molecule_hierarchy set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.molecule_hierarchy.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.molecule_hierarchy alter column molecule_id drop default;

alter table chembl_tmp.molecule_hierarchy add column parent_molecule_id integer not null default -1;
update chembl_tmp.molecule_hierarchy set parent_molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.molecule_hierarchy.parent_molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.molecule_hierarchy alter column parent_molecule_id drop default;
