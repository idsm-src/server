create function bioassay(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID' || id;
$$
immutable;


create function bioassay_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 49)::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function bioassay_data(bioassay in integer, type in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID' || bioassay ||
      case type 
        when 136 then '_Description' 
        when 1041 then '_Protocol'  
        when 1167 then '_Comment' 
        else null 
      end;
$$
immutable;


create function bioassay_data_inv1(iri in varchar) returns integer language sql as
$$
  select substring(iri, 49, strpos(iri, '_') - 49)::integer;
$$
immutable;


create function bioassay_data_inv2(iri in varchar) returns integer language sql as
$$
  select case substring(iri, strpos(iri, '_'))
    when '_Description' then 136
    when '_Protocol'    then 1041
    when '_Comment'     then 1167
    else null
  end;
$$
immutable;
