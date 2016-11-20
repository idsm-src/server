create function iri_source (in id_value integer) returns varchar
{
    vectored;
    return (select iri from source_bases where id = id_value);
};

create function iri_source_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from source_bases where iri = iri_value);
};

grant execute on iri_source to "SPARQL";
grant execute on iri_source_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_source_subject (in id_value integer) returns varchar
{
    vectored;
    return (select iri from source_subjects__reftable where id = id_value);
};

create function iri_source_subject_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from source_subjects__reftable where iri = iri_value);
};

grant execute on iri_source_subject to "SPARQL";
grant execute on iri_source_subject_INVERSE to "SPARQL";
