create index protein_class_bases__chembl_id on chembl.protein_class_bases(chembl_id);
create index protein_class_bases__label on chembl.protein_class_bases(label);
create index protein_class_bases__level on chembl.protein_class_bases(level);
create index protein_class_bases__path on chembl.protein_class_bases(path);
create index protein_class_bases__parent on chembl.protein_class_bases(parent);
grant select on chembl.protein_class_bases to sparql;

--------------------------------------------------------------------------------

create index protein_class_component_descendants__class on chembl.protein_class_component_descendants(class);
create index protein_class_component_descendants__component on chembl.protein_class_component_descendants(component);
grant select on chembl.protein_class_component_descendants to sparql;

--------------------------------------------------------------------------------

create index protein_class_target_descendants__class on chembl.protein_class_target_descendants(class);
create index protein_class_target_descendants__target on chembl.protein_class_target_descendants(target);
grant select on chembl.protein_class_target_descendants to sparql;
