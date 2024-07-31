alter table chembl_tmp.molecule_dictionary drop column molregno;
alter table chembl_tmp.molecule_dictionary drop column therapeutic_flag;
alter table chembl_tmp.molecule_dictionary drop column dosed_ingredient;
alter table chembl_tmp.molecule_dictionary drop column structure_type;
alter table chembl_tmp.molecule_dictionary drop column first_approval;
alter table chembl_tmp.molecule_dictionary drop column oral;
alter table chembl_tmp.molecule_dictionary drop column parenteral;
alter table chembl_tmp.molecule_dictionary drop column topical;
alter table chembl_tmp.molecule_dictionary drop column black_box_warning;
alter table chembl_tmp.molecule_dictionary drop column first_in_class;
alter table chembl_tmp.molecule_dictionary drop column chirality;
alter table chembl_tmp.molecule_dictionary drop column prodrug;
alter table chembl_tmp.molecule_dictionary drop column inorganic_flag;
alter table chembl_tmp.molecule_dictionary drop column usan_year;
alter table chembl_tmp.molecule_dictionary drop column availability_type;
alter table chembl_tmp.molecule_dictionary drop column usan_stem;
alter table chembl_tmp.molecule_dictionary drop column polymer_flag;
alter table chembl_tmp.molecule_dictionary drop column usan_substem;
alter table chembl_tmp.molecule_dictionary drop column usan_stem_definition;
alter table chembl_tmp.molecule_dictionary drop column indication_class;
alter table chembl_tmp.molecule_dictionary drop column withdrawn_flag;

alter table chembl_tmp.molecule_dictionary add primary key (id);
create index molecule_dictionary__pref_name on chembl_tmp.molecule_dictionary(pref_name);
create index molecule_dictionary__chembl_id on chembl_tmp.molecule_dictionary(chembl_id);
create index molecule_dictionary__max_phase on chembl_tmp.molecule_dictionary(max_phase);
create index molecule_dictionary__chebi_par_id on chembl_tmp.molecule_dictionary(chebi_par_id);
create index molecule_dictionary__molecule_type on chembl_tmp.molecule_dictionary(molecule_type);
grant select on chembl_tmp.molecule_dictionary to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_synonyms drop column molregno;
alter table chembl_tmp.molecule_synonyms drop column syn_type;
alter table chembl_tmp.molecule_synonyms drop column res_stem_id;

alter table chembl_tmp.molecule_synonyms add primary key (molsyn_id);
create index molecule_synonyms__synonyms on chembl_tmp.molecule_synonyms(synonyms);
create index molecule_synonyms__molecule_id on chembl_tmp.molecule_synonyms(molecule_id);
grant select on chembl_tmp.molecule_synonyms to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.biotherapeutics drop column molregno;

alter table chembl_tmp.biotherapeutics add primary key (molecule_id);
create index biotherapeutics__description on chembl_tmp.biotherapeutics(description);
create index biotherapeutics__helm_notation on chembl_tmp.biotherapeutics(helm_notation);
grant select on chembl_tmp.biotherapeutics to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_atc_classification drop column molregno;

alter table chembl_tmp.molecule_atc_classification add primary key (mol_atc_id);
create index molecule_atc_classification__level5 on chembl_tmp.molecule_atc_classification(level5);
create index molecule_atc_classification__molecule_id on chembl_tmp.molecule_atc_classification(molecule_id);
grant select on chembl_tmp.molecule_atc_classification to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.biotherapeutic_components drop column molregno;

alter table chembl_tmp.biotherapeutic_components add primary key (biocomp_id);
create index biotherapeutic_components__component_id on chembl_tmp.biotherapeutic_components(component_id);
create index biotherapeutic_components__molecule_id on chembl_tmp.biotherapeutic_components(molecule_id);
grant select on chembl_tmp.biotherapeutic_components to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_hrac_classification drop column molregno;

alter table chembl_tmp.molecule_hrac_classification add primary key (mol_hrac_id);
create index molecule_hrac_classification__hrac_class_id on chembl_tmp.molecule_hrac_classification(hrac_class_id);
create index molecule_hrac_classification__molecule_id on chembl_tmp.molecule_hrac_classification(molecule_id);
grant select on chembl_tmp.molecule_hrac_classification to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.hrac_classification drop column active_ingredient;
alter table chembl_tmp.hrac_classification drop column level1;
alter table chembl_tmp.hrac_classification drop column level1_description;
alter table chembl_tmp.hrac_classification drop column level2;
alter table chembl_tmp.hrac_classification drop column level2_description;
alter table chembl_tmp.hrac_classification drop column level3;

alter table chembl_tmp.hrac_classification add primary key (hrac_class_id);
create index hrac_classification__hrac_code on chembl_tmp.hrac_classification(hrac_code);
grant select on chembl_tmp.hrac_classification to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_irac_classification drop column molregno;

alter table chembl_tmp.molecule_irac_classification add primary key (mol_irac_id);
create index molecule_irac_classification__irac_class_id on chembl_tmp.molecule_irac_classification(irac_class_id);
create index molecule_irac_classification__molecule_id on chembl_tmp.molecule_irac_classification(molecule_id);
grant select on chembl_tmp.molecule_irac_classification to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.irac_classification drop column active_ingredient;
alter table chembl_tmp.irac_classification drop column level1;
alter table chembl_tmp.irac_classification drop column level1_description;
alter table chembl_tmp.irac_classification drop column level2_description;
alter table chembl_tmp.irac_classification drop column level3;
alter table chembl_tmp.irac_classification drop column level3_description;
alter table chembl_tmp.irac_classification drop column level4;
alter table chembl_tmp.irac_classification drop column irac_code;

alter table chembl_tmp.irac_classification add primary key (irac_class_id);
create index irac_classification__level2 on chembl_tmp.irac_classification(level2);
grant select on chembl_tmp.irac_classification to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_frac_classification drop column molregno;

alter table chembl_tmp.molecule_frac_classification add primary key (mol_frac_id);
create index molecule_frac_classification__frac_class_id on chembl_tmp.molecule_frac_classification(frac_class_id);
create index molecule_frac_classification__molecule_id on chembl_tmp.molecule_frac_classification(molecule_id);
grant select on chembl_tmp.molecule_frac_classification to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.frac_classification drop column active_ingredient;
alter table chembl_tmp.frac_classification drop column level1;
alter table chembl_tmp.frac_classification drop column level1_description;
alter table chembl_tmp.frac_classification drop column level2_description;
alter table chembl_tmp.frac_classification drop column level3;
alter table chembl_tmp.frac_classification drop column level3_description;
alter table chembl_tmp.frac_classification drop column level4;
alter table chembl_tmp.frac_classification drop column level4_description;
alter table chembl_tmp.frac_classification drop column level5;
alter table chembl_tmp.frac_classification drop column frac_code;

alter table chembl_tmp.frac_classification add primary key (frac_class_id);
create index frac_classification__level2 on chembl_tmp.frac_classification(level2);
grant select on chembl_tmp.frac_classification to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.compound_records drop column molregno;
alter table chembl_tmp.compound_records drop column doc_id;
alter table chembl_tmp.compound_records drop column compound_key;
alter table chembl_tmp.compound_records drop column src_id;
alter table chembl_tmp.compound_records drop column src_compound_id;
alter table chembl_tmp.compound_records drop column cidx;

alter table chembl_tmp.compound_records add primary key (record_id);
create index compound_records__compound_name on chembl_tmp.compound_records(compound_name);
create index compound_records__molecule_id on chembl_tmp.compound_records(molecule_id);
create index compound_records__document_id on chembl_tmp.compound_records(document_id);
grant select on chembl_tmp.compound_records to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.compound_properties drop column molregno;
alter table chembl_tmp.compound_properties drop column hba_lipinski;
alter table chembl_tmp.compound_properties drop column hbd_lipinski;
alter table chembl_tmp.compound_properties drop column num_lipinski_ro5_violations;
alter table chembl_tmp.compound_properties drop column np_likeness_score;

alter table chembl_tmp.compound_properties add primary key (molecule_id);
create index compound_properties__mw_freebase on chembl_tmp.compound_properties(mw_freebase);
create index compound_properties__alogp on chembl_tmp.compound_properties(alogp);
create index compound_properties__hba on chembl_tmp.compound_properties(hba);
create index compound_properties__hbd on chembl_tmp.compound_properties(hbd);
create index compound_properties__psa on chembl_tmp.compound_properties(psa);
create index compound_properties__rtb on chembl_tmp.compound_properties(rtb);
create index compound_properties__ro3_pass on chembl_tmp.compound_properties(ro3_pass);
create index compound_properties__num_ro5_violations on chembl_tmp.compound_properties(num_ro5_violations);
create index compound_properties__cx_most_apka on chembl_tmp.compound_properties(cx_most_apka);
create index compound_properties__cx_most_bpka on chembl_tmp.compound_properties(cx_most_bpka);
create index compound_properties__cx_logp on chembl_tmp.compound_properties(cx_logp);
create index compound_properties__cx_logd on chembl_tmp.compound_properties(cx_logd);
create index compound_properties__molecular_species on chembl_tmp.compound_properties(molecular_species);
create index compound_properties__full_mwt on chembl_tmp.compound_properties(full_mwt);
create index compound_properties__aromatic_rings on chembl_tmp.compound_properties(aromatic_rings);
create index compound_properties__heavy_atoms on chembl_tmp.compound_properties(heavy_atoms);
create index compound_properties__qed_weighted on chembl_tmp.compound_properties(qed_weighted);
create index compound_properties__mw_monoisotopic on chembl_tmp.compound_properties(mw_monoisotopic);
create index compound_properties__full_molformula on chembl_tmp.compound_properties(full_molformula);
grant select on chembl_tmp.compound_properties to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.compound_structures drop column molregno;
alter table chembl_tmp.compound_structures drop column molfile;

alter table chembl_tmp.compound_structures add primary key (molecule_id);
create index compound_structures__standard_inchi on chembl_tmp.compound_structures using hash (standard_inchi);
create index compound_structures__standard_inchi_key on chembl_tmp.compound_structures(standard_inchi_key);
create index compound_structures__canonical_smiles on chembl_tmp.compound_structures(canonical_smiles);
grant select on chembl_tmp.compound_structures to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.molecule_hierarchy drop column molregno;
alter table chembl_tmp.molecule_hierarchy drop column parent_molregno;
alter table chembl_tmp.molecule_hierarchy drop column active_molregno;

delete from chembl_tmp.molecule_hierarchy where molecule_id = parent_molecule_id;

alter table chembl_tmp.molecule_hierarchy add primary key (molecule_id);
create index molecule_hierarchy__parent_molecule_id on chembl_tmp.molecule_hierarchy(parent_molecule_id);
grant select on chembl_tmp.molecule_hierarchy to sparql;

--------------------------------------------------------------------------------

create view chembl_tmp.molecule_names as
    select id as molecule_id, pref_name as name from chembl_tmp.molecule_dictionary where pref_name is not null
  union
    select molecule_id, synonyms as name from chembl_tmp.molecule_synonyms where synonyms is not null
  union
    select molecule_id, compound_name as name from chembl_tmp.compound_records where compound_name is not null and compound_name <> 'NA';

grant select on chembl_tmp.molecule_names to sparql;

--------------------------------------------------------------------------------

create view chembl_tmp.molecule_docs as
  select distinct molecule_id, document_id from chembl_tmp.compound_records;

grant select on chembl_tmp.molecule_docs to sparql;
