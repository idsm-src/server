sparql
create iri class iri:substance "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID%d"
    (in compound integer not null) option (bijection) .;

sparql
create iri class iri:substance_type "http://purl.obolibrary.org/obo/CHEBI_%d"
    (in compound integer not null) option (bijection) .;

sparql
create iri class iri:substance_chembl "http://linkedchemistry.info/chembl/chemblid/CHEMBL%d"
    (in compound integer not null) option (bijection) .;

sparql
create iri class iri:substance_ebi_chembl "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL%d"
    (in compound integer not null) option (bijection) .;

sparql
create iri class iri:substance_schembl "http://linkedchemistry.info/chembl/chemblid/SCHEMBL%d"
    (in compound integer not null) option (bijection) .;

sparql
create iri class iri:substance_ebi_schembl "http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL%d"
    (in compound integer not null) option (bijection) .;

sparql
create iri class iri:substance_pdblink "http://rdf.wwpdb.org/pdb/%U"
    (in compound integer not null) option (bijection) .;
