create function reference(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID' || id;
$$
immutable parallel safe;


create function reference_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51)::integer;
$$
immutable parallel safe;
