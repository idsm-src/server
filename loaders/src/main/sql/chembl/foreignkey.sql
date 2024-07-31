-- activity
alter table chembl_tmp.activities add foreign key (document_id) references chembl_tmp.docs(id) initially deferred;
alter table chembl_tmp.activities add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;

-- assay
alter table chembl_tmp.assays add foreign key (src_id) references chembl_tmp.source(id) initially deferred;
alter table chembl_tmp.assays add foreign key (cell_line_id) references chembl_tmp.cell_dictionary(id) initially deferred;
alter table chembl_tmp.assays add foreign key (document_id) references chembl_tmp.docs(id) initially deferred;
alter table chembl_tmp.assays add foreign key (target_id) references chembl_tmp.target_dictionary(id) initially deferred;
alter table chembl_tmp.assays add foreign key (assay_type) references chembl_tmp.assay_type(assay_type) initially deferred;
alter table chembl_tmp.assays add foreign key (confidence_score) references chembl_tmp.confidence_score_lookup(confidence_score) initially deferred;
alter table chembl_tmp.assays add foreign key (relationship_type) references chembl_tmp.relationship_type(relationship_type) initially deferred;

-- bindingsite
alter table chembl_tmp.binding_sites add foreign key (target_id) references chembl_tmp.target_dictionary(id) initially deferred;

-- document
alter table chembl_tmp.docs add foreign key (journal_id) references chembl_tmp.journal_dictionary(id) initially deferred;

-- drug_indication
alter table chembl_tmp.drug_indication add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;

-- mechanism
alter table chembl_tmp.drug_mechanism add foreign key (site_id) references chembl_tmp.binding_sites(id) initially deferred;
alter table chembl_tmp.drug_mechanism add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.drug_mechanism add foreign key (target_id) references chembl_tmp.target_dictionary(id) initially deferred;

-- molecule_reference
alter table chembl_tmp.molecule_references add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_pubchem_references add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_pubchem_thom_pharm_references add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_pubchem_dotf_references add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_chebi_references add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;

-- molecule
alter table chembl_tmp.molecule_synonyms add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.biotherapeutics add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_atc_classification add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.biotherapeutic_components add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.biotherapeutic_components add foreign key (component_id) references chembl_tmp.bio_component_sequences(id) initially deferred;
alter table chembl_tmp.molecule_hrac_classification add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_hrac_classification add foreign key (hrac_class_id) references chembl_tmp.hrac_classification(hrac_class_id) initially deferred;
alter table chembl_tmp.molecule_irac_classification add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_irac_classification add foreign key (irac_class_id) references chembl_tmp.irac_classification(irac_class_id) initially deferred;
alter table chembl_tmp.molecule_frac_classification add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_frac_classification add foreign key (frac_class_id) references chembl_tmp.frac_classification(frac_class_id) initially deferred;
alter table chembl_tmp.compound_records add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.compound_records add foreign key (document_id) references chembl_tmp.docs(id) initially deferred;
alter table chembl_tmp.compound_properties add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.compound_structures add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_hierarchy add foreign key (molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;
alter table chembl_tmp.molecule_hierarchy add foreign key (parent_molecule_id) references chembl_tmp.molecule_dictionary(id) initially deferred;

-- protein_classification
alter table chembl_tmp.protein_classification add foreign key (parent_id) references chembl_tmp.protein_classification(id) initially deferred;

-- target_component_reference
alter table chembl_tmp.component_references add foreign key (component_id) references chembl_tmp.component_sequences(id) initially deferred;

-- target_component
alter table chembl_tmp.component_synonyms add foreign key (component_id) references chembl_tmp.component_sequences(id) initially deferred;
alter table chembl_tmp.component_class add foreign key (component_id) references chembl_tmp.component_sequences(id) initially deferred;
alter table chembl_tmp.component_class add foreign key (protein_class_id) references chembl_tmp.protein_classification(id) initially deferred;

-- target
alter table chembl_tmp.target_dictionary add foreign key (cell_line_id) references chembl_tmp.cell_dictionary(id) initially deferred;
alter table chembl_tmp.target_relations add foreign key (target_id) references chembl_tmp.target_dictionary(id) initially deferred;
alter table chembl_tmp.target_relations add foreign key (related_target_id) references chembl_tmp.target_dictionary(id) initially deferred;
alter table chembl_tmp.target_components add foreign key (target_id) references chembl_tmp.target_dictionary(id) initially deferred;
alter table chembl_tmp.target_components add foreign key (component_id) references chembl_tmp.component_sequences(id) initially deferred;
