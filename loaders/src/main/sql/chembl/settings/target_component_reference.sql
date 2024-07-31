create index component_references__component_id on chembl_tmp.component_references(component_id);
create index component_references__reference_type on chembl_tmp.component_references(reference_type);
create index component_references__reference on chembl_tmp.component_references(reference);
grant select on chembl_tmp.component_references to sparql;

--------------------------------------------------------------------------------

create view chembl_tmp.component_reference_types as
    select distinct reference_type, reference from chembl_tmp.component_references;

grant select on chembl_tmp.component_reference_types to sparql;
