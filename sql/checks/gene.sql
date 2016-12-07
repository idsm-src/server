-- check if there is something new

sparql
select distinct ?P from pubchem:gene where
{
    ?S ?P ?O .
    filter(?P != dcterms:alternative)
    filter(?P != dcterms:description)
    filter(?P != dcterms:title)
    filter(?P != obo:BFO_0000056)
    filter(?P != cito:isDiscussedBy)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:gene where
{
    ?S rdf:type ?T .
    filter(?T != bp:Gene)
};
