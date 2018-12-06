create function substance(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID' || id;
$$
immutable;


create function substance_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri,  50)::integer;
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
    when iri like 'http://linkedchemistry.info/chembl/chemblid/SCHEMBL%' then -1 * substring(iri, 52)::integer
    else substring(iri, 51)::integer
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
    when iri like 'http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL%' then -1 * substring(iri, 54)::integer
    else substring(iri, 53)::integer
  end;
$$
immutable;
