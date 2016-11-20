sparql
create iri class iri:concept using
    function db.rdf.iri_concept (in id integer) returns varchar,
    function db.rdf.iri_concept_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/%U" ).;

--------------------------------------------------------------------------------

sparql
create iri class iri:concept_type using
    function db.rdf.iri_concept_type (in id integer) returns varchar,
    function db.rdf.iri_concept_type_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://www.w3.org/2004/02/skos/core#Concept"
        union   "http://www.w3.org/2004/02/skos/core#ConceptScheme" ).;
