sparql
create iri class iri:measuregroup using
    function db.rdf.iri_measuregroup (in id1 integer, in id2 integer) returns varchar,
    function db.rdf.iri_measuregroup_INV_1 (in id varchar) returns integer,
    function db.rdf.iri_measuregroup_INV_2 (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_PMID"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_PMID%d"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID%d_%d" ).;
