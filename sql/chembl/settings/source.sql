alter table chembl.source add primary key (id);
create index source__src_description on chembl.source(src_description);
create index source__src_short_name on chembl.source(src_short_name);
create index source__chembl_id on chembl.source(chembl_id);
grant select on chembl.source to sparql;
