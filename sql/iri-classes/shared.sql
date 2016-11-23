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
        returns "http://id.nlm.nih.gov/mesh/M%d" ) .;
