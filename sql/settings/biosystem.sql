create bitmap index biosystem_bases__source on biosystem_bases(source);
create index biosystem_bases__organism on biosystem_bases(organism);
grant select on biosystem_bases to "SPARQL";

--------------------------------------------------------------------------------

create index biosystem_components__biosystem on biosystem_components(biosystem);
create index biosystem_components__component on biosystem_components(component);
grant select on biosystem_components to "SPARQL";

--------------------------------------------------------------------------------

create index biosystem_references__biosystem on biosystem_references(biosystem);
create index biosystem_references__reference on biosystem_references(reference);
grant select on biosystem_references to "SPARQL";

--------------------------------------------------------------------------------

create index biosystem_matches__wikipathway on biosystem_matches(wikipathway);
grant select on biosystem_matches to "SPARQL";
