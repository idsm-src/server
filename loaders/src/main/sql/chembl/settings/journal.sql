create index journal_bases__chembl_id on chembl.journal_bases(chembl_id);
create index journal_bases__label on chembl.journal_bases(label);
create index journal_bases__title on chembl.journal_bases(title);
create index journal_bases__short_title on chembl.journal_bases(short_title);
create index journal_bases__issn on chembl.journal_bases(issn);
create index journal_bases__eissn on chembl.journal_bases(eissn);
grant select on chembl.journal_bases to sparql;
