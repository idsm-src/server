sparql
create iri class iri:source using
    function db.rdf.iri_source (in id integer) returns varchar,
    function db.rdf.iri_source_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/source/%U" ).;


sparql
create iri class iri:source_subject using
    function db.rdf.iri_source_subject (in id integer) returns varchar,
    function db.rdf.iri_source_subject_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/%U" ).;
