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

--------------------------------------------------------------------------------

create function iri_cheminf(in type integer) returns varchar
{
    vectored;
    return sprintf('http://semanticscience.org/resource/CHEMINF_%06d', type);
};

create function iri_cheminf_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://semanticscience.org/resource/CHEMINF_%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

grant execute on iri_cheminf to "SPARQL";
grant execute on iri_cheminf_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_mesh(in type integer) returns varchar
{
    vectored;
    return sprintf('http://id.nlm.nih.gov/mesh/M%07d', type);
};

create function iri_mesh_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://id.nlm.nih.gov/mesh/M%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

grant execute on iri_mesh to "SPARQL";
grant execute on iri_mesh_INVERSE to "SPARQL";
