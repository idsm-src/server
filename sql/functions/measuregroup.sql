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
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID([0-9]+)(_(PMID[0-9]*|[0-9]+))?$', '\1')::integer;
$$
immutable;


create function measuregroup_inv2(iri in varchar) returns integer language sql as
$$
  select case
    when part = '' then 2147483647
    when part = '_PMID' then -2147483647
    when part like '_PMID%' then -1 * regexp_replace(part, '^_PMID', '')::integer
    else regexp_replace(part, '^_', '')::integer
  end
  from (select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID[0-9]+', '') as part) as tab;
$$
immutable;
