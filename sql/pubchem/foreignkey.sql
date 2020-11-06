-- bioassay
alter table pubchem.bioassay_bases add foreign key (source) references pubchem.source_bases(id) initially deferred;
alter table pubchem.bioassay_data add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_stages add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_confirmatory_assays add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_confirmatory_assays add foreign key (confirmatory_assay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_primary_assays add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_primary_assays add foreign key (primary_assay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_summary_assays add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_summary_assays add foreign key (summary_assay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_chembl_assays add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_chembl_mechanisms add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;


-- compound
alter table pubchem.compound_components add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_components add foreign key (component) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_isotopologues add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_isotopologues add foreign key (isotopologue) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_parents add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_parents add foreign key (parent) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_stereoisomers add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_stereoisomers add foreign key (isomer) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_same_connectivities add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_same_connectivities add foreign key (isomer) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_roles add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_types add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_active_ingredients add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table molecules.pubchem add foreign key (id) references pubchem.compound_bases(id) initially deferred;


-- concept
alter table pubchem.concept_bases add foreign key (scheme) references pubchem.concept_bases(id) initially deferred;
alter table pubchem.concept_bases add foreign key (broader) references pubchem.concept_bases(id) initially deferred;


-- conserveddomain
alter table pubchem.conserveddomain_references add foreign key (domain) references pubchem.conserveddomain_bases(id) initially deferred;
---- alter table pubchem.conserveddomain_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;


-- endpoint
alter table pubchem.endpoint_measurements add foreign key (substance, bioassay, measuregroup) references pubchem.endpoint_bases(substance, bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_measurement_values add foreign key (substance, bioassay, measuregroup) references pubchem.endpoint_bases(substance, bioassay, measuregroup) initially deferred;
---- alter table pubchem.endpoint_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.endpoint_references add foreign key (substance, bioassay, measuregroup) references pubchem.endpoint_bases(substance, bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_outcomes add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.endpoint_outcomes add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_outcomes add foreign key (substance, bioassay, measuregroup) references pubchem.endpoint_bases(substance, bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_bases add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.endpoint_bases add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;


-- gene
alter table pubchem.gene_alternatives add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_references add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
---- alter table pubchem.gene_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.gene_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_processes add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_functions add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_locations add foreign key (gene) references pubchem.gene_bases(id) initially deferred;


-- inchikey
alter table pubchem.inchikey_compounds add foreign key (inchikey) references pubchem.inchikey_bases(id) initially deferred;
alter table pubchem.inchikey_subjects add foreign key (inchikey) references pubchem.inchikey_bases(id) initially deferred;
alter table pubchem.inchikey_compounds add foreign key (compound) references pubchem.compound_bases(id) initially deferred;


-- measuregroup
alter table pubchem.measuregroup_bases add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.measuregroup_bases add foreign key (source) references pubchem.source_bases(id) initially deferred;
alter table pubchem.measuregroup_proteins add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_proteins add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.measuregroup_genes add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
---- alter table pubchem.measuregroup_genes add foreign key (gene) references pubchem.gene_bases(id) initially deferred;


-- pathway
alter table pubchem.pathway_bases add foreign key (source) references pubchem.source_bases(id) initially deferred;
alter table pubchem.pathway_compounds add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_compounds add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.pathway_proteins add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_proteins add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.pathway_genes add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_genes add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
---- alter table pubchem.pathway_components add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
---- alter table pubchem.pathway_components add foreign key (component) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_related_pathways add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
---- alter table pubchem.pathway_related_pathways add foreign key (related) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_references add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
---- alter table pubchem.pathway_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;


-- protein
alter table pubchem.protein_references add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
---- alter table pubchem.protein_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.protein_pdblinks add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_similarproteins add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_similarproteins add foreign key (simprotein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_genes add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
---- alter table pubchem.protein_genes add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.protein_closematches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_uniprot_closematches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_conserveddomains add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
---- alter table pubchem.protein_conserveddomains add foreign key (domain) references pubchem.conserveddomain_bases(id) initially deferred;
alter table pubchem.protein_continuantparts add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_continuantparts add foreign key (part) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_types add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_complexes add foreign key (protein) references pubchem.protein_bases(id) initially deferred;


-- reference
---- alter table pubchem.reference_discusses add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
---- alter table pubchem.reference_subjects add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
---- alter table pubchem.reference_primary_subjects add foreign key (reference) references pubchem.reference_bases(id) initially deferred;


-- source
alter table pubchem.source_subjects add foreign key (source) references pubchem.source_bases(id) initially deferred;
alter table pubchem.source_subjects add foreign key (subject) references pubchem.concept_bases(id) initially deferred;
alter table pubchem.source_alternatives add foreign key (source) references pubchem.source_bases(id) initially deferred;


-- substance
alter table pubchem.substance_bases add foreign key (source) references pubchem.source_bases(id) initially deferred;
---- alter table pubchem.substance_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.substance_synonyms add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.substance_bases add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.substance_types add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_matches add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_references add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_pdblinks add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_synonyms add foreign key (substance) references pubchem.substance_bases(id) initially deferred;


-- synonym
alter table pubchem.synonym_values add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_types add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_compounds add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_mesh_subjects add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_concept_subjects add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_concept_subjects add foreign key (concept) references pubchem.concept_bases(id) initially deferred;
alter table pubchem.synonym_compounds add foreign key (compound) references pubchem.compound_bases(id) initially deferred;


-- descriptor-compound
alter table pubchem.descriptor_compound_bases add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_molecular_formulas add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_isomeric_smileses add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_canonical_smileses add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_iupac_inchis add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_preferred_iupac_names add foreign key (compound) references pubchem.compound_bases(id) initially deferred;


-- descriptor-substance
alter table pubchem.descriptor_substance_bases add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
