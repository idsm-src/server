create function query_format(id integer) returns varchar language sql as
$$
  select case id
    when 0 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#UnspecifiedFormat'
    when 1 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#SMILES'
    when 2 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#MolFile'
    when 3 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#RGroup'
  end;
$$
immutable parallel safe;


create function query_format_inverse(iri in varchar) returns integer language sql as
$$
  select case iri
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#UnspecifiedFormat' then 0
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#SMILES' then 1
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#MolFile' then 2
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#RGroup' then 3
  end;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function search_mode(id integer) returns varchar language sql as
$$
  select case id
    when 0 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#substructureSearch'
    when 1 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#exactSearch'
  end;
$$
immutable parallel safe;


create function search_mode_inverse(iri in varchar) returns integer language sql as
$$
  select case iri
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#substructureSearch' then 0
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#exactSearch' then 1
  end;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function charge_mode(id integer) returns varchar language sql as
$$
  select case id
    when 0 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreCharges'
    when 1 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultChargeAsZero'
    when 2 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultChargeAsAny'
  end;
$$
immutable parallel safe;


create function charge_mode_inverse(iri in varchar) returns integer language sql as
$$
  select case iri
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreCharges' then 0
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultChargeAsZero' then 1
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultChargeAsAny' then 2
  end;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function isotope_mode(id integer) returns varchar language sql as
$$
  select case id
    when 0 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreIsotopes'
    when 1 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultIsotopeAsStandard'
    when 2 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultIsotopeAsAny'
  end;
$$
immutable parallel safe;


create function isotope_mode_inverse(iri varchar) returns integer language sql as
$$
  select case iri
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreIsotopes' then 0
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultIsotopeAsStandard' then 1
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultIsotopeAsAny' then 2
  end;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function stereo_mode(id integer) returns varchar language sql as
$$
  select case id
    when 0 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreStrereo'
    when 1 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#strictStereo'
  end;
$$
immutable parallel safe;


create function stereo_mode_inverse(iri varchar) returns integer language sql as
$$
  select case iri
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreStrereo' then 0
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#strictStereo' then 1
  end;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function tautomer_mode(id integer) returns varchar language sql as
$$
  select case id
    when 0 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreTautomers'
    when 1 then 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#inchiTautomers'
  end;
$$
immutable parallel safe;


create function tautomer_mode_inverse(iri varchar) returns integer language sql as
$$
  select case iri
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreTautomers' then 0
    when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#inchiTautomers' then 1
  end;
$$
immutable parallel safe;
