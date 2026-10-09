create index references__doi on molmedb.references(doi);
create index references__pmid on molmedb.references(pmid);
create index references__citation on molmedb.references(citation);
create index references__label on molmedb.references(label);
create index references__homepage on molmedb.references(homepage);
grant select on molmedb.references to sparql;

--------------------------------------------------------------------------------

create index reference_substances__reference on molmedb.reference_substances(reference);
create index reference_substances__substance on molmedb.reference_substances(substance);
grant select on molmedb.reference_substances to sparql;

--------------------------------------------------------------------------------

create index reference_membranes__reference on molmedb.reference_membranes(reference);
create index reference_membranes__membrane on molmedb.reference_membranes(membrane);
grant select on molmedb.reference_membranes to sparql;

--------------------------------------------------------------------------------

create index reference_methods__reference on molmedb.reference_methods(reference);
create index reference_methods__method on molmedb.reference_methods(method);
grant select on molmedb.reference_methods to sparql;

--------------------------------------------------------------------------------

create index reference_proteins__reference on molmedb.reference_proteins(reference);
create index reference_proteins__protein on molmedb.reference_proteins(protein);
grant select on molmedb.reference_proteins to sparql;
