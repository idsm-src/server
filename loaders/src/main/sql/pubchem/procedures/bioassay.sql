create function pubchem.bioassay(query in varchar) returns setof integer language sql as
$$
  select bioassay from pubchem.bioassay_texts where to_tsvector('english', text) @@ to_tsquery('english', query)
  union
  select id from pubchem.bioassays where to_tsvector('english', title) @@ to_tsquery('english', query);
$$
immutable parallel safe rows 100000;
