create index compound_compound_cooccurrences__subject on pubchem.compound_compound_cooccurrences(subject);
create index compound_compound_cooccurrences__object on pubchem.compound_compound_cooccurrences(object);
grant select on pubchem.compound_compound_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index compound_disease_cooccurrences__subject on pubchem.compound_disease_cooccurrences(subject);
create index compound_disease_cooccurrences__object on pubchem.compound_disease_cooccurrences(object);
grant select on pubchem.compound_disease_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index compound_genesymbol_cooccurrences__subject on pubchem.compound_genesymbol_cooccurrences(subject);
create index compound_genesymbol_cooccurrences__object on pubchem.compound_genesymbol_cooccurrences(object);
grant select on pubchem.compound_genesymbol_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index compound_enzyme_cooccurrences__subject on pubchem.compound_enzyme_cooccurrences(subject);
create index compound_enzyme_cooccurrences__object on pubchem.compound_enzyme_cooccurrences(object);
grant select on pubchem.compound_enzyme_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index disease_compound_cooccurrences__subject on pubchem.disease_compound_cooccurrences(subject);
create index disease_compound_cooccurrences__object on pubchem.disease_compound_cooccurrences(object);
grant select on pubchem.disease_compound_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index disease_disease_cooccurrences__subject on pubchem.disease_disease_cooccurrences(subject);
create index disease_disease_cooccurrences__object on pubchem.disease_disease_cooccurrences(object);
grant select on pubchem.disease_disease_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index disease_genesymbol_cooccurrences__subject on pubchem.disease_genesymbol_cooccurrences(subject);
create index disease_genesymbol_cooccurrences__object on pubchem.disease_genesymbol_cooccurrences(object);
grant select on pubchem.disease_genesymbol_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index disease_enzyme_cooccurrences__subject on pubchem.disease_enzyme_cooccurrences(subject);
create index disease_enzyme_cooccurrences__object on pubchem.disease_enzyme_cooccurrences(object);
grant select on pubchem.disease_enzyme_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index genesymbol_compound_cooccurrences__subject on pubchem.genesymbol_compound_cooccurrences(subject);
create index genesymbol_compound_cooccurrences__object on pubchem.genesymbol_compound_cooccurrences(object);
grant select on pubchem.genesymbol_compound_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index enzyme_compound_cooccurrences__subject on pubchem.enzyme_compound_cooccurrences(subject);
create index enzyme_compound_cooccurrences__object on pubchem.enzyme_compound_cooccurrences(object);
grant select on pubchem.enzyme_compound_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index genesymbol_disease_cooccurrences__subject on pubchem.genesymbol_disease_cooccurrences(subject);
create index genesymbol_disease_cooccurrences__object on pubchem.genesymbol_disease_cooccurrences(object);
grant select on pubchem.genesymbol_disease_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index enzyme_disease_cooccurrences__subject on pubchem.enzyme_disease_cooccurrences(subject);
create index enzyme_disease_cooccurrences__object on pubchem.enzyme_disease_cooccurrences(object);
grant select on pubchem.enzyme_disease_cooccurrences to sparql;

--------------------------------------------------------------------------------

create index genesymbol_genesymbol_cooccurrences__subject on pubchem.genesymbol_genesymbol_cooccurrences(subject);
create index genesymbol_genesymbol_cooccurrences__object on pubchem.genesymbol_genesymbol_cooccurrences(object);
grant select on pubchem.genesymbol_genesymbol_cooccurrences to sparql;
