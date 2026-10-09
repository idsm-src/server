create index journals__chembl_id on chembl.journals(chembl_id);
create index journals__label on chembl.journals(label);
create index journals__title on chembl.journals(title);
create index journals__short_title on chembl.journals(short_title);
create index journals__issn on chembl.journals(issn);
create index journals__eissn on chembl.journals(eissn);
grant select on chembl.journals to sparql;
