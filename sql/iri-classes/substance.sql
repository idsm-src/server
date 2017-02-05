sparql
create iri class iri:substance "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d"
    (in substance integer not null) option (bijection) .;


sparql
create iri class iri:substance_type "http://purl.obolibrary.org/obo/CHEBI_%d"
    (in type integer not null) option (bijection) .;


sparql
create iri class iri:substance_chembl using
    function db.rdf.iri_substance_chembl (in id integer) returns varchar,
    function db.rdf.iri_substance_chembl_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://linkedchemistry.info/chembl/chemblid/CHEMBL%d"
        union   "http://linkedchemistry.info/chembl/chemblid/SCHEMBL%d" ) .;


sparql
create iri class iri:substance_ebi_chembl using
    function db.rdf.iri_substance_ebi_chembl (in id integer) returns varchar,
    function db.rdf.iri_substance_ebi_chembl_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL%d"
        union   "http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL%d" ) .;
