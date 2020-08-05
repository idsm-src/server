grant select on source_bases to sparql;

--------------------------------------------------------------------------------

create index source_subjects__source on source_subjects(source);
create index source_subjects__subject on source_subjects(subject);
grant select on source_subjects to sparql;

--------------------------------------------------------------------------------

create index source_alternatives__source on source_alternatives(source);
create index source_alternatives__alternative on source_alternatives(alternative);
grant select on source_alternatives to sparql;
