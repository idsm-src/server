alter table chembl.component_sequences drop column sequence_md5sum;
alter table chembl.component_sequences drop column db_source;
alter table chembl.component_sequences drop column db_version;

alter table chembl.component_sequences add primary key (id);
create index component_sequences__component_type on chembl.component_sequences(component_type);
create index component_sequences__accession on chembl.component_sequences(accession);
create index component_sequences__sequence on chembl.component_sequences using hash (sequence);
create index component_sequences__description on chembl.component_sequences(description);
create index component_sequences__tax_id on chembl.component_sequences(tax_id);
create index component_sequences__organism on chembl.component_sequences(organism);
create index component_sequences__chembl_id on chembl.component_sequences(chembl_id);
grant select on chembl.component_sequences to sparql;

--------------------------------------------------------------------------------

delete from chembl.component_synonyms where syn_type = 'EC_NUMBER';
alter table chembl.component_synonyms drop column syn_type;

alter table chembl.component_synonyms add primary key (compsyn_id);
create index component_synonyms__component_id on chembl.component_synonyms(component_id);
create index component_synonyms__component_synonym on chembl.component_synonyms(component_synonym);
grant select on chembl.component_synonyms to sparql;

--------------------------------------------------------------------------------

alter table chembl.component_class add primary key (comp_class_id);
create index component_class__component_id on chembl.component_class(component_id);
create index component_class__protein_class_id on chembl.component_class(protein_class_id);
grant select on chembl.component_class to sparql;

