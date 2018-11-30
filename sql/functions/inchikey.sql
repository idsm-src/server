create function inchikey(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/' || inchikey from inchikey_bases where inchikey_bases.id = inchikey.id;
$$
immutable;


create function inchikey_inverse(iri in varchar) returns integer language sql as
$$
  select id from inchikey_bases where inchikey = substring(inchikey_inverse.iri, 46)::varchar;
$$
immutable;
