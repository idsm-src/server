create table pubchem.compound_descriptors
(
    compound                        integer not null,
    hydrogen_bond_acceptor_count    numeric,
    defined_atom_stereo_count       numeric,
    defined_bond_stereo_count       numeric,
    undefined_bond_stereo_count     numeric,
    isotope_atom_count              numeric,
    covalent_unit_count             numeric,
    hydrogen_bond_donor_count       numeric,
    non_hydrogen_atom_count         numeric,
    rotatable_bond_count            numeric,
    undefined_atom_stereo_count     numeric,
    total_formal_charge             numeric,
    structure_complexity            numeric,
    mono_isotopic_weight            numeric,
    xlogp3_aa                       numeric,
    xlogp3                          numeric,
    exact_mass                      numeric,
    molecular_weight                numeric,
    tpsa                            numeric,
    primary key(compound)
);


create table pubchem.compound_molecular_formulas
(
    compound             integer not null,
    molecular_formula    varchar not null,
    primary key(compound)
);


create table pubchem.compound_smileses
(
    compound           integer not null,
    smiles             varchar not null,
    primary key(compound)
);


create table pubchem.compound_connectivity_smileses
(
    compound            integer not null,
    connectivity_smiles varchar not null,
    primary key(compound)
);


create table pubchem.compound_iupac_inchis
(
    compound       integer not null,
    iupac_inchi    varchar not null,
    primary key(compound)
);


create table pubchem.compound_preferred_iupac_names
(
    compound                integer not null,
    preferred_iupac_name    varchar not null,
    primary key(compound)
);
