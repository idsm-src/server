create index journal_bases__catalogid on pubchem.journal_bases(catalogid);
create index journal_bases__title on pubchem.journal_bases(title);
create index journal_bases__abbreviation on pubchem.journal_bases(abbreviation);
create index journal_bases__issn on pubchem.journal_bases(issn);
create index journal_bases__eissn on pubchem.journal_bases(eissn);
grant select on pubchem.journal_bases to sparql;
