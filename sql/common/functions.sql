create function common.fulltext_match(text in varchar, query in varchar) returns boolean language sql as
$$
  select to_tsvector('english', text) @@ to_tsquery('english', query);
$$
immutable parallel safe;
