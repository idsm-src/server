create index reference_bases__type on pubchem.reference_bases(type_id);
create index reference_bases__dcdate on pubchem.reference_bases(dcdate);
create index reference_bases__title on pubchem.reference_bases using hash (title);
create index reference_bases__citation on pubchem.reference_bases using hash (citation);
grant select on pubchem.reference_bases to sparql;

--------------------------------------------------------------------------------

create index reference_discusses__reference on pubchem.reference_discusses(reference);
create index reference_discusses__statement on pubchem.reference_discusses(statement);
grant select on pubchem.reference_discusses to sparql;

--------------------------------------------------------------------------------

create index reference_subjects__reference on pubchem.reference_subjects(reference);
create index reference_subjects__subject on pubchem.reference_subjects(subject);
grant select on pubchem.reference_subjects to sparql;

--------------------------------------------------------------------------------

create index reference_primary_subjects__reference on pubchem.reference_primary_subjects(reference);
create index reference_primary_subjects__subject on pubchem.reference_primary_subjects(subject);
grant select on pubchem.reference_primary_subjects to sparql;
