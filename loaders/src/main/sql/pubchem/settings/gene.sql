create index gene_symbol_bases__symbol on pubchem.gene_symbol_bases(symbol);
grant select on pubchem.gene_symbol_bases to sparql;

--------------------------------------------------------------------------------

create index gene_bases__title on pubchem.gene_bases(title);
create index gene_bases__title__english on pubchem.gene_bases using gin (to_tsvector('english', title));
create index gene_bases__gene_symbol on pubchem.gene_bases(gene_symbol);
create index gene_bases__organism on pubchem.gene_bases(organism);
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

create index gene_matches__gene on pubchem.gene_matches(gene);
create index gene_matches__match on pubchem.gene_matches(match_unit, match_id);
grant select on pubchem.gene_matches to sparql;

--------------------------------------------------------------------------------

create index gene_ensembl_matches__gene on pubchem.gene_ensembl_matches(gene);
create index gene_ensembl_matches__match on pubchem.gene_ensembl_matches(match);
grant select on pubchem.gene_ensembl_matches to sparql;

--------------------------------------------------------------------------------

create index gene_mesh_matches__gene on pubchem.gene_mesh_matches(gene);
create index gene_mesh_matches__match on pubchem.gene_mesh_matches(match);
grant select on pubchem.gene_mesh_matches to sparql;

--------------------------------------------------------------------------------

create index gene_expasy_matches__gene on pubchem.gene_expasy_matches(gene);
create index gene_expasy_matches__match on pubchem.gene_expasy_matches(match);
grant select on pubchem.gene_expasy_matches to sparql;

--------------------------------------------------------------------------------

create index gene_medlineplus_matches__gene on pubchem.gene_medlineplus_matches(gene);
create index gene_medlineplus_matches__match on pubchem.gene_medlineplus_matches(match);
grant select on pubchem.gene_medlineplus_matches to sparql;

--------------------------------------------------------------------------------

create index gene_alliancegenome_matches__gene on pubchem.gene_alliancegenome_matches(gene);
create index gene_alliancegenome_matches__match on pubchem.gene_alliancegenome_matches(match);
grant select on pubchem.gene_alliancegenome_matches to sparql;

--------------------------------------------------------------------------------

create index gene_kegg_matches__gene on pubchem.gene_kegg_matches(gene);
create index gene_kegg_matches__match on pubchem.gene_kegg_matches(match);
grant select on pubchem.gene_kegg_matches to sparql;

--------------------------------------------------------------------------------

create index gene_pharos_matches__gene on pubchem.gene_pharos_matches(gene);
create index gene_pharos_matches__match on pubchem.gene_pharos_matches(match);
grant select on pubchem.gene_pharos_matches to sparql;

--------------------------------------------------------------------------------

create index gene_bgee_matches__gene on pubchem.gene_bgee_matches(gene);
create index gene_bgee_matches__match on pubchem.gene_bgee_matches(match);
grant select on pubchem.gene_bgee_matches to sparql;

--------------------------------------------------------------------------------

create index gene_pombase_matches__gene on pubchem.gene_pombase_matches(gene);
create index gene_pombase_matches__match on pubchem.gene_pombase_matches(match);
grant select on pubchem.gene_pombase_matches to sparql;

--------------------------------------------------------------------------------

create index gene_veupathdb_matches__gene on pubchem.gene_veupathdb_matches(gene);
create index gene_veupathdb_matches__match on pubchem.gene_veupathdb_matches(match);
grant select on pubchem.gene_veupathdb_matches to sparql;

--------------------------------------------------------------------------------

create index gene_zfin_matches__gene on pubchem.gene_zfin_matches(gene);
create index gene_zfin_matches__match on pubchem.gene_zfin_matches(match);
grant select on pubchem.gene_zfin_matches to sparql;

--------------------------------------------------------------------------------

create index gene_enzyme_matches__gene on pubchem.gene_enzyme_matches(gene);
create index gene_enzyme_matches__match on pubchem.gene_enzyme_matches(match);
grant select on pubchem.gene_enzyme_matches to sparql;

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
