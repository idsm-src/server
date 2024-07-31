create index reference_bases__dcdate on pubchem.reference_bases(dcdate);
create index reference_bases__title on pubchem.reference_bases using hash (title);
create index reference_bases__citation on pubchem.reference_bases using hash (citation);
create index reference_bases__publication on pubchem.reference_bases(publication);
create index reference_bases__issue on pubchem.reference_bases(issue);
create index reference_bases__starting_page on pubchem.reference_bases(starting_page);
create index reference_bases__ending_page on pubchem.reference_bases(ending_page);
create index reference_bases__page_range on pubchem.reference_bases(page_range);
create index reference_bases__lang on pubchem.reference_bases(lang);
grant select on pubchem.reference_bases to sparql;

--------------------------------------------------------------------------------

create index reference_discusses__reference on pubchem.reference_discusses(reference);
create index reference_discusses__statement on pubchem.reference_discusses(statement);
grant select on pubchem.reference_discusses to sparql;

--------------------------------------------------------------------------------

create index reference_subjects__reference on pubchem.reference_subjects(reference);
create index reference_subjects__subject on pubchem.reference_subjects(subject);
grant select on pubchem.reference_subjects to sparql;

--------------------------------------------------------------------------------

create index reference_anzsrc_subjects__reference on pubchem.reference_anzsrc_subjects(reference);
create index reference_anzsrc_subjects__subject on pubchem.reference_anzsrc_subjects(subject);
grant select on pubchem.reference_anzsrc_subjects to sparql;

--------------------------------------------------------------------------------

create index reference_primary_subjects__reference on pubchem.reference_primary_subjects(reference);
create index reference_primary_subjects__subject on pubchem.reference_primary_subjects(subject);
grant select on pubchem.reference_primary_subjects to sparql;

--------------------------------------------------------------------------------

create index reference_content_types__reference on pubchem.reference_content_types(reference);
create index reference_content_types__type on pubchem.reference_content_types(type);
grant select on pubchem.reference_content_types to sparql;

--------------------------------------------------------------------------------

create index reference_issn_numbers__reference on pubchem.reference_issn_numbers(reference);
create index reference_issn_numbers__issn on pubchem.reference_issn_numbers(issn);
grant select on pubchem.reference_issn_numbers to sparql;

--------------------------------------------------------------------------------

create index reference_authors__reference on pubchem.reference_authors(reference);
create index reference_authors__author on pubchem.reference_authors(author);
grant select on pubchem.reference_authors to sparql;

--------------------------------------------------------------------------------

create index reference_grants__reference on pubchem.reference_grants(reference);
create index reference_grants__grantid on pubchem.reference_grants(grantid);
grant select on pubchem.reference_grants to sparql;

--------------------------------------------------------------------------------

create index reference_organizations__reference on pubchem.reference_organizations(reference);
create index reference_organizations__organization on pubchem.reference_organizations(organization);
grant select on pubchem.reference_organizations to sparql;

--------------------------------------------------------------------------------

create index reference_journals__reference on pubchem.reference_journals(reference);
create index reference_journals__journal on pubchem.reference_journals(journal);
grant select on pubchem.reference_journals to sparql;

--------------------------------------------------------------------------------

create index reference_books__reference on pubchem.reference_books(reference);
create index reference_books__book on pubchem.reference_books(book);
grant select on pubchem.reference_books to sparql;

--------------------------------------------------------------------------------

create index reference_isbn_books__reference on pubchem.reference_isbn_books(reference);
create index reference_isbn_books__isbn on pubchem.reference_isbn_books(isbn);
grant select on pubchem.reference_isbn_books to sparql;

--------------------------------------------------------------------------------

create index reference_issn_journals__reference on pubchem.reference_issn_journals(reference);
create index reference_issn_journals__issn on pubchem.reference_issn_journals(issn);
grant select on pubchem.reference_issn_journals to sparql;

--------------------------------------------------------------------------------

create index reference_mined_compounds__reference on pubchem.reference_mined_compounds(reference);
create index reference_mined_compounds__compound on pubchem.reference_mined_compounds(compound);
grant select on pubchem.reference_mined_compounds to sparql;

--------------------------------------------------------------------------------

create index reference_mined_diseases__reference on pubchem.reference_mined_diseases(reference);
create index reference_mined_diseases__disease on pubchem.reference_mined_diseases(disease);
grant select on pubchem.reference_mined_diseases to sparql;

--------------------------------------------------------------------------------

create index reference_mined_genes__reference on pubchem.reference_mined_genes(reference);
create index reference_mined_genes__gene_symbol on pubchem.reference_mined_genes(gene_symbol);
grant select on pubchem.reference_mined_genes to sparql;

--------------------------------------------------------------------------------

create index reference_mined_enzymes__reference on pubchem.reference_mined_enzymes(reference);
create index reference_mined_enzymes__enzyme on pubchem.reference_mined_enzymes(enzyme);
grant select on pubchem.reference_mined_enzymes to sparql;

--------------------------------------------------------------------------------

create index reference_doi_identifiers__reference on pubchem.reference_doi_identifiers(reference);
create index reference_doi_identifiers__doi on pubchem.reference_doi_identifiers(doi);
grant select on pubchem.reference_doi_identifiers to sparql;

--------------------------------------------------------------------------------

create index reference_pubmed_identifiers__reference on pubchem.reference_pubmed_identifiers(reference);
create index reference_pubmed_identifiers__pubmed on pubchem.reference_pubmed_identifiers(pubmed);
grant select on pubchem.reference_pubmed_identifiers to sparql;

--------------------------------------------------------------------------------

create index reference_sources__reference on pubchem.reference_sources(reference);
create index reference_sources__source_type on pubchem.reference_sources(source_type);
grant select on pubchem.reference_sources to sparql;
