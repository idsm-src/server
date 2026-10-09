create index drug_indications__chembl_id on chembl.drug_indications(chembl_id);
create index drug_indications__molecule on chembl.drug_indications(molecule);
create index drug_indications__mesh on chembl.drug_indications(mesh);
create index drug_indications__mesh_heading on chembl.drug_indications(mesh_heading);
create index drug_indications__efo_unit_efo_id on chembl.drug_indications(efo_unit, efo_id);
create index drug_indications__efo_name on chembl.drug_indications(efo_name);
create index drug_indications__phase on chembl.drug_indications(phase);
grant select on chembl.drug_indications to sparql;
