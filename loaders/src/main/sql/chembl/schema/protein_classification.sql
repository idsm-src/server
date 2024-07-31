alter table chembl_tmp.protein_classification alter column protein_class_id type integer;
alter table chembl_tmp.protein_classification rename column protein_class_id to id;

alter table chembl_tmp.protein_classification add column class_level_name varchar not null default '';
update chembl_tmp.protein_classification set class_level_name = 'L' || class_level::varchar;
alter table chembl_tmp.protein_classification alter column class_level_name drop default;

alter table chembl_tmp.protein_classification add column chembl_id varchar not null generated always as ('CHEMBL_PC_' || id::varchar) stored;

alter table chembl_tmp.protein_classification alter column parent_id type integer;

alter table chembl_tmp.protein_classification alter column pref_name set not null;
alter table chembl_tmp.protein_classification alter column short_name set not null;
