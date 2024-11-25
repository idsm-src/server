create index enzyme_bases__parent on pubchem.enzyme_bases(parent);
create index enzyme_bases__title on pubchem.enzyme_bases(title);
create index enzyme_bases__title__english on pubchem.enzyme_bases using gin (to_tsvector('english', title));
grant select on pubchem.enzyme_bases to sparql;

--------------------------------------------------------------------------------

create index enzyme_alternatives__enzyme on pubchem.enzyme_alternatives(enzyme);
create index enzyme_alternatives__alternative on pubchem.enzyme_alternatives(alternative);
grant select on pubchem.enzyme_alternatives to sparql;

--------------------------------------------------------------------------------

create index protein_bases__organism on pubchem.protein_bases(organism);
create index protein_bases__title on pubchem.protein_bases(title);
create index protein_bases__title__english on pubchem.protein_bases using gin (to_tsvector('english', title));
grant select on pubchem.protein_bases to sparql;

--------------------------------------------------------------------------------

create index protein_alternatives__protein on pubchem.protein_alternatives(protein);
create index protein_alternatives__alternative on pubchem.protein_alternatives(alternative);
grant select on pubchem.protein_alternatives to sparql;

--------------------------------------------------------------------------------

create index protein_pdblinks__protein on pubchem.protein_pdblinks(protein);
create index protein_pdblinks__pdblink on pubchem.protein_pdblinks(pdblink);
grant select on pubchem.protein_pdblinks to sparql;

--------------------------------------------------------------------------------

create index protein_similarproteins__protein on pubchem.protein_similarproteins(protein);
create index protein_similarproteins__simprotein on pubchem.protein_similarproteins(simprotein);
grant select on pubchem.protein_similarproteins to sparql;

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
create index protein_matches__match on pubchem.protein_matches(match_unit, match_id);
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

create index protein_expasy_matches__protein on pubchem.protein_expasy_matches(protein);
create index protein_expasy_matches__match on pubchem.protein_expasy_matches(match);
grant select on pubchem.protein_expasy_matches to sparql;

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

create index protein_conserveddomains__protein on pubchem.protein_conserveddomains(protein);
create index protein_conserveddomains__domain on pubchem.protein_conserveddomains(domain);
grant select on pubchem.protein_conserveddomains to sparql;

--------------------------------------------------------------------------------

create index protein_continuantparts__protein on pubchem.protein_continuantparts(protein);
create index protein_continuantparts__part on pubchem.protein_continuantparts(part);
grant select on pubchem.protein_continuantparts to sparql;

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
create index protein_types__type on pubchem.protein_types(type_unit, type_id);
grant select on pubchem.protein_types to sparql;

--------------------------------------------------------------------------------

create index protein_references__protein on pubchem.protein_references(protein);
create index protein_references__reference on pubchem.protein_references(reference);
grant select on pubchem.protein_references to sparql;
