alter table chembl_tmp.drug_mechanism alter column mec_id type integer;
alter table chembl_tmp.drug_mechanism rename column mec_id to id;

alter table chembl_tmp.drug_mechanism add column molecule_id integer not null default -1;
update chembl_tmp.drug_mechanism set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.drug_mechanism.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.drug_mechanism alter column molecule_id drop default;

alter table chembl_tmp.drug_mechanism add column target_id integer;
update chembl_tmp.drug_mechanism set target_id = replace(chembl_tmp.target_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.target_dictionary where chembl_tmp.drug_mechanism.tid = chembl_tmp.target_dictionary.tid;

alter table chembl_tmp.drug_mechanism add column chembl_id varchar not null generated always as ('CHEMBL_MEC_' || id::varchar) stored;

alter table chembl_tmp.drug_mechanism alter column site_id type integer;
