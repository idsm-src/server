sparql drop iri class iri:sio .;
sparql drop iri class iri:bao .;
sparql drop iri class iri:cheminf .;
sparql drop iri class iri:mesh .;
sparql drop iri class iri:cmesh .;
sparql drop iri class iri:dmesh .;
sparql drop iri class iri:dqmesh .;

--------------------------------------------------------------------------------

create function iri_sio;
create function iri_sio_INVERSE;
create function iri_bao;
create function iri_bao_INVERSE;
create function iri_cheminf;
create function iri_cheminf_INVERSE;
create function iri_mesh;
create function iri_mesh_INVERSE;
create function iri_cmesh;
create function iri_cmesh_INVERSE;
create function iri_dmesh;
create function iri_dmesh_INVERSE;
create function iri_dqmesh;
create function iri_dqmesh_INV_1;
create function iri_dqmesh_INV_2;
