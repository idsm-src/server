-- check if there is something new

sparql
select distinct ?P from pubchem:compound where
{
    ?S ?P ?O .
    filter(?P != obo:BFO_0000056)
    filter(?P != obo:has-role)
    filter(?P != vocab:has_parent)
    filter(?P != vocab:is_active_ingredient_of)
    filter(?P != sio:CHEMINF_000455)
    filter(?P != sio:CHEMINF_000461)
    filter(?P != sio:CHEMINF_000462)
    filter(?P != sio:CHEMINF_000480)
    filter(?P != sio:has-attribute)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:compound where
{
    ?S rdf:type ?T .
    filter(! strstarts(str(?T), "http://purl.obolibrary.org/obo/CHEBI_"))
    filter(! strstarts(str(?T), "http://purl.bioontology.org/ontology/SNOMEDCT/"))
    filter(! strstarts(str(?T), "http://purl.bioontology.org/ontology/NDFRT/N"))
    filter(! strstarts(str(?T), "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C"))
    filter(?T != bp:SmallMolecule)
};

sparql
select distinct ?R from pubchem:compound where
{
    ?S obo:has-role ?R
    filter(?R != vocab:FDAApprovedDrugs)
};
