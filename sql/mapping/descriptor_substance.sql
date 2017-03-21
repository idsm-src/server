sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.descriptor_substance_bases as descriptor_substance_bases
{
    create map:descriptor_substance as graph descriptor:substance
    {
        iri:descriptor_substance_version(descriptor_substance_bases.substance)
            rdf:type obo:IAO_0000129 ;
            sio:has-value descriptor_substance_bases.version .
    }.
}.;
