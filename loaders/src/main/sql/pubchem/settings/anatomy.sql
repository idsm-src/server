create index anatomy_bases__label on pubchem.anatomy_bases(label);
grant select on pubchem.anatomy_bases to sparql;

--------------------------------------------------------------------------------

create index anatomy_alternatives__anatomy on pubchem.anatomy_alternatives(anatomy);
create index anatomy_alternatives__alternative on pubchem.anatomy_alternatives(alternative);
grant select on pubchem.anatomy_alternatives to sparql;

--------------------------------------------------------------------------------

create index anatomy_matches__anatomy on pubchem.anatomy_matches(anatomy);
create index anatomy_matches__match on pubchem.anatomy_matches(match_unit, match_id);
grant select on pubchem.anatomy_matches to sparql;

--------------------------------------------------------------------------------

create index anatomy_mesh_matches__anatomy on pubchem.anatomy_mesh_matches(anatomy);
create index anatomy_mesh_matches__match on pubchem.anatomy_mesh_matches(match);
grant select on pubchem.anatomy_mesh_matches to sparql;
