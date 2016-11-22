create function iri_bioassay_data (in bioassay integer, in type integer) returns varchar
{
    vectored;

    if(type = 136)
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID%d_Description', bioassay);

    if(type = 1041)
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID%d_Protocol', bioassay);

    if(type = 1167)
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID%d_Comment', bioassay);

    return null;
};

create function iri_bioassay_data_INV_1 (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID%d_%U', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

create function iri_bioassay_data_INV_2 (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID%d_%U', 0);

    if (parts is not null)
    {
        if(parts[1] = 'Description')
            return 136;

        if(parts[1] = 'Protocol')
            return 1041;

        if(parts[1] = 'Comment')
            return 1167;
    }

  return null;
};

grant execute on iri_bioassay_data to "SPARQL";
grant execute on iri_bioassay_data_INV_1 to "SPARQL";
grant execute on iri_bioassay_data_INV_2 to "SPARQL";
