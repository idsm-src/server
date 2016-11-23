sparql
create iri class iri:synonym using
    function db.rdf.iri_synonym (in id integer) returns varchar,
    function db.rdf.iri_synonym_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_%U" ) .;
