create function gene(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID' || id;
$$
immutable;


create function gene_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID', '')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function ensembl(id in varchar) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID' || id;
$$
immutable;


create function ensembl_inverse(iri in varchar) returns varchar language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID', '');
$$
immutable;
