sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.reference_bases                         as reference_bases
    from DB.rdf.reference_citations_long                as reference_citations_long
    from DB.rdf.reference_discusses_mesh                as reference_discusses_mesh
    from DB.rdf.reference_discusses_cmesh               as reference_discusses_cmesh
    from DB.rdf.reference_subject_descriptors           as reference_subject_descriptors
    from DB.rdf.reference_subject_descriptor_qualifiers as reference_subject_descriptor_qualifiers
{
    create map:reference as graph pubchem:reference option (exclusive)
    {
        iri:reference(reference_bases.id)
            rdf:type iri:reference_type(reference_bases.type) ;
            dcterms:title reference_bases.title ;
            dcterms:date reference_bases.dcdate ;
            dcterms:bibliographicCitation reference_bases.citation .

        iri:reference(reference_citations_long.reference)
            dcterms:bibliographicCitation reference_citations_long.citation .

        iri:reference(reference_discusses_mesh.reference)
            cito:discusses iri:mesh(reference_discusses_mesh.statement) .

        iri:reference(reference_discusses_cmesh.reference)
            cito:discusses iri:cmesh(reference_discusses_cmesh.statement) .

        iri:reference(reference_subject_descriptors.reference)
            fabio:hasSubjectTerm iri:dmesh(reference_subject_descriptors.descriptor) .

        iri:reference(reference_subject_descriptor_qualifiers.reference)
            fabio:hasSubjectTerm iri:dqmesh(reference_subject_descriptor_qualifiers.descriptor, reference_subject_descriptor_qualifiers.qualifier) .
    }.
}.;
