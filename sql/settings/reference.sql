create index reference_bases__type on reference_bases(type_id);
create index reference_bases__dcdate on reference_bases(dcdate);
grant select on reference_bases to sparql;

--------------------------------------------------------------------------------

create index reference_discusses__reference on reference_discusses(reference);
create index reference_discusses__statement on reference_discusses(statement);
grant select on reference_discusses to sparql;

--------------------------------------------------------------------------------

create index reference_subjects__reference on reference_subjects(reference);
create index reference_subjects__subject on reference_subjects(subject);
grant select on reference_subjects to sparql;

--------------------------------------------------------------------------------

create index reference_primary_subjects__reference on reference_primary_subjects(reference);
create index reference_primary_subjects__subject on reference_primary_subjects(subject);
grant select on reference_primary_subjects to sparql;
