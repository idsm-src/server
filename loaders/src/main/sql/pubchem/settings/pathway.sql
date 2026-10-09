create index pathways__source on pubchem.pathways(source);
create index pathways__title on pubchem.pathways(title);
create index pathways__reference_type on pubchem.pathways(reference_type);
create index pathways__reference on pubchem.pathways(reference);
create index pathways__organism on pubchem.pathways(organism);
grant select on pubchem.pathways to sparql;

--------------------------------------------------------------------------------

create index pathway_compounds__pathway on pubchem.pathway_compounds(pathway);
create index pathway_compounds__compound on pubchem.pathway_compounds(compound);
grant select on pubchem.pathway_compounds to sparql;

--------------------------------------------------------------------------------

create index pathway_proteins__pathway on pubchem.pathway_proteins(pathway);
create index pathway_proteins__protein on pubchem.pathway_proteins(protein);
grant select on pubchem.pathway_proteins to sparql;

--------------------------------------------------------------------------------

create index pathway_genes__pathway on pubchem.pathway_genes(pathway);
create index pathway_genes__gene on pubchem.pathway_genes(gene);
grant select on pubchem.pathway_genes to sparql;

--------------------------------------------------------------------------------

create index pathway_components__pathway on pubchem.pathway_components(pathway);
create index pathway_components__component on pubchem.pathway_components(component);
grant select on pubchem.pathway_components to sparql;

--------------------------------------------------------------------------------

create index pathway_related_pathways__pathway on pubchem.pathway_related_pathways(pathway);
create index pathway_related_pathways__related on pubchem.pathway_related_pathways(related);
grant select on pubchem.pathway_related_pathways to sparql;

--------------------------------------------------------------------------------

create index pathway_references__pathway on pubchem.pathway_references(pathway);
create index pathway_references__reference on pubchem.pathway_references(reference);
grant select on pubchem.pathway_references to sparql;
