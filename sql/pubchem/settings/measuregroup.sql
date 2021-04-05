create index measuregroup_bases__bioassay on pubchem.measuregroup_bases(bioassay);
create index measuregroup_bases__source on pubchem.measuregroup_bases(source);
create index measuregroup_bases__title on pubchem.measuregroup_bases(title);
grant select on pubchem.measuregroup_bases to sparql;

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
