create function compound(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID' || id;
$$
immutable;


create function compound_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID', '')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function compound_sdfile(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID' || id || '_SDfile';
$$
immutable;


create function compound_sdfile_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID([0-9]+)_SDfile$', '\1')::integer;
$$
immutable;
