sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.gene_bases        as gene_bases
    from DB.rdf.gene_biosystems   as gene_biosystems
    from DB.rdf.gene_alternatives as gene_alternatives
    from DB.rdf.gene_references   as gene_references
{
    create map:gene as graph pubchem:gene option (exclusive)
    {
        iri:gene(gene_bases.id)
            rdf:type bp:Gene ;
            dcterms:title gene_bases.title ;
            dcterms:description gene_bases.description .

        iri:gene(gene_biosystems.gene)
            obo:BFO_0000056 iri:biosystem(gene_biosystems.biosystem) .

        iri:gene(gene_alternatives.gene)
            dcterms:alternative gene_alternatives.alternative .

        iri:gene(gene_references.gene)
            cito:isDiscussedBy iri:reference(gene_references.reference)
    }.
}.;
