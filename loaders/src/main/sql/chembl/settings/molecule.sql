create index molecule_bases__chembl_id on chembl.molecule_bases(chembl_id);
create index molecule_bases__type on chembl.molecule_bases(type);
create index molecule_bases__label on chembl.molecule_bases(label);
create index molecule_bases__phase on chembl.molecule_bases(phase);
create index molecule_bases__biotherapeutic on chembl.molecule_bases(biotherapeutic);
create index molecule_bases__helm_notation on chembl.molecule_bases(helm_notation);
create index molecule_bases__description on chembl.molecule_bases(description);
create index molecule_bases__hrac_classification on chembl.molecule_bases(hrac_classification);
create index molecule_bases__irac_classification on chembl.molecule_bases(irac_classification);
create index molecule_bases__frac_classification on chembl.molecule_bases(frac_classification);
create index molecule_bases__parent on chembl.molecule_bases(parent);
grant select on chembl.molecule_bases to sparql;

--------------------------------------------------------------------------------

create index molecule_alternatives__molecule on chembl.molecule_alternatives(molecule);
create index molecule_alternatives__alternative on chembl.molecule_alternatives(alternative);
grant select on chembl.molecule_alternatives to sparql;

--------------------------------------------------------------------------------

create index molecule_atc_classifications__molecule on chembl.molecule_atc_classifications(molecule);
create index molecule_atc_classifications__classification on chembl.molecule_atc_classifications(classification);
grant select on chembl.molecule_atc_classifications to sparql;

--------------------------------------------------------------------------------

create index molecule_documents__molecule on chembl.molecule_documents(molecule);
create index molecule_documents__document on chembl.molecule_documents(document);
grant select on chembl.molecule_documents to sparql;

--------------------------------------------------------------------------------

create index molecule_biocomponents__molecule on chembl.molecule_biocomponents(molecule);
create index molecule_biocomponents__biocomponent on chembl.molecule_biocomponents(biocomponent);
grant select on chembl.molecule_biocomponents to sparql;

--------------------------------------------------------------------------------

create index molecule_descriptors__alogp on chembl.molecule_descriptors(alogp);
create index molecule_descriptors__aromatic_rings on chembl.molecule_descriptors(aromatic_rings);
create index molecule_descriptors__hba on chembl.molecule_descriptors(hba);
create index molecule_descriptors__hbd on chembl.molecule_descriptors(hbd);
create index molecule_descriptors__heavy_atoms on chembl.molecule_descriptors(heavy_atoms);
create index molecule_descriptors__num_ro5_violations on chembl.molecule_descriptors(num_ro5_violations);
create index molecule_descriptors__psa on chembl.molecule_descriptors(psa);
create index molecule_descriptors__qed_weighted on chembl.molecule_descriptors(qed_weighted);
create index molecule_descriptors__rtb on chembl.molecule_descriptors(rtb);
create index molecule_descriptors__mw_freebase on chembl.molecule_descriptors(mw_freebase);
create index molecule_descriptors__full_mwt on chembl.molecule_descriptors(full_mwt);
create index molecule_descriptors__ro3_pass on chembl.molecule_descriptors(ro3_pass);
create index molecule_descriptors__full_molformula on chembl.molecule_descriptors(full_molformula);
grant select on chembl.molecule_descriptors to sparql;

--------------------------------------------------------------------------------

create index molecule_structures__standard_inchi on chembl.molecule_structures using hash (standard_inchi);
create index molecule_structures__standard_inchi_key on chembl.molecule_structures(standard_inchi_key);
create index molecule_structures__canonical_smiles on chembl.molecule_structures(canonical_smiles);
grant select on chembl.molecule_structures to sparql;

--------------------------------------------------------------------------------

create index molecule_molfiles__molfile on chembl.molecule_molfiles using hash (molfile);
grant select on chembl.molecule_molfiles to sparql;

--------------------------------------------------------------------------------

create index molecule_labels__alogp on chembl.molecule_labels(alogp);
create index molecule_labels__aromatic_rings on chembl.molecule_labels(aromatic_rings);
create index molecule_labels__hba on chembl.molecule_labels(hba);
create index molecule_labels__hbd on chembl.molecule_labels(hbd);
create index molecule_labels__heavy_atoms on chembl.molecule_labels(heavy_atoms);
create index molecule_labels__num_ro5_violations on chembl.molecule_labels(num_ro5_violations);
create index molecule_labels__psa on chembl.molecule_labels(psa);
create index molecule_labels__qed_weighted on chembl.molecule_labels(qed_weighted);
create index molecule_labels__rtb on chembl.molecule_labels(rtb);
create index molecule_labels__mw_freebase on chembl.molecule_labels(mw_freebase);
create index molecule_labels__full_mwt on chembl.molecule_labels(full_mwt);
create index molecule_labels__ro3_pass on chembl.molecule_labels(ro3_pass);
create index molecule_labels__full_molformula on chembl.molecule_labels(full_molformula);
create index molecule_labels__standard_inchi on chembl.molecule_labels(standard_inchi);
create index molecule_labels__standard_inchi_key on chembl.molecule_labels(standard_inchi_key);
create index molecule_labels__canonical_smiles on chembl.molecule_labels(canonical_smiles);
create index molecule_labels__image on chembl.molecule_labels(image);
grant select on chembl.molecule_labels to sparql;

--------------------------------------------------------------------------------

create index molecule_references__molecule on chembl.molecule_references(molecule);
create index molecule_references__type on chembl.molecule_references(type);
create index molecule_references__reference on chembl.molecule_references(reference);
grant select on chembl.molecule_references to sparql;

--------------------------------------------------------------------------------

create index molecule_reference_labels__type on chembl.molecule_reference_labels(type);
create index molecule_reference_labels__reference on chembl.molecule_reference_labels(reference);
create index molecule_reference_labels__label on chembl.molecule_reference_labels(label);
grant select on chembl.molecule_reference_labels to sparql;

--------------------------------------------------------------------------------

create index molecule_pubchem_references__molecule on chembl.molecule_pubchem_references(molecule);
create index molecule_pubchem_references__compound on chembl.molecule_pubchem_references(compound);
grant select on chembl.molecule_pubchem_references to sparql;

--------------------------------------------------------------------------------

create index molecule_chebi_references__molecule on chembl.molecule_chebi_references(molecule);
create index molecule_chebi_references__chebi on chembl.molecule_chebi_references(chebi);
grant select on chembl.molecule_chebi_references to sparql;
