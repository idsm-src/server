create function iri_endpoint (in substance integer, in bioassay integer, in measuregroup integer) returns varchar
{
    vectored;

    if(measuregroup = -2147483647)
      return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d', substance, bioassay);

    if(measuregroup < 0)
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%d', substance, bioassay, -1 * measuregroup);

    if(measuregroup > 0)
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_PMID%d', substance, bioassay, measuregroup);

    if(measuregroup = 0)
        return sprintf ('http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_PMID', substance, bioassay);

    return null;
};

create function iri_endpoint_INV_1 (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%U', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

create function iri_endpoint_INV_2 (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d', 0);

    if (parts is not null)
        return parts[1];

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%U', 0);

    if (parts is not null)
        return parts[1];

    return null;
};

create function iri_endpoint_INV_3 (in id varchar) returns integer
{
    vectored;

    declare parts any;

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d', 0);

    if (parts is not null)
        return -2147483647;

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%d', 0);

    if (parts is not null)
        return -1 * parts[2];

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_PMID%d', 0);

    if (parts is not null)
        return parts[2];

    parts := sprintf_inverse (id, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_PMID', 0);

    if (parts is not null)
        return 0;

    return null;
};

grant execute on iri_endpoint to "SPARQL";
grant execute on iri_endpoint_INV_1 to "SPARQL";
grant execute on iri_endpoint_INV_2 to "SPARQL";
grant execute on iri_endpoint_INV_3 to "SPARQL";

--------------------------------------------------------------------------------

create function iri_endpoint_outcome (in id_value integer) returns varchar
{
    vectored;
    return (select iri from endpoint_outcomes__reftable where id = id_value);
};

create function iri_endpoint_outcome_INVERSE (in iri_value varchar) returns integer
{
    vectored;
    return (select id from endpoint_outcomes__reftable where iri = iri_value);
};

grant execute on iri_endpoint_outcome to "SPARQL";
grant execute on iri_endpoint_outcome_INVERSE to "SPARQL";
