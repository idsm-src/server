sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.substance_bases            as substance_bases
    from DB.rdf.substance_types            as substance_types
    from DB.rdf.substance_matches          as substance_matches
    from DB.rdf.substance_references       as substance_references
    from DB.rdf.substance_pdblinks         as substance_pdblinks
    from DB.rdf.endpoint_bases             as endpoint_bases
    from DB.rdf.substance_synonyms         as substance_synonyms
    from DB.rdf.descriptor_substance_bases as descriptor_substance_bases
{
    create map:substance as graph pubchem:substance
    {
        iri:substance(substance_bases.id)
            dcterms:available substance_bases.available ;
            dcterms:source substance_bases.source ;
            dcterms:modified substance_bases.modified ;
            sio:CHEMINF_000477 iri:compound(substance_bases.compound) where (^{substance_bases.}^.compound is not null) .

        iri:substance(substance_types.substance)
            rdf:type iri:substance_type(substance_types.chebi) .

        iri:substance(endpoint_bases.substance)
            obo:BFO_0000056 iri:measuregroup(endpoint_bases.bioassay, endpoint_bases.measuregroup) .

        iri:substance(substance_matches.substance)
            skos:exactMatch iri:substance_chembl(substance_matches.match) ;
            skos:exactMatch iri:substance_ebi_chembl(substance_matches.match) .

        iri:substance(substance_references.substance)
            cito:isDiscussedBy iri:reference(substance_references.reference) .

        iri:substance(substance_pdblinks.substance)
            pdbo:link_to_pdb iri:pdblink(substance_pdblinks.pdblink) .

        iri:substance(substance_synonyms.substance)
            sio:has-attribute iri:synonym(substance_synonyms.synonym) .


        iri:substance(descriptor_substance_bases.substance)
            sio:has-attribute iri:descriptor_substance_version(descriptor_substance_bases.substance) .
    }.
}.;
