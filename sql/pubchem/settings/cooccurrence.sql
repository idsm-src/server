create index chemical_chemical_cooccurrences__subject on pubchem.chemical_chemical_cooccurrences(subject);
create index chemical_chemical_cooccurrences__object on pubchem.chemical_chemical_cooccurrences(object);
grant select on pubchem.chemical_chemical_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index chemical_disease_cooccurrences__subject on pubchem.chemical_disease_cooccurrences(subject);
create index chemical_disease_cooccurrences__object on pubchem.chemical_disease_cooccurrences(object);
grant select on pubchem.chemical_disease_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index chemical_gene_cooccurrences__subject on pubchem.chemical_gene_cooccurrences(subject);
create index chemical_gene_cooccurrences__object on pubchem.chemical_gene_cooccurrences(object);
grant select on pubchem.chemical_gene_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index chemical_enzyme_cooccurrences__subject on pubchem.chemical_enzyme_cooccurrences(subject);
create index chemical_enzyme_cooccurrences__object on pubchem.chemical_enzyme_cooccurrences(object);
grant select on pubchem.chemical_enzyme_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index disease_chemical_cooccurrences__subject on pubchem.disease_chemical_cooccurrences(subject);
create index disease_chemical_cooccurrences__object on pubchem.disease_chemical_cooccurrences(object);
grant select on pubchem.disease_chemical_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index disease_disease_cooccurrences__subject on pubchem.disease_disease_cooccurrences(subject);
create index disease_disease_cooccurrences__object on pubchem.disease_disease_cooccurrences(object);
grant select on pubchem.disease_disease_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index disease_gene_cooccurrences__subject on pubchem.disease_gene_cooccurrences(subject);
create index disease_gene_cooccurrences__object on pubchem.disease_gene_cooccurrences(object);
grant select on pubchem.disease_gene_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index disease_enzyme_cooccurrences__subject on pubchem.disease_enzyme_cooccurrences(subject);
create index disease_enzyme_cooccurrences__object on pubchem.disease_enzyme_cooccurrences(object);
grant select on pubchem.disease_enzyme_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index gene_chemical_cooccurrences__subject on pubchem.gene_chemical_cooccurrences(subject);
create index gene_chemical_cooccurrences__object on pubchem.gene_chemical_cooccurrences(object);
grant select on pubchem.gene_chemical_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index enzyme_chemical_cooccurrences__subject on pubchem.enzyme_chemical_cooccurrences(subject);
create index enzyme_chemical_cooccurrences__object on pubchem.enzyme_chemical_cooccurrences(object);
grant select on pubchem.enzyme_chemical_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index gene_disease_cooccurrences__subject on pubchem.gene_disease_cooccurrences(subject);
create index gene_disease_cooccurrences__object on pubchem.gene_disease_cooccurrences(object);
grant select on pubchem.gene_disease_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index enzyme_disease_cooccurrences__subject on pubchem.enzyme_disease_cooccurrences(subject);
create index enzyme_disease_cooccurrences__object on pubchem.enzyme_disease_cooccurrences(object);
grant select on pubchem.enzyme_disease_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index gene_gene_cooccurrences__subject on pubchem.gene_gene_cooccurrences(subject);
create index gene_gene_cooccurrences__object on pubchem.gene_gene_cooccurrences(object);
grant select on pubchem.gene_gene_cooccurrences to sparql;
