sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.conserveddomain_bases      as conserveddomain_bases
    from DB.rdf.conserveddomain_references as conserveddomain_references
{
    create map:conserveddomain as graph pubchem:conserveddomain
    {
        iri:conserveddomain(conserveddomain_bases.id)
            rdf:type obo:SO_0000417 ;
            dcterms:title conserveddomain_bases.title ;
            dcterms:abstract conserveddomain_bases.abstract .

        iri:conserveddomain(conserveddomain_references.domain)
            cito:isDiscussedBy iri:reference(conserveddomain_references.reference) .
    }.
}.;
