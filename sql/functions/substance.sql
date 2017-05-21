create function substance(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID' || id;
$$
immutable;


create function substance_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID', '')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function substance_chembl(id in integer) returns varchar language sql as
$$
  select case
    when id >= 0 then 'http://linkedchemistry.info/chembl/chemblid/CHEMBL' || id
    else 'http://linkedchemistry.info/chembl/chemblid/SCHEMBL' || -id
  end
$$
immutable;


create function substance_chembl_inverse(iri in varchar) returns integer language sql as
$$
  select case
    when iri like '%SCHEMBL%' then -1 * regexp_replace(iri, '^http://linkedchemistry.info/chembl/chemblid/SCHEMBL', '')::integer
    else regexp_replace(iri, '^http://linkedchemistry.info/chembl/chemblid/CHEMBL', '')::integer
  end;
$$
immutable;

--------------------------------------------------------------------------------

create function substance_ebi_chembl(id in integer) returns varchar language sql as
$$
  select case
    when id >= 0 then 'http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL' || id
    else 'http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL' || -id
  end
$$
immutable;


create function substance_ebi_chembl_inverse(iri in varchar) returns integer language sql as
$$
  select case
    when iri like '%SCHEMBL%' then -1 * regexp_replace(iri, '^http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL', '')::integer
    else regexp_replace(iri, '^http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL', '')::integer
  end;
$$
immutable;
