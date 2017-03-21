sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.concept_bases as concept_bases
{
    create map:concept as graph pubchem:concept
    {
        concept:ATC
            rdf:type skos:ConceptScheme .

        concept:SubstanceCategorization
            rdf:type skos:ConceptScheme .

        iri:concept(concept_bases.id)
            skos:prefLabel concept_bases.label ;
            skos:inScheme iri:concept(concept_bases.scheme) ;
            skos:broader iri:concept(concept_bases.broader) ;
            <http://purl.org/pav/importedFrom> source:WHO where (^{concept_bases.}^.iri like 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/ATC%') ;
            rdf:type skos:Concept where (^{concept_bases.}^.iri <> 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/ATC' and
                                         ^{concept_bases.}^.iri <> 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/SubstanceCategorization') .
    }.
}.;
