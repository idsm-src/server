create function iri_inchikey (in id_value integer) returns varchar
{
    vectored;

    declare inchi any;
    inchi := (select inchikey from inchikey_bases where id = id_value);

    if(inchi is null)
        return null;

    return sprintf('http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/%s', inchi) ;
};

create function iri_inchikey_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from inchikey_bases where inchikey = sprintf_inverse(iri_value, 'http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/%U', 2)[0]);
};

grant execute on iri_inchikey to "SPARQL";
grant execute on iri_inchikey_INVERSE to "SPARQL";
