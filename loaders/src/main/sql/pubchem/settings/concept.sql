create index concepts__scheme on pubchem.concepts(scheme);
create index concepts__broader on pubchem.concepts(broader);
create index concepts__label on pubchem.concepts(label);
create index concepts__iri__atc on pubchem.concepts((iri like 'ATC%'));
grant select on pubchem.concepts to sparql;
