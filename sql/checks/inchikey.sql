-- check if there is something new

sparql
select distinct ?P from pubchem:inchikey where
{
    ?S ?P ?O .
    filter(?P != dcterms:subject)
    filter(?P != sio:has-value)
    filter(?P != sio:is-attribute-of)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:inchikey where
{
    ?S rdf:type ?T .
    filter(?T != sio:CHEMINF_000399)
};
