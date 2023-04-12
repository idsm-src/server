create index grant_bases__number on pubchem.grant_bases(number);
create index grant_bases__organization on pubchem.grant_bases(organization);
grant select on pubchem.grant_bases to sparql;
