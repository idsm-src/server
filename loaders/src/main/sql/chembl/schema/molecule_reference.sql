create type chembl_tmp.molecule_reference_type as enum
(
    'BINDING DB',
    'BRENDA',
    'CCDC',
    'CHEBI',
    'CLINICAL TRIALS',
    'COMPTOX',
    'DRUG CENTRAL',
    'DRUGBANK',
    'FDA SRS',
    'FOO DB',
    'HMDB',
    'IUPHAR',
    'LIPID MAPS',
    'MOLPORT',
    'NMR SHIFT DB2',
    'PDBE',
    'PROBES AND DRUGS',
    'PUBCHEM',
    'RCSB PDB',
    'RHEA CHEBI',
    'RHEA POLYMER',
    'SURE CHEMBL',
    'SWISS LIPIDS'
);


create table chembl_tmp.molecule_references
(
    refmol_id       integer not null,
    molecule_id     integer not null,
    reference_type  chembl_tmp.molecule_reference_type not null,
    reference       varchar not null,
    primary key(refmol_id)
);


create table chembl_tmp.molecule_pubchem_references
(
    molecule_id     integer not null,
    compound_id     integer not null,
    primary key(molecule_id, compound_id)
);


create table chembl_tmp.molecule_chebi_references
(
    molecule_id     integer not null,
    chebi_id        integer not null,
    primary key(molecule_id, chebi_id)
);
