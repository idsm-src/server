-- activity
alter table chembl.activities add foreign key (assay) references chembl.assays(id) initially deferred;
alter table chembl.activities add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.activities add foreign key (document) references chembl.documents(id) initially deferred;


-- assay
alter table chembl.assays add foreign key (document) references chembl.documents(id) initially deferred;
alter table chembl.assays add foreign key (target) references chembl.targets(id) initially deferred;
alter table chembl.assays add foreign key (source) references chembl.sources(id) initially deferred;
alter table chembl.assays add foreign key (cell_line) references chembl.cell_lines(id) initially deferred;


-- binding_site
alter table chembl.binding_sites add foreign key (target) references chembl.targets(id) initially deferred;


-- document
alter table chembl.documents add foreign key (journal) references chembl.journals(id) initially deferred;


-- drug_indication
alter table chembl.drug_indications add foreign key (molecule) references chembl.molecules(id) initially deferred;


-- mechanism
alter table chembl.mechanisms add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.mechanisms add foreign key (target) references chembl.targets(id) initially deferred;
alter table chembl.mechanisms add foreign key (binding_site) references chembl.binding_sites(id) initially deferred;


-- molecule
alter table chembl.molecules add foreign key (parent) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_alternatives add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_atc_classifications add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_documents add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_documents add foreign key (document) references chembl.documents(id) initially deferred;
alter table chembl.molecule_biocomponents add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_biocomponents add foreign key (biocomponent) references chembl.biocomponents(id) initially deferred;
alter table chembl.molecule_descriptors add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_structures add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_molfiles add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_labels add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_references add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_pubchem_references add foreign key (molecule) references chembl.molecules(id) initially deferred;
alter table chembl.molecule_chebi_references add foreign key (molecule) references chembl.molecules(id) initially deferred;


-- protein_class
alter table chembl.protein_classes add foreign key (parent) references chembl.protein_classes(id) initially deferred;
alter table chembl.protein_class_component_descendants add foreign key (protein_class) references chembl.protein_classes(id) initially deferred;
alter table chembl.protein_class_component_descendants add foreign key (component) references chembl.components(id) initially deferred;
alter table chembl.protein_class_target_descendants add foreign key (protein_class) references chembl.protein_classes(id) initially deferred;
alter table chembl.protein_class_target_descendants add foreign key (target) references chembl.targets(id) initially deferred;


-- target_component
alter table chembl.component_alternatives add foreign key (component) references chembl.components(id) initially deferred;
alter table chembl.component_references add foreign key (component) references chembl.components(id) initially deferred;


-- target
alter table chembl.targets add foreign key (cell_line) references chembl.cell_lines(id) initially deferred;
alter table chembl.target_components add foreign key (target) references chembl.targets(id) initially deferred;
alter table chembl.target_components add foreign key (component) references chembl.components(id) initially deferred;
alter table chembl.target_exact_matches add foreign key (target) references chembl.targets(id) initially deferred;
alter table chembl.target_exact_matches add foreign key (component) references chembl.components(id) initially deferred;
alter table chembl.target_related_matches add foreign key (target) references chembl.targets(id) initially deferred;
alter table chembl.target_related_matches add foreign key (component) references chembl.components(id) initially deferred;
alter table chembl.target_relations add foreign key (target) references chembl.targets(id) initially deferred;
alter table chembl.target_relations add foreign key (related) references chembl.targets(id) initially deferred;
