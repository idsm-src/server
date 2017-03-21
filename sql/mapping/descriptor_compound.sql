sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.descriptor_compound_bases                      as descriptor_compound_bases
    from DB.rdf.descriptor_compound_molecular_formulas         as descriptor_compound_molecular_formulas
    from DB.rdf.descriptor_compound_isomeric_smileses          as descriptor_compound_isomeric_smileses
    from DB.rdf.descriptor_compound_canonical_smileses         as descriptor_compound_canonical_smileses
    from DB.rdf.descriptor_compound_iupac_inchis               as descriptor_compound_iupac_inchis
    from DB.rdf.descriptor_compound_iupac_inchis_long          as descriptor_compound_iupac_inchis_long
    from DB.rdf.descriptor_compound_preferred_iupac_names      as descriptor_compound_preferred_iupac_names
    from DB.rdf.descriptor_compound_preferred_iupac_names_long as descriptor_compound_preferred_iupac_names_long
{
    create map:descriptor_compound as graph descriptor:compound
    {
        iri:descriptor_hydrogen_bond_acceptor_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000388 where (^{descriptor_compound_bases.}^.hydrogen_bond_acceptor_count is not null) ;
            template:itemTemplate "pubchem/descriptor/hydrogen_bond_acceptor_count.vm" where (^{descriptor_compound_bases.}^.hydrogen_bond_acceptor_count is not null) ;
            sio:has-value descriptor_compound_bases.hydrogen_bond_acceptor_count .

        iri:descriptor_tautomer_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000391 where (^{descriptor_compound_bases.}^.tautomer_count is not null) ;
            template:itemTemplate "pubchem/descriptor/tautomer_count.vm" where (^{descriptor_compound_bases.}^.tautomer_count is not null) ;
            sio:has-value descriptor_compound_bases.tautomer_count .

        iri:descriptor_defined_atom_stereo_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000370 where (^{descriptor_compound_bases.}^.defined_atom_stereo_count is not null) ;
            template:itemTemplate "pubchem/descriptor/defined_atom_stereo_count.vm" where (^{descriptor_compound_bases.}^.defined_atom_stereo_count is not null) ;
            sio:has-value descriptor_compound_bases.defined_atom_stereo_count .

        iri:descriptor_defined_bond_stereo_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000371 where (^{descriptor_compound_bases.}^.defined_bond_stereo_count is not null) ;
            template:itemTemplate "pubchem/descriptor/defined_bond_stereo_count.vm" where (^{descriptor_compound_bases.}^.defined_bond_stereo_count is not null) ;
            sio:has-value descriptor_compound_bases.defined_bond_stereo_count .

        iri:descriptor_undefined_bond_stereo_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000375 where (^{descriptor_compound_bases.}^.undefined_bond_stereo_count is not null) ;
            template:itemTemplate "pubchem/descriptor/undefined_bond_stereo_count.vm" where (^{descriptor_compound_bases.}^.undefined_bond_stereo_count is not null) ;
            sio:has-value descriptor_compound_bases.undefined_bond_stereo_count .

        iri:descriptor_isotope_atom_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000372 where (^{descriptor_compound_bases.}^.isotope_atom_count is not null) ;
            template:itemTemplate "pubchem/descriptor/isotope_atom_count.vm" where (^{descriptor_compound_bases.}^.isotope_atom_count is not null) ;
            sio:has-value descriptor_compound_bases.isotope_atom_count .

        iri:descriptor_covalent_unit_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000369 where (^{descriptor_compound_bases.}^.covalent_unit_count is not null) ;
            template:itemTemplate "pubchem/descriptor/covalent_unit_count.vm" where (^{descriptor_compound_bases.}^.covalent_unit_count is not null) ;
            sio:has-value descriptor_compound_bases.covalent_unit_count .

        iri:descriptor_hydrogen_bond_donor_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000387 where (^{descriptor_compound_bases.}^.hydrogen_bond_donor_count is not null) ;
            template:itemTemplate "pubchem/descriptor/hydrogen_bond_donor_count.vm" where (^{descriptor_compound_bases.}^.hydrogen_bond_donor_count is not null) ;
            sio:has-value descriptor_compound_bases.hydrogen_bond_donor_count .

        iri:descriptor_non_hydrogen_atom_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000373 where (^{descriptor_compound_bases.}^.non_hydrogen_atom_count is not null) ;
            template:itemTemplate "pubchem/descriptor/non_hydrogen_atom_count.vm" where (^{descriptor_compound_bases.}^.non_hydrogen_atom_count is not null) ;
            sio:has-value descriptor_compound_bases.non_hydrogen_atom_count .

        iri:descriptor_rotatable_bond_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000389 where (^{descriptor_compound_bases.}^.rotatable_bond_count is not null) ;
            template:itemTemplate "pubchem/descriptor/rotatable_bond_count.vm" where (^{descriptor_compound_bases.}^.rotatable_bond_count is not null) ;
            sio:has-value descriptor_compound_bases.rotatable_bond_count .

        iri:descriptor_undefined_atom_stereo_count(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000374 where (^{descriptor_compound_bases.}^.undefined_atom_stereo_count is not null) ;
            template:itemTemplate "pubchem/descriptor/undefined_atom_stereo_count.vm" where (^{descriptor_compound_bases.}^.undefined_atom_stereo_count is not null) ;
            sio:has-value descriptor_compound_bases.undefined_atom_stereo_count .

        iri:descriptor_total_formal_charge(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000336 where (^{descriptor_compound_bases.}^.total_formal_charge is not null) ;
            template:itemTemplate "pubchem/descriptor/total_formal_charge.vm" where (^{descriptor_compound_bases.}^.total_formal_charge is not null) ;
            sio:has-value descriptor_compound_bases.total_formal_charge .

        iri:descriptor_structure_complexity(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000390 where (^{descriptor_compound_bases.}^.structure_complexity is not null) ;
            template:itemTemplate "pubchem/descriptor/structure_complexity.vm" where (^{descriptor_compound_bases.}^.structure_complexity is not null) ;
            sio:has-value descriptor_compound_bases.structure_complexity .

        iri:descriptor_mono_isotopic_weight(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000337 where (^{descriptor_compound_bases.}^.mono_isotopic_weight is not null) ;
            template:itemTemplate "pubchem/descriptor/mono_isotopic_weight.vm" where (^{descriptor_compound_bases.}^.mono_isotopic_weight is not null) ;
            sio:has-unit obo:UO_0000055 where (^{descriptor_compound_bases.}^.mono_isotopic_weight is not null) ;
            sio:has-value descriptor_compound_bases.mono_isotopic_weight .

        iri:descriptor_xlogp3_aa(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000395 where (^{descriptor_compound_bases.}^.xlogp3_aa is not null) ;
            template:itemTemplate "pubchem/descriptor/xlogp3_aa.vm" where (^{descriptor_compound_bases.}^.xlogp3_aa is not null) ;
            sio:has-value descriptor_compound_bases.xlogp3_aa .

        iri:descriptor_exact_mass(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000338 where (^{descriptor_compound_bases.}^.exact_mass is not null) ;
            template:itemTemplate "pubchem/descriptor/exact_mass.vm" where (^{descriptor_compound_bases.}^.exact_mass is not null) ;
            sio:has-unit obo:UO_0000055 where (^{descriptor_compound_bases.}^.exact_mass is not null) ;
            sio:has-value descriptor_compound_bases.exact_mass .

        iri:descriptor_molecular_weight(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000334 where (^{descriptor_compound_bases.}^.molecular_weight is not null) ;
            template:itemTemplate "pubchem/descriptor/molecular_weight.vm" where (^{descriptor_compound_bases.}^.molecular_weight is not null) ;
            sio:has-unit obo:UO_0000055 where (^{descriptor_compound_bases.}^.molecular_weight is not null) ;
            sio:has-value descriptor_compound_bases.molecular_weight .

        iri:descriptor_tpsa(descriptor_compound_bases.compound)
            rdf:type sio:CHEMINF_000392 where (^{descriptor_compound_bases.}^.tpsa is not null) ;
            template:itemTemplate "pubchem/descriptor/tpsa.vm" where (^{descriptor_compound_bases.}^.tpsa is not null) ;
            sio:has-unit obo:UO_0000324 where (^{descriptor_compound_bases.}^.tpsa is not null) ;
            sio:has-value descriptor_compound_bases.tpsa .

        iri:descriptor_molecular_formula(descriptor_compound_molecular_formulas.compound)
            rdf:type sio:CHEMINF_000335 ;
            template:itemTemplate "pubchem/descriptor/molecular_formula.vm";
            sio:has-value descriptor_compound_molecular_formulas.molecular_formula .

        iri:descriptor_isomeric_smiles(descriptor_compound_isomeric_smileses.compound)
            rdf:type sio:CHEMINF_000379 ;
            template:itemTemplate "pubchem/descriptor/isomeric_smiles.vm";
            sio:has-value descriptor_compound_isomeric_smileses.isomeric_smiles .

        iri:descriptor_canonical_smiles(descriptor_compound_canonical_smileses.compound)
            rdf:type sio:CHEMINF_000376 ;
            template:itemTemplate "pubchem/descriptor/canonical_smiles.vm";
            sio:has-value descriptor_compound_canonical_smileses.canonical_smiles .

        iri:descriptor_iupac_inchi(descriptor_compound_iupac_inchis.compound)
            rdf:type sio:CHEMINF_000396 ;
            template:itemTemplate "pubchem/descriptor/iupac_inchi.vm";
            sio:has-value descriptor_compound_iupac_inchis.iupac_inchi .

        iri:descriptor_iupac_inchi(descriptor_compound_iupac_inchis_long.compound)
            rdf:type sio:CHEMINF_000396 ;
            template:itemTemplate "pubchem/descriptor/iupac_inchi.vm";
            sio:has-value descriptor_compound_iupac_inchis_long.iupac_inchi .

        iri:descriptor_preferred_iupac_name(descriptor_compound_preferred_iupac_names.compound)
            rdf:type sio:CHEMINF_000382 ;
            template:itemTemplate "pubchem/descriptor/preferred_iupac_name.vm";
            sio:has-value descriptor_compound_preferred_iupac_names.preferred_iupac_name .

        iri:descriptor_preferred_iupac_name(descriptor_compound_preferred_iupac_names_long.compound)
            rdf:type sio:CHEMINF_000382 ;
            template:itemTemplate "pubchem/descriptor/preferred_iupac_name.vm";
            sio:has-value descriptor_compound_preferred_iupac_names_long.preferred_iupac_name .
    }.
}.;
