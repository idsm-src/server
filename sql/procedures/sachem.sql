create function queryFormat_inverse(iri in varchar) returns integer language plpgsql as
$$
  declare value record;
  begin
    select into value 
      case iri
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#UnspecifiedFormat' then 0
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#SMILES' then 1
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#MolFile' then 2
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#RGroup' then 3
      end::integer AS retval;
    return value.retval;
  end;
$$
immutable;


create function graphMode_inverse(iri in varchar) returns integer language plpgsql as
$$
  declare value record;
  begin
    select into value 
      case iri
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#substructureSearch' then 0
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#exactSearch' then 1
      end::integer AS retval;
    return value.retval;
  end;
$$
immutable;


create function chargeMode_inverse(iri in varchar) returns integer language plpgsql as
$$
  declare value record;
  begin
    select into value 
      case iri
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreCharges' then 0
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultChargeAsZero' then 1
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultChargeAsAny' then 2
      end::integer AS retval;
    return value.retval;
  end;
$$
immutable;


create function isotopeMode_inverse(iri in varchar) returns integer language plpgsql as
$$
  declare value record;
  begin
    select into value 
      case iri
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreIsotopes' then 0
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultIsotopeAsStandard' then 1
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#defaultIsotopeAsAny' then 2
      end::integer AS retval;
    return value.retval;
  end;
$$
immutable;


create function stereoMode_inverse(iri in varchar) returns integer language plpgsql as
$$
  declare value record;
  begin
    select into value 
      case iri
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreStrereo' then 0
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#strictStereo' then 1
      end::integer AS retval;
    return value.retval;
  end;
$$
immutable;


create function tautomerMode_inverse(iri in varchar) returns integer language plpgsql as
$$
  declare value record;
  begin
    select into value 
      case iri
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#ignoreTautomers' then 0
        when 'http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#inchiTautomers' then 1
      end::integer AS retval;
    return value.retval;
  end;
$$
immutable;
