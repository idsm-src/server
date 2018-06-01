create index protein_bases__organism on protein_bases(organism);
create index protein_bases__title on protein_bases(title);
grant select on protein_bases to "SPARQL";

--------------------------------------------------------------------------------

create index protein_references__protein on protein_references(protein);
create index protein_references__reference on protein_references(reference);
grant select on protein_references to "SPARQL";

--------------------------------------------------------------------------------

create index protein_pdblinks__protein on protein_pdblinks(protein);
create index protein_pdblinks__pdblink on protein_pdblinks(pdblink);
grant select on protein_pdblinks to "SPARQL";

--------------------------------------------------------------------------------

create index protein_similarproteins__protein on protein_similarproteins(protein);
create index protein_similarproteins__simprotein on protein_similarproteins(simprotein);
grant select on protein_similarproteins to "SPARQL";

--------------------------------------------------------------------------------

create index protein_genes__gene on protein_genes(gene);
grant select on protein_genes to "SPARQL";

--------------------------------------------------------------------------------

create index protein_closematches__protein on protein_closematches(protein);
create index protein_closematches__match on protein_closematches(match);
grant select on protein_closematches to "SPARQL";

--------------------------------------------------------------------------------

create index protein_conserveddomains__domain on protein_conserveddomains(domain);
grant select on protein_conserveddomains to "SPARQL";

--------------------------------------------------------------------------------

create index protein_continuantparts__protein on protein_continuantparts(protein);
create index protein_continuantparts__part on protein_continuantparts(part);
grant select on protein_continuantparts to "SPARQL";

--------------------------------------------------------------------------------

create index protein_participates_goes__protein on protein_participates_goes(protein);
create index protein_participates_goes__participation on protein_participates_goes(participation);
grant select on protein_participates_goes to "SPARQL";

--------------------------------------------------------------------------------

create index protein_participates_biosystems__protein on protein_participates_biosystems(protein);
create index protein_participates_biosystems__biosystem on protein_participates_biosystems(biosystem);
grant select on protein_participates_biosystems to "SPARQL";

--------------------------------------------------------------------------------

create index protein_functions__protein on protein_functions(protein);
create index protein_functions__gofunction on protein_functions(gofunction);
grant select on protein_functions to "SPARQL";

--------------------------------------------------------------------------------

create index protein_locations__protein on protein_locations(protein);
create index protein_locations__location on protein_locations(location);
grant select on protein_locations to "SPARQL";

--------------------------------------------------------------------------------

create index protein_types__protein on protein_types(protein);
create index protein_types__type on protein_types(type);
grant select on protein_types to "SPARQL";

--------------------------------------------------------------------------------

insert into protein_complexes(protein)
select id from protein_bases where name like 'GI%GI%';

grant select on protein_complexes to "SPARQL";
