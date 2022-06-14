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
create index compound_roles__role on pubchem.compound_roles(role_id);
grant select on pubchem.compound_roles to sparql;

--------------------------------------------------------------------------------

create index compound_types__compound on pubchem.compound_types(compound);
create index compound_types__type on pubchem.compound_types(type_id);
grant select on pubchem.compound_types to sparql;

--------------------------------------------------------------------------------

create index compound_active_ingredients__compound on pubchem.compound_active_ingredients(compound);
create index compound_active_ingredients__ingredient on pubchem.compound_active_ingredients(ingredient_unit, ingredient_id);
grant select on pubchem.compound_active_ingredients to sparql;

--------------------------------------------------------------------------------

create index compound_titles__title on pubchem.compound_titles(title);
create index compound_titles__title__gin on pubchem.compound_titles using gin (to_tsvector('english', title));
grant select on pubchem.compound_titles to sparql;

--------------------------------------------------------------------------------

insert into pubchem.compound_bases(id, keep)
select distinct id, false from molecules.pubchem where not exists (select id from pubchem.compound_bases where id = molecules.pubchem.id);

grant select on pubchem.compound_bases to sparql;


create function pubchem.compound_bases__sync() returns trigger language plpgsql as
$$
  begin
    if TG_OP = 'INSERT' then
      insert into pubchem.compound_bases(id, keep) values (NEW.id, false) on conflict do nothing;
    elsif TG_OP = 'DELETE' then
      delete from pubchem.compound_bases where id = OLD.id and not keep;
    end if;
    return NEW;
  end;
$$;

create trigger compound_bases__sync_insert before insert on molecules.pubchem for each row execute procedure pubchem.compound_bases__sync();
create trigger compound_bases__sync_delete after  delete on molecules.pubchem for each row execute procedure pubchem.compound_bases__sync();
