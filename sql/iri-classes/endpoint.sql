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
create iri class iri:bao using
    function db.rdf.iri_bao (in id integer) returns varchar,
    function db.rdf.iri_bao_INVERSE (in id varchar) returns integer
    option (bijection,
        returns "http://www.bioassayontology.org/bao#BAO_0000034"
        union   "http://www.bioassayontology.org/bao#BAO_0000186"
        union   "http://www.bioassayontology.org/bao#BAO_0000187"
        union   "http://www.bioassayontology.org/bao#BAO_0000188"
        union   "http://www.bioassayontology.org/bao#BAO_0000189"
        union   "http://www.bioassayontology.org/bao#BAO_0000190"
        union   "http://www.bioassayontology.org/bao#BAO_0000192"
        union   "http://www.bioassayontology.org/bao#BAO_0000194"
        union   "http://www.bioassayontology.org/bao#BAO_0000349"
        union   "http://www.bioassayontology.org/bao#BAO_0000477"
        union   "http://www.bioassayontology.org/bao#BAO_0002117"
        union   "http://www.bioassayontology.org/bao#BAO_0002144"
        union   "http://www.bioassayontology.org/bao#BAO_0002145"
        union   "http://www.bioassayontology.org/bao#BAO_0002146"
        union   "http://www.bioassayontology.org/bao#BAO_0002162"
        union   "http://www.bioassayontology.org/bao#BAO_0002862"
        union   "http://www.bioassayontology.org/bao#BAO_0002877"
        union   "http://www.bioassayontology.org/bao#BAO_0002878"
        union   "http://www.bioassayontology.org/bao#BAO_0002879"
        union   "http://www.bioassayontology.org/bao#BAO_0002880"
        union   "http://www.bioassayontology.org/bao#BAO_0002881"
        union   "http://www.bioassayontology.org/bao#BAO_0002882"
        union   "http://www.bioassayontology.org/bao#BAO_0002883"
        union   "http://www.bioassayontology.org/bao#BAO_0002884"
        union   "http://www.bioassayontology.org/bao#BAO_0002886"
        union   "http://www.bioassayontology.org/bao#BAO_0002887"
        union   "http://www.bioassayontology.org/bao#BAO_0003036" ).;
