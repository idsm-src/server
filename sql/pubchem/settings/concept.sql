create index concept_bases__scheme on concept_bases(scheme);
create index concept_bases__broader on concept_bases(broader);
create index concept_bases__label on concept_bases(label);
grant select on concept_bases to sparql;
