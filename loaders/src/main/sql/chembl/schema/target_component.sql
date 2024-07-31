alter table chembl_tmp.component_sequences alter column component_id type integer;
alter table chembl_tmp.component_sequences rename column component_id to id;

alter table chembl_tmp.component_sequences add column chembl_id varchar not null generated always as ('CHEMBL_TC_' || id::varchar) stored;

alter table chembl_tmp.component_sequences alter column tax_id type integer;
alter table chembl_tmp.component_sequences alter column sequence type varchar;

alter table chembl_tmp.component_sequences alter column component_type set not null;
alter table chembl_tmp.component_sequences alter column description set not null;
alter table chembl_tmp.component_sequences alter column tax_id set not null;
alter table chembl_tmp.component_sequences alter column organism set not null;

--------------------------------------------------------------------------------

alter table chembl_tmp.component_synonyms alter column compsyn_id type integer;
alter table chembl_tmp.component_synonyms alter column component_id type integer;

alter table chembl_tmp.component_synonyms alter column component_synonym set not null;

--------------------------------------------------------------------------------

alter table chembl_tmp.component_class alter column component_id type integer;
alter table chembl_tmp.component_class alter column protein_class_id type integer;
alter table chembl_tmp.component_class alter column comp_class_id type integer;
