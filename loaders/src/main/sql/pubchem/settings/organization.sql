grant select on pubchem.organization_bases to sparql;

--------------------------------------------------------------------------------

create index organization_country_names__name on pubchem.organization_country_names(name);
grant select on pubchem.organization_country_names to sparql;

--------------------------------------------------------------------------------

create index organization_formatted_names__organization on pubchem.organization_formatted_names(organization);
create index organization_formatted_names__name on pubchem.organization_formatted_names(name);
grant select on pubchem.organization_formatted_names to sparql;

--------------------------------------------------------------------------------

create index organization_crossref_matches__organization on pubchem.organization_crossref_matches(organization);
create index organization_crossref_matches__name on pubchem.organization_crossref_matches(crossref);
grant select on pubchem.organization_crossref_matches to sparql;
