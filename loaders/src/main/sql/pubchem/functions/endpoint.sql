create function pubchem.endpoint(substance in integer, bioassay in integer, measuregroup in integer, value in integer) returns varchar language sql as
$$
  select case
    when measuregroup = 2147483647  then 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID' || substance || '_AID' || bioassay || '_VALUE' || value
    when measuregroup = -2147483647 then 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID' || substance || '_AID' || bioassay || '_PMID' || '_VALUE' || value
    when measuregroup < 0           then 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID' || substance || '_AID' || bioassay || '_PMID' || (-1 * measuregroup) || '_VALUE' || value
    when measuregroup >= 0          then 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID' || substance || '_AID' || bioassay || '_' || measuregroup || '_VALUE' || value
  end;
$$
immutable parallel safe;


create function pubchem.endpoint_inv1(iri in varchar) returns integer language sql as
$$
  select substring(iri, 49, strpos(iri, '_') - 49)::integer;
$$
immutable parallel safe;


create function pubchem.endpoint_inv2(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 2), 4)::integer;
$$
immutable parallel safe;


create function pubchem.endpoint_inv3(iri in varchar) returns integer language sql as
$$
  select case
    when left(part, 5) = 'VALUE' then 2147483647
    when part = 'PMID' then -2147483647
    when left(part, 4) = 'PMID' then -1 * substring(part, 5)::integer
    else part::integer
  end
  from (select coalesce(split_part(iri, '_', 3), '') as part) as tab;
$$
immutable parallel safe;


create function pubchem.endpoint_inv4(iri in varchar) returns integer language sql as
$$
  select substr(iri, strpos(iri, '_VALUE') + 6)::integer;
$$
immutable parallel safe;
