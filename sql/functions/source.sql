create function source(id in smallint) returns varchar language sql as
$$
  select iri from source_bases where source_bases.id = source.id;
$$
immutable parallel safe;


create function source_inverse(iri in varchar) returns smallint language sql as
$$
  select id from source_bases where source_bases.iri = source_inverse.iri;
$$
immutable parallel safe;
