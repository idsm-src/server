create function pubchem.bioassay(query in varchar) returns setof integer language sql as
$$
  select bioassay from pubchem.bioassay_data where to_tsvector('english', value) @@ to_tsquery('english', query)
  union
  select id from pubchem.bioassay_bases where to_tsvector('english', title) @@ to_tsquery('english', query);
$$
immutable parallel safe rows 100000;
