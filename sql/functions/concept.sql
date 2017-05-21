create function concept(id in smallint) returns varchar language sql as
$$
  select iri from concept_bases where concept_bases.id = concept.id;
$$
immutable;


create function concept_inverse(iri in varchar) returns smallint language sql as
$$
  select id from concept_bases where concept_bases.iri = concept_inverse.iri;
$$
immutable;
