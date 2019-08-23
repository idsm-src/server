create function compound(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID' || id;
$$
immutable parallel safe;


create function compound_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 49)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function compound_molfile(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID' || id || '_Molfile';
$$
immutable parallel safe;


create function compound_molfile_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 49, octet_length(iri) - 56)::integer;
$$
immutable parallel safe;
