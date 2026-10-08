create index drug_indication_bases__chembl_id on chembl.drug_indication_bases(chembl_id);
create index drug_indication_bases__molecule on chembl.drug_indication_bases(molecule);
create index drug_indication_bases__mesh on chembl.drug_indication_bases(mesh);
create index drug_indication_bases__mesh_heading on chembl.drug_indication_bases(mesh_heading);
create index drug_indication_bases__efo on chembl.drug_indication_bases(efo_unit, efo_id);
create index drug_indication_bases__efo_name on chembl.drug_indication_bases(efo_name);
create index drug_indication_bases__phase on chembl.drug_indication_bases(phase);
grant select on chembl.drug_indication_bases to sparql;
