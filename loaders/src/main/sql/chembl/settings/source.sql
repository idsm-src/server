create index sources__chembl_id on chembl.sources(chembl_id);
create index sources__label on chembl.sources(label);
create index sources__description on chembl.sources(description);
grant select on chembl.sources to sparql;
