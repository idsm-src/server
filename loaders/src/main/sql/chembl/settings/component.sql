create index components__chembl_id on chembl.components(chembl_id);
create index components__type on chembl.components(type);
create index components__description on chembl.components(description);
create index components__organism on chembl.components(organism);
create index components__taxonomy on chembl.components(taxonomy);
create index components__sequence on chembl.components using hash (sequence);
create index components__accession on chembl.components(accession);
grant select on chembl.components to sparql;

--------------------------------------------------------------------------------

create index component_alternatives__component on chembl.component_alternatives(component);
create index component_alternatives__alternative on chembl.component_alternatives(alternative);
grant select on chembl.component_alternatives to sparql;

--------------------------------------------------------------------------------

create index component_references__component on chembl.component_references(component);
create index component_references__type on chembl.component_references(type);
create index component_references__reference on chembl.component_references(reference);
grant select on chembl.component_references to sparql;

--------------------------------------------------------------------------------

create index component_reference_labels__type on chembl.component_reference_labels(type);
create index component_reference_labels__reference on chembl.component_reference_labels(reference);
create index component_reference_labels__label on chembl.component_reference_labels(label);
grant select on chembl.component_reference_labels to sparql;
