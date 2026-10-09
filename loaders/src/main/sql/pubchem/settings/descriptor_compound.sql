create index compound_descriptors__hydrogen_bond_acceptor_count on pubchem.compound_descriptors(hydrogen_bond_acceptor_count);
create index compound_descriptors__defined_atom_stereo_count on pubchem.compound_descriptors(defined_atom_stereo_count);
create index compound_descriptors__defined_bond_stereo_count on pubchem.compound_descriptors(defined_bond_stereo_count);
create index compound_descriptors__undefined_bond_stereo_count on pubchem.compound_descriptors(undefined_bond_stereo_count);
create index compound_descriptors__isotope_atom_count on pubchem.compound_descriptors(isotope_atom_count);
create index compound_descriptors__covalent_unit_count on pubchem.compound_descriptors(covalent_unit_count);
create index compound_descriptors__hydrogen_bond_donor_count on pubchem.compound_descriptors(hydrogen_bond_donor_count);
create index compound_descriptors__non_hydrogen_atom_count on pubchem.compound_descriptors(non_hydrogen_atom_count);
create index compound_descriptors__rotatable_bond_count on pubchem.compound_descriptors(rotatable_bond_count);
create index compound_descriptors__undefined_atom_stereo_count on pubchem.compound_descriptors(undefined_atom_stereo_count);
create index compound_descriptors__total_formal_charge on pubchem.compound_descriptors(total_formal_charge);
create index compound_descriptors__structure_complexity on pubchem.compound_descriptors(structure_complexity);
create index compound_descriptors__mono_isotopic_weight on pubchem.compound_descriptors(mono_isotopic_weight);
create index compound_descriptors__xlogp3_aa on pubchem.compound_descriptors(xlogp3_aa);
create index compound_descriptors__xlogp3 on pubchem.compound_descriptors(xlogp3);
create index compound_descriptors__exact_mass on pubchem.compound_descriptors(exact_mass);
create index compound_descriptors__molecular_weight on pubchem.compound_descriptors(molecular_weight);
create index compound_descriptors__tpsa on pubchem.compound_descriptors(tpsa);
grant select on pubchem.compound_descriptors to sparql;

--------------------------------------------------------------------------------

create index compound_molecular_formulas__molecular_formula on pubchem.compound_molecular_formulas(molecular_formula);
grant select on pubchem.compound_molecular_formulas to sparql;

--------------------------------------------------------------------------------

create index compound_smileses__smiles on pubchem.compound_smileses using hash (smiles);
grant select on pubchem.compound_smileses to sparql;

--------------------------------------------------------------------------------

create index compound_connectivity_smileses__connectivity_smiles on pubchem.compound_connectivity_smileses using hash (connectivity_smiles);
grant select on pubchem.compound_connectivity_smileses to sparql;

--------------------------------------------------------------------------------

create index compound_iupac_inchis__iupac_inchi on pubchem.compound_iupac_inchis using hash (iupac_inchi);
grant select on pubchem.compound_iupac_inchis to sparql;

--------------------------------------------------------------------------------

create index compound_preferred_iupac_names__preferred_iupac_name on pubchem.compound_preferred_iupac_names using hash (preferred_iupac_name);
create index compound_preferred_iupac_names__preferred_iupac_name__lower on pubchem.compound_preferred_iupac_names using hash (lower(preferred_iupac_name));
create index compound_preferred_iupac_names__preferred_iupac_name__english on pubchem.compound_preferred_iupac_names using gin (to_tsvector('english', preferred_iupac_name));
create index compound_preferred_iupac_names__preferred_iupac_name__simple on pubchem.compound_preferred_iupac_names using gin (to_tsvector('simple', preferred_iupac_name));
grant select on pubchem.compound_preferred_iupac_names to sparql;
