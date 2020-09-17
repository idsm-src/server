alter table chembl.binding_sites alter column site_id type integer;
alter table chembl.binding_sites rename column site_id to id;

alter table chembl.binding_sites add column target_id integer not null default -1;
update chembl.binding_sites set target_id = replace(chembl.target_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.target_dictionary where chembl.binding_sites.tid = chembl.target_dictionary.tid;
alter table chembl.binding_sites alter column target_id drop default;

alter table chembl.binding_sites add column chembl_id varchar not null generated always as ('CHEMBL_BS_' || id::varchar) stored;

alter table chembl.binding_sites alter column site_name set not null;
