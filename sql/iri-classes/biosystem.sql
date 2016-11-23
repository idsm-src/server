sparql
create iri class iri:biosystem "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID%d"
    (in biosystem integer not null) option (bijection) .;


sparql
create iri class iri:taxonomy "http://identifiers.org/taxonomy/%d"
    (in taxonomy integer not null) option (bijection) .;


sparql
create iri class iri:wikipathway "http://identifiers.org/wikipathways/WP%d"
    (in wikipathway integer not null) option (bijection) .;
