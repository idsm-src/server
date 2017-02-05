create function iri_substance_chembl(in id integer) returns varchar
{
    vectored;
    
    if(type >= 0)
        return sprintf('http://linkedchemistry.info/chembl/chemblid/CHEMBL%d', id);
    else
        return sprintf('http://linkedchemistry.info/chembl/chemblid/SCHEMBL%d', id);
};

create function iri_substance_chembl_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://linkedchemistry.info/chembl/chemblid/CHEMBL%d', 0);

    if (parts is not null)
        return parts[0];

    parts := sprintf_inverse(id, 'http://linkedchemistry.info/chembl/chemblid/SCHEMBL%d', 0);

    if (parts is not null)
        return -parts[0];

    return null;
};

grant execute on iri_substance_chembl to "SPARQL";
grant execute on iri_substance_chembl_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_substance_ebi_chembl(in id integer) returns varchar
{
    vectored;
    
    if(type >= 0)
        return sprintf('http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL%d', id);
    else
        return sprintf('http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL%d', id);
};

create function iri_substance_ebi_chembl_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL%d', 0);

    if (parts is not null)
        return parts[0];

    parts := sprintf_inverse(id, 'http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL%d', 0);

    if (parts is not null)
        return -parts[0];

    return null;
};

grant execute on iri_substance_ebi_chembl to "SPARQL";
grant execute on iri_substance_ebi_chembl_INVERSE to "SPARQL";
