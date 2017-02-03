sparql
create iri class iri:sio using
    function db.rdf.iri_sio (in id integer) returns varchar,
    function db.rdf.iri_sio_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://semanticscience.org/resource/SIO_%d" ).;


sparql
create iri class iri:bao using
    function db.rdf.iri_bao (in id integer) returns varchar,
    function db.rdf.iri_bao_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://www.bioassayontology.org/bao#BAO_%d" ).;


sparql
create iri class iri:cheminf using
    function db.rdf.iri_cheminf (in id integer) returns varchar,
    function db.rdf.iri_cheminf_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://semanticscience.org/resource/CHEMINF_%d" ) .;


sparql
create iri class iri:mesh using
    function db.rdf.iri_mesh (in id integer) returns varchar,
    function db.rdf.iri_mesh_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://id.nlm.nih.gov/mesh/M%d"
        union   "http://id.nlm.nih.gov/mesh/C%d" ) .;


sparql
create iri class iri:dqmesh using
    function db.rdf.iri_dqmesh (in id1 integer, in id2 integer) returns varchar,
    function db.rdf.iri_dqmesh_INV_1 (in id varchar) returns integer,
    function db.rdf.iri_dqmesh_INV_2 (in id varchar) returns integer
    option (bijection,
        returns "http://id.nlm.nih.gov/mesh/D%d"
        union   "http://id.nlm.nih.gov/mesh/D%dQ%d" ) .;


sparql
create iri class iri:taxonomy "http://identifiers.org/taxonomy/%d"
    (in taxonomy integer not null) option (bijection) .;


sparql
create iri class iri:pdblink "http://rdf.wwpdb.org/pdb/%U"
    (in pdblink varchar not null) option (bijection) .;


sparql
create iri class iri:go using
    function db.rdf.iri_go (in id integer) returns varchar,
    function db.rdf.iri_go_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://purl.obolibrary.org/obo/GO_%d" ).;


sparql
create iri class iri:pr using
    function db.rdf.iri_pr (in id integer) returns varchar,
    function db.rdf.iri_pr_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://purl.obolibrary.org/obo/PR_%d" ).;


sparql
create iri class iri:uniprot "http://purl.uniprot.org/uniprot/%U"
    (in uniprot varchar not null) option (bijection) .;
