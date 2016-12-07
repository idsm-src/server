create function iri_reference_type (in id_value integer) returns varchar
{
    vectored;
    return (select iri from reference_types__reftable where id = id_value);
};

create function iri_reference_type_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from reference_types__reftable where iri = iri_value);
};

grant execute on iri_reference_type to "SPARQL";
grant execute on iri_reference_type_INVERSE to "SPARQL";
