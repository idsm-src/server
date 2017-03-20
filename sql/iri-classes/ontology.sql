sparql
create iri class iri:class using
    function db.rdf.iri_class (in id integer) returns varchar,
    function db.rdf.iri_class_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://blanknodes/%s"
        union   "http://purl.obolibrary.org/%s"
        union   "http://purl.org/%s"
        union   "http://rdfs.org/%s"
        union   "http://rdf.wwpdb.org/%s"
        union   "http://semanticscience.org/%s"
        union   "http://usefulinc.com/%s"
        union   "http://www.bioassayontology.org/%s"
        union   "http://www.biopax.org/%s"
        union   "http://www.ebi.ac.uk/%s"
        union   "http://www.geneontology.org/%s"
        union   "http://www.ifomis.org/%s"
        union   "http://www.w3.org/%s"
        union   "http://xmlns.com/%s"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#FDAApprovedDrugs"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PC2D_Fingerprint_TanimotoScore"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PC2D_structural_similarity"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PC3D_Feature_TanimotoScore"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PC3D_Shape_TanimotoScore"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PC3D_structural_similarity"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PubChemBioAssayOutcomeCategory"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#probe" ).;


sparql
create iri class iri:property using
    function db.rdf.iri_property (in id integer) returns varchar,
    function db.rdf.iri_property_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://bioinfo.iocb.cz/%s"
        union   "http://blanknodes/%s"
        union   "http://purl.obolibrary.org/%s"
        union   "http://purl.org/%s"
        union   "http://rdfs.org/%s"
        union   "http://rdf.wwpdb.org/%s"
        union   "http://semanticscience.org/%s"
        union   "http://schema.org/%s"
        union   "http://usefulinc.com/%s"
        union   "http://www.bioassayontology.org/%s"
        union   "http://www.biopax.org/%s"
        union   "http://www.geneontology.org/%s"
        union   "http://www.obofoundry.org/%s"
        union   "http://www.ontologydesignpatterns.org/%s"
        union   "http://www.semanticweb.org/%s"
        union   "http://www.w3.org/%s"
        union   "http://xmlns.com/%s"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PubChemAssayOutcome"
        union   "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent"
).;
