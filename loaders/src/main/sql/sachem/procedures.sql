create function sachem.substructure_search_stub(varchar, varchar, sachem.search_mode = 'SUBSTRUCTURE', sachem.charge_mode = 'DEFAULT_AS_ANY', sachem.isotope_mode = 'IGNORE', sachem.radical_mode = 'DEFAULT_AS_ANY', sachem.stereo_mode = 'IGNORE', sachem.aromaticity_mode = 'AUTO', sachem.tautomer_mode = 'IGNORE', varchar = 'UNSPECIFIED', numeric = -1, boolean = false, numeric = 0) returns table (compound int, score float8) language sql as
$$
  select compound, score::float8 from sachem.substructure_search($1, $2, $3, $4, $5, $6, $7, $8, $9, $11::int, $12, $13::int);
$$
immutable parallel safe strict;


create function sachem.similarity_search_stub(varchar, varchar, float8 = 0.85, numeric = 1, sachem.aromaticity_mode = 'AUTO', sachem.tautomer_mode = 'IGNORE', varchar = 'UNSPECIFIED', numeric = -1, boolean = false) returns table (compound int, score float8) language sql as
$$
  select compound, score::float8 from sachem.similarity_search($1, $2, $3::float4, $4::int, $5, $6, $8::int, $9);
$$
immutable parallel safe strict;


create function sachem.similarity_stub(varchar, varchar, numeric = 1, sachem.aromaticity_mode = 'AUTO') returns float8 language sql as
$$
  select sachem.similarity($1, $2, $3::int, $4);
$$
immutable parallel safe strict;


create function sachem.substructure_search_multi(varchar[], varchar, sachem.search_mode = 'SUBSTRUCTURE', sachem.charge_mode = 'DEFAULT_AS_ANY', sachem.isotope_mode = 'IGNORE', sachem.radical_mode = 'DEFAULT_AS_ANY', sachem.stereo_mode = 'IGNORE', sachem.aromaticity_mode = 'AUTO', sachem.tautomer_mode = 'IGNORE', varchar = 'UNSPECifIED', numeric = -1, boolean = false, numeric = 0) returns setof record language plpgsql as
$$
  declare
    col varchar;
    index varchar;
    query varchar;
  begin
    query = '';

    foreach index in array $1
    loop
        if query != '' then
          query = query || ' union all ';
        end if;

        query = query || 'select score::float8';

        foreach col in array $1
        loop
          if col = index then
              query = query || ', compound as ' || index;
          else
              query = query || ', NULL::integer';
          end if;
        end loop;

        query = query || '  from sachem.substructure_search(''' || index || ''', $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)';
    end loop;

    return query execute query using $2, $3, $4, $5, $6, $7, $8, $9, $11::int, $12, $13::int;
  end
$$
immutable parallel safe strict;


create function sachem.similarity_search_multi(varchar[], varchar, float8 = 0.85, numeric = 1, sachem.aromaticity_mode = 'AUTO', sachem.tautomer_mode = 'IGNORE', varchar = 'UNSPECIFIED', numeric = -1, boolean = false) returns setof record language plpgsql as
$$
  declare
    col varchar;
    index varchar;
    query varchar;
  begin
    query = '';

    foreach index in array $1
    loop
        if query != '' then
          query = query || ' union all ';
        end if;

        query = query || 'select score::float8';

        foreach col in array $1
        loop
          if col = index then
              query = query || ', compound as ' || index;
          else
              query = query || ', NULL::integer';
          end if;
        end loop;

        query = query || '  from sachem.similarity_search(''' || index || ''', $1, $2, $3, $4, $5, $6, $7)';
    end loop;

    if $8 != -1 then
      query = 'select * from (' || query || ') tab order by score desc limit $6';
    elsif $9 = true then
      query = 'select * from (' || query || ') tab order by score desc';
    end if;

    return query execute query using $2, $3::float4, $4::int, $5, $6, $8::int, $9;
  end
$$
immutable parallel safe strict;


create function sachem.substructure_search_all(varchar, sachem.search_mode = 'SUBSTRUCTURE', sachem.charge_mode = 'DEFAULT_AS_ANY', sachem.isotope_mode = 'IGNORE', sachem.radical_mode = 'DEFAULT_AS_ANY', sachem.stereo_mode = 'IGNORE', sachem.aromaticity_mode = 'AUTO', sachem.tautomer_mode = 'IGNORE', varchar = 'UNSPECIFIED', numeric = -1, boolean = false, numeric = 0) returns table (score float8, pubchem integer, chembl integer, chebi integer, wikidata integer, mona integer, isdb integer, drugbank integer) as
$$
  select * from sachem.substructure_search_multi('{"pubchem","chembl","chebi","wikidata","mona","isdb","drugbank"}'::varchar[], $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
    as (score float8, pubchem integer, chembl integer, chebi integer, wikidata integer, mona integer, isdb integer, drugbank integer);
$$
language sql immutable strict;


create function sachem.similarity_search_all(varchar, float8 = 0.85, numeric = 1, sachem.aromaticity_mode = 'AUTO', sachem.tautomer_mode = 'IGNORE', varchar = 'UNSPECIFIED', numeric = -1, boolean = false)returns table (score float8, pubchem integer, chembl integer, chebi integer, wikidata integer, mona integer, isdb integer, drugbank integer) as
$$
  select * from sachem.similarity_search_multi('{"pubchem","chembl","chebi","wikidata","mona","isdb","drugbank"}'::varchar[], $1, $2, $3, $4, $5, $6, $7, $8)
    as (score float8, pubchem integer, chembl integer, chebi integer, wikidata integer, mona integer, isdb integer, drugbank integer);
$$
language sql immutable strict;

