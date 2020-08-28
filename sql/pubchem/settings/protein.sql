create index protein_bases__organism on pubchem.protein_bases(organism_id);
create index protein_bases__title on pubchem.protein_bases(title);
create index protein_bases__title__gin on pubchem.protein_bases using gin (to_tsvector('english', title));
grant select on pubchem.protein_bases to sparql;

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

create index protein_genes__gene on pubchem.protein_genes(gene);
grant select on pubchem.protein_genes to sparql;

--------------------------------------------------------------------------------

create index protein_closematches__protein on pubchem.protein_closematches(protein);
create index protein_closematches__match on pubchem.protein_closematches(match);
grant select on pubchem.protein_closematches to sparql;

--------------------------------------------------------------------------------

create index protein_conserveddomains__domain on pubchem.protein_conserveddomains(domain);
grant select on pubchem.protein_conserveddomains to sparql;

--------------------------------------------------------------------------------

create index protein_continuantparts__protein on pubchem.protein_continuantparts(protein);
create index protein_continuantparts__part on pubchem.protein_continuantparts(part);
grant select on pubchem.protein_continuantparts to sparql;

--------------------------------------------------------------------------------

create index protein_processes__protein on pubchem.protein_processes(protein);
create index protein_processes__process on pubchem.protein_processes(process_id);
grant select on pubchem.protein_processes to sparql;

--------------------------------------------------------------------------------

create index protein_biosystems__protein on pubchem.protein_biosystems(protein);
create index protein_biosystems__biosystem on pubchem.protein_biosystems(biosystem);
grant select on pubchem.protein_biosystems to sparql;

--------------------------------------------------------------------------------

create index protein_functions__protein on pubchem.protein_functions(protein);
create index protein_functions__function on pubchem.protein_functions(function_id);
grant select on pubchem.protein_functions to sparql;

--------------------------------------------------------------------------------

create index protein_locations__protein on pubchem.protein_locations(protein);
create index protein_locations__location on pubchem.protein_locations(location_id);
grant select on pubchem.protein_locations to sparql;

--------------------------------------------------------------------------------

create index protein_types__protein on pubchem.protein_types(protein);
create index protein_types__type on pubchem.protein_types(type_id);
grant select on pubchem.protein_types to sparql;

--------------------------------------------------------------------------------

grant select on pubchem.protein_complexes to sparql;
