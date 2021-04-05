alter table chembl.bio_component_sequences drop column sequence_md5sum;

alter table chembl.bio_component_sequences add primary key (id);
create index bio_component_sequences__component_type on chembl.bio_component_sequences(component_type);
create index bio_component_sequences__description on chembl.bio_component_sequences(description);
create index bio_component_sequences__sequence on chembl.bio_component_sequences using hash (sequence);
create index bio_component_sequences__tax_id on chembl.bio_component_sequences(tax_id);
create index bio_component_sequences__organism on chembl.bio_component_sequences(organism);
create index bio_component_sequences__chembl_id on chembl.bio_component_sequences(chembl_id);
grant select on chembl.bio_component_sequences to sparql;
