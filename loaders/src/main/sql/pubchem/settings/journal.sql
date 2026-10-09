create index journals__catalog_id on pubchem.journals(catalog_id);
create index journals__title on pubchem.journals(title);
create index journals__abbreviation on pubchem.journals(abbreviation);
create index journals__issn on pubchem.journals(issn);
create index journals__eissn on pubchem.journals(eissn);
grant select on pubchem.journals to sparql;
