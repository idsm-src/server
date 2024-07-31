alter table chembl_tmp.source rename column src_id to id;

alter table chembl_tmp.source add column chembl_id varchar not null generated always as ('CHEMBL_SRC_' || id::varchar) stored;

alter table chembl_tmp.source alter column src_description set not null;
alter table chembl_tmp.source alter column src_short_name set not null;
