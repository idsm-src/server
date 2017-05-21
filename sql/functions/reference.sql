create function reference(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID' || id;
$$
immutable;


create function reference_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID', '')::integer;
$$
immutable;
