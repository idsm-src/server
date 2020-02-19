create index protein_bases__organism on protein_bases(organism_id);
create index protein_bases__title on protein_bases(title);
grant select on protein_bases to sparql;

--------------------------------------------------------------------------------

create index protein_references__protein on protein_references(protein);
create index protein_references__reference on protein_references(reference);
grant select on protein_references to sparql;

--------------------------------------------------------------------------------

create index protein_pdblinks__protein on protein_pdblinks(protein);
create index protein_pdblinks__pdblink on protein_pdblinks(pdblink);
grant select on protein_pdblinks to sparql;

--------------------------------------------------------------------------------

create index protein_similarproteins__protein on protein_similarproteins(protein);
create index protein_similarproteins__simprotein on protein_similarproteins(simprotein);
grant select on protein_similarproteins to sparql;

--------------------------------------------------------------------------------

create index protein_genes__gene on protein_genes(gene);
grant select on protein_genes to sparql;

--------------------------------------------------------------------------------

create index protein_closematches__protein on protein_closematches(protein);
create index protein_closematches__match on protein_closematches(match);
grant select on protein_closematches to sparql;

--------------------------------------------------------------------------------

create index protein_conserveddomains__domain on protein_conserveddomains(domain);
grant select on protein_conserveddomains to sparql;

--------------------------------------------------------------------------------

create index protein_continuantparts__protein on protein_continuantparts(protein);
create index protein_continuantparts__part on protein_continuantparts(part);
grant select on protein_continuantparts to sparql;

--------------------------------------------------------------------------------

create index protein_processes__protein on protein_processes(protein);
create index protein_processes__process on protein_processes(process_id);
grant select on protein_processes to sparql;

--------------------------------------------------------------------------------

create index protein_biosystems__protein on protein_biosystems(protein);
create index protein_biosystems__biosystem on protein_biosystems(biosystem);
grant select on protein_biosystems to sparql;

--------------------------------------------------------------------------------

create index protein_functions__protein on protein_functions(protein);
create index protein_functions__function on protein_functions(function_id);
grant select on protein_functions to sparql;

--------------------------------------------------------------------------------

create index protein_locations__protein on protein_locations(protein);
create index protein_locations__location on protein_locations(location_id);
grant select on protein_locations to sparql;

--------------------------------------------------------------------------------

create index protein_types__protein on protein_types(protein);
create index protein_types__type on protein_types(type_id);
grant select on protein_types to sparql;

--------------------------------------------------------------------------------

insert into protein_complexes(protein)
select id from protein_bases where name like 'GI%GI%';

grant select on protein_complexes to sparql;
