alter table chembl.binding_sites drop column tid;

alter table chembl.binding_sites add primary key (id);
create index binding_sites__site_name on chembl.binding_sites(site_name);
create index binding_sites__target_id on chembl.binding_sites(target_id);
create index binding_sites__chembl_id on chembl.binding_sites(chembl_id);
grant select on chembl.binding_sites to sparql;
