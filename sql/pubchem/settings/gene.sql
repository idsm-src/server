create index gene_bases__symbol on pubchem.gene_bases(symbol);
create index gene_bases__title on pubchem.gene_bases(title);
create index gene_bases__title__english on pubchem.gene_bases using gin (to_tsvector('english', title));
create index gene_bases__organism on pubchem.gene_bases(organism_id);
grant select on pubchem.gene_bases to sparql;

--------------------------------------------------------------------------------

create index gene_alternatives__gene on pubchem.gene_alternatives(gene);
create index gene_alternatives__alternative on pubchem.gene_alternatives(alternative);
grant select on pubchem.gene_alternatives to sparql;

--------------------------------------------------------------------------------

create index gene_references__gene on pubchem.gene_references(gene);
create index gene_references__reference on pubchem.gene_references(reference);
grant select on pubchem.gene_references to sparql;

--------------------------------------------------------------------------------

create index gene_ensembl_matches__gene on pubchem.gene_ensembl_matches(gene);
create index gene_ensembl_matches__match on pubchem.gene_ensembl_matches(match);
grant select on pubchem.gene_ensembl_matches to sparql;

--------------------------------------------------------------------------------

create index gene_mesh_matches__gene on pubchem.gene_mesh_matches(gene);
create index gene_mesh_matches__match on pubchem.gene_mesh_matches(match);
grant select on pubchem.gene_mesh_matches to sparql;

--------------------------------------------------------------------------------

create index gene_thesaurus_matches__gene on pubchem.gene_thesaurus_matches(gene);
create index gene_thesaurus_matches__match on pubchem.gene_thesaurus_matches(match);
grant select on pubchem.gene_thesaurus_matches to sparql;

--------------------------------------------------------------------------------

create index gene_ctdbase_matches__gene on pubchem.gene_ctdbase_matches(gene);
create index gene_ctdbase_matches__match on pubchem.gene_ctdbase_matches(match);
grant select on pubchem.gene_ctdbase_matches to sparql;

--------------------------------------------------------------------------------

create index gene_expasy_matches__gene on pubchem.gene_expasy_matches(gene);
create index gene_expasy_matches__match on pubchem.gene_expasy_matches(match);
grant select on pubchem.gene_expasy_matches to sparql;

--------------------------------------------------------------------------------

create index gene_medlineplus_matches__gene on pubchem.gene_medlineplus_matches(gene);
create index gene_medlineplus_matches__match on pubchem.gene_medlineplus_matches(match);
grant select on pubchem.gene_medlineplus_matches to sparql;

--------------------------------------------------------------------------------

create index gene_omim_matches__gene on pubchem.gene_omim_matches(gene);
create index gene_omim_matches__match on pubchem.gene_omim_matches(match);
grant select on pubchem.gene_omim_matches to sparql;

--------------------------------------------------------------------------------

create index gene_alliancegenome_matches__gene on pubchem.gene_alliancegenome_matches(gene);
create index gene_alliancegenome_matches__match on pubchem.gene_alliancegenome_matches(match);
grant select on pubchem.gene_alliancegenome_matches to sparql;

--------------------------------------------------------------------------------

create index gene_genenames_matches__gene on pubchem.gene_genenames_matches(gene);
create index gene_genenames_matches__match on pubchem.gene_genenames_matches(match);
grant select on pubchem.gene_genenames_matches to sparql;

--------------------------------------------------------------------------------

create index gene_kegg_matches__gene on pubchem.gene_kegg_matches(gene);
create index gene_kegg_matches__match on pubchem.gene_kegg_matches(match);
grant select on pubchem.gene_kegg_matches to sparql;

--------------------------------------------------------------------------------

create index gene_processes__gene on pubchem.gene_processes(gene);
create index gene_processes__process on pubchem.gene_processes(process_id);
grant select on pubchem.gene_processes to sparql;

--------------------------------------------------------------------------------

create index gene_functions__gene on pubchem.gene_functions(gene);
create index gene_functions__function on pubchem.gene_functions(function_id);
grant select on pubchem.gene_functions to sparql;

--------------------------------------------------------------------------------

create index gene_locations__gene on pubchem.gene_locations(gene);
create index gene_locations__location on pubchem.gene_locations(location_id);
grant select on pubchem.gene_locations to sparql;

--------------------------------------------------------------------------------

create index gene_orthologs__gene on pubchem.gene_orthologs(gene);
create index gene_orthologs__ortholog on pubchem.gene_orthologs(ortholog);
grant select on pubchem.gene_orthologs to sparql;
