alter table chembl.target_dictionary drop column tid;

alter table chembl.target_dictionary add primary key (id);
create index target_dictionary__target_type on chembl.target_dictionary(target_type);
create index target_dictionary__pref_name on chembl.target_dictionary(pref_name);
create index target_dictionary__tax_id on chembl.target_dictionary(tax_id);
create index target_dictionary__organism on chembl.target_dictionary(organism);
create index target_dictionary__chembl_id on chembl.target_dictionary(chembl_id);
create index target_dictionary__species_group_flag on chembl.target_dictionary(species_group_flag);
create index target_dictionary__cell_line_id on chembl.target_dictionary(cell_line_id);
grant select on chembl.target_dictionary to sparql;

--------------------------------------------------------------------------------

alter table chembl.target_relations drop column tid;
alter table chembl.target_relations drop column related_tid;

alter table chembl.target_relations add primary key (targrel_id);
create index target_relations__relationship on chembl.target_relations(relationship);
create index target_relations__target_id on chembl.target_relations(target_id);
create index target_relations__related_target_id on chembl.target_relations(related_target_id);
grant select on chembl.target_relations to sparql;

--------------------------------------------------------------------------------

alter table chembl.target_components drop column tid;
alter table chembl.target_components drop column targcomp_id;
alter table chembl.target_components drop column homologue;

alter table chembl.target_components add primary key (targcomp_id);
create index target_components__component_id on chembl.target_components(component_id);
create index target_components__target_id on chembl.target_components(target_id);
create index target_components__is_exact on chembl.target_components(is_exact);
create index target_components__is_related on chembl.target_components(is_related);
grant select on chembl.target_components to sparql;
