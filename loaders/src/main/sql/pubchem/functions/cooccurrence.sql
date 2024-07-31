create function pubchem.chemical_chemical_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/CID' || subject || '_CID' || object;
$$
immutable parallel safe;


create function pubchem.chemical_chemical_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 53)::integer;
$$
immutable parallel safe;


create function pubchem.chemical_chemical_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 2), 4)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.chemical_disease_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/CID' || subject || '_DZID' || object;
$$
immutable parallel safe;


create function pubchem.chemical_disease_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 53)::integer;
$$
immutable parallel safe;


create function pubchem.chemical_disease_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 2), 5)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.chemical_gene_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/CID' || subject || '_' || (select iri from pubchem.gene_symbol_bases where id = object);
$$
immutable parallel safe;


create function pubchem.chemical_gene_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 53)::integer;
$$
immutable parallel safe;


create function pubchem.chemical_gene_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.gene_symbol_bases where iri = regexp_replace(chemical_gene_cooccurrence_inv2.iri, '^[^_]*_', '');
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.chemical_enzyme_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/CID' || subject || '_EC_' || (select iri from pubchem.enzyme_bases where id = object);
$$
immutable parallel safe;


create function pubchem.chemical_enzyme_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 53)::integer;
$$
immutable parallel safe;


create function pubchem.chemical_enzyme_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.enzyme_bases where iri = regexp_replace(chemical_enzyme_cooccurrence_inv2.iri, '^[^_]*_EC_', '');
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.disease_chemical_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/DZID' || subject || '_CID' || object;
$$
immutable parallel safe;


create function pubchem.disease_chemical_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 54)::integer;
$$
immutable parallel safe;


create function pubchem.disease_chemical_cooccurrence_inv2(iri in varchar) returns integer language sql as
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

create function pubchem.disease_gene_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/DZID' || subject || '_' || (select iri from pubchem.gene_symbol_bases where id = object);
$$
immutable parallel safe;


create function pubchem.disease_gene_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 54)::integer;
$$
immutable parallel safe;


create function pubchem.disease_gene_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.gene_symbol_bases where iri = regexp_replace(disease_gene_cooccurrence_inv2.iri, '^[^_]*_', '');
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.disease_enzyme_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/DZID' || subject || '_EC_' || (select iri from pubchem.enzyme_bases where id = object);
$$
immutable parallel safe;


create function pubchem.disease_enzyme_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select substring(split_part(iri, '_', 1), 54)::integer;
$$
immutable parallel safe;


create function pubchem.disease_enzyme_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.enzyme_bases where iri = regexp_replace(disease_enzyme_cooccurrence_inv2.iri, '^[^_]*_EC_', '');
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.gene_chemical_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/' || (select iri from pubchem.gene_symbol_bases where id = subject) || '_CID' || object;
$$
immutable parallel safe;


create function pubchem.gene_chemical_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.gene_symbol_bases where iri = regexp_replace(substring(gene_chemical_cooccurrence_inv1.iri, 50), '_[^_]*$', '');
$$
immutable parallel safe;


create function pubchem.gene_chemical_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
    select regexp_replace(iri, '^.*_CID', '')::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.enzyme_chemical_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/EC_' || (select iri from pubchem.enzyme_bases where id = subject) || '_CID' || object;
$$
immutable parallel safe;


create function pubchem.enzyme_chemical_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.enzyme_bases where iri = regexp_replace(substring(enzyme_chemical_cooccurrence_inv1.iri, 53), '_[^_]*$', '');
$$
immutable parallel safe;


create function pubchem.enzyme_chemical_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
    select regexp_replace(iri, '^.*_CID', '')::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.gene_disease_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/' || (select iri from pubchem.gene_symbol_bases where id = subject) || '_DZID' || object;
$$
immutable parallel safe;


create function pubchem.gene_disease_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.gene_symbol_bases where iri = regexp_replace(substring(gene_disease_cooccurrence_inv1.iri, 50), '_[^_]*$', '');
$$
immutable parallel safe;


create function pubchem.gene_disease_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
    select regexp_replace(iri, '^.*_DZID', '')::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.enzyme_disease_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/EC_' || (select iri from pubchem.enzyme_bases where id = subject) || '_DZID' || object;
$$
immutable parallel safe;


create function pubchem.enzyme_disease_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.enzyme_bases where iri = regexp_replace(substring(enzyme_disease_cooccurrence_inv1.iri, 53), '_[^_]*$', '');
$$
immutable parallel safe;


create function pubchem.enzyme_disease_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
    select regexp_replace(iri, '^.*_DZID', '')::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function pubchem.gene_gene_cooccurrence(subject in integer, object in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/' || (select iri from pubchem.gene_symbol_bases where id = subject) || '_' || (select iri from pubchem.gene_symbol_bases where id = object);
$$
immutable parallel safe;


create function pubchem.gene_gene_cooccurrence_inv1(iri in varchar) returns integer language sql as
$$
  select id from pubchem.gene_symbol_bases where starts_with(substring(gene_gene_cooccurrence_inv1.iri, 50), iri || '_');
$$
immutable parallel safe;


create function pubchem.gene_gene_cooccurrence_inv2(iri in varchar) returns integer language sql as
$$
  select id from pubchem.gene_symbol_bases where right(gene_gene_cooccurrence_inv2.iri, length(iri) + 1) = ('_' || iri);
$$
immutable parallel safe;
