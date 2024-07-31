create index journal_dictionary__label on chembl_tmp.journal_dictionary(label);
create index journal_dictionary__title on chembl_tmp.journal_dictionary(title);
create index journal_dictionary__short_title on chembl_tmp.journal_dictionary(short_title);
create index journal_dictionary__issn on chembl_tmp.journal_dictionary(issn);
create index journal_dictionary__eissn on chembl_tmp.journal_dictionary(eissn);
create index journal_dictionary__chembl_id on chembl_tmp.journal_dictionary(chembl_id);
grant select on chembl_tmp.journal_dictionary to sparql;
