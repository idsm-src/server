sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.compound_bases                                 as compound_bases
    from DB.rdf.compound_sdfiles                               as compound_sdfiles
    from DB.rdf.compound_relations                             as compound_relations
    from DB.rdf.compound_roles                                 as compound_roles
    from DB.rdf.compound_biosystems                            as compound_biosystems
    from DB.rdf.compound_types                                 as compound_types
    from DB.rdf.compound_active_ingredients                    as compound_active_ingredients
    from DB.rdf.descriptor_compound_bases                      as descriptor_compound_bases
    from DB.rdf.descriptor_compound_molecular_formulas         as descriptor_compound_molecular_formulas
    from DB.rdf.descriptor_compound_isomeric_smileses          as descriptor_compound_isomeric_smileses
    from DB.rdf.descriptor_compound_canonical_smileses         as descriptor_compound_canonical_smileses
    from DB.rdf.descriptor_compound_iupac_inchis               as descriptor_compound_iupac_inchis
    from DB.rdf.descriptor_compound_iupac_inchis_long          as descriptor_compound_iupac_inchis_long
    from DB.rdf.descriptor_compound_preferred_iupac_names      as descriptor_compound_preferred_iupac_names
    from DB.rdf.descriptor_compound_preferred_iupac_names_long as descriptor_compound_preferred_iupac_names_long
{
    create map:compound as graph pubchem:compound option (exclusive)
    {
        iri:compound(compound_bases.id)
            rdf:type sio:SIO_010004 ;
            template:itemTemplate "pubchem/Compound.vm" ;
            template:pageTemplate "pubchem/Compound.vm" .

        iri:compound_sdfile(compound_sdfiles.compound)
            rdf:type sio:SIO_011120 ;
            sio:is-attribute-of iri:compound(compound_sdfiles.compound) ;
            sio:has-value compound_sdfiles.sdf .

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


        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_hydrogen_bond_acceptor_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.hydrogen_bond_acceptor_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_tautomer_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.tautomer_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_defined_atom_stereo_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.defined_atom_stereo_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_defined_bond_stereo_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.defined_bond_stereo_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_undefined_bond_stereo_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.undefined_bond_stereo_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_isotope_atom_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.isotope_atom_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_covalent_unit_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.covalent_unit_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_hydrogen_bond_donor_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.hydrogen_bond_donor_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_non_hydrogen_atom_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.non_hydrogen_atom_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_rotatable_bond_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.rotatable_bond_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_undefined_atom_stereo_count(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.undefined_atom_stereo_count is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_total_formal_charge(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.total_formal_charge is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_structure_complexity(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.structure_complexity is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_mono_isotopic_weight(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.mono_isotopic_weight is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_xlogp3_aa(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.xlogp3_aa is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_exact_mass(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.exact_mass is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_molecular_weight(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.molecular_weight is not null) .

        iri:compound(descriptor_compound_bases.compound)
            sio:has-attribute iri:descriptor_tpsa(descriptor_compound_bases.compound) where (^{descriptor_compound_bases.}^.tpsa is not null) .

        iri:compound(descriptor_compound_molecular_formulas.compound)
            sio:has-attribute iri:descriptor_molecular_formula(descriptor_compound_molecular_formulas.compound) .

        iri:compound(descriptor_compound_isomeric_smileses.compound)
            sio:has-attribute iri:descriptor_isomeric_smiles(descriptor_compound_isomeric_smileses.compound) .

        iri:compound(descriptor_compound_canonical_smileses.compound)
            sio:has-attribute iri:descriptor_canonical_smiles(descriptor_compound_canonical_smileses.compound) .

        iri:compound(descriptor_compound_iupac_inchis.compound)
            sio:has-attribute iri:descriptor_iupac_inchi(descriptor_compound_iupac_inchis.compound) .

        iri:compound(descriptor_compound_iupac_inchis_long.compound)
            sio:has-attribute iri:descriptor_iupac_inchi(descriptor_compound_iupac_inchis_long.compound) .

        iri:compound(descriptor_compound_preferred_iupac_names.compound)
            sio:has-attribute iri:descriptor_preferred_iupac_name(descriptor_compound_preferred_iupac_names.compound) .

        iri:compound(descriptor_compound_preferred_iupac_names_long.compound)
            sio:has-attribute iri:descriptor_preferred_iupac_name(descriptor_compound_preferred_iupac_names_long.compound) .
    }.
}.;
