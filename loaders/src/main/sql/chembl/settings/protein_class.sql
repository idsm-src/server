create index protein_classes__chembl_id on chembl.protein_classes(chembl_id);
create index protein_classes__label on chembl.protein_classes(label);
create index protein_classes__level on chembl.protein_classes(level);
create index protein_classes__path on chembl.protein_classes(path);
create index protein_classes__parent on chembl.protein_classes(parent);
grant select on chembl.protein_classes to sparql;

--------------------------------------------------------------------------------

create index protein_class_component_descendants__protein_class on chembl.protein_class_component_descendants(protein_class);
create index protein_class_component_descendants__component on chembl.protein_class_component_descendants(component);
grant select on chembl.protein_class_component_descendants to sparql;

--------------------------------------------------------------------------------

create index protein_class_target_descendants__protein_class on chembl.protein_class_target_descendants(protein_class);
create index protein_class_target_descendants__target on chembl.protein_class_target_descendants(target);
grant select on chembl.protein_class_target_descendants to sparql;
