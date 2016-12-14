-- check if there is something new

sparql
select distinct ?P from pubchem:substance where
{
    ?S ?P ?O .
    filter(?P != sio:CHEMINF_000477)
    filter(?P != obo:BFO_0000056)
    filter(?P != pdbo:link_to_pdb)
    filter(?P != skos:exactMatch)
    filter(?P != cito:isDiscussedBy)
    filter(?P != dcterms:available)
    filter(?P != dcterms:modified)
    filter(?P != dcterms:source)
    filter(?P != sio:has-attribute)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from pubchem:substance where
{
    ?S rdf:type ?T .
    filter(! strstarts(str(?T), "http://purl.obolibrary.org/obo/CHEBI_"))
};

sparql
select ?A from pubchem:substance where
{
    ?S sio:has-attribute ?A
    filter(!strstarts(str(?A), "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_"))
    filter(!strends(str(?A), "_Substance_Version"))
};


-- select probably incorrect references
sparql
select ?C from pubchem:substance where
{
    ?S skos:exactMatch ?C.
    filter(!regex(str(?C), "^http://linkedchemistry.info/chembl/chemblid/S?CHEMBL[0-9]+$$"))
    filter(!regex(str(?C), "^http://rdf.ebi.ac.uk/resource/chembl/molecule/S?CHEMBL[0-9]+$$"))
};
