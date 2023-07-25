create function pubchem.compound_fulltext(query in varchar) returns table (compound int, score float4, name varchar) language sql as
$$
  select distinct on (compound) compound, score, name from (
    select compound, trgm.similarity(title, query) as score, title as name
        from pubchem.compound_titles
        where to_tsvector('simple', title) @@ to_tsquery('simple', query)
    union all
    select compound, trgm.similarity(preferred_iupac_name, query) as score, preferred_iupac_name as name
        from pubchem.descriptor_compound_preferred_iupac_names
        where to_tsvector('simple', preferred_iupac_name) @@ to_tsquery('simple', query)
    union all
    select cmp.compound as compound, trgm.similarity(val.value, query) as score, val.value as name
        from pubchem.synonym_values val, pubchem.synonym_compounds cmp
        where val.synonym = cmp.synonym and to_tsvector('simple', val.value) @@ to_tsquery('simple', query)
  ) tab
  order by compound, score desc;
$$
immutable parallel safe rows 10000;
