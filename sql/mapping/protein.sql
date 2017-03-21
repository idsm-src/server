sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.protein_bases                   as protein_bases
    from DB.rdf.protein_references              as protein_references
    from DB.rdf.protein_pdblinks                as protein_pdblinks
    from DB.rdf.protein_similarproteins         as protein_similarproteins
    from DB.rdf.protein_genes                   as protein_genes
    from DB.rdf.protein_closematches            as protein_closematches
    from DB.rdf.protein_conserveddomains        as protein_conserveddomains
    from DB.rdf.protein_continuantparts         as protein_continuantparts
    from DB.rdf.protein_participates_goes       as protein_participates_goes
    from DB.rdf.protein_participates_biosystems as protein_participates_biosystems
    from DB.rdf.protein_functions               as protein_functions
    from DB.rdf.protein_locations               as protein_locations
    from DB.rdf.protein_types                   as protein_types
    from DB.rdf.protein_complexes               as protein_complexes
{
    create map:protein as graph pubchem:protein
    {
        iri:protein(protein_bases.id)
            rdf:type bp:Protein ;
            dcterms:title protein_bases.title ;
            bp:organism iri:taxonomy(protein_bases.organism) where (^{protein_bases.}^.organism is not null) .

        iri:protein(protein_references.protein)
            cito:isDiscussedBy iri:reference(protein_references.reference) .

        iri:protein(protein_pdblinks.protein)
            pdbo:link_to_pdb iri:pdblink(protein_pdblinks.pdblink) .

        iri:protein(protein_similarproteins.protein)
            vocab:hasSimilarProtein iri:protein(protein_similarproteins.similar) .

        iri:protein(protein_genes.protein)
            vocab:encodedBy iri:gene(protein_genes.gene) .

        iri:protein(protein_closematches.protein)
            skos:closeMatch iri:uniprot(protein_closematches.match) .

        iri:protein(protein_conserveddomains.protein)
            obo:BFO_0000110 iri:conserveddomain(protein_conserveddomains.domain) .

        iri:protein(protein_continuantparts.protein)
            obo:BFO_0000178 iri:protein(protein_continuantparts.part) .

        iri:protein(protein_participates_goes.protein)
            obo:BFO_0000056 iri:go(protein_participates_goes.participation) .

        iri:protein(protein_participates_biosystems.protein)
            obo:BFO_0000056 iri:biosystem(protein_participates_biosystems.biosystem) .

        iri:protein(protein_functions.protein)
            obo:BFO_0000160 iri:go(protein_functions.gofunction) .

        iri:protein(protein_locations.protein)
            obo:BFO_0000171 iri:go(protein_locations.location) .

        iri:protein(protein_types.protein)
            rdf:type iri:pr(protein_types.type) .

        iri:protein(protein_complexes.protein)
            rdf:type obo:GO_0043234 .
    }.
}.;
