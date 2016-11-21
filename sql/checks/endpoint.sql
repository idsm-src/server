-- check if there is something new

sparql
select distinct ?P from pubchem:endpoint where
{
    ?S ?P ?O .
    filter(?P != obo:IAO_0000136)
    filter(?P != cito:citesAsDataSource)
    filter(?P != vocab:PubChemAssayOutcome)
    filter(?P != rdfs:label)
    filter(?P != sio:has-unit)
    filter(?P != sio:has-value)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:endpoint where
{
    ?S rdf:type ?T .
    filter(! strstarts(str(?T), "http://www.bioassayontology.org/bao#BAO_"))
};

sparql
select distinct ?O from pubchem:endpoint where
{
    ?S vocab:PubChemAssayOutcome ?O .
    filter(?O != vocab:active)
    filter(?O != vocab:inactive)
    filter(?O != vocab:inconclusive)
    filter(?O != vocab:unspecified)
    filter(?O != vocab:probe)
};
