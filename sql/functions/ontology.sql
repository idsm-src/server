create function class(id in integer) returns varchar language sql as
$$
  select iri from class_bases where class_bases.id = class.id;
$$
immutable;


create function class_inverse(iri in varchar) returns integer language sql as
$$
  select id from class_bases where class_bases.iri = class_inverse.iri;
$$
immutable;

--------------------------------------------------------------------------------

create function property(id in integer) returns varchar language sql as
$$
  select iri from property_bases where property_bases.id = property.id;
$$
immutable;


create function property_inverse(iri in varchar) returns integer language sql as
$$
  select id from property_bases where property_bases.iri = property_inverse.iri;
$$
immutable;
