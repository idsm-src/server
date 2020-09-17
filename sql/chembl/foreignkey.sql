-- activity
alter table chembl.activities add foreign key (document_id) references chembl.docs(id) initially deferred;
alter table chembl.activities add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;

-- assay
alter table chembl.assays add foreign key (src_id) references chembl.source(id) initially deferred;
alter table chembl.assays add foreign key (cell_line_id) references chembl.cell_dictionary(id) initially deferred;
alter table chembl.assays add foreign key (document_id) references chembl.docs(id) initially deferred;
alter table chembl.assays add foreign key (target_id) references chembl.target_dictionary(id) initially deferred;
alter table chembl.assays add foreign key (assay_type) references chembl.assay_type(assay_type) initially deferred;
alter table chembl.assays add foreign key (confidence_score) references chembl.confidence_score_lookup(confidence_score) initially deferred;
alter table chembl.assays add foreign key (relationship_type) references chembl.relationship_type(relationship_type) initially deferred;

-- bindingsite
alter table chembl.binding_sites add foreign key (target_id) references chembl.target_dictionary(id) initially deferred;

-- document
alter table chembl.docs add foreign key (journal_id) references chembl.journal_dictionary(id) initially deferred;

-- drug_indication
alter table chembl.drug_indication add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;

-- mechanism
alter table chembl.drug_mechanism add foreign key (site_id) references chembl.binding_sites(id) initially deferred;
alter table chembl.drug_mechanism add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.drug_mechanism add foreign key (target_id) references chembl.target_dictionary(id) initially deferred;

-- molecule_reference
alter table chembl.molecule_references add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.molecule_pubchem_references add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.molecule_pubchem_thom_pharm_references add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.molecule_pubchem_dotf_references add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;

-- molecule
alter table chembl.molecule_synonyms add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.biotherapeutics add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.molecule_atc_classification add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.biotherapeutic_components add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.biotherapeutic_components add foreign key (component_id) references chembl.bio_component_sequences(id) initially deferred;
alter table chembl.molecule_hrac_classification add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.molecule_hrac_classification add foreign key (hrac_class_id) references chembl.hrac_classification(hrac_class_id) initially deferred;
alter table chembl.molecule_irac_classification add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.molecule_irac_classification add foreign key (irac_class_id) references chembl.irac_classification(irac_class_id) initially deferred;
alter table chembl.molecule_frac_classification add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.molecule_frac_classification add foreign key (frac_class_id) references chembl.frac_classification(frac_class_id) initially deferred;
alter table chembl.compound_records add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.compound_records add foreign key (document_id) references chembl.docs(id) initially deferred;
alter table chembl.compound_properties add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.compound_structures add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.molecule_hierarchy add foreign key (molecule_id) references chembl.molecule_dictionary(id) initially deferred;
alter table chembl.molecule_hierarchy add foreign key (parent_molecule_id) references chembl.molecule_dictionary(id) initially deferred;

-- protein_classification
alter table chembl.protein_classification add foreign key (parent_id) references chembl.protein_classification(id) initially deferred;

-- target_component_reference
alter table chembl.component_references add foreign key (component_id) references chembl.component_sequences(id) initially deferred;

-- target_component
alter table chembl.component_synonyms add foreign key (component_id) references chembl.component_sequences(id) initially deferred;
alter table chembl.component_class add foreign key (component_id) references chembl.component_sequences(id) initially deferred;
alter table chembl.component_class add foreign key (protein_class_id) references chembl.protein_classification(id) initially deferred;

-- target
alter table chembl.target_dictionary add foreign key (cell_line_id) references chembl.cell_dictionary(id) initially deferred;
alter table chembl.target_relations add foreign key (target_id) references chembl.target_dictionary(id) initially deferred;
alter table chembl.target_relations add foreign key (related_target_id) references chembl.target_dictionary(id) initially deferred;
alter table chembl.target_components add foreign key (target_id) references chembl.target_dictionary(id) initially deferred;
alter table chembl.target_components add foreign key (component_id) references chembl.component_sequences(id) initially deferred;
