create function hydrogen_bond_acceptor_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Hydrogen_Bond_Acceptor_Count';
$$
immutable parallel safe;


create function hydrogen_bond_acceptor_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function tautomer_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Tautomer_Count';
$$
immutable parallel safe;


create function tautomer_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function defined_atom_stereo_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Defined_Atom_Stereo_Count';
$$
immutable parallel safe;


create function defined_atom_stereo_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function defined_bond_stereo_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Defined_Bond_Stereo_Count';
$$
immutable parallel safe;


create function defined_bond_stereo_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function undefined_bond_stereo_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Undefined_Bond_Stereo_Count';
$$
immutable parallel safe;


create function undefined_bond_stereo_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function isotope_atom_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Isotope_Atom_Count';
$$
immutable parallel safe;


create function isotope_atom_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function covalent_unit_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Covalent_Unit_Count';
$$
immutable parallel safe;


create function covalent_unit_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function hydrogen_bond_donor_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Hydrogen_Bond_Donor_Count';
$$
immutable parallel safe;


create function hydrogen_bond_donor_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function non_hydrogen_atom_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Non-hydrogen_Atom_Count';
$$
immutable parallel safe;


create function non_hydrogen_atom_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function rotatable_bond_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Rotatable_Bond_Count';
$$
immutable parallel safe;


create function rotatable_bond_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function undefined_atom_stereo_count(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Undefined_Atom_Stereo_Count';
$$
immutable parallel safe;


create function undefined_atom_stereo_count_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function total_formal_charge(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Total_Formal_Charge';
$$
immutable parallel safe;


create function total_formal_charge_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function structure_complexity(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Structure_Complexity';
$$
immutable parallel safe;


create function structure_complexity_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function mono_isotopic_weight(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Mono_Isotopic_Weight';
$$
immutable parallel safe;


create function mono_isotopic_weight_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function xlogp3_aa(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_XLogP3-AA';
$$
immutable parallel safe;


create function xlogp3_aa_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function exact_mass(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Exact_Mass';
$$
immutable parallel safe;


create function exact_mass_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function molecular_weight(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Molecular_Weight';
$$
immutable parallel safe;


create function molecular_weight_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function tpsa(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_TPSA';
$$
immutable parallel safe;


create function tpsa_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function molecular_formula(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Molecular_Formula';
$$
immutable parallel safe;


create function molecular_formula_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function isomeric_smiles(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Isomeric_SMILES';
$$
immutable parallel safe;


create function isomeric_smiles_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function canonical_smiles(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Canonical_SMILES';
$$
immutable parallel safe;


create function canonical_smiles_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function iupac_inchi(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_IUPAC_InChI';
$$
immutable parallel safe;


create function iupac_inchi_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;

--------------------------------------------------------------------------------

create function preferred_iupac_name(id in integer) returns varchar language sql as
$$
  select 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID' || id || '_Preferred_IUPAC_Name';
$$
immutable parallel safe;


create function preferred_iupac_name_inverse(iri in varchar) returns integer language sql as
$$
  select substring(iri, 51, strpos(iri, '_') - 51)::integer;
$$
immutable parallel safe;
