alter table chembl.molecule_dictionary add column id integer not null default -1;
update chembl.molecule_dictionary set id = replace(chembl_id, 'CHEMBL', '')::integer;
alter table chembl.molecule_dictionary alter column id drop default;

alter table chembl.molecule_dictionary alter column chebi_par_id type integer;
alter table chembl.molecule_dictionary alter column max_phase type integer;

update chembl.molecule_dictionary set molecule_type = 'Unknown' where molecule_type is null;
alter table chembl.molecule_dictionary alter column molecule_type set not null;

--------------------------------------------------------------------------------

alter table chembl.molecule_synonyms add column molecule_id integer not null default -1;
update chembl.molecule_synonyms set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.molecule_synonyms.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.molecule_synonyms alter column molecule_id drop default;

alter table chembl.molecule_synonyms alter column molsyn_id type integer;

alter table chembl.molecule_synonyms alter column synonyms set not null;

--------------------------------------------------------------------------------

alter table chembl.biotherapeutics add column molecule_id integer not null default -1;
update chembl.biotherapeutics set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.biotherapeutics.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.biotherapeutics alter column molecule_id drop default;

--------------------------------------------------------------------------------

alter table chembl.molecule_atc_classification add column molecule_id integer not null default -1;
update chembl.molecule_atc_classification set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.molecule_atc_classification.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.molecule_atc_classification alter column molecule_id drop default;

alter table chembl.molecule_atc_classification alter column mol_atc_id type integer;

--------------------------------------------------------------------------------

alter table chembl.biotherapeutic_components add column molecule_id integer not null default -1;
update chembl.biotherapeutic_components set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.biotherapeutic_components.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.biotherapeutic_components alter column molecule_id drop default;

alter table chembl.biotherapeutic_components alter column biocomp_id type integer;
alter table chembl.biotherapeutic_components alter column component_id type integer;

--------------------------------------------------------------------------------

alter table chembl.molecule_hrac_classification add column molecule_id integer not null default -1;
update chembl.molecule_hrac_classification set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.molecule_hrac_classification.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.molecule_hrac_classification alter column molecule_id drop default;

alter table chembl.molecule_hrac_classification alter column mol_hrac_id type integer;
alter table chembl.molecule_hrac_classification alter column hrac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl.hrac_classification alter column hrac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl.molecule_irac_classification add column molecule_id integer not null default -1;
update chembl.molecule_irac_classification set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.molecule_irac_classification.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.molecule_irac_classification alter column molecule_id drop default;

alter table chembl.molecule_irac_classification alter column mol_irac_id type integer;
alter table chembl.molecule_irac_classification alter column irac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl.irac_classification alter column irac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl.molecule_frac_classification add column molecule_id integer not null default -1;
update chembl.molecule_frac_classification set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.molecule_frac_classification.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.molecule_frac_classification alter column molecule_id drop default;

alter table chembl.molecule_frac_classification alter column mol_frac_id type integer;
alter table chembl.molecule_frac_classification alter column frac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl.frac_classification alter column frac_class_id type integer;

--------------------------------------------------------------------------------

alter table chembl.compound_records add column molecule_id integer not null default -1;
update chembl.compound_records set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.compound_records.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.compound_records alter column molecule_id drop default;

alter table chembl.compound_records add column document_id integer not null default -1;
update chembl.compound_records set document_id = replace(chembl.docs.chembl_id, 'CHEMBL', '')::integer from chembl.docs where chembl.compound_records.doc_id = chembl.docs.doc_id;
alter table chembl.compound_records alter column document_id drop default;

alter table chembl.compound_records alter column record_id type integer;

--------------------------------------------------------------------------------

alter table chembl.compound_properties add column molecule_id integer not null default -1;
update chembl.compound_properties set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.compound_properties.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.compound_properties alter column molecule_id drop default;

alter table chembl.compound_properties alter column cx_most_apka type float8;
alter table chembl.compound_properties alter column cx_most_bpka type float8;
alter table chembl.compound_properties alter column cx_logd type float8;
alter table chembl.compound_properties alter column cx_logp type float8;
alter table chembl.compound_properties alter column alogp type float8;
alter table chembl.compound_properties alter column aromatic_rings type float8;
alter table chembl.compound_properties alter column hba type float8;
alter table chembl.compound_properties alter column hbd type float8;
alter table chembl.compound_properties alter column heavy_atoms type float8;
alter table chembl.compound_properties alter column num_ro5_violations type float8;
alter table chembl.compound_properties alter column psa type float8;
alter table chembl.compound_properties alter column qed_weighted type float8;
alter table chembl.compound_properties alter column rtb type float8;
alter table chembl.compound_properties alter column mw_freebase type float8;
alter table chembl.compound_properties alter column mw_monoisotopic type float8;
alter table chembl.compound_properties alter column full_mwt type float8;

alter table chembl.compound_properties alter column full_mwt set not null;
alter table chembl.compound_properties alter column full_molformula set not null;

--------------------------------------------------------------------------------

alter table chembl.compound_structures add column molecule_id integer not null default -1;
update chembl.compound_structures set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.compound_structures.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.compound_structures alter column molecule_id drop default;

--------------------------------------------------------------------------------

alter table chembl.molecule_hierarchy add column molecule_id integer not null default -1;
update chembl.molecule_hierarchy set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.molecule_hierarchy.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.molecule_hierarchy alter column molecule_id drop default;

alter table chembl.molecule_hierarchy add column parent_molecule_id integer not null default -1;
update chembl.molecule_hierarchy set parent_molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.molecule_hierarchy.parent_molregno = chembl.molecule_dictionary.molregno;
alter table chembl.molecule_hierarchy alter column parent_molecule_id drop default;
