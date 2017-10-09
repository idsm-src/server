create function "bioassay"(query in varchar) returns setof integer language sql as
$$
  select bioassay from bioassay_data where to_tsvector('english', value) @@ to_tsquery('english', query)
  union
  select id from bioassay_bases where to_tsvector('english', title) @@ to_tsquery('english', query);
$$
immutable rows 100000;
