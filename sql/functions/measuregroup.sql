create function measuregroup(bioassay in integer, measuregroup in integer) returns varchar language sql as
$$
  select case
    when measuregroup = 2147483647  then 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID' || bioassay
    when measuregroup = -2147483647 then 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID' || bioassay || '_PMID'
    when measuregroup < 0           then 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID' || bioassay || '_PMID' || (-1 * measuregroup)
    when measuregroup >= 0          then 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID' || bioassay || '_' || measuregroup
    else null
  end;
$$
immutable;


create function measuregroup_inv1(iri in varchar) returns integer language sql as
$$
  select substring(iri, 53, strpos(iri, '_') - 53)::integer;
$$
immutable;


create function measuregroup_inv2(iri in varchar) returns integer language sql as
$$
  select case
    when part = '' then 2147483647
    when part = 'PMID' then -2147483647
    when left(part, 4) = 'PMID' then -1 * substring(part, 5)::integer
    else part::integer
  end
  from (select coalesce(split_part(iri, '_', 2), '') as part) as tab;
$$
immutable;
