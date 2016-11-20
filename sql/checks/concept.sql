-- check if there is something new

sparql
select distinct ?P from pubchem:concept where
{
    ?S ?P ?O .
    filter(?P != <http://purl.org/pav/importedFrom>)
    filter(?P != skos:broader)
    filter(?P != skos:inScheme)
    filter(?P != skos:prefLabel)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:concept where
{
    ?S rdf:type ?T .
    filter(?T != skos:ConceptScheme)
    filter(?T != skos:Concept)
    filter(?T != skos:concept)
};

sparql
select distinct ?I from pubchem:concept where
{
    ?S <http://purl.org/pav/importedFrom> ?I
    filter(?I != source:WHO)
};
