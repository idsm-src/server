create function iri_compound_relation (in id_value integer) returns varchar
{
    vectored;
    return (select iri from compound_relations__reftable where id = id_value);
};

create function iri_compound_relation_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from compound_relations__reftable where iri = iri_value);
};

grant execute on iri_compound_relation to "SPARQL";
grant execute on iri_compound_relation_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_compound_role (in id_value integer) returns varchar
{
    vectored;
    return (select iri from compound_roles__reftable where id = id_value);
};

create function iri_compound_role_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from compound_roles__reftable where iri = iri_value);
};

grant execute on iri_compound_role to "SPARQL";
grant execute on iri_compound_role_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_compound_type (in unit_value integer, in type_value integer) returns varchar
{
    vectored;

    declare prefix varchar;
    prefix := (select iri from compound_type_units__reftable where id = unit_value);

    if (prefix is null or type_value = -1)
      return prefix;
    else
      return concat(prefix, type_value);
};

create function iri_compound_type_INV_1 (in iri_value varchar) returns integer
{
    vectored;
    return (select id from compound_type_units__reftable where iri_value like concat(iri, '%'));
};

create function iri_compound_type_INV_2 (in iri_value varchar) returns integer
{
    vectored;

    declare ret integer;

    ret := (select top 1 result[0] from (
        select sprintf_inverse(iri_value, concat(iri, '%d'), 0) as result
            from compound_type_units__reftable
        ) as tbl where result is not null);

    if (ret is not null)
        return ret;
    else
        return (select -1 from compound_type_units__reftable where iri_value=iri);
};

grant execute on iri_compound_type to "SPARQL";
grant execute on iri_compound_type_INV_1 to "SPARQL";
grant execute on iri_compound_type_INV_2 to "SPARQL";
