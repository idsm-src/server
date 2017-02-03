sparql drop iri class iri:sio .;
sparql drop iri class iri:bao .;
sparql drop iri class iri:cheminf .;
sparql drop iri class iri:mesh .;
sparql drop iri class iri:dqmesh .;
sparql drop iri class iri:taxonomy .;
sparql drop iri class iri:pdblink .;
sparql drop iri class iri:go .;
sparql drop iri class iri:pr .;
sparql drop iri class iri:uniprot .;

--------------------------------------------------------------------------------

drop function iri_sio;
drop function iri_sio_INVERSE;
drop function iri_bao;
drop function iri_bao_INVERSE;
drop function iri_cheminf;
drop function iri_cheminf_INVERSE;
drop function iri_mesh;
drop function iri_mesh_INVERSE;
drop function iri_dqmesh;
drop function iri_dqmesh_INV_1;
drop function iri_dqmesh_INV_2;
drop function iri_go;
drop function iri_go_INVERSE;
drop function iri_pr;
drop function iri_pr_INVERSE;
