alter table chembl_tmp.drug_mechanism drop column record_id;
alter table chembl_tmp.drug_mechanism drop column molregno;
alter table chembl_tmp.drug_mechanism drop column tid;
alter table chembl_tmp.drug_mechanism drop column direct_interaction;
alter table chembl_tmp.drug_mechanism drop column molecular_mechanism;
alter table chembl_tmp.drug_mechanism drop column disease_efficacy;
alter table chembl_tmp.drug_mechanism drop column mechanism_comment;
alter table chembl_tmp.drug_mechanism drop column selectivity_comment;
alter table chembl_tmp.drug_mechanism drop column binding_site_comment;
alter table chembl_tmp.drug_mechanism drop column variant_id;

alter table chembl_tmp.drug_mechanism add primary key (id);
create index drug_mechanism__mechanism_of_action on chembl_tmp.drug_mechanism(mechanism_of_action);
create index drug_mechanism__site_id on chembl_tmp.drug_mechanism(site_id);
create index drug_mechanism__action_type on chembl_tmp.drug_mechanism(action_type);
create index drug_mechanism__molecule_id on chembl_tmp.drug_mechanism(molecule_id);
create index drug_mechanism__target_id on chembl_tmp.drug_mechanism(target_id);
create index drug_mechanism__chembl_id on chembl_tmp.drug_mechanism(chembl_id);
grant select on chembl_tmp.drug_mechanism to sparql;
