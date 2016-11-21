create function iri_concept (in id_value integer) returns varchar
{
    vectored;
    return (select iri from concept_bases where id = id_value);
};

create function iri_concept_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from concept_bases where iri = iri_value);
};

grant execute on iri_concept to "SPARQL";
grant execute on iri_concept_INVERSE to "SPARQL";
