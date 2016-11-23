sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.biosystem_bases      as biosystem_bases
    from DB.rdf.biosystem_components as biosystem_components
    from DB.rdf.biosystem_references as biosystem_references
    from DB.rdf.biosystem_matches    as biosystem_matches
{
    create map:biosystem as graph pubchem:biosystem option (exclusive)
    {
        iri:biosystem(biosystem_bases.id)
            rdf:type bp:Pathway ;
            dcterms:title biosystem_bases.title ;
            dcterms:source iri:source(biosystem_bases.source) ;
            bp:organism iri:taxonomy(biosystem_bases.organism) where (^{biosystem_bases.}^.organism is not null) .

        iri:biosystem(biosystem_components.biosystem)
            bp:pathwayComponent iri:biosystem(biosystem_components.component) .

        iri:biosystem(biosystem_references.biosystem)
            cito:isDiscussedBy iri:reference(biosystem_references.reference) .

        iri:biosystem(biosystem_matches.biosystem)
            skos:exactMatch iri:wikipathway(biosystem_matches.wikipathway) .
    }.
}.;
