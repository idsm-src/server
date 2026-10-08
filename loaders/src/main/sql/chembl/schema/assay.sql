create table chembl.assay_bases
(
    id                    integer not null,
    chembl_id             varchar,
    type                  varchar,
    description           varchar,
    document              integer,
    target                integer,
    source                integer,
    cell_line             integer,
    format_id             integer,
    organism              varchar,
    taxonomy              integer,
    category              varchar,
    cell_type             varchar,
    strain                varchar,
    tissue                varchar,
    subcellular_fraction  varchar,
    test_type             varchar,
    relationship_type     varchar,
    relationship_desc     varchar,
    confidence_score      integer,
    confidence_desc       varchar,
    pubchem_assay         integer,
    pubchem_bioassay      integer,
    primary key(id)
);


create table chembl.assay_reference_labels
(
    reference  integer not null,
    label      varchar not null,
    primary key(reference, label)
);
