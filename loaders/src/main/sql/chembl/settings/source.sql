alter table chembl_tmp.source add primary key (id);
create index source__src_description on chembl_tmp.source(src_description);
create index source__src_short_name on chembl_tmp.source(src_short_name);
create index source__chembl_id on chembl_tmp.source(chembl_id);
grant select on chembl_tmp.source to sparql;
