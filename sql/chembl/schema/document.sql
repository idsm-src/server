alter table chembl.docs add column id integer not null default -1;
update chembl.docs set id = replace(chembl_id, 'CHEMBL', '')::integer;
alter table chembl.docs alter column id drop default;

alter table chembl.docs add column journal_id integer;

alter table chembl.docs alter column pubmed_id type integer;
