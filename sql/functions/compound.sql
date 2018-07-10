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

create function compound_molfile(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID' || id || '_Molfile';
$$
immutable;


create function compound_molfile_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID([0-9]+)_Molfile$', '\1')::integer;
$$
immutable;
