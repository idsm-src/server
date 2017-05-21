create function hydrogen_bond_acceptor_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Hydrogen_Bond_Acceptor_Count';
$$
immutable;


create function hydrogen_bond_acceptor_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Hydrogen_Bond_Acceptor_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function tautomer_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Tautomer_Count';
$$
immutable;


create function tautomer_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Tautomer_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function defined_atom_stereo_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Defined_Atom_Stereo_Count';
$$
immutable;


create function defined_atom_stereo_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Defined_Atom_Stereo_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function defined_bond_stereo_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Defined_Bond_Stereo_Count';
$$
immutable;


create function defined_bond_stereo_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Defined_Bond_Stereo_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function undefined_bond_stereo_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Undefined_Bond_Stereo_Count';
$$
immutable;


create function undefined_bond_stereo_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Undefined_Bond_Stereo_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function isotope_atom_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Isotope_Atom_Count';
$$
immutable;


create function isotope_atom_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Isotope_Atom_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function covalent_unit_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Covalent_Unit_Count';
$$
immutable;


create function covalent_unit_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Covalent_Unit_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function hydrogen_bond_donor_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Hydrogen_Bond_Donor_Count';
$$
immutable;


create function hydrogen_bond_donor_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Hydrogen_Bond_Donor_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function non_hydrogen_atom_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Non-hydrogen_Atom_Count';
$$
immutable;


create function non_hydrogen_atom_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Non-hydrogen_Atom_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function rotatable_bond_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Rotatable_Bond_Count';
$$
immutable;


create function rotatable_bond_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Rotatable_Bond_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function undefined_atom_stereo_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Undefined_Atom_Stereo_Count';
$$
immutable;


create function undefined_atom_stereo_count_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Undefined_Atom_Stereo_Count$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function total_formal_charge(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Total_Formal_Charge';
$$
immutable;


create function total_formal_charge_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Total_Formal_Charge$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function structure_complexity(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Structure_Complexity';
$$
immutable;


create function structure_complexity_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Structure_Complexity$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function mono_isotopic_weight(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Mono_Isotopic_Weight';
$$
immutable;


create function mono_isotopic_weight_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Mono_Isotopic_Weight$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function xlogp3_aa(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_XLogP3-AA';
$$
immutable;


create function xlogp3_aa_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_XLogP3-AA$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function exact_mass(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Exact_Mass';
$$
immutable;


create function exact_mass_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Exact_Mass$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function molecular_weight(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Molecular_Weight';
$$
immutable;


create function molecular_weight_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Molecular_Weight$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function tpsa(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_TPSA';
$$
immutable;


create function tpsa_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_TPSA$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function molecular_formula(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Molecular_Formula';
$$
immutable;


create function molecular_formula_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Molecular_Formula$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function isomeric_smiles(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Isomeric_SMILES';
$$
immutable;


create function isomeric_smiles_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Isomeric_SMILES$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function canonical_smiles(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Canonical_SMILES';
$$
immutable;


create function canonical_smiles_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Canonical_SMILES$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function iupac_inchi(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_IUPAC_InChI';
$$
immutable;


create function iupac_inchi_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_IUPAC_InChI$', '\1')::integer;
$$
immutable;

--------------------------------------------------------------------------------

create function preferred_iupac_name(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Preferred_IUPAC_Name';
$$
immutable;


create function preferred_iupac_name_inverse(iri in varchar) returns integer language sql as
$$
  select regexp_replace(iri, '^http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID([0-9]+)_Preferred_IUPAC_Name$', '\1')::integer;
$$
immutable;
