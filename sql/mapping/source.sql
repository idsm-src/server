sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.source_bases as source_bases
    from DB.rdf.source_subjects as source_subjects
    from DB.rdf.source_alternatives as source_alternatives
{
    create map:source as graph pubchem:source
    {
        iri:source(source_bases.id)
            rdf:type dcterms:Dataset ;
            dcterms:title source_bases.title .

        iri:source(source_subjects.source)
            dcterms:subject iri:source_subject(source_subjects.subject) .

        iri:source(source_alternatives.source)
            dcterms:alternative source_alternatives.alternative .
    }.
}.;
