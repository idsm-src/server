sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.reference_bases               as reference_bases
    from DB.rdf.reference_citations_long      as reference_citations_long
    from DB.rdf.reference_discusses           as reference_discusses
    from DB.rdf.reference_subject_descriptors as reference_subject_descriptors
{
    create map:reference as graph pubchem:reference
    {
        iri:reference(reference_bases.id)
            rdf:type iri:reference_type(reference_bases.type) ;
            dcterms:title reference_bases.title ;
            dcterms:date reference_bases.dcdate ;
            dcterms:bibliographicCitation reference_bases.citation .

        iri:reference(reference_citations_long.reference)
            dcterms:bibliographicCitation reference_citations_long.citation .

        iri:reference(reference_discusses.reference)
            cito:discusses iri:mesh(reference_discusses.statement) .

        iri:reference(reference_subject_descriptors.reference)
            fabio:hasSubjectTerm iri:dqmesh(reference_subject_descriptors.descriptor, reference_subject_descriptors.qualifier) .
    }.
}.;
