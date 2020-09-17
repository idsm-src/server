alter table chembl.protein_classification drop column protein_class_desc;
alter table chembl.protein_classification drop column definition;
alter table chembl.protein_classification drop column class_level;

alter table chembl.protein_classification add primary key (id);
create index protein_classification__parent_id on chembl.protein_classification(parent_id);
create index protein_classification__pref_name on chembl.protein_classification(pref_name);
create index protein_classification__chembl_id on chembl.protein_classification(chembl_id);
create index protein_classification__class_level_name on chembl.protein_classification(class_level_name);
grant select on chembl.protein_classification to sparql;

--------------------------------------------------------------------------------

create recursive view chembl.component_classes(component_id, protein_class_id) as
    select component_id, protein_class_id from chembl.component_class
  union
    select component_classes.component_id, chembl.protein_classification.parent_id from component_classes, chembl.protein_classification
        where component_classes.protein_class_id = chembl.protein_classification.id and chembl.protein_classification.parent_id is not null;

grant select on chembl.component_classes to sparql;

--------------------------------------------------------------------------------

create recursive view chembl.target_classes(target_id, protein_class_id) as
    select chembl.target_components.target_id, protein_class_id from chembl.target_components, chembl.component_class where chembl.target_components.component_id = chembl.component_class.component_id
  union
    select target_classes.target_id, chembl.protein_classification.parent_id from target_classes, chembl.protein_classification
        where target_classes.protein_class_id = chembl.protein_classification.id and chembl.protein_classification.parent_id is not null;

grant select on chembl.target_classes to sparql;

--------------------------------------------------------------------------------

create view chembl.protein_classification_paths as
  with recursive protein_classification_paths(protein_class_id, parent_id, path) as (
    select id, parent_id, '/' || short_name from chembl.protein_classification
  union
    select protein_classification_paths.protein_class_id, chembl.protein_classification.parent_id, '/' || chembl.protein_classification.short_name || protein_classification_paths.path
        from chembl.protein_classification, protein_classification_paths
        where protein_classification_paths.parent_id = chembl.protein_classification.id)
  select protein_class_id, path from protein_classification_paths where parent_id is null;

grant select on chembl.protein_classification_paths to sparql;
