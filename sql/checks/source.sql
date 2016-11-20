-- check if there is something new

sparql
select distinct ?P from pubchem:source where
{
    ?S ?P ?O .
    filter(?P != dcterms:title)
    filter(?P != dcterms:alternative)
    filter(?P != dcterms:subject)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:source where
{
    ?S rdf:type ?T .
    filter(?T != dcterms:Dataset)
};
