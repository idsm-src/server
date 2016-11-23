-- check if there is something new

sparql
select distinct ?P from pubchem:synonym where
{
    ?S ?P ?O .
    filter(?P != dcterms:subject)
    filter(?P != sio:has-value)
    filter(?P != sio:is-attribute-of)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:synonym where
{
    ?S rdf:type ?T .
    filter(?T != sio:CHEMINF_000339)
    filter(?T != sio:CHEMINF_000382)
    filter(?T != sio:CHEMINF_000467)
    filter(?T != sio:CHEMINF_000561)
    filter(?T != sio:CHEMINF_000446)
    filter(?T != sio:CHEMINF_000563)
    filter(?T != sio:CHEMINF_000109)
    filter(?T != sio:CHEMINF_000562)
    filter(?T != sio:CHEMINF_000407)
    filter(?T != sio:CHEMINF_000409)
    filter(?T != sio:CHEMINF_000565)
    filter(?T != sio:CHEMINF_000412)
    filter(?T != sio:CHEMINF_000447)
    filter(?T != sio:CHEMINF_000566)
    filter(?T != sio:CHEMINF_000564)
    filter(?T != sio:CHEMINF_000406)
};

sparql
select distinct ?V from pubchem:synonym where
{
    ?S dcterms:subject ?V .
    filter(!strstarts(str(?V),'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/'))
    filter(!strstarts(str(?V),'http://id.nlm.nih.gov/mesh/M'))
};
