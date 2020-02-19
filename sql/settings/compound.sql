create index compound_components__compound on compound_components(compound);
create index compound_components__component on compound_components(component);
grant select on compound_components to sparql;

--------------------------------------------------------------------------------
create index compound_isotopologues__compound on compound_isotopologues(compound);
create index compound_isotopologues__isotopologue on compound_isotopologues(isotopologue);
grant select on compound_isotopologues to sparql;

--------------------------------------------------------------------------------
create index compound_parents__compound on compound_parents(compound);
create index compound_parents__parent on compound_parents(parent);
grant select on compound_parents to sparql;

--------------------------------------------------------------------------------
create index compound_stereoisomers__compound on compound_stereoisomers(compound);
create index compound_stereoisomers__isomer on compound_stereoisomers(isomer);
grant select on compound_stereoisomers to sparql;

--------------------------------------------------------------------------------

create index compound_same_connectivities__compound on compound_same_connectivities(compound);
create index compound_same_connectivities__isomer on compound_same_connectivities(isomer);
grant select on compound_same_connectivities to sparql;

--------------------------------------------------------------------------------

create index compound_roles__compound on compound_roles(compound);
create index compound_roles__role on compound_roles(role_id);
grant select on compound_roles to sparql;

--------------------------------------------------------------------------------

create index compound_biosystems__compound on compound_biosystems(compound);
create index compound_biosystems__biosystem on compound_biosystems(biosystem);
grant select on compound_biosystems to sparql;

--------------------------------------------------------------------------------

create index compound_types__compound on compound_types(compound);
create index compound_types__type on compound_types(type_id);
grant select on compound_types to sparql;

--------------------------------------------------------------------------------

create index compound_active_ingredients__compound on compound_active_ingredients(compound);
create index compound_active_ingredients__ingredient on compound_active_ingredients(ingredient_unit, ingredient_id);
grant select on compound_active_ingredients to sparql;

--------------------------------------------------------------------------------

insert into compound_bases(id)
select distinct id from compounds as t where not exists (select id from compound_bases where id = t.id);

insert into compound_bases(id)
select distinct compound from compound_components where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct component from compound_components where not exists (select id from compound_bases where id = component);

insert into compound_bases(id)
select distinct compound from compound_isotopologues where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct isotopologue from compound_isotopologues where not exists (select id from compound_bases where id = isotopologue);

insert into compound_bases(id)
select distinct compound from compound_parents where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct parent from compound_parents where not exists (select id from compound_bases where id = parent);

insert into compound_bases(id)
select distinct compound from compound_stereoisomers where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct isomer from compound_stereoisomers where not exists (select id from compound_bases where id = isomer);

insert into compound_bases(id)
select distinct compound from compound_same_connectivities where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct isomer from compound_same_connectivities where not exists (select id from compound_bases where id = isomer);

insert into compound_bases(id)
select distinct compound from compound_roles where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct compound from compound_biosystems where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct compound from compound_types where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct compound from compound_active_ingredients where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct compound from substance_bases where not exists (select id from compound_bases where id = compound);

insert into compound_bases(id)
select distinct compound from descriptor_compound_bases where not exists (select id from compound_bases where id = compound);

grant select on compound_bases to sparql;
