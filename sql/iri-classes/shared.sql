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
