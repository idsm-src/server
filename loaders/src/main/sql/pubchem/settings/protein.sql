create index enzymes__parent on pubchem.enzymes(parent);
create index enzymes__title on pubchem.enzymes(title);
create index enzymes__title__english on pubchem.enzymes using gin (to_tsvector('english', title));
grant select on pubchem.enzymes to sparql;

--------------------------------------------------------------------------------

create index enzyme_alternatives__enzyme on pubchem.enzyme_alternatives(enzyme);
create index enzyme_alternatives__alternative on pubchem.enzyme_alternatives(alternative);
grant select on pubchem.enzyme_alternatives to sparql;

--------------------------------------------------------------------------------

create index proteins__organism on pubchem.proteins(organism);
create index proteins__title on pubchem.proteins(title);
create index proteins__title__english on pubchem.proteins using gin (to_tsvector('english', title));
grant select on pubchem.proteins to sparql;

--------------------------------------------------------------------------------

create index protein_alternatives__protein on pubchem.protein_alternatives(protein);
create index protein_alternatives__alternative on pubchem.protein_alternatives(alternative);
grant select on pubchem.protein_alternatives to sparql;

--------------------------------------------------------------------------------

create index protein_pdblinks__protein on pubchem.protein_pdblinks(protein);
create index protein_pdblinks__pdblink on pubchem.protein_pdblinks(pdblink);
grant select on pubchem.protein_pdblinks to sparql;

--------------------------------------------------------------------------------

create index protein_similar_proteins__protein on pubchem.protein_similar_proteins(protein);
create index protein_similar_proteins__similar_protein on pubchem.protein_similar_proteins(similar_protein);
grant select on pubchem.protein_similar_proteins to sparql;

--------------------------------------------------------------------------------

create index protein_genes__protein on pubchem.protein_genes(protein);
create index protein_genes__gene on pubchem.protein_genes(gene);
grant select on pubchem.protein_genes to sparql;

--------------------------------------------------------------------------------

create index protein_enzymes__protein on pubchem.protein_enzymes(protein);
create index protein_enzymes__enzyme on pubchem.protein_enzymes(enzyme);
grant select on pubchem.protein_enzymes to sparql;

--------------------------------------------------------------------------------

create index protein_uniprot_enzymes__protein on pubchem.protein_uniprot_enzymes(protein);
create index protein_uniprot_enzymes__enzyme on pubchem.protein_uniprot_enzymes(enzyme);
grant select on pubchem.protein_uniprot_enzymes to sparql;

--------------------------------------------------------------------------------

create index protein_matches__protein on pubchem.protein_matches(protein);
create index protein_matches__match_unit_match_id on pubchem.protein_matches(match_unit, match_id);
grant select on pubchem.protein_matches to sparql;

--------------------------------------------------------------------------------

create index protein_ncbi_matches__protein on pubchem.protein_ncbi_matches(protein);
create index protein_ncbi_matches__match on pubchem.protein_ncbi_matches(match);
grant select on pubchem.protein_ncbi_matches to sparql;

--------------------------------------------------------------------------------

create index protein_uniprot_matches__protein on pubchem.protein_uniprot_matches(protein);
create index protein_uniprot_matches__match on pubchem.protein_uniprot_matches(match);
grant select on pubchem.protein_uniprot_matches to sparql;

--------------------------------------------------------------------------------

create index protein_mesh_matches__protein on pubchem.protein_mesh_matches(protein);
create index protein_mesh_matches__match on pubchem.protein_mesh_matches(match);
grant select on pubchem.protein_mesh_matches to sparql;

--------------------------------------------------------------------------------

create index protein_glygen_matches__protein on pubchem.protein_glygen_matches(protein);
create index protein_glygen_matches__match on pubchem.protein_glygen_matches(match);
grant select on pubchem.protein_glygen_matches to sparql;

--------------------------------------------------------------------------------

create index protein_glycosmos_matches__protein on pubchem.protein_glycosmos_matches(protein);
create index protein_glycosmos_matches__match on pubchem.protein_glycosmos_matches(match);
grant select on pubchem.protein_glycosmos_matches to sparql;

--------------------------------------------------------------------------------

create index protein_alphafold_matches__protein on pubchem.protein_alphafold_matches(protein);
create index protein_alphafold_matches__match on pubchem.protein_alphafold_matches(match);
grant select on pubchem.protein_alphafold_matches to sparql;

--------------------------------------------------------------------------------

create index protein_pharos_matches__protein on pubchem.protein_pharos_matches(protein);
create index protein_pharos_matches__match on pubchem.protein_pharos_matches(match);
grant select on pubchem.protein_pharos_matches to sparql;

--------------------------------------------------------------------------------

create index protein_proconsortium_matches__protein on pubchem.protein_proconsortium_matches(protein);
create index protein_proconsortium_matches__match on pubchem.protein_proconsortium_matches(match);
grant select on pubchem.protein_proconsortium_matches to sparql;

--------------------------------------------------------------------------------

create index protein_wormbase_matches__protein on pubchem.protein_wormbase_matches(protein);
create index protein_wormbase_matches__match on pubchem.protein_wormbase_matches(match);
grant select on pubchem.protein_wormbase_matches to sparql;

--------------------------------------------------------------------------------

create index protein_brenda_matches__protein on pubchem.protein_brenda_matches(protein);
create index protein_brenda_matches__match on pubchem.protein_brenda_matches(match);
grant select on pubchem.protein_brenda_matches to sparql;

--------------------------------------------------------------------------------

create index protein_intact_matches__protein on pubchem.protein_intact_matches(protein);
create index protein_intact_matches__match on pubchem.protein_intact_matches(match);
grant select on pubchem.protein_intact_matches to sparql;

--------------------------------------------------------------------------------

create index protein_interpro_matches__protein on pubchem.protein_interpro_matches(protein);
create index protein_interpro_matches__match on pubchem.protein_interpro_matches(match);
grant select on pubchem.protein_interpro_matches to sparql;

--------------------------------------------------------------------------------

create index protein_nextprot_matches__protein on pubchem.protein_nextprot_matches(protein);
create index protein_nextprot_matches__match on pubchem.protein_nextprot_matches(match);
grant select on pubchem.protein_nextprot_matches to sparql;

--------------------------------------------------------------------------------

create index protein_stringdb_matches__protein on pubchem.protein_stringdb_matches(protein);
create index protein_stringdb_matches__match on pubchem.protein_stringdb_matches(match);
grant select on pubchem.protein_stringdb_matches to sparql;

--------------------------------------------------------------------------------

create index protein_enzymedatabase_matches__protein on pubchem.protein_enzymedatabase_matches(protein);
create index protein_enzymedatabase_matches__match on pubchem.protein_enzymedatabase_matches(match);
grant select on pubchem.protein_enzymedatabase_matches to sparql;

--------------------------------------------------------------------------------

create index protein_chembl_matches__protein on pubchem.protein_chembl_matches(protein);
create index protein_chembl_matches__match on pubchem.protein_chembl_matches(match);
grant select on pubchem.protein_chembl_matches to sparql;

--------------------------------------------------------------------------------

create index protein_wikidata_matches__protein on pubchem.protein_wikidata_matches(protein);
create index protein_wikidata_matches__match on pubchem.protein_wikidata_matches(match);
grant select on pubchem.protein_wikidata_matches to sparql;

--------------------------------------------------------------------------------

create index protein_conserveddomains__protein on pubchem.protein_conserveddomains(protein);
create index protein_conserveddomains__conserveddomain on pubchem.protein_conserveddomains(conserveddomain);
grant select on pubchem.protein_conserveddomains to sparql;

--------------------------------------------------------------------------------

create index protein_continuant_parts__protein on pubchem.protein_continuant_parts(protein);
create index protein_continuant_parts__part on pubchem.protein_continuant_parts(part);
grant select on pubchem.protein_continuant_parts to sparql;

--------------------------------------------------------------------------------

create index protein_families__protein on pubchem.protein_families(protein);
create index protein_families__family on pubchem.protein_families(family);
grant select on pubchem.protein_families to sparql;

--------------------------------------------------------------------------------

create index protein_interpro_families__protein on pubchem.protein_interpro_families(protein);
create index protein_interpro_families__family on pubchem.protein_interpro_families(family);
grant select on pubchem.protein_interpro_families to sparql;

--------------------------------------------------------------------------------

create index protein_types__protein on pubchem.protein_types(protein);
create index protein_types__type_unit_type_id on pubchem.protein_types(type_unit, type_id);
grant select on pubchem.protein_types to sparql;

--------------------------------------------------------------------------------

create index protein_references__protein on pubchem.protein_references(protein);
create index protein_references__reference on pubchem.protein_references(reference);
grant select on pubchem.protein_references to sparql;

--------------------------------------------------------------------------------

create index protein_patents__protein on pubchem.protein_patents(protein);
create index protein_patents__patent on pubchem.protein_patents(patent);
grant select on pubchem.protein_patents to sparql;
