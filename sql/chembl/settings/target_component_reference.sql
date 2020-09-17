create index component_references__component_id on chembl.component_references(component_id);
create index component_references__reference_type on chembl.component_references(reference_type);
create index component_references__reference on chembl.component_references(reference);
grant select on chembl.component_references to sparql;

--------------------------------------------------------------------------------

create view chembl.component_reference_types as
    select distinct reference_type, reference from chembl.component_references;

grant select on chembl.component_reference_types to sparql;
