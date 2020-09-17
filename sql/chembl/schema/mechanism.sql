alter table chembl.drug_mechanism alter column mec_id type integer;
alter table chembl.drug_mechanism rename column mec_id to id;

alter table chembl.drug_mechanism add column molecule_id integer not null default -1;
update chembl.drug_mechanism set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.drug_mechanism.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.drug_mechanism alter column molecule_id drop default;

alter table chembl.drug_mechanism add column target_id integer;
update chembl.drug_mechanism set target_id = replace(chembl.target_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.target_dictionary where chembl.drug_mechanism.tid = chembl.target_dictionary.tid;

alter table chembl.drug_mechanism add column chembl_id varchar not null generated always as ('CHEMBL_MEC_' || id::varchar) stored;

alter table chembl.drug_mechanism alter column site_id type integer;
