sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.compound_bases              as compound_bases
    from DB.rdf.compound_relations          as compound_relations
    from DB.rdf.compound_roles              as compound_roles
    from DB.rdf.compound_biosystems         as compound_biosystems
    from DB.rdf.compound_types              as compound_types
    from DB.rdf.compound_active_ingredients as compound_active_ingredients
{
    create map:compound as graph pubchem:compound option (exclusive)
    {
        iri:compound(compound_bases.id)
            rdf:type sio:SIO_010004 .

        iri:compound_sdfile(compound_bases.id)
            rdf:type sio:SIO_011120 ;
            sio:is-attribute-of iri:compound(compound_bases.id) ;
            sio:has-value compound_bases.sdf .

        iri:compound(compound_relations.compound_from)
            iri:compound_relation(compound_relations.relation) iri:compound(compound_relations.compound_to) .

        iri:compound(compound_roles.compound)
            obo:has-role iri:compound_role(compound_roles.roleid) .

        iri:compound(compound_biosystems.compound)
            obo:BFO_0000056 iri:biosystem(compound_biosystems.biosystem) .

        iri:compound(compound_types.compound)
            rdf:type iri:compound_type(compound_types.unit, compound_types.type) .

        iri:compound(compound_active_ingredients.compound)
            vocab:is_active_ingredient_of iri:compound_type(compound_active_ingredients.unit, compound_active_ingredients.ingredient) .
    }.
}.;
