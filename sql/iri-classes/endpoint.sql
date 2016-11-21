sparql
create iri class iri:endpoint using
    function db.rdf.iri_endpoint (in id1 integer, in id2 integer, in id3 integer) returns varchar,
    function db.rdf.iri_endpoint_INV_1 (in id varchar) returns integer,
    function db.rdf.iri_endpoint_INV_2 (in id varchar) returns integer,
    function db.rdf.iri_endpoint_INV_3 (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_PMID"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_PMID%d"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/SID%d_AID%d_%d" ).;


sparql
create iri class iri:endpoint_outcome using
    function db.rdf.iri_endpoint_outcome (in id integer) returns varchar,
    function db.rdf.iri_endpoint_outcome_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#%U" ).;


sparql
create iri class iri:endpoint_type using
    function db.rdf.iri_endpoint_type (in id integer) returns varchar,
    function db.rdf.iri_endpoint_type_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://www.bioassayontology.org/bao#BAO_%d" ).;
