create index substance_bases__source on substance_bases(source);
create index substance_bases__available on substance_bases(available);
create index substance_bases__modified on substance_bases(modified);
create index substance_bases__compound on substance_bases(compound);
grant select on substance_bases to sparql;

--------------------------------------------------------------------------------

create index substance_types__substance on substance_types(substance);
create index substance_types__chebi on substance_types(chebi);
grant select on substance_types to sparql;

--------------------------------------------------------------------------------

create index substance_matches__substance on substance_matches(substance);
create index substance_matches__match on substance_matches(match);
grant select on substance_matches to sparql;

--------------------------------------------------------------------------------

create index substance_references__substance on substance_references(substance);
create index substance_references__reference on substance_references(reference);
grant select on substance_references to sparql;

--------------------------------------------------------------------------------

create index substance_pdblinks__substance on substance_pdblinks(substance);
create index substance_pdblinks__pdblink on substance_pdblinks(pdblink);
grant select on substance_pdblinks to sparql;

--------------------------------------------------------------------------------

create index substance_synonyms__substance on substance_synonyms(substance);
create index substance_synonyms__synonym on substance_synonyms(synonym);
grant select on substance_synonyms to sparql;
