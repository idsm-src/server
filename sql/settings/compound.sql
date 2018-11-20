create index compound_relations__compound_from on compound_relations(compound_from);
create index compound_relations__relation on compound_relations(relation_unit, relation_id);
create index compound_relations__compound_to on compound_relations(compound_to);
grant select on compound_relations to "SPARQL";

--------------------------------------------------------------------------------

create index compound_roles__compound on compound_roles(compound);
create index compound_roles__role on compound_roles(role_id);
grant select on compound_roles to "SPARQL";

--------------------------------------------------------------------------------

create index compound_biosystems__compound on compound_biosystems(compound);
create index compound_biosystems__biosystem on compound_biosystems(biosystem);
grant select on compound_biosystems to "SPARQL";

--------------------------------------------------------------------------------

create index compound_types__compound on compound_types(compound);
create index compound_types__type on compound_types(type_id);
grant select on compound_types to "SPARQL";

--------------------------------------------------------------------------------

create index compound_active_ingredients__compound on compound_active_ingredients(compound);
create index compound_active_ingredients__ingredient on compound_active_ingredients(ingredient_unit, ingredient_id);
grant select on compound_active_ingredients to "SPARQL";

--------------------------------------------------------------------------------

insert into compound_bases(id)
select distinct id from compounds as t where not exists (select id from compound_bases where id = t.id);

insert into compound_bases(id)
select distinct compound_from from compound_relations as t where not exists (select id from compound_bases where id = t.compound_from);

insert into compound_bases(id)
select distinct compound_to from compound_relations where not exists (select id from compound_bases where id = compound_to);

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

grant select on compound_bases to "SPARQL";
