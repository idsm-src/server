create index target_component_bases__chembl_id on chembl.target_component_bases(chembl_id);
create index target_component_bases__type on chembl.target_component_bases(type);
create index target_component_bases__description on chembl.target_component_bases(description);
create index target_component_bases__organism on chembl.target_component_bases(organism);
create index target_component_bases__taxonomy on chembl.target_component_bases(taxonomy);
create index target_component_bases__sequence on chembl.target_component_bases using hash (sequence);
create index target_component_bases__accession on chembl.target_component_bases(accession);
grant select on chembl.target_component_bases to sparql;

--------------------------------------------------------------------------------

create index target_component_alternatives__component on chembl.target_component_alternatives(component);
create index target_component_alternatives__alternative on chembl.target_component_alternatives(alternative);
grant select on chembl.target_component_alternatives to sparql;

--------------------------------------------------------------------------------

create index target_component_references__component on chembl.target_component_references(component);
create index target_component_references__type on chembl.target_component_references(type);
create index target_component_references__reference on chembl.target_component_references(reference);
grant select on chembl.target_component_references to sparql;

--------------------------------------------------------------------------------

create index target_component_reference_labels__type on chembl.target_component_reference_labels(type);
create index target_component_reference_labels__reference on chembl.target_component_reference_labels(reference);
create index target_component_reference_labels__label on chembl.target_component_reference_labels(label);
grant select on chembl.target_component_reference_labels to sparql;
