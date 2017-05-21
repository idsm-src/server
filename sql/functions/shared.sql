create function graph(id in smallint) returns varchar language sql as
$$
  select iri from graphs__reftable where graphs__reftable.id = graph.id;
$$
immutable;


create function graph_inverse(iri in varchar) returns smallint language sql as
$$
  select id from graphs__reftable where graphs__reftable.iri = graph_inverse.iri;
$$
immutable;

--------------------------------------------------------------------------------

create function go(id in integer) returns varchar language sql as
$$
  select 'http://purl.obolibrary.org/obo/GO_' || id;
$$
immutable;


create function go_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://purl.obolibrary.org/obo/GO_', '')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function dqmesh(descriptor in integer, qualifier in integer) returns varchar language sql as
$$
  select case qualifier
    when -1 then 'http://id.nlm.nih.gov/mesh/D' || lpad(descriptor::text, 6, '0')
    else 'http://id.nlm.nih.gov/mesh/D' || lpad(descriptor::text, 6, '0') || 'Q' || lpad(qualifier::text, 6, '0')
  end;
$$
immutable;


create function dqmesh_inv1(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://id.nlm.nih.gov/mesh/D([0-9]+)(Q[0-9]+)?$', '\1')::integer;
$$
immutable;


create function dqmesh_inv2(iri in varchar) returns integer language sql as
$$
  select case
    when iri like 'http://id.nlm.nih.gov/mesh/D%Q%' then regexp_replace(iri, '^http://id.nlm.nih.gov/mesh/D[0-9]+(Q([0-9]+))?$', '\2')::integer
    else -1
  end;
$$
immutable;

--------------------------------------------------------------------------------

create function mesh(id in integer) returns varchar language sql as
$$
  select case
    when id >= 0 then 'http://id.nlm.nih.gov/mesh/M' || lpad(id::text, 7, '0')
    else              'http://id.nlm.nih.gov/mesh/C' || lpad((-1 * id)::text, 9, '0')
  end;
$$
immutable;


create function mesh_inverse(iri in varchar) returns integer language sql as
$$
  select case
    when iri like 'http://id.nlm.nih.gov/mesh/M%' then regexp_replace(iri, '^http://id.nlm.nih.gov/mesh/M([0-9]+)$', '\1')::integer
    else -1 * regexp_replace(iri, '^http://id.nlm.nih.gov/mesh/C([0-9]+)$', '\1')::integer
  end;
$$
immutable;

--------------------------------------------------------------------------------

create function pdblink(id in varchar) returns varchar language sql as
$$
  select 'http://rdf.wwpdb.org/pdb/' || id;
$$
immutable;


create function pdblink_inverse(iri in varchar) returns varchar language sql as
$$
  select regexp_replace(iri, '^http://rdf.wwpdb.org/pdb/', '');
$$
immutable;

--------------------------------------------------------------------------------

create function taxonomy(id in integer) returns varchar language sql as
$$
  select 'http://identifiers.org/taxonomy/' || id;
$$
immutable;


create function taxonomy_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://identifiers.org/taxonomy/', '')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function uniprot(id in varchar) returns varchar language sql as
$$
  select 'http://purl.uniprot.org/uniprot/' || id;
$$
immutable;


create function uniprot_inverse(iri in varchar) returns varchar language sql as
$$
  select regexp_replace(iri, '^http://purl.uniprot.org/uniprot/', '');
$$
immutable;
