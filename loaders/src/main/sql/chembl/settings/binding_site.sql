create index binding_site_bases__chembl_id on chembl.binding_site_bases(chembl_id);
create index binding_site_bases__name on chembl.binding_site_bases(name);
create index binding_site_bases__target on chembl.binding_site_bases(target);
grant select on chembl.binding_site_bases to sparql;
