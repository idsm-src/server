create function ontology_resource(unit in smallint, id in integer) returns varchar language plpgsql as
$$
  declare
    rec record;
    res varchar;
    val integer;
  begin
    if unit = 0 then
      return (select iri from ontology_resources__reftable where resource_id = id);
    end if;
	  
    select prefix, value_length into rec from ontology_resource_categories__reftable where unit_id = unit;
    
	if unit = 32 or unit = 33 then
      -- [A-Z][0-9][A-Z0-9]{3}[0-9](-([12])?[0-9])?
	  if unit = 32 then
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

      return rec.prefix || res;
    elsif unit = 34 then
      -- [A-Z0-9]G[0-9]{5}
      res := 'G' || lpad((id % 100000)::varchar, 5, '0');
      id := id / 100000;
      
      if id >= 10 then
        res := chr(id - 10 + ascii('A')) || res;
      else
        res := chr(id + ascii('0')) || res;
      end if;
    
      return rec.prefix || res;
    elsif unit = 35 then
      -- [0-9]{6}-([1-3])?[0-9]{1,3}$
      return rec.prefix || lpad(((id::bigint & x'FFFFFFFF'::bigint) / 4000)::varchar, 6, '0') || '-' || ((id::bigint & x'FFFFFFFF'::bigint) % 4000)::varchar;
    elsif rec.value_length = 0 then
      return rec.prefix || id;
    else
      return rec.prefix || lpad(id::varchar, rec.value_length, '0');
    end if;
  end;
$$
immutable;


create function ontology_resource_inv1(iri in varchar) returns smallint language plpgsql as
$$
  declare unit smallint;
  begin
    select unit_id into unit from ontology_resource_categories__reftable where iri ~ pattern;
    
    if not found then
      return 0;
    end if;

    return unit;
  end;
$$
immutable;


create function ontology_resource_inv2(iri in varchar) returns integer language plpgsql as
$$
  declare rec record;
  declare tail varchar;
  declare val char;
  declare big bigint;
  begin
    select unit_id, value_offset into rec from ontology_resource_categories__reftable where iri ~ pattern;
    
    if not found then
      return (select resource_id from ontology_resources__reftable tab where tab.iri = $1);
    end if;

    tail := substring(iri, rec.value_offset);
  
    if rec.unit_id < 32 or rec.unit_id > 35 then
      return tail::integer;
    end if;
  
    if rec.unit_id = 33 or rec.unit_id = 32 then
      big := ascii(substring(tail, 1, 1)) - ascii('A');
      big := big * 10 + ascii(substring(tail, 2, 1)) - ascii('0');
      
      for i in 3..5 loop
        val := substring(tail, i, 1);
        big := big * 36 + (case when ascii(val) > ascii('9') then 10 + ascii(val) - ascii('A') else ascii(val) - ascii('0') end);
      end loop;
      
      big := big * 10 + ascii(substring(tail, 6, 1)) - ascii('0');
      
      if rec.unit_id = 32 then
        big := big * 30 + substring(tail, 8)::integer;
      end if;
    elsif rec.unit_id = 34 then
      val := substring(tail, 1, 1);
      big := (case when ascii(val) > ascii('9') then 10 + ascii(val) - ascii('A') else ascii(val) - ascii('0') end) * 100000 + right(tail, 5)::integer;
    elsif rec.unit_id = 35 then
      big := left(tail, 6)::bigint * 4000 + substring(tail, 8)::bigint;
    end if;

    if big < (1::bigint << 31) then
      return big::integer;
    else
      return (big - (1::bigint << 32))::integer;
    end if;
  end;
$$
immutable;
