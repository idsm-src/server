create table chembl.molecules
(
    id                   integer not null,
    chembl_id            varchar,
    type                 varchar,
    label                varchar,
    phase                real,
    biotherapeutic       boolean,
    helm_notation        varchar,
    description          varchar,
    hrac_classification  varchar,
    irac_classification  varchar,
    frac_classification  varchar,
    parent               integer,
    primary key(id)
);


create table chembl.molecule_alternatives
(
    molecule     integer not null,
    alternative  varchar not null,
    primary key(molecule, alternative)
);


create table chembl.molecule_atc_classifications
(
    molecule        integer not null,
    classification  varchar not null,
    primary key(molecule, classification)
);


create table chembl.molecule_documents
(
    molecule  integer not null,
    document  integer not null,
    primary key(molecule, document)
);


create table chembl.molecule_biocomponents
(
    molecule      integer not null,
    biocomponent  integer not null,
    primary key(molecule, biocomponent)
);


create table chembl.molecule_descriptors
(
    molecule            integer not null,
    alogp               float8,
    aromatic_rings      float8,
    hba                 float8,
    hbd                 float8,
    heavy_atoms         float8,
    num_ro5_violations  float8,
    psa                 float8,
    qed_weighted        float8,
    rtb                 float8,
    mw_freebase         float8,
    full_mwt            float8,
    ro3_pass            varchar,
    full_molformula     varchar,
    primary key(molecule)
);


create table chembl.molecule_structures
(
    molecule            integer not null,
    standard_inchi      varchar,
    standard_inchi_key  varchar,
    canonical_smiles    varchar,
    primary key(molecule)
);


create table chembl.molecule_molfiles
(
    molecule            integer not null,
    molfile             varchar not null,
    primary key(molecule)
);


create table chembl.molecule_labels
(
    molecule            integer not null,
    alogp               varchar,
    aromatic_rings      varchar,
    hba                 varchar,
    hbd                 varchar,
    heavy_atoms         varchar,
    num_ro5_violations  varchar,
    psa                 varchar,
    qed_weighted        varchar,
    rtb                 varchar,
    mw_freebase         varchar,
    full_mwt            varchar,
    ro3_pass            varchar,
    full_molformula     varchar,
    standard_inchi      varchar,
    standard_inchi_key  varchar,
    canonical_smiles    varchar,
    image               varchar,
    primary key(molecule)
);


create type chembl.molecule_reference_type as enum
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


create table chembl.molecule_references
(
    molecule   integer not null,
    type       chembl.molecule_reference_type not null,
    reference  varchar not null,
    primary key(molecule, type, reference)
);


create table chembl.molecule_reference_labels
(
    type       chembl.molecule_reference_type not null,
    reference  varchar not null,
    label      varchar not null,
    primary key(type, reference, label)
);


create table chembl.molecule_pubchem_references
(
    molecule  integer not null,
    compound  integer not null,
    primary key(molecule, compound)
);


create table chembl.molecule_chebi_references
(
    molecule  integer not null,
    chebi     integer not null,
    primary key(molecule, chebi)
);
