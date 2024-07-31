create table pubchem.patent_bases
(
    id                  integer not null,
    iri                 varchar unique not null,
    title               varchar,
    abstract            varchar,
    publication_number  varchar,
    filing_date         date,
    grant_date          date,
    publication_date    date,
    priority_date       date,
    primary key(id)
);


create table pubchem.patent_cpc_additional_classifications
(
    patent              integer not null,
    classification      varchar not null,
    primary key(patent, classification)
);


create table pubchem.patent_cpc_inventive_classifications
(
    patent              integer not null,
    classification      varchar not null,
    primary key(patent, classification)
);


create table pubchem.patent_ipc_additional_classifications
(
    patent              integer not null,
    classification      varchar not null,
    primary key(patent, classification)
);


create table pubchem.patent_ipc_inventive_classifications
(
    patent              integer not null,
    classification      varchar not null,
    primary key(patent, classification)
);


create table pubchem.patent_citations
(
    patent              integer not null,
    citation            integer not null,
    primary key(patent, citation)
);


create table pubchem.patent_substances
(
    patent              integer not null,
    substance           integer not null,
    primary key(patent, substance)
);


create table pubchem.patent_compounds
(
    patent              integer not null,
    compound            integer not null,
    primary key(patent, compound)
);


create table pubchem.patent_genes
(
    patent              integer not null,
    gene                integer not null,
    primary key(patent, gene)
);


create table pubchem.patent_proteins
(
    patent              integer not null,
    protein             integer not null,
    primary key(patent, protein)
);


create table pubchem.patent_taxonomies
(
    patent              integer not null,
    taxonomy            integer not null,
    primary key(patent, taxonomy)
);


create table pubchem.patent_anatomies
(
    patent              integer not null,
    anatomy             integer not null,
    primary key(patent, anatomy)
);


create table pubchem.patent_inventors
(
    patent              integer not null,
    inventor            varchar not null,
    primary key(patent, inventor)
);


create table pubchem.patent_applicants
(
    patent              integer not null,
    applicant           varchar not null,
    primary key(patent, applicant)
);


create table pubchem.patent_inventor_names
(
    inventor            varchar not null,
    formatted_name      varchar not null,
    primary key(inventor, formatted_name)
);


create table pubchem.patent_applicant_names
(
    applicant           varchar not null,
    formatted_name      varchar not null,
    primary key(applicant, formatted_name)
);
