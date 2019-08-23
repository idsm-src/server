create function conserveddomain(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID' || id;
$$
immutable parallel safe;


create function conserveddomain_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 59)::integer;
$$
immutable parallel safe;
