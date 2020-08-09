grant select on pubchem.source_bases to sparql;

--------------------------------------------------------------------------------

create index source_subjects__source on pubchem.source_subjects(source);
create index source_subjects__subject on pubchem.source_subjects(subject);
grant select on pubchem.source_subjects to sparql;

--------------------------------------------------------------------------------

create index source_alternatives__source on pubchem.source_alternatives(source);
create index source_alternatives__alternative on pubchem.source_alternatives(alternative);
grant select on pubchem.source_alternatives to sparql;
