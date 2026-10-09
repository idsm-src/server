create index references__dcdate on pubchem.references(dcdate);
create index references__date on pubchem.references(date);
create index references__title on pubchem.references using hash (title);
create index references__citation on pubchem.references using hash (citation);
create index references__publication on pubchem.references(publication);
create index references__issue on pubchem.references(issue);
create index references__starting_page on pubchem.references(starting_page);
create index references__ending_page on pubchem.references(ending_page);
create index references__page_range on pubchem.references(page_range);
create index references__lang on pubchem.references(lang);
grant select on pubchem.references to sparql;

--------------------------------------------------------------------------------

create index reference_discussed_headings__reference on pubchem.reference_discussed_headings(reference);
create index reference_discussed_headings__heading on pubchem.reference_discussed_headings(heading);
grant select on pubchem.reference_discussed_headings to sparql;

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

create index reference_isbn_numbers__reference on pubchem.reference_isbn_numbers(reference);
create index reference_isbn_numbers__isbn on pubchem.reference_isbn_numbers(isbn);
grant select on pubchem.reference_isbn_numbers to sparql;

--------------------------------------------------------------------------------

create index reference_authors__reference on pubchem.reference_authors(reference);
create index reference_authors__author on pubchem.reference_authors(author);
grant select on pubchem.reference_authors to sparql;

--------------------------------------------------------------------------------

create index reference_grants__reference on pubchem.reference_grants(reference);
create index reference_grants__supporting_grant on pubchem.reference_grants(supporting_grant);
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

create index reference_mined_genesymbols__reference on pubchem.reference_mined_genesymbols(reference);
create index reference_mined_genesymbols__genesymbol on pubchem.reference_mined_genesymbols(genesymbol);
grant select on pubchem.reference_mined_genesymbols to sparql;

--------------------------------------------------------------------------------

create index reference_mined_enzymes__reference on pubchem.reference_mined_enzymes(reference);
create index reference_mined_enzymes__enzyme on pubchem.reference_mined_enzymes(enzyme);
grant select on pubchem.reference_mined_enzymes to sparql;

--------------------------------------------------------------------------------

create index reference_identifiers__reference on pubchem.reference_identifiers(reference);
create index reference_identifiers__identifier on pubchem.reference_identifiers(identifier);
grant select on pubchem.reference_identifiers to sparql;

--------------------------------------------------------------------------------

create index reference_source_types__reference on pubchem.reference_source_types(reference);
create index reference_source_types__source_type on pubchem.reference_source_types(source_type);
grant select on pubchem.reference_source_types to sparql;
