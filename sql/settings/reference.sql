grant select on reference_types__reftable to "SPARQL";

--------------------------------------------------------------------------------

create bitmap index reference_bases__type on reference_bases(type);
create index reference_bases__dcdate on reference_bases(dcdate);
grant select on reference_bases to "SPARQL";

--------------------------------------------------------------------------------

grant select on reference_citations_long to "SPARQL";

--------------------------------------------------------------------------------

create index reference_discusses__reference on reference_discusses(reference);
create index reference_discusses__statement on reference_discusses(statement);
grant select on reference_discusses to "SPARQL";

--------------------------------------------------------------------------------

create index reference_subject_descriptors__reference on reference_subject_descriptors(reference);
create index reference_subject_descriptors__descriptor_qualifier on reference_subject_descriptors(descriptor, qualifier);
grant select on reference_subject_descriptors to "SPARQL";
