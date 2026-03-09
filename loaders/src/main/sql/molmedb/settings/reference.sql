create index reference_bases__doi on molmedb.reference_bases(doi);
create index reference_bases__pmid on molmedb.reference_bases(pmid);
create index reference_bases__citation on molmedb.reference_bases(citation);
create index reference_bases__label on molmedb.reference_bases(label);
create index reference_bases__homepage on molmedb.reference_bases(homepage);
grant select on molmedb.reference_bases to sparql;

--------------------------------------------------------------------------------

create index reference_substances__reference_id on molmedb.reference_substances(reference_id);
create index reference_substances__substance_id on molmedb.reference_substances(substance_id);
grant select on molmedb.reference_substances to sparql;

--------------------------------------------------------------------------------

create index reference_membranes__reference_id on molmedb.reference_membranes(reference_id);
create index reference_membranes__membrane_id on molmedb.reference_membranes(membrane_id);
grant select on molmedb.reference_membranes to sparql;

--------------------------------------------------------------------------------

create index reference_methods__reference_id on molmedb.reference_methods(reference_id);
create index reference_methods__method_id on molmedb.reference_methods(method_id);
grant select on molmedb.reference_methods to sparql;

--------------------------------------------------------------------------------

create index reference_proteins__reference_id on molmedb.reference_proteins(reference_id);
create index reference_proteins__protein_id on molmedb.reference_proteins(protein_id);
grant select on molmedb.reference_proteins to sparql;
