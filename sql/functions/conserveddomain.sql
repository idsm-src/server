create function conserveddomain(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID' || id;
$$
immutable;


create function conserveddomain_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID', '')::integer;
$$
immutable;
