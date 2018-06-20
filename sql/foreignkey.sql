-- bioassay
alter table bioassay_bases add foreign key (source) references  source_bases(id);
alter table bioassay_data add foreign key (bioassay) references  bioassay_bases(id);


-- biosystem
alter table biosystem_bases add foreign key (source) references  source_bases(id);
alter table biosystem_components add foreign key (biosystem) references  biosystem_bases(id);
alter table biosystem_references add foreign key (biosystem) references  biosystem_bases(id);
alter table biosystem_references add foreign key (reference) references reference_bases(id);
alter table biosystem_matches add foreign key (biosystem) references  biosystem_bases(id);
--  168257 : alter table biosystem_components add foreign key (component) references  biosystem_bases(id);


-- compound
alter table compound_relations add foreign key (compound_from) references compound_bases(id);
alter table compound_relations add foreign key (compound_to) references compound_bases(id);
alter table compound_relations add foreign key (relation) references compound_relations__reftable(id);
alter table compound_roles add foreign key (compound) references compound_bases(id);
alter table compound_roles add foreign key (roleid) references compound_roles__reftable(id);
alter table compound_biosystems add foreign key (compound) references compound_bases(id);
alter table compound_biosystems add foreign key (biosystem) references biosystem_bases(id);
alter table compound_types add foreign key (compound) references compound_bases(id);
alter table compound_types add foreign key (unit) references compound_type_units__reftable(id);
alter table compound_active_ingredients add foreign key (compound) references compound_bases(id);
alter table compound_active_ingredients add foreign key (unit) references compound_type_units__reftable(id);


-- concept
alter table concept_bases add foreign key (scheme) references concept_bases(id);
alter table concept_bases add foreign key (broader) references concept_bases(id);


-- conserveddomain
alter table conserveddomain_references add foreign key (domain) references  conserveddomain_bases(id);
alter table conserveddomain_references add foreign key (reference) references reference_bases(id);


-- endpoint
alter table endpoint_bases add foreign key (outcome) references endpoint_outcomes__reftable(id);
alter table endpoint_measurements add foreign key (substance, bioassay, measuregroup) references endpoint_bases(substance, bioassay, measuregroup);
alter table endpoint_references add foreign key (reference) references reference_bases(id);
--  394284 : alter table endpoint_bases add foreign key (substance) references substance_bases(id);
--     276 : alter table endpoint_bases add foreign key (bioassay, measuregroup) references measuregroup_bases(bioassay, measuregroup);
--    1497 : alter table endpoint_references add foreign key (substance, bioassay, measuregroup) references endpoint_bases(substance, bioassay, measuregroup);


-- gene
alter table gene_biosystems add foreign key (gene) references gene_bases(id);
alter table gene_biosystems add foreign key (biosystem) references biosystem_bases(id);
alter table gene_alternatives add foreign key (gene) references gene_bases(id);
alter table gene_references add foreign key (gene) references gene_bases(id);
alter table gene_references add foreign key (reference) references reference_bases(id);
alter table gene_matches add foreign key (gene) references gene_bases(id);


-- inchikey
alter table inchikey_compounds add foreign key (inchikey) references inchikey_bases(id);
alter table inchikey_subjects add foreign key (inchikey) references inchikey_bases(id);
--       9 : alter table inchikey_compounds add foreign key (compound) references compound_bases(id);


-- measuregroup
alter table measuregroup_bases add foreign key (bioassay) references bioassay_bases(id);
alter table measuregroup_bases add foreign key (source) references source_bases(id);
alter table measuregroup_proteins add foreign key (protein) references protein_bases(id);
alter table measuregroup_genes add foreign key (gene) references gene_bases(id);
--      82 : alter table measuregroup_proteins add foreign key (bioassay, measuregroup) references measuregroup_bases(bioassay, measuregroup);
--       1 : alter table measuregroup_genes add foreign key (bioassay, measuregroup) references measuregroup_bases(bioassay, measuregroup);


-- protein
alter table protein_references add foreign key (protein) references protein_bases(id);
alter table protein_references add foreign key (reference) references reference_bases(id);
alter table protein_pdblinks add foreign key (protein) references protein_bases(id);
alter table protein_similarproteins add foreign key (protein) references protein_bases(id);
alter table protein_similarproteins add foreign key (simprotein) references protein_bases(id);
alter table protein_genes add foreign key (protein) references protein_bases(id);
alter table protein_genes add foreign key (gene) references gene_bases(id);
alter table protein_closematches add foreign key (protein) references protein_bases(id);
alter table protein_conserveddomains add foreign key (protein) references protein_bases(id);
alter table protein_conserveddomains add foreign key (domain) references conserveddomain_bases(id);
alter table protein_continuantparts add foreign key (protein) references protein_bases(id);
alter table protein_continuantparts add foreign key (part) references protein_bases(id);
alter table protein_participates_goes add foreign key (protein) references protein_bases(id);
alter table protein_participates_biosystems add foreign key (protein) references protein_bases(id);
alter table protein_participates_biosystems add foreign key (biosystem) references biosystem_bases(id);
alter table protein_functions add foreign key (protein) references protein_bases(id);
alter table protein_locations add foreign key (protein) references protein_bases(id);
alter table protein_types add foreign key (protein) references protein_bases(id);
alter table protein_complexes add foreign key (protein) references protein_bases(id);


-- reference
alter table reference_bases add foreign key (type) references reference_types__reftable(id);
alter table reference_discusses add foreign key (reference) references reference_bases(id);
alter table reference_subject_descriptors add foreign key (reference) references reference_bases(id);


-- source
alter table source_subjects add foreign key (source) references source_bases(id);
alter table source_subjects add foreign key (subject) references source_subjects__reftable(id);
alter table source_alternatives add foreign key (source) references source_bases(id);


-- substance
alter table substance_bases add foreign key (source) references source_bases(id);
alter table substance_references add foreign key (reference) references reference_bases(id);
alter table substance_synonyms add foreign key (synonym) references synonym_bases(id);
--       9 : alter table substance_bases add foreign key (compound) references compound_bases(id);
--  376791 : alter table substance_types add foreign key (substance) references substance_bases(id);
--    8459 : alter table substance_matches add foreign key (substance) references substance_bases(id);
--   15262 : alter table substance_references add foreign key (substance) references substance_bases(id);
--    1059 : alter table substance_pdblinks add foreign key (substance) references substance_bases(id);
-- 3129740 : alter table substance_synonyms add foreign key (substance) references substance_bases(id);


-- synonym
alter table synonym_values add foreign key (synonym) references synonym_bases(id);
alter table synonym_types add foreign key (synonym) references synonym_bases(id);
alter table synonym_compounds add foreign key (synonym) references synonym_bases(id);
alter table synonym_mesh_subjects add foreign key (synonym) references synonym_bases(id);
alter table synonym_concept_subjects add foreign key (synonym) references synonym_bases(id);
alter table synonym_concept_subjects add foreign key (concept) references concept_bases(id);
--        9 : alter table synonym_compounds add foreign key (compound) references compound_bases(id);


-- descriptor-compound
--        9 : alter table descriptor_compound_bases add foreign key (compound) references compound_bases(id);
--        9 : alter table descriptor_compound_molecular_formulas add foreign key (compound) references compound_bases(id);
--        9 : alter table descriptor_compound_isomeric_smileses add foreign key (compound) references compound_bases(id);
--        9 : alter table descriptor_compound_canonical_smileses add foreign key (compound) references compound_bases(id);
--        9 : alter table descriptor_compound_iupac_inchis add foreign key (compound) references compound_bases(id);
--        9 : alter table descriptor_compound_preferred_iupac_names add foreign key (compound) references compound_bases(id);


-- descriptor-substance
--  8925739 : alter table descriptor_substance_bases add foreign key (substance) references substance_bases(id);
