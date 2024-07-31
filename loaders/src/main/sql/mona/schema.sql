create table mona.compound_bases
(
    id              integer not null,
    created         date,
    curated         date,
    updated         date,
    accession       varchar unique not null,
    spectrum        pgms.spectrum,
    splash          varchar,
    level           integer,
    ionization_mode varchar,
    ionization_type integer,
    library         integer,
    submitter       integer,
    link            varchar,
    primary key(id)
);


create table mona.compound_structures
(
    compound        integer not null,
    structure       varchar not null,
    primary key(compound)
);


create table mona.compound_names
(
    id              serial unique not null,
    compound        integer not null,
    name            varchar not null,
    primary key(compound, name)
);

create table mona.compound_classyfires
(
    compound        integer not null,
    class           integer not null,
    primary key(compound, class)
);


create table mona.compound_chebi_classes
(
    compound        integer not null,
    chebi           integer not null,
    primary key(compound, chebi)
);


create table mona.compound_mesh_classes
(
    compound        integer not null,
    mesh            varchar not null,
    primary key(compound, mesh)
);


create table mona.compound_inchis
(
    id              integer not null,
    compound        integer not null,
    inchi           varchar not null,
    primary key(id)
);


create table mona.compound_inchikeys
(
    id              serial unique not null,
    compound        integer not null,
    inchikey        varchar not null,
    primary key(compound, inchikey)
);


create table mona.compound_formulas
(
    id              serial unique not null,
    compound        integer not null,
    formula         varchar not null,
    primary key(compound, formula)
);


create table mona.compound_smileses
(
    id              serial unique not null,
    compound        integer not null,
    smiles          varchar not null,
    primary key(compound, smiles)
);


create table mona.compound_exact_masses
(
    id              serial unique not null,
    compound        integer not null,
    mass            real not null,
    primary key(compound, mass)
);


create table mona.compound_monoisotopic_masses
(
    id              serial unique not null,
    compound        integer not null,
    mass            real not null,
    primary key(compound, mass)
);


create table mona.compound_cas_numbers
(
    id              serial unique not null,
    compound        integer not null,
    cas             varchar not null,
    primary key(compound, cas)
);


create table mona.compound_hmdb_ids
(
    id              serial unique not null,
    compound        integer not null,
    hmdb            varchar not null,
    primary key(compound, hmdb)
);


create table mona.compound_chebi_ids
(
    id              serial unique not null,
    compound        integer not null,
    chebi           integer not null,
    primary key(compound, chebi)
);


create table mona.compound_chemspider_ids
(
    id              serial unique not null,
    compound        integer not null,
    chemspider      varchar not null,
    primary key(compound, chemspider)
);


create table mona.compound_kegg_ids
(
    id              serial unique not null,
    compound        integer not null,
    kegg            varchar not null,
    primary key(compound, kegg)
);


create table mona.compound_knapsack_ids
(
    id              serial unique not null,
    compound        integer not null,
    knapsack        varchar not null,
    primary key(compound, knapsack)
);


create table mona.compound_lipidbank_ids
(
    id              serial unique not null,
    compound        integer not null,
    lipidbank       varchar not null,
    primary key(compound, lipidbank)
);


create table mona.compound_lipidmaps_ids
(
    id              serial unique not null,
    compound        integer not null,
    lipidmaps       varchar not null,
    primary key(compound, lipidmaps)
);


create table mona.compound_pubchem_compound_ids
(
    id              serial unique not null,
    compound        integer not null,
    cid             integer not null,
    primary key(compound, cid)
);


create table mona.compound_pubchem_substance_ids
(
    id              serial unique not null,
    compound        integer not null,
    sid             integer not null,
    primary key(compound, sid)
);


create table mona.spectrum_annotations
(
    id              integer not null,
    compound        integer not null,
    peak            real not null,
    value           varchar not null,
    primary key(id)
);


create table mona.spectrum_tags
(
    id              serial unique not null,
    compound        integer not null,
    tag             varchar not null,
    primary key(compound, tag)
);


create table mona.spectrum_normalized_entropies
(
    compound        integer not null,
    entropy         real not null,
    primary key(compound, entropy)
);


create table mona.spectrum_spectral_entropies
(
    compound        integer not null,
    entropy         real not null,
    primary key(compound, entropy)
);


create table mona.spectrum_retention_times
(
    id              serial unique not null,
    compound        integer not null,
    time            real not null,
    unit            integer not null,
    primary key(compound, time, unit)
);


create table mona.spectrum_collision_energies
(
    id              serial unique not null,
    compound        integer not null,
    energy          real not null,
    unit            integer not null,
    primary key(compound, energy, unit)
);


create table mona.spectrum_collision_energy_ramps
(
    id              serial unique not null,
    compound        integer not null,
    ramp_start      real not null,
    ramp_end        real not null,
    unit            integer not null,
    primary key(compound, ramp_start, ramp_end, unit)
);


create table mona.spectrum_instrument_types
(
    id              serial unique not null,
    compound        integer not null,
    type            varchar not null,
    primary key(compound, type)
);


create table mona.spectrum_instruments
(
    id              serial unique not null,
    compound        integer not null,
    instrument      varchar not null,
    primary key(compound, instrument)
);


create table mona.spectrum_precursor_types
(
    id              serial unique not null,
    compound        integer not null,
    type            varchar not null,
    primary key(compound, type)
);


create table mona.spectrum_precursor_mzs
(
    id              serial unique not null,
    compound        integer not null,
    mz              real not null,
    primary key(compound, mz)
);


create table mona.library_bases
(
    id              integer not null,
    name            varchar unique not null,
    description     varchar,
    primary key(id)
);


create table mona.submitter_bases
(
    id              integer not null,
    email           varchar,
    first_name      varchar,
    last_name       varchar,
    institution     varchar,
    primary key(id)
);
