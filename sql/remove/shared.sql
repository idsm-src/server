sparql drop iri class iri:sio .;
sparql drop iri class iri:bao .;
sparql drop iri class iri:cheminf .;
sparql drop iri class iri:mesh .;

--------------------------------------------------------------------------------

create function iri_sio;
create function iri_sio_INVERSE;
create function iri_bao;
create function iri_bao_INVERSE;
create function iri_cheminf;
create function iri_cheminf_INVERSE;
create function iri_mesh;
create function iri_mesh_INVERSE;
