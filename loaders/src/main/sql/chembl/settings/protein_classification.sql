alter table chembl_tmp.protein_classification drop column protein_class_desc;
alter table chembl_tmp.protein_classification drop column definition;
alter table chembl_tmp.protein_classification drop column class_level;

alter table chembl_tmp.protein_classification add primary key (id);
create index protein_classification__parent_id on chembl_tmp.protein_classification(parent_id);
create index protein_classification__pref_name on chembl_tmp.protein_classification(pref_name);
create index protein_classification__chembl_id on chembl_tmp.protein_classification(chembl_id);
create index protein_classification__class_level_name on chembl_tmp.protein_classification(class_level_name);
grant select on chembl_tmp.protein_classification to sparql;

--------------------------------------------------------------------------------

create recursive view chembl_tmp.component_classes(component_id, protein_class_id) as
    select component_id, protein_class_id from chembl_tmp.component_class
  union
    select component_classes.component_id, chembl_tmp.protein_classification.parent_id from component_classes, chembl_tmp.protein_classification
        where component_classes.protein_class_id = chembl_tmp.protein_classification.id and chembl_tmp.protein_classification.parent_id is not null;

grant select on chembl_tmp.component_classes to sparql;

--------------------------------------------------------------------------------

create recursive view chembl_tmp.target_classes(target_id, protein_class_id) as
    select chembl_tmp.target_components.target_id, protein_class_id from chembl_tmp.target_components, chembl_tmp.component_class where chembl_tmp.target_components.component_id = chembl_tmp.component_class.component_id
  union
    select target_classes.target_id, chembl_tmp.protein_classification.parent_id from target_classes, chembl_tmp.protein_classification
        where target_classes.protein_class_id = chembl_tmp.protein_classification.id and chembl_tmp.protein_classification.parent_id is not null;

grant select on chembl_tmp.target_classes to sparql;

--------------------------------------------------------------------------------

create view chembl_tmp.protein_classification_paths as
  with recursive protein_classification_paths(protein_class_id, parent_id, path) as (
    select id, parent_id, '/' || short_name from chembl_tmp.protein_classification
  union
    select protein_classification_paths.protein_class_id, chembl_tmp.protein_classification.parent_id, '/' || chembl_tmp.protein_classification.short_name || protein_classification_paths.path
        from chembl_tmp.protein_classification, protein_classification_paths
        where protein_classification_paths.parent_id = chembl_tmp.protein_classification.id)
  select protein_class_id, path from protein_classification_paths where parent_id is null;

grant select on chembl_tmp.protein_classification_paths to sparql;
