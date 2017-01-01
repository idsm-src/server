sparql
create iri class iri:class using
    function db.rdf.iri_class (in id integer) returns varchar,
    function db.rdf.iri_class_INVERSE (in id varchar) returns integer
    option (bijection).;


sparql
create iri class iri:property using
    function db.rdf.iri_property (in id integer) returns varchar,
    function db.rdf.iri_property_INVERSE (in id varchar) returns integer
    option (bijection).;
