create type chembl_tmp.molecule_reference_type as enum
(
    'ACTOR',
    'ATLAS',
    'DRUGBANK',
    'EMOLECULES',
    'FDA SRS',
    'HMDB',
    'IUPHAR',
    'KEGG LIGAND',
    'LINCS',
    'MCULE',
    'NIKKAJI',
    'NMR SHIFT DB2',
    'PDBE',
    'PHARM GKB',
    'RECON',
    'SELLECK',
    'SURE CHEMBL',
    'WIKIPEDIA MOL',
    'ZINC'
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


create table chembl_tmp.molecule_pubchem_thom_pharm_references
(
    molecule_id     integer not null,
    substance_id    integer not null,
    primary key(molecule_id, substance_id)
);


create table chembl_tmp.molecule_pubchem_dotf_references
(
    molecule_id     integer not null,
    substance_id    integer not null,
    primary key(molecule_id, substance_id)
);


create table chembl_tmp.molecule_chebi_references
(
    molecule_id     integer not null,
    chebi_id        integer not null,
    primary key(molecule_id, chebi_id)
);
