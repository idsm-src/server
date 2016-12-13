-- check if there is something new

sparql
select distinct ?P from pubchem:protein where
{
    ?S ?P ?O .
    filter(?P != obo:BFO_0000056)
    filter(?P != obo:BFO_0000110)
    filter(?P != obo:BFO_0000160)
    filter(?P != obo:BFO_0000171)
    filter(?P != obo:BFO_0000178)
    filter(?P != dcterms:title)
    filter(?P != cito:isDiscussedBy)
    filter(?P != vocab:encodedBy)
    filter(?P != vocab:hasSimilarProtein)
    filter(?P != pdbo:link_to_pdb)
    filter(?P != bp:organism)
    filter(?P != skos:closeMatch)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:protein where
{
    ?S rdf:type ?T .
    filter(?P != bp:Protein)
    filter(?P != obo:GO_0043234)
    filter(! strstarts(str(?T), "http://purl.obolibrary.org/obo/PR_"))
};

sparql
select ?O from pubchem:protein where
{
    ?S obo:BFO_0000056 ?O .
    filter(! strstarts(str(?O), "http://purl.obolibrary.org/obo/GO_"))
    filter(! strstarts(str(?O), "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"))
};
