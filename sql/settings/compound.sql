grant select on compound_sdfiles to "SPARQL";

--------------------------------------------------------------------------------

grant select on compound_relations__reftable to "SPARQL";

--------------------------------------------------------------------------------

create index compound_relations__compound_from on compound_relations(compound_from);
create index compound_relations__relation on compound_relations(relation);
create index compound_relations__compound_to on compound_relations(compound_to);
grant select on compound_relations to "SPARQL";

--------------------------------------------------------------------------------

grant select on compound_roles__reftable to "SPARQL";

--------------------------------------------------------------------------------

create index compound_roles__compound on compound_roles(compound);
create index compound_roles__roleid on compound_roles(roleid);
grant select on compound_roles to "SPARQL";

--------------------------------------------------------------------------------

create index compound_biosystems__compound on compound_biosystems(compound);
create index compound_biosystems__biosystem on compound_biosystems(biosystem);
grant select on compound_biosystems to "SPARQL";

--------------------------------------------------------------------------------

grant select on compound_type_units__reftable to "SPARQL";

--------------------------------------------------------------------------------

create index compound_types__compound on compound_types(compound);
create index compound_types__unit on compound_types(unit);
create index compound_types__unit_type on compound_types(unit, type);
grant select on compound_types to "SPARQL";

--------------------------------------------------------------------------------

create index compound_active_ingredients__compound on compound_active_ingredients(compound);
create index compound_active_ingredients__unit on compound_active_ingredients(unit);
create index compound_active_ingredients__unit_ingredient on compound_active_ingredients(unit, ingredient);
grant select on compound_active_ingredients to "SPARQL";

--------------------------------------------------------------------------------

insert into compound_bases(id)
select compound from compound_sdfiles;

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

grant select on compound_bases to "SPARQL";
