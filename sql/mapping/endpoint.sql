sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.endpoint_bases              as endpoint_bases
    from DB.rdf.endpoint_measurements       as endpoint_measurements
    from DB.rdf.endpoint_references         as endpoint_references
    from DB.rdf.endpoint_outcomes__reftable as endpoint_outcomes
{
    create map:endpoint as graph pubchem:endpoint
    {
        iri:endpoint(endpoint_bases.substance, endpoint_bases.bioassay, endpoint_bases.measuregroup)
            obo:IAO_0000136 iri:substance(endpoint_bases.substance) ;
            vocab:PubChemAssayOutcome iri:endpoint_outcome(endpoint_bases.outcome) .

        iri:endpoint(endpoint_measurements.substance, endpoint_measurements.bioassay, endpoint_measurements.measuregroup)
            sio:has-unit obo:UO_0000064 ;
            rdf:type iri:bao(endpoint_measurements.type) ;
            rdfs:label endpoint_measurements.label ;
            sio:has-value endpoint_measurements.value .

        iri:endpoint(endpoint_references.substance, endpoint_references.bioassay, endpoint_references.measuregroup)
            cito:citesAsDataSource iri:reference(endpoint_references.reference) .

        iri:endpoint_outcome(endpoint_outcomes.id)
            rdf:type vocab:PubChemBioAssayOutcomeCategory ;
            template:itemTemplate "pubchem/PubChemBioAssayOutcomeCategory.vm" ;
            template:pageTemplate "pubchem/PubChemBioAssayOutcomeCategory.vm" .
    }.
}.;
