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
  select substring(iri, 29, 6)::integer
$$
immutable parallel safe;


create function dqmesh_inv2(iri in varchar) returns integer language sql as
$$
  select case octet_length(iri)
    when 34 then -1
    else substring(iri, 36, 6)::integer
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
