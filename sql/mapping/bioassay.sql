sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.bioassay_bases         as bioassay_bases text literal title
    from DB.rdf.bioassay_measuregroups as bioassay_measuregroups
    from DB.rdf.bioassay_data          as bioassay_data text literal value
{
    create map:bioassay as graph pubchem:bioassay option (exclusive)
    {
        iri:bioassay(bioassay_bases.id)
            rdf:type bao:BAO_0000015 ;
            dcterms:title bioassay_bases.title ;
            dcterms:source iri:source(bioassay_bases.source) .

        iri:bioassay(bioassay_measuregroups.bioassay)
            bao:BAO_0000209 iri:measuregroup(bioassay_measuregroups.bioassay, bioassay_measuregroups.measuregroup) .

        iri:bioassay_data(bioassay_data.bioassay, bioassay_data.type)
            rdf:type iri:sio(bioassay_data.type) ;
            sio:is-attribute-of iri:bioassay(bioassay_data.bioassay) ;
            sio:has-value bioassay_data.value .
    }.
}.;
