create index targets__chembl_id on chembl.targets(chembl_id);
create index targets__type on chembl.targets(type);
create index targets__label on chembl.targets(label);
create index targets__organism on chembl.targets(organism);
create index targets__taxonomy on chembl.targets(taxonomy);
create index targets__cell_line on chembl.targets(cell_line);
create index targets__species_group on chembl.targets(species_group);
grant select on chembl.targets to sparql;

--------------------------------------------------------------------------------

create index target_components__target on chembl.target_components(target);
create index target_components__component on chembl.target_components(component);
grant select on chembl.target_components to sparql;

--------------------------------------------------------------------------------

create index target_exact_matches__target on chembl.target_exact_matches(target);
create index target_exact_matches__component on chembl.target_exact_matches(component);
grant select on chembl.target_exact_matches to sparql;

--------------------------------------------------------------------------------

create index target_related_matches__target on chembl.target_related_matches(target);
create index target_related_matches__component on chembl.target_related_matches(component);
grant select on chembl.target_related_matches to sparql;

--------------------------------------------------------------------------------

create index target_relations__target on chembl.target_relations(target);
create index target_relations__relationship on chembl.target_relations(relationship);
create index target_relations__related on chembl.target_relations(related);
grant select on chembl.target_relations to sparql;
