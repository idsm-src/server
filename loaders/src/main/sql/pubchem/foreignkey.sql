-- anatomy
alter table pubchem.anatomy_alternatives add foreign key (anatomy) references pubchem.anatomy_bases(id) initially deferred;
alter table pubchem.anatomy_matches add foreign key (anatomy) references pubchem.anatomy_bases(id) initially deferred;
alter table pubchem.anatomy_mesh_matches add foreign key (anatomy) references pubchem.anatomy_bases(id) initially deferred;


-- author
alter table pubchem.author_given_names add foreign key (author) references pubchem.author_bases(id) initially deferred;
alter table pubchem.author_family_names add foreign key (author) references pubchem.author_bases(id) initially deferred;
alter table pubchem.author_formatted_names add foreign key (author) references pubchem.author_bases(id) initially deferred;
alter table pubchem.author_organizations add foreign key (author) references pubchem.author_bases(id) initially deferred;
alter table pubchem.author_orcids add foreign key (author) references pubchem.author_bases(id) initially deferred;


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
alter table pubchem.bioassay_patent_references add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.bioassay_patent_references add foreign key (patent) references pubchem.patent_bases(id) initially deferred;


-- book
alter table pubchem.book_authors add foreign key (book) references pubchem.book_bases(id) initially deferred;
alter table pubchem.book_authors add foreign key (author) references pubchem.author_bases(id) initially deferred;


-- cell
alter table pubchem.cell_bases add foreign key (organism) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.cell_alternatives add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.cell_occurrences add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.cell_references add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.cell_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.cell_matches add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.cell_mesh_matches add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.cell_wikidata_matches add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.cell_cellosaurus_matches add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.cell_chembl_card_matches add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.cell_anatomies add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.cell_anatomies add foreign key (anatomy) references pubchem.anatomy_bases(id) initially deferred;


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
alter table pubchem.compound_titles add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_thesaurus_matches add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.compound_wikidata_matches add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table molecules.pubchem add foreign key (id) references pubchem.compound_bases(id) initially deferred;


-- concept
alter table pubchem.concept_bases add foreign key (scheme) references pubchem.concept_bases(id) initially deferred;
alter table pubchem.concept_bases add foreign key (broader) references pubchem.concept_bases(id) initially deferred;


-- conserveddomain
alter table pubchem.conserveddomain_references add foreign key (domain) references pubchem.conserveddomain_bases(id) initially deferred;
alter table pubchem.conserveddomain_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;


-- cooccurrences
alter table pubchem.chemical_chemical_cooccurrences add foreign key (subject) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.chemical_chemical_cooccurrences add foreign key (object) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.chemical_disease_cooccurrences add foreign key (subject) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.chemical_disease_cooccurrences add foreign key (object) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.chemical_gene_cooccurrences add foreign key (subject) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.chemical_gene_cooccurrences add foreign key (object) references pubchem.gene_symbol_bases(id) initially deferred;
alter table pubchem.chemical_enzyme_cooccurrences add foreign key (subject) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.chemical_enzyme_cooccurrences add foreign key (object) references pubchem.enzyme_bases(id) initially deferred;
alter table pubchem.disease_chemical_cooccurrences add foreign key (subject) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.disease_chemical_cooccurrences add foreign key (object) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.disease_disease_cooccurrences add foreign key (subject) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.disease_disease_cooccurrences add foreign key (object) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.disease_gene_cooccurrences add foreign key (subject) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.disease_gene_cooccurrences add foreign key (object) references pubchem.gene_symbol_bases(id) initially deferred;
alter table pubchem.disease_enzyme_cooccurrences add foreign key (subject) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.disease_enzyme_cooccurrences add foreign key (object) references pubchem.enzyme_bases(id) initially deferred;
alter table pubchem.gene_chemical_cooccurrences add foreign key (subject) references pubchem.gene_symbol_bases(id) initially deferred;
alter table pubchem.gene_chemical_cooccurrences add foreign key (object) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.enzyme_chemical_cooccurrences add foreign key (subject) references pubchem.enzyme_bases(id) initially deferred;
alter table pubchem.enzyme_chemical_cooccurrences add foreign key (object) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.gene_disease_cooccurrences add foreign key (subject) references pubchem.gene_symbol_bases(id) initially deferred;
alter table pubchem.gene_disease_cooccurrences add foreign key (object) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.enzyme_disease_cooccurrences add foreign key (subject) references pubchem.enzyme_bases(id) initially deferred;
alter table pubchem.enzyme_disease_cooccurrences add foreign key (object) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.gene_gene_cooccurrences add foreign key (subject) references pubchem.gene_symbol_bases(id) initially deferred;
alter table pubchem.gene_gene_cooccurrences add foreign key (object) references pubchem.gene_symbol_bases(id) initially deferred;


-- disease
alter table pubchem.disease_alternatives add foreign key (disease) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.disease_matches add foreign key (disease) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.disease_mesh_matches add foreign key (disease) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.disease_related_matches add foreign key (disease) references pubchem.disease_bases(id) initially deferred;


-- endpoint
alter table pubchem.endpoint_bases add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.endpoint_bases add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.endpoint_bases add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_bases add foreign key (bioassay, measuregroup, substance) references pubchem.measuregroup_substances(bioassay, measuregroup, substance) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (bioassay, measuregroup, substance) references pubchem.measuregroup_substances(bioassay, measuregroup, substance) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (bioassay, measuregroup, substance, value) references pubchem.endpoint_bases(bioassay, measuregroup, substance, value) initially deferred;
alter table pubchem.endpoint_references add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.endpoint_references add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.endpoint_references add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_references add foreign key (substance, bioassay, measuregroup, value) references pubchem.endpoint_bases(substance, bioassay, measuregroup, value) initially deferred;
alter table pubchem.endpoint_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;


-- gene
alter table pubchem.gene_bases add foreign key (gene_symbol) references pubchem.gene_symbol_bases(id) initially deferred;
alter table pubchem.gene_bases add foreign key (organism) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.gene_alternatives add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_references add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.gene_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_ensembl_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_mesh_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_expasy_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_medlineplus_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_alliancegenome_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_kegg_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_pharos_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_bgee_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_pombase_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_veupathdb_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_zfin_matches add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_processes add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_functions add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_locations add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_orthologs add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.gene_orthologs add foreign key (ortholog) references pubchem.gene_bases(id) initially deferred;


-- grant
alter table pubchem.grant_bases add foreign key (organization) references pubchem.organization_bases(id) initially deferred;


-- inchikey
alter table pubchem.inchikey_compounds add foreign key (inchikey) references pubchem.inchikey_bases(id) initially deferred;
alter table pubchem.inchikey_compounds add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.inchikey_subjects add foreign key (inchikey) references pubchem.inchikey_bases(id) initially deferred;


-- journal


-- measuregroup
alter table pubchem.measuregroup_bases add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.measuregroup_bases add foreign key (source) references pubchem.source_bases(id) initially deferred;
alter table pubchem.measuregroup_substances add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.measuregroup_substances add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_substances add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.measuregroup_proteins add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.measuregroup_proteins add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_proteins add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.measuregroup_genes add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.measuregroup_genes add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_genes add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.measuregroup_taxonomies add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.measuregroup_taxonomies add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_taxonomies add foreign key (taxonomy) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.measuregroup_cells add foreign key (bioassay) references pubchem.bioassay_bases(id) initially deferred;
alter table pubchem.measuregroup_cells add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_cells add foreign key (cell) references pubchem.cell_bases(id) initially deferred;
alter table pubchem.measuregroup_anatomies add foreign key (bioassay, measuregroup) references pubchem.measuregroup_bases(bioassay, measuregroup) initially deferred;


-- organization
alter table pubchem.organization_country_names add foreign key (organization) references pubchem.organization_bases(id) initially deferred;
alter table pubchem.organization_formatted_names add foreign key (organization) references pubchem.organization_bases(id) initially deferred;
alter table pubchem.organization_crossref_matches add foreign key (organization) references pubchem.organization_bases(id) initially deferred;


-- patent
alter table pubchem.patent_cpc_additional_classifications add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_cpc_inventive_classifications add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_ipc_additional_classifications add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_ipc_inventive_classifications add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_citations add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_citations add foreign key (citation) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_substances add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_substances add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.patent_compounds add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_compounds add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.patent_genes add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_genes add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.patent_proteins add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_proteins add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.patent_taxonomies add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_taxonomies add foreign key (taxonomy) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.patent_anatomies add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_inventors add foreign key (patent) references pubchem.patent_bases(id) initially deferred;
alter table pubchem.patent_applicants add foreign key (patent) references pubchem.patent_bases(id) initially deferred;


-- pathway
alter table pubchem.pathway_bases add foreign key (source) references pubchem.source_bases(id) initially deferred;
alter table pubchem.pathway_bases add foreign key (organism) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.pathway_compounds add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_compounds add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.pathway_proteins add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_proteins add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.pathway_genes add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_genes add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.pathway_components add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_components add foreign key (component) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_related_pathways add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_related_pathways add foreign key (related) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_references add foreign key (pathway) references pubchem.pathway_bases(id) initially deferred;
alter table pubchem.pathway_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;


-- protein
alter table pubchem.enzyme_bases add foreign key (parent) references pubchem.enzyme_bases(id) initially deferred;
alter table pubchem.enzyme_alternatives add foreign key (enzyme) references pubchem.enzyme_bases(id) initially deferred;
alter table pubchem.protein_bases add foreign key (organism) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.protein_alternatives add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_pdblinks add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_similarproteins add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_similarproteins add foreign key (simprotein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_genes add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_genes add foreign key (gene) references pubchem.gene_bases(id) initially deferred;
alter table pubchem.protein_enzymes add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_enzymes add foreign key (enzyme) references pubchem.enzyme_bases(id) initially deferred;
alter table pubchem.protein_uniprot_enzymes add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_ncbi_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_uniprot_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_mesh_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_glygen_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_glycosmos_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_alphafold_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_expasy_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_pharos_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_proconsortium_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_wormbase_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_brenda_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_intact_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_interpro_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_nextprot_matches add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_conserveddomains add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_conserveddomains add foreign key (domain) references pubchem.conserveddomain_bases(id) initially deferred;
alter table pubchem.protein_continuantparts add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_continuantparts add foreign key (part) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_families add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_interpro_families add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_types add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_references add foreign key (protein) references pubchem.protein_bases(id) initially deferred;
alter table pubchem.protein_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;


-- reference
alter table pubchem.reference_discusses add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_subjects add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_anzsrc_subjects add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_primary_subjects add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_content_types add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_issn_numbers add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_authors add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_authors add foreign key (author) references pubchem.author_bases(id) initially deferred;
alter table pubchem.reference_grants add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_grants add foreign key (grantid) references pubchem.grant_bases(id) initially deferred;
alter table pubchem.reference_organizations add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_organizations add foreign key (organization) references pubchem.organization_bases(id) initially deferred;
alter table pubchem.reference_journals add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_journals add foreign key (journal) references pubchem.journal_bases(id) initially deferred;
alter table pubchem.reference_books add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_books add foreign key (book) references pubchem.book_bases(id) initially deferred;
alter table pubchem.reference_isbn_books add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_issn_journals add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_mined_compounds add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_mined_compounds add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.reference_mined_diseases add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_mined_diseases add foreign key (disease) references pubchem.disease_bases(id) initially deferred;
alter table pubchem.reference_mined_genes add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_mined_genes add foreign key (gene_symbol) references pubchem.gene_symbol_bases(id) initially deferred;
alter table pubchem.reference_mined_enzymes add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_mined_enzymes add foreign key (enzyme) references pubchem.enzyme_bases(id) initially deferred;
alter table pubchem.reference_doi_identifiers add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_pubmed_identifiers add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.reference_sources add foreign key (reference) references pubchem.reference_bases(id) initially deferred;


-- source
alter table pubchem.source_subjects add foreign key (source) references pubchem.source_bases(id) initially deferred;
alter table pubchem.source_subjects add foreign key (subject) references pubchem.concept_bases(id) initially deferred;
alter table pubchem.source_alternatives add foreign key (source) references pubchem.source_bases(id) initially deferred;


-- substance
alter table pubchem.substance_bases add foreign key (source) references pubchem.source_bases(id) initially deferred;
alter table pubchem.substance_bases add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.substance_types add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_chembl_matches add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_glytoucan_matches add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_references add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.substance_pdblinks add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_synonyms add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
alter table pubchem.substance_synonyms add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;


-- synonym
alter table pubchem.synonym_values add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_types add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_compounds add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_compounds add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.synonym_mesh_subjects add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_concept_subjects add foreign key (synonym) references pubchem.synonym_bases(id) initially deferred;
alter table pubchem.synonym_concept_subjects add foreign key (concept) references pubchem.concept_bases(id) initially deferred;


-- taxonomy
alter table pubchem.taxonomy_alternatives add foreign key (taxonomy) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.taxonomy_references add foreign key (taxonomy) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.taxonomy_references add foreign key (reference) references pubchem.reference_bases(id) initially deferred;
alter table pubchem.taxonomy_matches add foreign key (taxonomy) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.taxonomy_mesh_matches add foreign key (taxonomy) references pubchem.taxonomy_bases(id) initially deferred;
alter table pubchem.taxonomy_catalogueoflife_matches add foreign key (taxonomy) references pubchem.taxonomy_bases(id) initially deferred;


-- descriptor-compound
alter table pubchem.descriptor_compound_bases add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_molecular_formulas add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_isomeric_smileses add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_canonical_smileses add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_iupac_inchis add foreign key (compound) references pubchem.compound_bases(id) initially deferred;
alter table pubchem.descriptor_compound_preferred_iupac_names add foreign key (compound) references pubchem.compound_bases(id) initially deferred;


-- descriptor-substance
alter table pubchem.descriptor_substance_bases add foreign key (substance) references pubchem.substance_bases(id) initially deferred;
