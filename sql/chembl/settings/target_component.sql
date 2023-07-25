alter table chembl_tmp.component_sequences drop column sequence_md5sum;
alter table chembl_tmp.component_sequences drop column db_source;
alter table chembl_tmp.component_sequences drop column db_version;

alter table chembl_tmp.component_sequences add primary key (id);
create index component_sequences__component_type on chembl_tmp.component_sequences(component_type);
create index component_sequences__accession on chembl_tmp.component_sequences(accession);
create index component_sequences__sequence on chembl_tmp.component_sequences using hash (sequence);
create index component_sequences__description on chembl_tmp.component_sequences(description);
create index component_sequences__tax_id on chembl_tmp.component_sequences(tax_id);
create index component_sequences__organism on chembl_tmp.component_sequences(organism);
create index component_sequences__chembl_id on chembl_tmp.component_sequences(chembl_id);
grant select on chembl_tmp.component_sequences to sparql;

--------------------------------------------------------------------------------

delete from chembl_tmp.component_synonyms where syn_type = 'EC_NUMBER';
alter table chembl_tmp.component_synonyms drop column syn_type;

delete from chembl_tmp.component_synonyms where compsyn_id not in (select min(compsyn_id) from chembl_tmp.component_synonyms group by component_id, component_synonym);

alter table chembl_tmp.component_synonyms add primary key (compsyn_id);
create index component_synonyms__component_id on chembl_tmp.component_synonyms(component_id);
create index component_synonyms__component_synonym on chembl_tmp.component_synonyms(component_synonym);
grant select on chembl_tmp.component_synonyms to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.component_class add primary key (comp_class_id);
create index component_class__component_id on chembl_tmp.component_class(component_id);
create index component_class__protein_class_id on chembl_tmp.component_class(protein_class_id);
grant select on chembl_tmp.component_class to sparql;

