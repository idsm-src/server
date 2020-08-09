create index biosystem_bases__source on pubchem.biosystem_bases(source);
create index biosystem_bases__organism on pubchem.biosystem_bases(organism_id);
grant select on pubchem.biosystem_bases to sparql;

--------------------------------------------------------------------------------

create index biosystem_components__biosystem on pubchem.biosystem_components(biosystem);
create index biosystem_components__component on pubchem.biosystem_components(component);
grant select on pubchem.biosystem_components to sparql;

--------------------------------------------------------------------------------

create index biosystem_references__biosystem on pubchem.biosystem_references(biosystem);
create index biosystem_references__reference on pubchem.biosystem_references(reference);
grant select on pubchem.biosystem_references to sparql;

--------------------------------------------------------------------------------

create index biosystem_matches__wikipathway on pubchem.biosystem_matches(wikipathway);
grant select on pubchem.biosystem_matches to sparql;
