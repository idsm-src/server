create function iri_synonym (in id_value integer) returns varchar
{
    vectored;

    declare value any;
    value := (select md5 from synonym_bases where id = id_value);

    if(value is null)
        return null;

    return sprintf('http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_%s', value) ;
};

create function iri_synonym_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from synonym_bases where md5 = sprintf_inverse(iri_value, 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_%U', 2)[0]);
};

grant execute on iri_synonym to "SPARQL";
grant execute on iri_synonym_INVERSE to "SPARQL";
