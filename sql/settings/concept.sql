create index concept_bases__scheme on concept_bases(scheme);
create index concept_bases__broader on concept_bases(broader);
grant select on concept_bases to sparql;
