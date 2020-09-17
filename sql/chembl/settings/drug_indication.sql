alter table chembl.drug_indication drop column record_id;
alter table chembl.drug_indication drop column molregno;
alter table chembl.drug_indication drop column efo_id;

alter table chembl.drug_indication add primary key (id);
create index drug_indication__max_phase_for_ind on chembl.drug_indication(max_phase_for_ind);
create index drug_indication__mesh_id on chembl.drug_indication(mesh_id);
create index drug_indication__mesh_heading on chembl.drug_indication(mesh_heading);
create index drug_indication__efo_term on chembl.drug_indication(efo_term);
create index drug_indication__molecule_id on chembl.drug_indication(molecule_id);
create index drug_indication__efo_resource on chembl.drug_indication(efo_resource_unit, efo_resource_id);
create index drug_indication__chembl_id on chembl.drug_indication(chembl_id);
grant select on chembl.drug_indication to sparql;
