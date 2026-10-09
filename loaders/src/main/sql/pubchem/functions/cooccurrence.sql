create function pubchem.compound_compound_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/CID' || subject || '_CID' || object;
$$
immutable parallel safe;


create function pubchem.compound_compound_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 53)::integer;
$$
immutable parallel safe;


create function pubchem.compound_compound_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 2), 4)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.compound_disease_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/CID' || subject || '_DZID' || object;
$$
immutable parallel safe;


create function pubchem.compound_disease_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 53)::integer;
$$
immutable parallel safe;


create function pubchem.compound_disease_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 2), 5)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.compound_genesymbol_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/CID' || subject || '_' || (select iri from pubchem.genesymbols where id = object);
$$
immutable parallel safe;


create function pubchem.compound_genesymbol_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 53)::integer;
$$
immutable parallel safe;


create function pubchem.compound_genesymbol_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.genesymbols where iri = regexp_replace(compound_genesymbol_cooccurrence_inv2.iri, '^[^_]*_', '');
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.compound_enzyme_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/CID' || subject || '_EC_' || (select iri from pubchem.enzymes where id = object);
$$
immutable parallel safe;


create function pubchem.compound_enzyme_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 53)::integer;
$$
immutable parallel safe;


create function pubchem.compound_enzyme_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.enzymes where iri = regexp_replace(compound_enzyme_cooccurrence_inv2.iri, '^[^_]*_EC_', '');
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.disease_compound_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/DZID' || subject || '_CID' || object;
$$
immutable parallel safe;


create function pubchem.disease_compound_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 54)::integer;
$$
immutable parallel safe;


create function pubchem.disease_compound_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 2), 4)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.disease_disease_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/DZID' || subject || '_DZID' || object;
$$
immutable parallel safe;


create function pubchem.disease_disease_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 54)::integer;
$$
immutable parallel safe;


create function pubchem.disease_disease_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 2), 5)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.disease_genesymbol_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/DZID' || subject || '_' || (select iri from pubchem.genesymbols where id = object);
$$
immutable parallel safe;


create function pubchem.disease_genesymbol_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 54)::integer;
$$
immutable parallel safe;


create function pubchem.disease_genesymbol_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.genesymbols where iri = regexp_replace(disease_genesymbol_cooccurrence_inv2.iri, '^[^_]*_', '');
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.disease_enzyme_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/DZID' || subject || '_EC_' || (select iri from pubchem.enzymes where id = object);
$$
immutable parallel safe;


create function pubchem.disease_enzyme_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 54)::integer;
$$
immutable parallel safe;


create function pubchem.disease_enzyme_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.enzymes where iri = regexp_replace(disease_enzyme_cooccurrence_inv2.iri, '^[^_]*_EC_', '');
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.genesymbol_compound_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/' || (select iri from pubchem.genesymbols where id = subject) || '_CID' || object;
$$
immutable parallel safe;


create function pubchem.genesymbol_compound_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.genesymbols where iri = regexp_replace(substring(genesymbol_compound_cooccurrence_inv1.iri, 50), '_[^_]*$', '');
$$
immutable parallel safe;


create function pubchem.genesymbol_compound_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
    select regexp_replace(iri, '^.*_CID', '')::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.enzyme_compound_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/EC_' || (select iri from pubchem.enzymes where id = subject) || '_CID' || object;
$$
immutable parallel safe;


create function pubchem.enzyme_compound_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.enzymes where iri = regexp_replace(substring(enzyme_compound_cooccurrence_inv1.iri, 53), '_[^_]*$', '');
$$
immutable parallel safe;


create function pubchem.enzyme_compound_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
    select regexp_replace(iri, '^.*_CID', '')::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.genesymbol_disease_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/' || (select iri from pubchem.genesymbols where id = subject) || '_DZID' || object;
$$
immutable parallel safe;


create function pubchem.genesymbol_disease_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.genesymbols where iri = regexp_replace(substring(genesymbol_disease_cooccurrence_inv1.iri, 50), '_[^_]*$', '');
$$
immutable parallel safe;


create function pubchem.genesymbol_disease_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
    select regexp_replace(iri, '^.*_DZID', '')::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.enzyme_disease_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/EC_' || (select iri from pubchem.enzymes where id = subject) || '_DZID' || object;
$$
immutable parallel safe;


create function pubchem.enzyme_disease_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.enzymes where iri = regexp_replace(substring(enzyme_disease_cooccurrence_inv1.iri, 53), '_[^_]*$', '');
$$
immutable parallel safe;


create function pubchem.enzyme_disease_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
    select regexp_replace(iri, '^.*_DZID', '')::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.genesymbol_genesymbol_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/' || (select iri from pubchem.genesymbols where id = subject) || '_' || (select iri from pubchem.genesymbols where id = object);
$$
immutable parallel safe;


create function pubchem.genesymbol_genesymbol_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.genesymbols where starts_with(substring(genesymbol_genesymbol_cooccurrence_inv1.iri, 50), iri || '_');
$$
immutable parallel safe;


create function pubchem.genesymbol_genesymbol_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.genesymbols where right(genesymbol_genesymbol_cooccurrence_inv2.iri, length(iri) + 1) = ('_' || iri);
$$
immutable parallel safe;
