-- check if there is something new

sparql
select distinct ?P from pubchem:reference where
{
    ?S ?P ?O .
    filter(?P != dcterms:title)
    filter(?P != dcterms:date)
    filter(?P != dcterms:bibliographicCitation)
    filter(?P != fabio:hasSubjectTerm)
    filter(?P != cito:discusses)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:reference where
{
    ?S rdf:type ?T .
    filter(?T != fabio:JournalArticle)
    filter(?T != fabio:ReviewArticle)
};

sparql
select distinct ?T from pubchem:compound where
{
    ?S cito:discusses ?O .
    filter(! strstarts(str(?O), "http://id.nlm.nih.gov/mesh/M"))
    filter(! strstarts(str(?O), "http://id.nlm.nih.gov/mesh/C"))
};
