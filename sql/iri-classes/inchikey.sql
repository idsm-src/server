sparql
create iri class iri:inchikey using
    function db.rdf.iri_inchikey (in id integer) returns varchar,
    function db.rdf.iri_inchikey_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/%U" ) .;
