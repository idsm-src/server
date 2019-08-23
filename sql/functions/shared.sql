create function graph(id in smallint) returns varchar language sql as
$$
  select iri from graphs__reftable where graphs__reftable.id = graph.id;
$$
immutable parallel safe;


create function graph_inverse(iri in varchar) returns smallint language sql as
$$
  select id from graphs__reftable where graphs__reftable.iri = graph_inverse.iri;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function dqmesh(descriptor in integer, qualifier in integer) returns varchar language sql as
$$
  select case qualifier
    when -1 then 'http://id.nlm.nih.gov/mesh/D' || lpad(descriptor::text, 6, '0')
    else 'http://id.nlm.nih.gov/mesh/D' || lpad(descriptor::text, 6, '0') || 'Q' || lpad(qualifier::text, 6, '0')
  end;
$$
immutable parallel safe;


create function dqmesh_inv1(iri in varchar) returns integer language sql as
$$
  select case strpos(iri, 'Q')
    when 0 then substring(iri, 29)::integer
    else substring(iri, 29, strpos(iri, 'Q') - 29)::integer
  end;
$$
immutable parallel safe;


create function dqmesh_inv2(iri in varchar) returns integer language sql as
$$
  select case strpos(iri, 'Q')
    when 0 then -1
    else substring(iri, strpos(iri, 'Q') + 1)::integer
  end;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function mesh(id in integer) returns varchar language sql as
$$
  select case
    when id >= 0 then 'http://id.nlm.nih.gov/mesh/M' || lpad(id::text, 7, '0')
    else              'http://id.nlm.nih.gov/mesh/C' || lpad((-1 * id)::text, 9, '0')
  end;
$$
immutable parallel safe;


create function mesh_inverse(iri in varchar) returns integer language sql as
$$
  select case
    when iri like 'http://id.nlm.nih.gov/mesh/M%' then substring(iri, 29)::integer
    else -1 * substring(iri, 29)::integer
  end;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pdblink(id in varchar) returns varchar language sql as
$$
  select 'http://rdf.wwpdb.org/pdb/' || id;
$$
immutable parallel safe;


create function pdblink_inverse(iri in varchar) returns varchar language sql as
$$
  select substring(iri, 26);
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function uniprot(id in varchar) returns varchar language sql as
$$
  select 'http://purl.uniprot.org/uniprot/' || id;
$$
immutable parallel safe;


create function uniprot_inverse(iri in varchar) returns varchar language sql as
$$
  select substring(iri, 33);
$$
immutable parallel safe;
