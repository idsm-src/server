create index protein_bases__organism on pubchem.protein_bases(organism_id);
create index protein_bases__title on pubchem.protein_bases(title);
create index protein_bases__title__english on pubchem.protein_bases using gin (to_tsvector('english', title));
grant select on pubchem.protein_bases to sparql;

--------------------------------------------------------------------------------

create index protein_alternatives__protein on pubchem.protein_alternatives(protein);
create index protein_alternatives__alternative on pubchem.protein_alternatives(alternative);
grant select on pubchem.protein_alternatives to sparql;

--------------------------------------------------------------------------------

create index protein_references__protein on pubchem.protein_references(protein);
create index protein_references__reference on pubchem.protein_references(reference);
grant select on pubchem.protein_references to sparql;

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

create index protein_thesaurus_matches__protein on pubchem.protein_thesaurus_matches(protein);
create index protein_thesaurus_matches__match on pubchem.protein_thesaurus_matches(match);
grant select on pubchem.protein_thesaurus_matches to sparql;

--------------------------------------------------------------------------------

create index protein_expasy_matches__protein on pubchem.protein_expasy_matches(protein);
create index protein_expasy_matches__match on pubchem.protein_expasy_matches(match);
grant select on pubchem.protein_expasy_matches to sparql;

--------------------------------------------------------------------------------

create index protein_guidetopharmacology_matches__protein on pubchem.protein_guidetopharmacology_matches(protein);
create index protein_guidetopharmacology_matches__match on pubchem.protein_guidetopharmacology_matches(match);
grant select on pubchem.protein_guidetopharmacology_matches to sparql;

--------------------------------------------------------------------------------

create index protein_drugbank_matches__protein on pubchem.protein_drugbank_matches(protein);
create index protein_drugbank_matches__match on pubchem.protein_drugbank_matches(match);
grant select on pubchem.protein_drugbank_matches to sparql;

--------------------------------------------------------------------------------

create index protein_chembl_matches__protein on pubchem.protein_chembl_matches(protein);
create index protein_chembl_matches__match on pubchem.protein_chembl_matches(match);
grant select on pubchem.protein_chembl_matches to sparql;

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

create index protein_types__protein on pubchem.protein_types(protein);
create index protein_types__type on pubchem.protein_types(type_unit, type_id);
grant select on pubchem.protein_types to sparql;

--------------------------------------------------------------------------------

grant select on pubchem.protein_complexes to sparql;
