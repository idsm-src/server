sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:descriptor_compound
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:descriptor_hydrogen_bond_acceptor_count .;
sparql drop iri class iri:descriptor_tautomer_count .;
sparql drop iri class iri:descriptor_defined_atom_stereo_count .;
sparql drop iri class iri:descriptor_defined_bond_stereo_count .;
sparql drop iri class iri:descriptor_undefined_bond_stereo_count .;
sparql drop iri class iri:descriptor_isotope_atom_count .;
sparql drop iri class iri:descriptor_covalent_unit_count .;
sparql drop iri class iri:descriptor_hydrogen_bond_donor_count .;
sparql drop iri class iri:descriptor_non_hydrogen_atom_count .;
sparql drop iri class iri:descriptor_rotatable_bond_count .;
sparql drop iri class iri:descriptor_undefined_atom_stereo_count .;
sparql drop iri class iri:descriptor_total_formal_charge .;
sparql drop iri class iri:descriptor_structure_complexity .;
sparql drop iri class iri:descriptor_mono_isotopic_weight .;
sparql drop iri class iri:descriptor_xlogp3_aa .;
sparql drop iri class iri:descriptor_exact_mass .;
sparql drop iri class iri:descriptor_molecular_weight .;
sparql drop iri class iri:descriptor_tpsa .;
sparql drop iri class iri:descriptor_molecular_formula .;
sparql drop iri class iri:descriptor_isomeric_smiles .;
sparql drop iri class iri:descriptor_canonical_smiles .;
sparql drop iri class iri:descriptor_iupac_inchi .;
sparql drop iri class iri:descriptor_preferred_iupac_name .;

--------------------------------------------------------------------------------

drop table descriptor_compound_preferred_iupac_names_long;
drop table descriptor_compound_preferred_iupac_names;
drop table descriptor_compound_iupac_inchis_long;
drop table descriptor_compound_iupac_inchis;
drop table descriptor_compound_canonical_smileses;
drop table descriptor_compound_isomeric_smileses;
drop table descriptor_compound_molecular_formulas;
drop table descriptor_compound_bases;
