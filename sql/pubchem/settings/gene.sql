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

create index gene_matches__gene on pubchem.gene_matches(gene);
create index gene_matches__match on pubchem.gene_matches(match);
grant select on pubchem.gene_matches to sparql;

--------------------------------------------------------------------------------

create index gene_ncit_matches__gene on pubchem.gene_ncit_matches(gene);
create index gene_ncit_matches__match on pubchem.gene_ncit_matches(match);
grant select on pubchem.gene_ncit_matches to sparql;

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
