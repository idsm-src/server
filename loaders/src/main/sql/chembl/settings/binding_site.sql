create index binding_sites__chembl_id on chembl.binding_sites(chembl_id);
create index binding_sites__name on chembl.binding_sites(name);
create index binding_sites__target on chembl.binding_sites(target);
grant select on chembl.binding_sites to sparql;
