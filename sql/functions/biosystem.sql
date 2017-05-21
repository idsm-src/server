create function biosystem(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID' || id;
$$
immutable;


create function biosystem_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID', '')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function wikipathway(id in integer) returns varchar language sql as
$$
  select 'http://identifiers.org/wikipathways/WP' || id;
$$
immutable;


create function wikipathway_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://identifiers.org/wikipathways/WP', '')::integer;
$$
immutable;
