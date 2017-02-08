sparql
create iri class iri:gene "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID%d"
    (in gene integer not null) option (bijection) .;


sparql
create iri class iri:ensembl "http://rdf.ebi.ac.uk/resource/ensembl/%U"
    (in ensembl varchar not null) option (bijection) .;
