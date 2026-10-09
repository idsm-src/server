create index compounds__id__varchar on pubchem.compounds((id::varchar));
grant select on pubchem.compounds to sparql;

--------------------------------------------------------------------------------

create index compound_components__compound on pubchem.compound_components(compound);
create index compound_components__component on pubchem.compound_components(component);
grant select on pubchem.compound_components to sparql;

--------------------------------------------------------------------------------

create index compound_isotopologues__compound on pubchem.compound_isotopologues(compound);
create index compound_isotopologues__isotopologue on pubchem.compound_isotopologues(isotopologue);
grant select on pubchem.compound_isotopologues to sparql;

--------------------------------------------------------------------------------

create index compound_parents__compound on pubchem.compound_parents(compound);
create index compound_parents__parent on pubchem.compound_parents(parent);
grant select on pubchem.compound_parents to sparql;

--------------------------------------------------------------------------------

create index compound_stereoisomers__compound on pubchem.compound_stereoisomers(compound);
create index compound_stereoisomers__isomer on pubchem.compound_stereoisomers(isomer);
grant select on pubchem.compound_stereoisomers to sparql;

--------------------------------------------------------------------------------

create index compound_same_connectivities__compound on pubchem.compound_same_connectivities(compound);
create index compound_same_connectivities__isomer on pubchem.compound_same_connectivities(isomer);
grant select on pubchem.compound_same_connectivities to sparql;

--------------------------------------------------------------------------------

create index compound_roles__compound on pubchem.compound_roles(compound);
create index compound_roles__role_id on pubchem.compound_roles(role_id);
grant select on pubchem.compound_roles to sparql;

--------------------------------------------------------------------------------

create index compound_types__compound on pubchem.compound_types(compound);
create index compound_types__type_id on pubchem.compound_types(type_id);
grant select on pubchem.compound_types to sparql;

--------------------------------------------------------------------------------

create index compound_active_ingredients__compound on pubchem.compound_active_ingredients(compound);
create index compound_active_ingredients__ingredient_unit_ingredient_id on pubchem.compound_active_ingredients(ingredient_unit, ingredient_id);
grant select on pubchem.compound_active_ingredients to sparql;

--------------------------------------------------------------------------------

create index compound_labels__label on pubchem.compound_labels(label);
create index compound_labels__label__lower on pubchem.compound_labels(lower(label));
create index compound_labels__label__english on pubchem.compound_labels using gin (to_tsvector('english', label));
create index compound_labels__label__simple on pubchem.compound_labels using gin (to_tsvector('simple', label));
grant select on pubchem.compound_labels to sparql;

--------------------------------------------------------------------------------

create index compound_matches__compound on pubchem.compound_matches(compound);
create index compound_matches__match_unit_match_id on pubchem.compound_matches(match_unit, match_id);
grant select on pubchem.compound_matches to sparql;

--------------------------------------------------------------------------------

create index compound_wikidata_matches__compound on pubchem.compound_wikidata_matches(compound);
create index compound_wikidata_matches__match on pubchem.compound_wikidata_matches(match);
grant select on pubchem.compound_wikidata_matches to sparql;

--------------------------------------------------------------------------------

create index compound_molfiles__molfile on pubchem.compound_molfiles using hash (molfile);
grant select on pubchem.compound_molfiles to sparql;
