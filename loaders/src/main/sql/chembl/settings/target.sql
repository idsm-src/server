create index target_bases__chembl_id on chembl.target_bases(chembl_id);
create index target_bases__type on chembl.target_bases(type);
create index target_bases__label on chembl.target_bases(label);
create index target_bases__organism on chembl.target_bases(organism);
create index target_bases__taxonomy on chembl.target_bases(taxonomy);
create index target_bases__cell_line on chembl.target_bases(cell_line);
create index target_bases__species_group on chembl.target_bases(species_group);
grant select on chembl.target_bases to sparql;

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
