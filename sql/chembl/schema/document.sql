alter table chembl_tmp.docs add column id integer not null default -1;
update chembl_tmp.docs set id = replace(chembl_id, 'CHEMBL', '')::integer;
alter table chembl_tmp.docs alter column id drop default;

alter table chembl_tmp.docs add column journal_id integer;

alter table chembl_tmp.docs alter column pubmed_id type integer;
