insert into measuregroup_bases(bioassay, measuregroup)
select distinct bioassay, measuregroup from endpoint_outcomes as t where not exists (select bioassay, measuregroup from measuregroup_bases where bioassay = t.bioassay and measuregroup = t.measuregroup);

create index measuregroup_bases__bioassay on measuregroup_bases(bioassay);
create index measuregroup_bases__source on measuregroup_bases(source);
grant select on measuregroup_bases to "SPARQL";

--------------------------------------------------------------------------------

create index measuregroup_proteins__bioassay on measuregroup_proteins(bioassay);
create index measuregroup_proteins__bioassay_measuregroup on measuregroup_proteins(bioassay, measuregroup);
create index measuregroup_proteins__protein on measuregroup_proteins(protein);
grant select on measuregroup_proteins to "SPARQL";

--------------------------------------------------------------------------------

create index measuregroup_genes__bioassay on measuregroup_genes(bioassay);
create index measuregroup_genes__bioassay_measuregroup on measuregroup_genes(bioassay, measuregroup);
create index measuregroup_genes__gene on measuregroup_genes(gene);
grant select on measuregroup_genes to "SPARQL";
