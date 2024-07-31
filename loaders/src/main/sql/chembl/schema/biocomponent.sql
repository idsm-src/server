alter table chembl_tmp.bio_component_sequences alter column component_id type integer;
alter table chembl_tmp.bio_component_sequences rename column component_id to id;

alter table chembl_tmp.bio_component_sequences add column chembl_id varchar not null generated always as ('CHEMBL_BC_' || id::varchar) stored;

alter table chembl_tmp.bio_component_sequences alter column tax_id type integer;
alter table chembl_tmp.bio_component_sequences alter column sequence type varchar;

alter table chembl_tmp.bio_component_sequences alter column sequence set not null;
