-- anatomy
alter table pubchem.anatomy_alternatives add foreign key (anatomy) references pubchem.anatomies(id) initially deferred;
alter table pubchem.anatomy_matches add foreign key (anatomy) references pubchem.anatomies(id) initially deferred;
alter table pubchem.anatomy_mesh_matches add foreign key (anatomy) references pubchem.anatomies(id) initially deferred;
alter table pubchem.anatomy_patents add foreign key (anatomy) references pubchem.anatomies(id) initially deferred;
alter table pubchem.anatomy_patents add foreign key (patent) references pubchem.patents(id) initially deferred;


-- author
alter table pubchem.author_given_names add foreign key (author) references pubchem.authors(id) initially deferred;
alter table pubchem.author_family_names add foreign key (author) references pubchem.authors(id) initially deferred;
alter table pubchem.author_formatted_names add foreign key (author) references pubchem.authors(id) initially deferred;
alter table pubchem.author_organizations add foreign key (author) references pubchem.authors(id) initially deferred;
alter table pubchem.author_orcids add foreign key (author) references pubchem.authors(id) initially deferred;


-- bioassay
alter table pubchem.bioassays add foreign key (source) references pubchem.sources(id) initially deferred;
alter table pubchem.bioassay_texts add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.bioassay_stages add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.bioassay_confirmatory_assays add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.bioassay_confirmatory_assays add foreign key (confirmatory_assay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.bioassay_primary_assays add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.bioassay_primary_assays add foreign key (primary_assay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.bioassay_summary_assays add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.bioassay_summary_assays add foreign key (summary_assay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.bioassay_chembl_assays add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.bioassay_chembl_mechanisms add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;


-- book
alter table pubchem.book_authors add foreign key (book) references pubchem.books(id) initially deferred;
alter table pubchem.book_authors add foreign key (author) references pubchem.authors(id) initially deferred;


-- cell
alter table pubchem.cells add foreign key (organism) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.cell_alternatives add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.cell_occurrences add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.cell_references add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.cell_references add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.cell_matches add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.cell_mesh_matches add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.cell_wikidata_matches add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.cell_cellosaurus_matches add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.cell_chembl_card_matches add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.cell_anatomies add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.cell_anatomies add foreign key (anatomy) references pubchem.anatomies(id) initially deferred;


-- compound
alter table pubchem.compound_components add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_components add foreign key (component) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_isotopologues add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_isotopologues add foreign key (isotopologue) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_parents add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_parents add foreign key (parent) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_stereoisomers add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_stereoisomers add foreign key (isomer) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_same_connectivities add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_same_connectivities add foreign key (isomer) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_roles add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_types add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_active_ingredients add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_labels add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_matches add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_wikidata_matches add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_molfiles add foreign key (compound) references pubchem.compounds(id) initially deferred;


-- concept
alter table pubchem.concepts add foreign key (scheme) references pubchem.concepts(id) initially deferred;
alter table pubchem.concepts add foreign key (broader) references pubchem.concepts(id) initially deferred;


-- conserveddomain
alter table pubchem.conserveddomain_references add foreign key (conserveddomain) references pubchem.conserveddomains(id) initially deferred;
alter table pubchem.conserveddomain_references add foreign key (reference) references pubchem.references(id) initially deferred;


-- cooccurrences
alter table pubchem.compound_compound_cooccurrences add foreign key (subject) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_compound_cooccurrences add foreign key (object) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_disease_cooccurrences add foreign key (subject) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_disease_cooccurrences add foreign key (object) references pubchem.diseases(id) initially deferred;
alter table pubchem.compound_genesymbol_cooccurrences add foreign key (subject) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_genesymbol_cooccurrences add foreign key (object) references pubchem.genesymbols(id) initially deferred;
alter table pubchem.compound_enzyme_cooccurrences add foreign key (subject) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_enzyme_cooccurrences add foreign key (object) references pubchem.enzymes(id) initially deferred;
alter table pubchem.disease_compound_cooccurrences add foreign key (subject) references pubchem.diseases(id) initially deferred;
alter table pubchem.disease_compound_cooccurrences add foreign key (object) references pubchem.compounds(id) initially deferred;
alter table pubchem.disease_disease_cooccurrences add foreign key (subject) references pubchem.diseases(id) initially deferred;
alter table pubchem.disease_disease_cooccurrences add foreign key (object) references pubchem.diseases(id) initially deferred;
alter table pubchem.disease_genesymbol_cooccurrences add foreign key (subject) references pubchem.diseases(id) initially deferred;
alter table pubchem.disease_genesymbol_cooccurrences add foreign key (object) references pubchem.genesymbols(id) initially deferred;
alter table pubchem.disease_enzyme_cooccurrences add foreign key (subject) references pubchem.diseases(id) initially deferred;
alter table pubchem.disease_enzyme_cooccurrences add foreign key (object) references pubchem.enzymes(id) initially deferred;
alter table pubchem.genesymbol_compound_cooccurrences add foreign key (subject) references pubchem.genesymbols(id) initially deferred;
alter table pubchem.genesymbol_compound_cooccurrences add foreign key (object) references pubchem.compounds(id) initially deferred;
alter table pubchem.enzyme_compound_cooccurrences add foreign key (subject) references pubchem.enzymes(id) initially deferred;
alter table pubchem.enzyme_compound_cooccurrences add foreign key (object) references pubchem.compounds(id) initially deferred;
alter table pubchem.genesymbol_disease_cooccurrences add foreign key (subject) references pubchem.genesymbols(id) initially deferred;
alter table pubchem.genesymbol_disease_cooccurrences add foreign key (object) references pubchem.diseases(id) initially deferred;
alter table pubchem.enzyme_disease_cooccurrences add foreign key (subject) references pubchem.enzymes(id) initially deferred;
alter table pubchem.enzyme_disease_cooccurrences add foreign key (object) references pubchem.diseases(id) initially deferred;
alter table pubchem.genesymbol_genesymbol_cooccurrences add foreign key (subject) references pubchem.genesymbols(id) initially deferred;
alter table pubchem.genesymbol_genesymbol_cooccurrences add foreign key (object) references pubchem.genesymbols(id) initially deferred;


-- disease
alter table pubchem.disease_alternatives add foreign key (disease) references pubchem.diseases(id) initially deferred;
alter table pubchem.disease_matches add foreign key (disease) references pubchem.diseases(id) initially deferred;
alter table pubchem.disease_mesh_matches add foreign key (disease) references pubchem.diseases(id) initially deferred;
alter table pubchem.disease_related_matches add foreign key (disease) references pubchem.diseases(id) initially deferred;


-- endpoint
alter table pubchem.endpoints add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.endpoints add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.endpoints add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;
alter table pubchem.endpoints add foreign key (bioassay, measuregroup, substance) references pubchem.measuregroup_substances(bioassay, measuregroup, substance) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (bioassay, measuregroup, substance) references pubchem.measuregroup_substances(bioassay, measuregroup, substance) initially deferred;
alter table pubchem.endpoint_measurements add foreign key (bioassay, measuregroup, substance, value) references pubchem.endpoints(bioassay, measuregroup, substance, value) initially deferred;
alter table pubchem.endpoint_references add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.endpoint_references add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.endpoint_references add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_references add foreign key (substance, bioassay, measuregroup, value) references pubchem.endpoints(substance, bioassay, measuregroup, value) initially deferred;
alter table pubchem.endpoint_references add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.endpoint_patents add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.endpoint_patents add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.endpoint_patents add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;
alter table pubchem.endpoint_patents add foreign key (substance, bioassay, measuregroup, value) references pubchem.endpoints(substance, bioassay, measuregroup, value) initially deferred;
alter table pubchem.endpoint_patents add foreign key (patent) references pubchem.patents(id) initially deferred;


-- gene
alter table pubchem.genes add foreign key (genesymbol) references pubchem.genesymbols(id) initially deferred;
alter table pubchem.genes add foreign key (organism) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.gene_alternatives add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_references add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_references add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.gene_patents add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_patents add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.gene_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_ensembl_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_mesh_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_expasy_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_medlineplus_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_alliancegenome_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_kegg_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_pharos_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_bgee_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_pombase_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_veupathdb_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_zfin_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_enzyme_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_wikidata_matches add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_processes add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_functions add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_locations add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_orthologs add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.gene_orthologs add foreign key (ortholog) references pubchem.genes(id) initially deferred;


-- grant
alter table pubchem.grants add foreign key (organization) references pubchem.organizations(id) initially deferred;


-- inchikey
alter table pubchem.inchikey_compounds add foreign key (inchikey) references pubchem.inchikeys(id) initially deferred;
alter table pubchem.inchikey_compounds add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.inchikey_subjects add foreign key (inchikey) references pubchem.inchikeys(id) initially deferred;


-- journal


-- measuregroup
alter table pubchem.measuregroups add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.measuregroups add foreign key (source) references pubchem.sources(id) initially deferred;
alter table pubchem.measuregroup_substances add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.measuregroup_substances add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_substances add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.measuregroup_proteins add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.measuregroup_proteins add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_proteins add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.measuregroup_genes add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.measuregroup_genes add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_genes add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.measuregroup_taxonomies add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.measuregroup_taxonomies add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_taxonomies add foreign key (taxonomy) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.measuregroup_cells add foreign key (bioassay) references pubchem.bioassays(id) initially deferred;
alter table pubchem.measuregroup_cells add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;
alter table pubchem.measuregroup_cells add foreign key (cell) references pubchem.cells(id) initially deferred;
alter table pubchem.measuregroup_anatomies add foreign key (bioassay, measuregroup) references pubchem.measuregroups(bioassay, measuregroup) initially deferred;


-- organization
alter table pubchem.organization_country_names add foreign key (organization) references pubchem.organizations(id) initially deferred;
alter table pubchem.organization_formatted_names add foreign key (organization) references pubchem.organizations(id) initially deferred;
alter table pubchem.organization_crossref_matches add foreign key (organization) references pubchem.organizations(id) initially deferred;


-- patent
alter table pubchem.patent_cpc_additional_classifications add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.patent_cpc_inventive_classifications add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.patent_ipc_additional_classifications add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.patent_ipc_inventive_classifications add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.patent_cpc_additional_classifications add foreign key (classification) references pubchem.patentcpcs(id) initially deferred;
alter table pubchem.patent_cpc_inventive_classifications add foreign key (classification) references pubchem.patentcpcs(id) initially deferred;
alter table pubchem.patent_ipc_additional_classifications add foreign key (classification) references pubchem.patentipcs(id) initially deferred;
alter table pubchem.patent_ipc_inventive_classifications add foreign key (classification) references pubchem.patentipcs(id) initially deferred;
alter table pubchem.patent_citations add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.patent_citations add foreign key (citation) references pubchem.patents(id) initially deferred;
alter table pubchem.patent_inventors add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.patent_inventors add foreign key (inventor) references pubchem.inventors(id) initially deferred;
alter table pubchem.patent_applicants add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.patent_applicants add foreign key (applicant) references pubchem.applicants(id) initially deferred;


-- patentcpc
alter table pubchem.patentcpcs add foreign key (concordant_ipc) references pubchem.patentipcs(id) initially deferred;
alter table pubchem.patentcpc_broaders add foreign key (patentcpc) references pubchem.patentcpcs(id) initially deferred;
alter table pubchem.patentcpc_broaders add foreign key (broader) references pubchem.patentcpcs(id) initially deferred;
alter table pubchem.patentcpc_modified_dates add foreign key (patentcpc) references pubchem.patentcpcs(id) initially deferred;


-- patentipc
alter table pubchem.patentipcs add foreign key (broader) references pubchem.patentipcs(id) initially deferred;
alter table pubchem.patentipc_titles add foreign key (patentipc) references pubchem.patentipcs(id) initially deferred;


-- pathway
alter table pubchem.pathways add foreign key (source) references pubchem.sources(id) initially deferred;
alter table pubchem.pathways add foreign key (organism) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.pathway_compounds add foreign key (pathway) references pubchem.pathways(id) initially deferred;
alter table pubchem.pathway_compounds add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.pathway_proteins add foreign key (pathway) references pubchem.pathways(id) initially deferred;
alter table pubchem.pathway_proteins add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.pathway_genes add foreign key (pathway) references pubchem.pathways(id) initially deferred;
alter table pubchem.pathway_genes add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.pathway_components add foreign key (pathway) references pubchem.pathways(id) initially deferred;
alter table pubchem.pathway_components add foreign key (component) references pubchem.pathways(id) initially deferred;
alter table pubchem.pathway_related_pathways add foreign key (pathway) references pubchem.pathways(id) initially deferred;
alter table pubchem.pathway_related_pathways add foreign key (related) references pubchem.pathways(id) initially deferred;
alter table pubchem.pathway_references add foreign key (pathway) references pubchem.pathways(id) initially deferred;
alter table pubchem.pathway_references add foreign key (reference) references pubchem.references(id) initially deferred;


-- protein
alter table pubchem.enzymes add foreign key (parent) references pubchem.enzymes(id) initially deferred;
alter table pubchem.enzyme_alternatives add foreign key (enzyme) references pubchem.enzymes(id) initially deferred;
alter table pubchem.proteins add foreign key (organism) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.protein_alternatives add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_pdblinks add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_similar_proteins add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_similar_proteins add foreign key (similar_protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_genes add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_genes add foreign key (gene) references pubchem.genes(id) initially deferred;
alter table pubchem.protein_enzymes add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_enzymes add foreign key (enzyme) references pubchem.enzymes(id) initially deferred;
alter table pubchem.protein_uniprot_enzymes add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_ncbi_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_uniprot_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_mesh_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_glygen_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_glycosmos_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_alphafold_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_pharos_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_proconsortium_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_wormbase_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_brenda_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_intact_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_interpro_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_nextprot_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_stringdb_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_enzymedatabase_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_chembl_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_wikidata_matches add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_conserveddomains add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_conserveddomains add foreign key (conserveddomain) references pubchem.conserveddomains(id) initially deferred;
alter table pubchem.protein_continuant_parts add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_continuant_parts add foreign key (part) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_families add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_interpro_families add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_types add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_references add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_references add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.protein_patents add foreign key (protein) references pubchem.proteins(id) initially deferred;
alter table pubchem.protein_patents add foreign key (patent) references pubchem.patents(id) initially deferred;


-- reference
alter table pubchem.reference_discussed_headings add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_subjects add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_anzsrc_subjects add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_primary_subjects add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_content_types add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_issn_numbers add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_isbn_numbers add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_authors add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_authors add foreign key (author) references pubchem.authors(id) initially deferred;
alter table pubchem.reference_grants add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_grants add foreign key (supporting_grant) references pubchem.grants(id) initially deferred;
alter table pubchem.reference_organizations add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_organizations add foreign key (organization) references pubchem.organizations(id) initially deferred;
alter table pubchem.reference_journals add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_journals add foreign key (journal) references pubchem.journals(id) initially deferred;
alter table pubchem.reference_books add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_books add foreign key (book) references pubchem.books(id) initially deferred;
alter table pubchem.reference_isbn_books add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_issn_journals add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_mined_compounds add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_mined_compounds add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.reference_mined_diseases add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_mined_diseases add foreign key (disease) references pubchem.diseases(id) initially deferred;
alter table pubchem.reference_mined_genesymbols add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_mined_genesymbols add foreign key (genesymbol) references pubchem.genesymbols(id) initially deferred;
alter table pubchem.reference_mined_enzymes add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_mined_enzymes add foreign key (enzyme) references pubchem.enzymes(id) initially deferred;
alter table pubchem.reference_identifiers add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.reference_source_types add foreign key (reference) references pubchem.references(id) initially deferred;


-- source
alter table pubchem.source_subjects add foreign key (source) references pubchem.sources(id) initially deferred;
alter table pubchem.source_subjects add foreign key (subject) references pubchem.concepts(id) initially deferred;
alter table pubchem.source_alternatives add foreign key (source) references pubchem.sources(id) initially deferred;


-- substance
alter table pubchem.substances add foreign key (source) references pubchem.sources(id) initially deferred;
alter table pubchem.substances add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.substance_types add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.substance_chembl_matches add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.substance_glytoucan_matches add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.substance_references add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.substance_references add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.substance_patents add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.substance_patents add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.substance_pdblinks add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.substance_synonyms add foreign key (substance) references pubchem.substances(id) initially deferred;
alter table pubchem.substance_synonyms add foreign key (synonym) references pubchem.synonyms(id) initially deferred;


-- synonym
alter table pubchem.synonym_values add foreign key (synonym) references pubchem.synonyms(id) initially deferred;
alter table pubchem.synonym_types add foreign key (synonym) references pubchem.synonyms(id) initially deferred;
alter table pubchem.synonym_compounds add foreign key (synonym) references pubchem.synonyms(id) initially deferred;
alter table pubchem.synonym_compounds add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.synonym_mesh_subjects add foreign key (synonym) references pubchem.synonyms(id) initially deferred;
alter table pubchem.synonym_concept_subjects add foreign key (synonym) references pubchem.synonyms(id) initially deferred;
alter table pubchem.synonym_concept_subjects add foreign key (concept) references pubchem.concepts(id) initially deferred;


-- taxonomy
alter table pubchem.taxonomy_alternatives add foreign key (taxonomy) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.taxonomy_references add foreign key (taxonomy) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.taxonomy_references add foreign key (reference) references pubchem.references(id) initially deferred;
alter table pubchem.taxonomy_patents add foreign key (taxonomy) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.taxonomy_patents add foreign key (patent) references pubchem.patents(id) initially deferred;
alter table pubchem.taxonomy_matches add foreign key (taxonomy) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.taxonomy_mesh_matches add foreign key (taxonomy) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.taxonomy_catalogueoflife_matches add foreign key (taxonomy) references pubchem.taxonomies(id) initially deferred;
alter table pubchem.taxonomy_wikidata_matches add foreign key (taxonomy) references pubchem.taxonomies(id) initially deferred;


-- descriptor-compound
alter table pubchem.compound_descriptors add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_molecular_formulas add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_smileses add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_connectivity_smileses add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_iupac_inchis add foreign key (compound) references pubchem.compounds(id) initially deferred;
alter table pubchem.compound_preferred_iupac_names add foreign key (compound) references pubchem.compounds(id) initially deferred;


-- descriptor-substance
alter table pubchem.substance_versions add foreign key (substance) references pubchem.substances(id) initially deferred;
