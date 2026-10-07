create function ontology.ontology_resource(unit in smallint, id in integer) returns varchar language plpgsql as
$$
  declare
    rec record;
    res varchar;
    val integer;
  begin
    if unit = 0 then
      return (select iri from ontology.resources__reftable where resource_id = id);
    end if;
	
    select prefix, value_length, suffix into rec from ontology.resource_categories__reftable where unit_id = unit;

    if unit = 102 then
      -- [0-9][A-Z0-9][0-9][A-Z0-9]{3}[0-9]
      res := chr((id % 10) + ascii('0'));
      id := id / 10;

      for i in 1..3 loop
        val := (id % 36);
        id := id / 36;

        if val >= 10 then
          res := chr(val - 10 + ascii('A')) || res;
        else
          res := chr(val + ascii('0')) || res;
        end if;
      end loop;

      res := chr((id % 10) + ascii('0')) || res;
      id := id / 10;

      val := (id % 36);
      id := id / 36;

      if val >= 10 then
        res := chr(val - 10 + ascii('A')) || res;
      else
        res := chr(val + ascii('0')) || res;
      end if;

      res := chr((id % 10) + ascii('0')) || res;

      return rec.prefix || res || rec.suffix;
	elsif unit = 101 or unit = 100 then
      -- [A-Z][0-9][A-Z0-9]{3}[0-9](-([12])?[0-9])?
	  if unit = 101 then
        res := '-' || ((id::bigint & x'FFFFFFFF'::bigint) % 30);
        id := (id::bigint & x'FFFFFFFF'::bigint) / 30;
	  else
	    res := '';
	  end if;
	
	  res := chr((id % 10) + ascii('0')) || res;
      id := id / 10;

      for i in 1..3 loop
        val := (id % 36);
        id := id / 36;

        if val >= 10 then
          res := chr(val - 10 + ascii('A')) || res;
        else
          res := chr(val + ascii('0')) || res;
        end if;
      end loop;

      res := chr((id % 10) + ascii('0')) || res;
      id := id / 10;

      res := chr(id + ascii('A')) || res;

      return rec.prefix || res || rec.suffix;
    elsif rec.value_length = 0 then
      return rec.prefix || id || rec.suffix;
    else
      return rec.prefix || lpad(id::varchar, rec.value_length, '0') || rec.suffix;
    end if;
  end;
$$
immutable parallel safe;


create function ontology.ontology_resource_inv1(iri in varchar) returns smallint language sql as
$$
  select coalesce((select unit_id from ontology.resource_categories__reftable where starts_with(iri, prefix) and iri ~ pattern limit 1), 0::smallint);
$$
immutable parallel safe;


create function ontology.ontology_resource_inv2(iri in varchar) returns integer language plpgsql as
$$
  declare rec record;
  declare tail varchar;
  declare val char;
  declare big bigint;
  begin
    select unit_id, value_offset, suffix into rec from ontology.resource_categories__reftable where starts_with(iri, prefix) and iri ~ pattern limit 1;

    if not found then
      return (select resource_id from ontology.resources__reftable tab where tab.iri = ontology_resource_inv2.iri);
    end if;

    tail := substring(iri, rec.value_offset, length(iri) - rec.value_offset + 1 - length(rec.suffix));

    if rec.unit_id < 100 or rec.unit_id > 102 then
      return tail::integer;
    end if;

    if rec.unit_id = 102 then
      big := ascii(substring(tail, 1, 1)) - ascii('0');

      val := substring(tail, 2, 1);
      big := big * 36 + (case when ascii(val) > ascii('9') then 10 + ascii(val) - ascii('A') else ascii(val) - ascii('0') end);

      big := big * 10 + ascii(substring(tail, 3, 1)) - ascii('0');

      for i in 4..6 loop
        val := substring(tail, i, 1);
        big := big * 36 + (case when ascii(val) > ascii('9') then 10 + ascii(val) - ascii('A') else ascii(val) - ascii('0') end);
      end loop;

      big := big * 10 + ascii(substring(tail, 7, 1)) - ascii('0');
    elsif rec.unit_id = 100 or rec.unit_id = 101 then
      big := ascii(substring(tail, 1, 1)) - ascii('A');
      big := big * 10 + ascii(substring(tail, 2, 1)) - ascii('0');

      for i in 3..5 loop
        val := substring(tail, i, 1);
        big := big * 36 + (case when ascii(val) > ascii('9') then 10 + ascii(val) - ascii('A') else ascii(val) - ascii('0') end);
      end loop;

      big := big * 10 + ascii(substring(tail, 6, 1)) - ascii('0');

      if rec.unit_id = 101 then
        big := big * 30 + substring(tail, 8)::integer;
      end if;
    end if;

    if big < (1::bigint << 31) then
      return big::integer;
    else
      return (big - (1::bigint << 32))::integer;
    end if;
  end;
$$
immutable parallel safe;
