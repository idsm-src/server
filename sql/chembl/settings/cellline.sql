alter table chembl.cell_dictionary drop column cell_id;
alter table chembl.cell_dictionary drop column cell_source_tissue;
alter table chembl.cell_dictionary drop column clo_id;
alter table chembl.cell_dictionary drop column efo_id;
alter table chembl.cell_dictionary drop column cell_ontology_id;

alter table chembl.cell_dictionary add primary key (id);
create index cell_dictionary__cell_name on chembl.cell_dictionary(cell_name);
create index cell_dictionary__cell_description on chembl.cell_dictionary(cell_description);
create index cell_dictionary__cell_source_organism on chembl.cell_dictionary(cell_source_organism);
create index cell_dictionary__cell_source_tax_id on chembl.cell_dictionary(cell_source_tax_id);
create index cell_dictionary__cellosaurus_id on chembl.cell_dictionary(cellosaurus_id);
create index cell_dictionary__cl_lincs_id on chembl.cell_dictionary(cl_lincs_id);
create index cell_dictionary__chembl_id on chembl.cell_dictionary(chembl_id);
create index cell_dictionary__clo_resource_id on chembl.cell_dictionary(clo_resource_id);
create index cell_dictionary__efo_resource_id on chembl.cell_dictionary(efo_resource_id);
grant select on chembl.cell_dictionary to sparql;
