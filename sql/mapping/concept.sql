sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.concept_bases as concept_bases
    from DB.rdf.concept_types as concept_types
    from DB.rdf.concept_imports as concept_imports
{
    create map:concept as graph pubchem:concept option (exclusive)
    {
        iri:concept(concept_types.id)
            rdf:type iri:concept_type(concept_types.type) .

        iri:concept(concept_bases.id)
            skos:prefLabel concept_bases.label ;
            skos:inScheme iri:concept(concept_bases.scheme) ;
            skos:broader iri:concept(concept_bases.broader) .

        iri:concept(concept_imports.id)
            <http://purl.org/pav/importedFrom> source:WHO .
    }.
}.;
