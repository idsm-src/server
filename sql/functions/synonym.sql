create function synonym(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_' || md5 from synonym_bases where synonym_bases.id = synonym.id;
$$
immutable;


create function synonym_inverse(iri in varchar) returns integer language sql as
$$
  select id from synonym_bases where ('http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_' || md5) = synonym_inverse.iri;
$$
immutable;
