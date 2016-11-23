-- check if there is something new

sparql
select distinct ?P from pubchem:conserveddomain where
{
    ?S ?P ?O .
    filter(?P != cito:isDiscussedBy)
    filter(?P != dcterms:abstract)
    filter(?P != dcterms:title)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:conserveddomain where
{
    ?S rdf:type ?T .
    filter(?T != obo:SO_0000417)
};
