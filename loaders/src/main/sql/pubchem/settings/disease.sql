create index disease_bases__label on pubchem.disease_bases(label);
grant select on pubchem.disease_bases to sparql;

--------------------------------------------------------------------------------

create index disease_alternatives__disease on pubchem.disease_alternatives(disease);
create index disease_alternatives__alternative on pubchem.disease_alternatives(alternative);
grant select on pubchem.disease_alternatives to sparql;

--------------------------------------------------------------------------------

create index disease_matches__disease on pubchem.disease_matches(disease);
create index disease_matches__match on pubchem.disease_matches(match_unit, match_id);
grant select on pubchem.disease_matches to sparql;

--------------------------------------------------------------------------------

create index disease_mesh_matches__disease on pubchem.disease_mesh_matches(disease);
create index disease_mesh_matches__match on pubchem.disease_mesh_matches(match);
grant select on pubchem.disease_mesh_matches to sparql;

--------------------------------------------------------------------------------

create index disease_related_matches__disease on pubchem.disease_related_matches(disease);
create index disease_related_matches__match on pubchem.disease_related_matches(match_unit, match_id);
grant select on pubchem.disease_related_matches to sparql;
