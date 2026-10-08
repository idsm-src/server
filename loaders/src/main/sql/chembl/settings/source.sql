create index source_bases__chembl_id on chembl.source_bases(chembl_id);
create index source_bases__label on chembl.source_bases(label);
create index source_bases__description on chembl.source_bases(description);
grant select on chembl.source_bases to sparql;
