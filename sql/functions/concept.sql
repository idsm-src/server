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

--------------------------------------------------------------------------------

create function iri_concept_type (in id_value integer) returns varchar
{
    vectored;

    if(id_value = 1)
        return 'http://www.w3.org/2004/02/skos/core#ConceptScheme';
    else
        return 'http://www.w3.org/2004/02/skos/core#Concept';
};

create function iri_concept_type_INVERSE (in iri_value varchar) returns integer
{
    vectored;

    if(iri_value = 'http://www.w3.org/2004/02/skos/core#ConceptScheme')
        return 1;
    else
        return 0;
};

grant execute on iri_concept_type to "SPARQL";
grant execute on iri_concept_type_INVERSE to "SPARQL";
