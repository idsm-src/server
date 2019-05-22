create index descriptor_compound_bases__hydrogen_bond_acceptor_count on descriptor_compound_bases(hydrogen_bond_acceptor_count);
create index descriptor_compound_bases__tautomer_count on descriptor_compound_bases(tautomer_count);
create index descriptor_compound_bases__defined_atom_stereo_count on descriptor_compound_bases(defined_atom_stereo_count);
create index descriptor_compound_bases__defined_bond_stereo_count on descriptor_compound_bases(defined_bond_stereo_count);
create index descriptor_compound_bases__undefined_bond_stereo_count on descriptor_compound_bases(undefined_bond_stereo_count);
create index descriptor_compound_bases__isotope_atom_count on descriptor_compound_bases(isotope_atom_count);
create index descriptor_compound_bases__covalent_unit_count on descriptor_compound_bases(covalent_unit_count);
create index descriptor_compound_bases__hydrogen_bond_donor_count on descriptor_compound_bases(hydrogen_bond_donor_count);
create index descriptor_compound_bases__non_hydrogen_atom_count on descriptor_compound_bases(non_hydrogen_atom_count);
create index descriptor_compound_bases__rotatable_bond_count on descriptor_compound_bases(rotatable_bond_count);
create index descriptor_compound_bases__undefined_atom_stereo_count on descriptor_compound_bases(undefined_atom_stereo_count);
create index descriptor_compound_bases__total_formal_charge on descriptor_compound_bases(total_formal_charge);
create index descriptor_compound_bases__structure_complexity on descriptor_compound_bases(structure_complexity);
create index descriptor_compound_bases__mono_isotopic_weight on descriptor_compound_bases(mono_isotopic_weight);
create index descriptor_compound_bases__xlogp3_aa on descriptor_compound_bases(xlogp3_aa);
create index descriptor_compound_bases__exact_mass on descriptor_compound_bases(exact_mass);
create index descriptor_compound_bases__molecular_weight on descriptor_compound_bases(molecular_weight);
create index descriptor_compound_bases__tpsa on descriptor_compound_bases(tpsa);
grant select on descriptor_compound_bases to "SPARQL";

--------------------------------------------------------------------------------

create index descriptor_compound_molecular_formulas__molecular_formula on descriptor_compound_molecular_formulas(molecular_formula);
grant select on descriptor_compound_molecular_formulas to "SPARQL";

--------------------------------------------------------------------------------

create index descriptor_compound_isomeric_smileses__isomeric_smiles on descriptor_compound_isomeric_smileses(isomeric_smiles);
grant select on descriptor_compound_isomeric_smileses to "SPARQL";

--------------------------------------------------------------------------------

create index descriptor_compound_canonical_smileses__canonical_smiles on descriptor_compound_canonical_smileses(canonical_smiles);
grant select on descriptor_compound_canonical_smileses to "SPARQL";

--------------------------------------------------------------------------------

-- create index descriptor_compound_iupac_inchis__iupac_inchi on descriptor_compound_iupac_inchis(iupac_inchi); -- Values larger than 1/3 of a buffer page cannot be indexed.
grant select on descriptor_compound_iupac_inchis to "SPARQL";

--------------------------------------------------------------------------------

create index descriptor_compound_preferred_iupac_names__iupac_name on descriptor_compound_preferred_iupac_names(preferred_iupac_name);
create index descriptor_compound_preferred_iupac_names__iupac_name__gin on descriptor_compound_preferred_iupac_names using gin (to_tsvector('english', preferred_iupac_name));
grant select on descriptor_compound_preferred_iupac_names to "SPARQL";
