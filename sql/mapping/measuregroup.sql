sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.measuregroup_bases    as measuregroup_bases
    from DB.rdf.measuregroup_genes    as measuregroup_genes
    from DB.rdf.measuregroup_proteins as measuregroup_proteins
    from DB.rdf.endpoint_bases        as endpoint_bases
{
    create map:measuregroup as graph pubchem:measuregroup option (exclusive)
    {
        iri:measuregroup(measuregroup_bases.bioassay, measuregroup_bases.measuregroup)
            rdf:type bao:BAO_0000040 ;
            dcterms:title measuregroup_bases.title ;
            dcterms:source iri:source(measuregroup_bases.source) where (^{measuregroup_bases.}^.source is not null) .

        iri:measuregroup(endpoint_bases.bioassay, endpoint_bases.measuregroup)
            obo:OBI_0000299 iri:endpoint(endpoint_bases.substance, endpoint_bases.bioassay, endpoint_bases.measuregroup) .

        iri:measuregroup(measuregroup_genes.bioassay, measuregroup_genes.measuregroup)
            obo:BFO_0000057 iri:gene(measuregroup_genes.gene) .

        iri:measuregroup(measuregroup_proteins.bioassay, measuregroup_proteins.measuregroup)
            obo:BFO_0000057 iri:protein(measuregroup_proteins.protein) .
    }.
}.;
