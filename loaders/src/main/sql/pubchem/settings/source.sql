grant select on pubchem.source_bases to sparql;
create index source_bases__homepage on pubchem.source_bases using hash (homepage);
create index source_bases__license on pubchem.source_bases using hash (license);

--------------------------------------------------------------------------------

create index source_subjects__source on pubchem.source_subjects(source);
create index source_subjects__subject on pubchem.source_subjects(subject);
grant select on pubchem.source_subjects to sparql;

--------------------------------------------------------------------------------

create index source_alternatives__source on pubchem.source_alternatives(source);
grant select on pubchem.source_alternatives to sparql;
