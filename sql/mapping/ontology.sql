sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.class_bases              as class_bases
    from DB.rdf.class_superclasses       as class_superclasses
    from DB.rdf.property_bases           as property_bases
    from DB.rdf.property_superproperties as property_superproperties
    from DB.rdf.property_domains         as property_domains
    from DB.rdf.property_ranges          as property_ranges
{
    create map:ontology as graph pubchem:ontology
    {
        iri:class(class_bases.id)
            rdf:type owl:Class ;
            template:itemTemplate "base/Class.vm" ;
            template:pageTemplate "base/Class.vm" .

        iri:class(class_bases.id)
            rdfs:label class_bases.label .

        iri:class(class_superclasses.class)
            rdfs:subClassOf iri:class(class_superclasses.superclass).

        iri:property(property_bases.id)
            rdf:type rdf:Property ;
            template:itemTemplate "base/Property.vm" ;
            template:pageTemplate "base/Property.vm" .

        iri:property(property_bases.id)
            rdfs:label property_bases.label .

        iri:property(property_superproperties.property)
            rdfs:subPropertyOf iri:property(property_superproperties.superproperty).

        iri:property(property_domains.property)
            rdfs:domain iri:class(property_domains.domain).

        iri:property(property_ranges.property)
            rdfs:range iri:class(property_ranges.range).
    }.
}.;
