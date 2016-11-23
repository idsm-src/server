-- check if there is something new

sparql
select distinct ?P from pubchem:biosystem where
{
    ?S ?P ?O .
    filter(?P != dcterms:title)
    filter(?P != dcterms:source)
    filter(?P != bp:organism)
    filter(?P != bp:pathwayComponent)
    filter(?P != cito:isDiscussedBy)
    filter(?P != skos:exactMatch)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:biosystem where
{
    ?S rdf:type ?T .
    filter(?T != bp:Pathway)
};
