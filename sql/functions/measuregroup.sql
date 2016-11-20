create function iri_measuregroup (in bioassay integer, in measuregroup integer) returns varchar
{
    if(measuregroup = -2147483647)
    {
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d', bioassay);
    }

    if(measuregroup < 0)
    {
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_%d', bioassay, -1 * measuregroup);
    }

    if(measuregroup > 0)
    {
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_PMID%d', bioassay, measuregroup);
    }

    if(measuregroup = 0)
    {
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_PMID', bioassay);
    }

    return NULL;
};

create function iri_measuregroup_INV_1 (in id varchar) returns integer
{
    declare parts any;

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d', 0);
    if (parts is not null)
    {
        return parts[0];
    }

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_%U', 0);
    if (parts is not null)
    {
        return parts[0];
    }

    return NULL;
};

create function iri_measuregroup_INV_2 (in id varchar) returns integer
{
    declare parts any;

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d', 0);
    if (parts is not null)
    {
        return -2147483647;
    }

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_%d', 0);
    if (parts is not null)
    {
        return -1 * parts[1];
    }

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_PMID%d', 0);
    if (parts is not null)
    {
        return parts[1];
    }

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_PMID', 0);
    if (parts is not null)
    {
        return 0;
    }

    return NULL;
};

grant execute on iri_measuregroup to "SPARQL";
grant execute on iri_measuregroup_INV_1 to "SPARQL";
grant execute on iri_measuregroup_INV_2 to "SPARQL";
