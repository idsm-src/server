create index mechanism_bases__chembl_id on chembl.mechanism_bases(chembl_id);
create index mechanism_bases__molecule on chembl.mechanism_bases(molecule);
create index mechanism_bases__target on chembl.mechanism_bases(target);
create index mechanism_bases__binding_site on chembl.mechanism_bases(binding_site);
create index mechanism_bases__description on chembl.mechanism_bases(description);
create index mechanism_bases__action_type on chembl.mechanism_bases(action_type);
grant select on chembl.mechanism_bases to sparql;
