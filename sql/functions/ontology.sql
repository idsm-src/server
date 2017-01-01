create function iri_class (in id_value integer) returns varchar
{
    vectored;
    return (select iri from class_bases where id = id_value);
};

create function iri_class_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from class_bases where iri = iri_value);
};

grant execute on iri_class to "SPARQL";
grant execute on iri_class_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_property (in id_value integer) returns varchar
{
    vectored;
    return (select iri from property_bases where id = id_value);
};

create function iri_property_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from property_bases where iri = iri_value);
};

grant execute on iri_property to "SPARQL";
grant execute on iri_property_INVERSE to "SPARQL";
