create function endpoint(substance in integer, bioassay in integer, measuregroup in integer) returns varchar language sql as
$$
  select case
    when measuregroup = 2147483647  then 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID' || substance || '_AID' || bioassay
    when measuregroup = -2147483647 then 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID' || substance || '_AID' || bioassay || '_PMID'
    when measuregroup < 0           then 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID' || substance || '_AID' || bioassay || '_PMID' || (-1 * measuregroup)
    when measuregroup >= 0          then 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID' || substance || '_AID' || bioassay || '_' || measuregroup
    else null
  end;
$$
immutable;


create function endpoint_inv1(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID([0-9]+)_AID[0-9]+(_(PMID[0-9]*|[0-9]+))?$', '\1')::integer;
$$
immutable;


create function endpoint_inv2(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID[0-9]+_AID([0-9]+)(_(PMID[0-9]*|[0-9]+))?$', '\1')::integer;
$$
immutable;


create function endpoint_inv3(iri in varchar) returns integer language sql as
$$
  select case
    when part = '' then 2147483647
    when part = '_PMID' then -2147483647
    when part like '_PMID%' then -1 * regexp_replace(part, '^_PMID', '')::integer
    else regexp_replace(part, '^_', '')::integer
  end
  from (select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID[0-9]+_AID[0-9]+', '') as part) as tab;
$$
immutable;

--------------------------------------------------------------------------------

create function outcome(id in smallint) returns varchar language sql as
$$
  select iri from endpoint_outcomes__reftable where endpoint_outcomes__reftable.id = outcome.id;
$$
immutable;


create function outcome_inverse(iri in varchar) returns smallint language sql as
$$
  select id from endpoint_outcomes__reftable where endpoint_outcomes__reftable.iri = outcome_inverse.iri;
$$
immutable;
