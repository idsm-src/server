create index measuregroup_bases__bioassay on pubchem.measuregroup_bases(bioassay);
create index measuregroup_bases__source on pubchem.measuregroup_bases(source);
create index measuregroup_bases__title on pubchem.measuregroup_bases(title);
grant select on pubchem.measuregroup_bases to sparql;

--------------------------------------------------------------------------------

create index measuregroup_substances__bioassay on pubchem.measuregroup_substances(bioassay);
create index measuregroup_substances__bioassay_measuregroup on pubchem.measuregroup_substances(bioassay, measuregroup);
create index measuregroup_substances__substance on pubchem.measuregroup_substances(substance);
grant select on pubchem.measuregroup_substances to sparql;

--------------------------------------------------------------------------------

create index measuregroup_proteins__bioassay on pubchem.measuregroup_proteins(bioassay);
create index measuregroup_proteins__bioassay_measuregroup on pubchem.measuregroup_proteins(bioassay, measuregroup);
create index measuregroup_proteins__protein on pubchem.measuregroup_proteins(protein);
grant select on pubchem.measuregroup_proteins to sparql;

--------------------------------------------------------------------------------

create index measuregroup_genes__bioassay on pubchem.measuregroup_genes(bioassay);
create index measuregroup_genes__bioassay_measuregroup on pubchem.measuregroup_genes(bioassay, measuregroup);
create index measuregroup_genes__gene on pubchem.measuregroup_genes(gene);
grant select on pubchem.measuregroup_genes to sparql;

--------------------------------------------------------------------------------

create index measuregroup_taxonomies__bioassay on pubchem.measuregroup_taxonomies(bioassay);
create index measuregroup_taxonomies__bioassay_measuregroup on pubchem.measuregroup_taxonomies(bioassay, measuregroup);
create index measuregroup_taxonomies__taxonomy on pubchem.measuregroup_taxonomies(taxonomy);
grant select on pubchem.measuregroup_taxonomies to sparql;

--------------------------------------------------------------------------------

create index measuregroup_cells__bioassay on pubchem.measuregroup_cells(bioassay);
create index measuregroup_cells__bioassay_measuregroup on pubchem.measuregroup_cells(bioassay, measuregroup);
create index measuregroup_cells__cell on pubchem.measuregroup_cells(cell);
grant select on pubchem.measuregroup_cells to sparql;
