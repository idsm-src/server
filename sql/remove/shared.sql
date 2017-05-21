drop function graph(id in smallint);
drop function graph_inverse(iri in varchar);
drop function go(id in integer);
drop function go_inverse(iri in varchar);
drop function dqmesh(descriptor in integer, qualifier in integer);
drop function dqmesh_inv1(iri in varchar);
drop function dqmesh_inv2(iri in varchar);
drop function mesh(id in integer);
drop function mesh_inverse(iri in varchar);
drop function pdblink(id in varchar);
drop function pdblink_inverse(iri in varchar);
drop function taxonomy(id in integer);
drop function taxonomy_inverse(iri in varchar);
drop function uniprot(id in varchar);
drop function uniprot_inverse(iri in varchar);

--------------------------------------------------------------------------------

drop table graphs__reftable;
