sparql
create iri class iri:protein using
    function db.rdf.iri_protein (in id integer) returns varchar,
    function db.rdf.iri_protein_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/%U" ).;
