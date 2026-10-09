grant select on pubchem.organizations to sparql;

--------------------------------------------------------------------------------

create index organization_country_names__name on pubchem.organization_country_names(name);
grant select on pubchem.organization_country_names to sparql;

--------------------------------------------------------------------------------

create index organization_formatted_names__organization on pubchem.organization_formatted_names(organization);
create index organization_formatted_names__name on pubchem.organization_formatted_names(name);
grant select on pubchem.organization_formatted_names to sparql;

--------------------------------------------------------------------------------

create index organization_crossref_matches__organization on pubchem.organization_crossref_matches(organization);
create index organization_crossref_matches__match on pubchem.organization_crossref_matches(match);
grant select on pubchem.organization_crossref_matches to sparql;
