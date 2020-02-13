create function "compound"(query in varchar) returns table (compound int, name varchar) language sql as
$$
  select compound, preferred_iupac_name from pubchem.descriptor_compound_preferred_iupac_names
      where to_tsvector('english', preferred_iupac_name) @@ to_tsquery('english', query)
  union
  select cmp.compound, val.value from pubchem.synonym_values val, pubchem.synonym_compounds cmp
      where to_tsvector('english', val.value) @@ to_tsquery('english', query) and val.synonym = cmp.synonym;
$$
immutable parallel safe rows 10000;
