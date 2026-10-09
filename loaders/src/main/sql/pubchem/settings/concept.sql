create index concept_bases__scheme on pubchem.concept_bases(scheme);
create index concept_bases__broader on pubchem.concept_bases(broader);
create index concept_bases__label on pubchem.concept_bases(label);
create index concept_bases__iri__atc on pubchem.concept_bases((iri like 'ATC%'));
grant select on pubchem.concept_bases to sparql;
