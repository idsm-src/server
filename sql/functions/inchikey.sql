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

--------------------------------------------------------------------------------

create function iri_inchikey_subject(in type integer) returns varchar
{
    vectored;
    return sprintf('http://id.nlm.nih.gov/mesh/M%07d', type);
};

create function iri_inchikey_subject_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://id.nlm.nih.gov/mesh/M%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

grant execute on iri_inchikey_subject to "SPARQL";
grant execute on iri_inchikey_subject_INVERSE to "SPARQL";
