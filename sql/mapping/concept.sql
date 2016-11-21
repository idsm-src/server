sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.concept_bases as concept_bases
{
    create map:concept as graph pubchem:concept option (exclusive)
    {
        iri:concept(concept_bases.id)
            rdf:type skos:ConceptScheme where (^{concept_bases.}^.scheme is null) ;
            rdf:type skos:Concept where (^{concept_bases.}^.scheme is not null) ;
            skos:prefLabel concept_bases.label ;
            skos:inScheme iri:concept(concept_bases.scheme) ;
            skos:broader iri:concept(concept_bases.broader) ;
            <http://purl.org/pav/importedFrom> source:WHO where (^{concept_bases.}^.iri like 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/ATC%') .
    }.
}.;
