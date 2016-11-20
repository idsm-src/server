sparql
create iri class iri:compound "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d"
    (in compound integer not null) option (bijection) .;


sparql
create iri class iri:compound_sdfile "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID%d_SDfile"
    (in compound integer not null) option (bijection) .;


sparql
create iri class iri:compound_relation using
    function db.rdf.iri_compound_relation (in id integer) returns varchar,
    function db.rdf.iri_compound_relation_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://semanticscience.org/resource/CHEMINF_%d"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent" ).;


sparql
create iri class iri:compound_role using
    function db.rdf.iri_compound_role (in id integer) returns varchar,
    function db.rdf.iri_compound_role_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#FDAApprovedDrugs" ).;


sparql
create iri class iri:compound_type using
    function db.rdf.iri_compound_type (in id1 integer, in id2 integer) returns varchar,
    function db.rdf.iri_compound_type_INV_1 (in id varchar) returns integer,
    function db.rdf.iri_compound_type_INV_2 (in id varchar) returns integer
    option (bijection,
        returns "http://purl.obolibrary.org/obo/CHEBI_%d"
        union   "http://purl.bioontology.org/ontology/SNOMEDCT/%d"
        union   "http://purl.bioontology.org/ontology/NDFRT/N%d"
        union   "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C%d"
        union   "http://www.biopax.org/release/biopax-level3.owl#SmallMolecule" ).;
