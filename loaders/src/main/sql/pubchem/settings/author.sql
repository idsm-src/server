grant select on pubchem.author_bases to sparql;

--------------------------------------------------------------------------------

create index author_given_names__author on pubchem.author_given_names(author);
create index author_given_names__name on pubchem.author_given_names(name);
grant select on pubchem.author_given_names to sparql;

--------------------------------------------------------------------------------

create index author_family_names__author on pubchem.author_family_names(author);
create index author_family_names__name on pubchem.author_family_names(name);
grant select on pubchem.author_family_names to sparql;

--------------------------------------------------------------------------------

create index author_formatted_names__author on pubchem.author_formatted_names(author);
create index author_formatted_names__name on pubchem.author_formatted_names(name);
grant select on pubchem.author_formatted_names to sparql;

--------------------------------------------------------------------------------

create index author_organizations__author on pubchem.author_organizations(author);
create index author_organizations__organization on pubchem.author_organizations using hash(organization);
grant select on pubchem.author_organizations to sparql;

--------------------------------------------------------------------------------

create index author_orcids__author on pubchem.author_orcids(author);
create index author_orcids__orcid on pubchem.author_orcids(orcid);
grant select on pubchem.author_orcids to sparql;
