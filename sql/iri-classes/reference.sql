sparql
create iri class iri:reference "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d"
    (in reference integer not null) option (bijection) .;


sparql
create iri class iri:reference_type using
    function db.rdf.iri_reference_type (in id integer) returns varchar,
    function db.rdf.iri_reference_type_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://purl.org/spar/fabio/ReviewArticle"
        union   "http://purl.org/spar/fabio/JournalArticle" ).;
