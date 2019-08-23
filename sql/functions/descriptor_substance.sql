create function substance_version(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID' || id || '_Substance_Version';
$$
immutable parallel safe;


create function substance_version_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, octet_length(iri) - 68)::integer;
$$
immutable parallel safe;
