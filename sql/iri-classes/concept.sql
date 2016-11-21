sparql
create iri class iri:concept using
    function db.rdf.iri_concept (in id integer) returns varchar,
    function db.rdf.iri_concept_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/%U" ).;
