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

--------------------------------------------------------------------------------

create function iri_cmesh(in type integer) returns varchar
{
    vectored;
    return sprintf('http://id.nlm.nih.gov/mesh/C%09d', type);
};

create function iri_cmesh_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://id.nlm.nih.gov/mesh/C%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

grant execute on iri_cmesh to "SPARQL";
grant execute on iri_cmesh_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_dmesh(in type integer) returns varchar
{
    vectored;
    return sprintf('http://id.nlm.nih.gov/mesh/D%06d', type);
};

create function iri_dmesh_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://id.nlm.nih.gov/mesh/D%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

grant execute on iri_dmesh to "SPARQL";
grant execute on iri_dmesh_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_dqmesh(in descriptor integer, in qualifier integer) returns varchar
{
    vectored;
    return sprintf('http://id.nlm.nih.gov/mesh/D%06dQ%06d', type, );
};

create function iri_dqmesh_INV_1 (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://id.nlm.nih.gov/mesh/D%dQ%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

create function iri_dqmesh_INV_2 (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://id.nlm.nih.gov/mesh/D%dQ%d', 0);

    if (parts is not null)
        return parts[1];

    return null;
};

grant execute on iri_dqmesh to "SPARQL";
grant execute on iri_dqmesh_INV_1 to "SPARQL";
grant execute on iri_dqmesh_INV_2 to "SPARQL";

--------------------------------------------------------------------------------

create function iri_go(in type integer) returns varchar
{
    vectored;
    return sprintf('http://purl.obolibrary.org/obo/GO_%07d', type);
};

create function iri_go_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://purl.obolibrary.org/obo/GO_%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

grant execute on iri_go to "SPARQL";
grant execute on iri_go_INVERSE to "SPARQL";

--------------------------------------------------------------------------------

create function iri_pr(in type integer) returns varchar
{
    vectored;
    return sprintf('http://purl.obolibrary.org/obo/PR_%09d', type);
};

create function iri_pr_INVERSE (in id varchar) returns integer
{
    vectored;

    declare parts any;
    parts := sprintf_inverse(id, 'http://purl.obolibrary.org/obo/PR_%d', 0);

    if (parts is not null)
        return parts[0];

    return null;
};

grant execute on iri_pr to "SPARQL";
grant execute on iri_pr_INVERSE to "SPARQL";
