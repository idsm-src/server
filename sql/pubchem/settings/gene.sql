create index gene_bases__title on pubchem.gene_bases(title);
create index gene_bases__title__gin on pubchem.gene_bases using gin (to_tsvector('english', title));
create index gene_bases__description on pubchem.gene_bases(description);
create index gene_bases__description__gin on pubchem.gene_bases using gin (to_tsvector('english', description));
grant select on pubchem.gene_bases to sparql;

--------------------------------------------------------------------------------

create index gene_biosystems__gene on pubchem.gene_biosystems(gene);
create index gene_biosystems__biosystem on pubchem.gene_biosystems(biosystem);
grant select on pubchem.gene_biosystems to sparql;

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
