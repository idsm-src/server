grant select on pubchem.sources to sparql;
create index sources__homepage on pubchem.sources using hash (homepage);
create index sources__license on pubchem.sources using hash (license);

--------------------------------------------------------------------------------

create index source_subjects__source on pubchem.source_subjects(source);
create index source_subjects__subject on pubchem.source_subjects(subject);
grant select on pubchem.source_subjects to sparql;

--------------------------------------------------------------------------------

create index source_alternatives__source on pubchem.source_alternatives(source);
grant select on pubchem.source_alternatives to sparql;
