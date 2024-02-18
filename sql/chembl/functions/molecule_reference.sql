create function chembl_tmp.url_decode(input text) returns text language plpgsql as
$$
  declare
    bin bytea = '';
    byte text;
  begin
    for byte in (select (regexp_matches(input, '(%..|.)', 'g'))[1]) loop
      if length(byte) = 3 then
        bin = bin || decode(substring(byte, 2, 2), 'hex');
      elseif  byte = '+' then
        bin = bin || ' '::bytea;
      else
        bin = bin || byte::bytea;
      end if;
    end loop;
    return convert_from(bin, 'utf8');
  end
$$
immutable strict parallel safe;
