sparql
create iri class iri:bioassay "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID%d"
    (in bioassay integer not null) option (bijection) .;


sparql
create iri class iri:bioassay_data using
    function db.rdf.iri_bioassay_data (in id1 integer, in id2 integer) returns varchar,
    function db.rdf.iri_bioassay_data_INV_1 (in id varchar) returns integer,
    function db.rdf.iri_bioassay_data_INV_2 (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID%d_Description"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID%d_Protocol"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID%d_Comment" ).;
