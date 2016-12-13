create function iri_protein (in id_value integer) returns varchar
{
    vectored;

    declare part any;
    part := (select name from protein_bases where id = id_value);

    if(part is null)
        return null;

    return sprintf('http://rdf.ncbi.nlm.nih.gov/pubchem/protein/%s', part) ;
};

create function iri_protein_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from protein_bases where name = sprintf_inverse(iri_value, 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/%U', 2)[0]);
};

grant execute on iri_protein to "SPARQL";
grant execute on iri_protein_INVERSE to "SPARQL";
