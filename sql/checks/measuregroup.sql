-- check if there is something new

sparql
select distinct ?P from pubchem:measuregroup where
{
    ?S ?P ?O .
    filter(?P != obo:BFO_0000057)
    filter(?P != obo:OBI_0000299)
    filter(?P != dcterms:source)
    filter(?P != dcterms:title)
    filter(?P != dcterms:source)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:measuregroup where
{
    ?S rdf:type ?T .
    filter(?T != bao:BAO_0000040)
};

sparql
select distinct ?T from pubchem:measuregroup where
{
    ?S obo:BFO_0000057 ?P .
    filter(!strstarts(str(?P), "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"))
    filter(!strstarts(str(?P), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"))
};
