-- check if there is something new

sparql
select distinct ?P from pubchem:bioassay where
{
    ?S ?P ?O .
    filter(?P != dcterms:title)
    filter(?P != dcterms:source)
    filter(?P != bao:BAO_0000209)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:bioassay where
{
    ?S rdf:type ?T .
    filter(?T != bao:BAO_0000015)
};
