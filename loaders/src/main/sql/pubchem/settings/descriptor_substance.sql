create index substance_versions__version on pubchem.substance_versions(version);
grant select on pubchem.substance_versions to sparql;
