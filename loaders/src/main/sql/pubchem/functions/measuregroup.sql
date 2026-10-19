-- The integers are cast to text explicitly, so that the functions without a sub-select are inlined: '||' with an integer
-- resolves to the stable function textanycat, and an immutable function whose body is stable is not inlined by the planner.
-- Not strict: a strict function whose body is a CASE expression is not inlined either, and skipping the call for a
-- null argument saves nothing once the body is inlined.

create function pubchem.measuregroup(bioassay in integer, measuregroup in integer) returns varchar language sql as
$$
  select case
    when measuregroup = 2147483647  then 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID' || bioassay::text
    when measuregroup = 2147483646  then 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID' || bioassay::text || '_'
    when measuregroup = -2147483647 then 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID' || bioassay::text || '_PMID'
    when measuregroup < 0           then 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID' || bioassay::text || '_PMID' || (-1 * measuregroup)::text
    when measuregroup >= 0          then 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID' || bioassay::text || '_' || measuregroup::text
    else null
  end;
$$
immutable parallel safe;


create function pubchem.measuregroup_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 53)::integer;
$$
immutable parallel safe strict;


create function pubchem.measuregroup_inv2(iri in varchar) returns integer language sql as
$$
  select case
    when part = '' and right(iri, 1) != '_' then 2147483647
    when part = '' and right(iri, 1) = '_' then 2147483646
    when part = 'PMID' then -2147483647
    when left(part, 4) = 'PMID' then -1 * substring(part, 5)::integer
    else part::integer
  end
  from (select split_part(iri, '_', 2) as part) as tab;
$$
immutable parallel safe strict;
