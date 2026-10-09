create index mechanisms__chembl_id on chembl.mechanisms(chembl_id);
create index mechanisms__molecule on chembl.mechanisms(molecule);
create index mechanisms__target on chembl.mechanisms(target);
create index mechanisms__binding_site on chembl.mechanisms(binding_site);
create index mechanisms__description on chembl.mechanisms(description);
create index mechanisms__action_type on chembl.mechanisms(action_type);
grant select on chembl.mechanisms to sparql;
