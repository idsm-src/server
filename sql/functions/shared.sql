create function iri_sio(in type integer) returns varchar
{
    vectored;
    return sprintf('http://semanticscience.org/resource/SIO_%06d', type);
};

create function iri_sio_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://semanticscience.org/resource/SIO_%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

grant execute on iri_sio to "SPARQL";
grant execute on iri_sio_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_bao(in type integer) returns varchar
{
    vectored;
    return sprintf('http://www.bioassayontology.org/bao#BAO_%07d', type);
};

create function iri_bao_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://www.bioassayontology.org/bao#BAO_%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

grant execute on iri_bao to "SPARQL";
grant execute on iri_bao_INVERSE to "SPARQL";
