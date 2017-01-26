create view compound_sdfiles as select compound, gz_decompress(cast(sdf_gz as varchar)) as sdf from compound_sdfiles_gz;
grant select on compound_sdfiles TO "SPARQL";

--------------------------------------------------------------------------------

grant select on compound_relations__reftable to "SPARQL";

--------------------------------------------------------------------------------

create index compound_relations__compound_from on compound_relations(compound_from);
create bitmap index compound_relations__relation on compound_relations(relation);
create index compound_relations__compound_to on compound_relations(compound_to);
grant select on compound_relations to "SPARQL";

--------------------------------------------------------------------------------

grant select on compound_roles__reftable to "SPARQL";

--------------------------------------------------------------------------------

create index compound_roles__compound on compound_roles(compound);
create bitmap index compound_roles__roleid on compound_roles(roleid);
grant select on compound_roles to "SPARQL";

--------------------------------------------------------------------------------

create index compound_biosystems__compound on compound_biosystems(compound);
create index compound_biosystems__biosystem on compound_biosystems(biosystem);
grant select on compound_biosystems to "SPARQL";

--------------------------------------------------------------------------------

grant select on compound_type_units__reftable to "SPARQL";

--------------------------------------------------------------------------------

create index compound_types__compound on compound_types(compound);
create bitmap index compound_types__unit on compound_types(unit);
create index compound_types__unit_type on compound_types(unit, type);
grant select on compound_types to "SPARQL";

--------------------------------------------------------------------------------

create index compound_active_ingredients__compound on compound_active_ingredients(compound);
create bitmap index compound_active_ingredients__unit on compound_active_ingredients(unit);
create index compound_active_ingredients__unit_ingredient on compound_active_ingredients(unit, ingredient);
grant select on compound_active_ingredients to "SPARQL";

--------------------------------------------------------------------------------

insert into compound_bases(id)
select compound from compound_sdfiles_gz;

insert replacing compound_bases(id)
select compound_from from compound_relations;

insert replacing compound_bases(id)
select compound_to from compound_relations;

insert replacing compound_bases(id)
select compound from compound_roles;

insert replacing compound_bases(id)
select compound from compound_biosystems;

insert replacing compound_bases(id)
select compound from compound_types;

insert replacing compound_bases(id)
select compound from compound_active_ingredients;

grant select on compound_bases to "SPARQL";
