sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.synonym_values           as synonym_values
    from DB.rdf.synonym_types            as synonym_types
    from DB.rdf.synonym_compounds        as synonym_compounds
    from DB.rdf.synonym_mesh_subjects    as synonym_mesh_subjects
    from DB.rdf.synonym_concept_subjects as synonym_concept_subjects

{
    create map:synonym as graph pubchem:synonym option (exclusive)
    {
        iri:synonym(synonym_values.synonym)
            sio:has-value synonym_values.value .

        iri:synonym(synonym_types.synonym)
            rdf:type iri:cheminf(synonym_types.type) .

        iri:synonym(synonym_compounds.synonym)
            sio:is-attribute-of iri:compound(synonym_compounds.compound) .

        iri:synonym(synonym_mesh_subjects.synonym)
            dcterms:subject iri:mesh(synonym_mesh_subjects.subject) .

        iri:synonym(synonym_concept_subjects.synonym)
            dcterms:subject iri:concept(synonym_concept_subjects.concept) .
    }.
}.;
