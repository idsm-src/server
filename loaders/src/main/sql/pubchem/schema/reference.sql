create type pubchem.reference_source_type as enum
(
    'DRUGBANK',
    'DATACITE',
    'THIEME_CHEMISTRY',
    'HMDB',
    'SPRINGER',
    'SPRINGERNATURE',
    'PUBMED',
    'CROSSREF',
    'NATURE_NATCATAL',
    'NATURE_PORTFOLIO',
    'NATURE_NATSYNTH',
    'NATURE_NCHEMBIO',
    'NATURE_NCOMMS',
    'NATURE_NCHEM'
);


create table pubchem.reference_bases
(
    id              integer not null,
    dcdate          date,
    title           varchar,
    citation        varchar,
    publication     varchar,
    issue           varchar,
    starting_page   varchar,
    ending_page     varchar,
    page_range      varchar,
    lang            varchar,
    primary key(id)
);


create table pubchem.reference_discusses
(
    reference   integer not null,
    statement   varchar not null,
    primary key(reference, statement)
);


create table pubchem.reference_subjects
(
    reference   integer not null,
    subject     varchar not null,
    primary key(reference, subject)
);


create table pubchem.reference_anzsrc_subjects
(
    reference   integer not null,
    subject     varchar not null,
    primary key(reference, subject)
);


create table pubchem.reference_primary_subjects
(
    reference   integer not null,
    subject     varchar not null,
    primary key(reference, subject)
);


create table pubchem.reference_content_types
(
    reference   integer not null,
    type        varchar not null,
    primary key(reference, type)
);


create table pubchem.reference_issn_numbers
(
    reference   integer not null,
    issn        varchar not null,
    primary key(reference, issn)
);


create table pubchem.reference_authors
(
    reference   integer not null,
    author      integer not null,
    primary key(reference, author)
);


create table pubchem.reference_grants
(
    reference   integer not null,
    grantid     integer not null,
    primary key(reference, grantid)
);


create table pubchem.reference_organizations
(
    reference       integer not null,
    organization    integer not null,
    primary key(reference, organization)
);


create table pubchem.reference_journals
(
    reference   integer not null,
    journal     integer not null,
    primary key(reference, journal)
);


create table pubchem.reference_books
(
    reference   integer not null,
    book        integer not null,
    primary key(reference, book)
);


create table pubchem.reference_isbn_books
(
    reference   integer not null,
    isbn        varchar not null,
    primary key(reference, isbn)
);


create table pubchem.reference_issn_journals
(
    reference   integer not null,
    issn        varchar not null,
    primary key(reference, issn)
);


create table pubchem.reference_mined_compounds
(
    reference   integer not null,
    compound    integer not null,
    primary key(reference, compound)
);


create table pubchem.reference_mined_diseases
(
    reference   integer not null,
    disease     integer not null,
    primary key(reference, disease)
);


create table pubchem.reference_mined_genes
(
    reference   integer not null,
    gene_symbol integer not null,
    primary key(reference, gene_symbol)
);


create table pubchem.reference_mined_enzymes
(
    reference   integer not null,
    enzyme      integer not null,
    primary key(reference, enzyme)
);


create table pubchem.reference_doi_identifiers
(
    reference   integer not null,
    doi         varchar not null,
    primary key(reference, doi)
);


create table pubchem.reference_pubmed_identifiers
(
    reference   integer not null,
    pubmed      varchar not null,
    primary key(reference, pubmed)
);


create table pubchem.reference_sources
(
    reference   integer not null,
    source_type      pubchem.reference_source_type not null,
    primary key(reference, source_type)
);
