-- activity
alter table chembl.activity_bases add foreign key (assay) references chembl.assay_bases(id) initially deferred;
alter table chembl.activity_bases add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.activity_bases add foreign key (document) references chembl.document_bases(id) initially deferred;


-- assay
alter table chembl.assay_bases add foreign key (document) references chembl.document_bases(id) initially deferred;
alter table chembl.assay_bases add foreign key (target) references chembl.target_bases(id) initially deferred;
alter table chembl.assay_bases add foreign key (source) references chembl.source_bases(id) initially deferred;
alter table chembl.assay_bases add foreign key (cell_line) references chembl.cell_line_bases(id) initially deferred;


-- binding_site
alter table chembl.binding_site_bases add foreign key (target) references chembl.target_bases(id) initially deferred;


-- document
alter table chembl.document_bases add foreign key (journal) references chembl.journal_bases(id) initially deferred;


-- drug_indication
alter table chembl.drug_indication_bases add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;


-- mechanism
alter table chembl.mechanism_bases add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.mechanism_bases add foreign key (target) references chembl.target_bases(id) initially deferred;
alter table chembl.mechanism_bases add foreign key (binding_site) references chembl.binding_site_bases(id) initially deferred;


-- molecule
alter table chembl.molecule_bases add foreign key (parent) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_alternatives add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_atc_classifications add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_documents add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_documents add foreign key (document) references chembl.document_bases(id) initially deferred;
alter table chembl.molecule_biocomponents add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_biocomponents add foreign key (biocomponent) references chembl.biocomponent_bases(id) initially deferred;
alter table chembl.molecule_descriptors add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_structures add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_molfiles add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_labels add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_references add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_pubchem_references add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;
alter table chembl.molecule_chebi_references add foreign key (molecule) references chembl.molecule_bases(id) initially deferred;


-- protein_class
alter table chembl.protein_class_bases add foreign key (parent) references chembl.protein_class_bases(id) initially deferred;
alter table chembl.protein_class_component_descendants add foreign key (class) references chembl.protein_class_bases(id) initially deferred;
alter table chembl.protein_class_component_descendants add foreign key (component) references chembl.target_component_bases(id) initially deferred;
alter table chembl.protein_class_target_descendants add foreign key (class) references chembl.protein_class_bases(id) initially deferred;
alter table chembl.protein_class_target_descendants add foreign key (target) references chembl.target_bases(id) initially deferred;


-- target_component
alter table chembl.target_component_alternatives add foreign key (component) references chembl.target_component_bases(id) initially deferred;
alter table chembl.target_component_references add foreign key (component) references chembl.target_component_bases(id) initially deferred;


-- target
alter table chembl.target_bases add foreign key (cell_line) references chembl.cell_line_bases(id) initially deferred;
alter table chembl.target_components add foreign key (target) references chembl.target_bases(id) initially deferred;
alter table chembl.target_components add foreign key (component) references chembl.target_component_bases(id) initially deferred;
alter table chembl.target_exact_matches add foreign key (target) references chembl.target_bases(id) initially deferred;
alter table chembl.target_exact_matches add foreign key (component) references chembl.target_component_bases(id) initially deferred;
alter table chembl.target_related_matches add foreign key (target) references chembl.target_bases(id) initially deferred;
alter table chembl.target_related_matches add foreign key (component) references chembl.target_component_bases(id) initially deferred;
alter table chembl.target_relations add foreign key (target) references chembl.target_bases(id) initially deferred;
alter table chembl.target_relations add foreign key (related) references chembl.target_bases(id) initially deferred;
