create index measuregroup_bases__bioassay on measuregroup_bases(bioassay);
create index measuregroup_bases__source on measuregroup_bases(source);
grant select on measuregroup_bases to sparql;

--------------------------------------------------------------------------------

create index measuregroup_proteins__bioassay on measuregroup_proteins(bioassay);
create index measuregroup_proteins__bioassay_measuregroup on measuregroup_proteins(bioassay, measuregroup);
create index measuregroup_proteins__protein on measuregroup_proteins(protein);
grant select on measuregroup_proteins to sparql;

--------------------------------------------------------------------------------

create index measuregroup_genes__bioassay on measuregroup_genes(bioassay);
create index measuregroup_genes__bioassay_measuregroup on measuregroup_genes(bioassay, measuregroup);
create index measuregroup_genes__gene on measuregroup_genes(gene);
grant select on measuregroup_genes to sparql;
