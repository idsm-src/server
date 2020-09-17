alter table chembl.target_dictionary add column id integer not null default -1;
update chembl.target_dictionary set id = replace(chembl_id, 'CHEMBL', '')::integer;
alter table chembl.target_dictionary alter column id drop default;

alter table chembl.target_dictionary add column cell_line_id integer;

alter table chembl.target_dictionary drop constraint ck_targdict_species;
alter table chembl.target_dictionary alter column species_group_flag type boolean using species_group_flag::int::boolean;

alter table chembl.target_dictionary alter column tax_id type integer;

alter table chembl.target_dictionary alter column target_type set not null;

--------------------------------------------------------------------------------

create type chembl.target_relationship_type as enum
(
    'EQUIVALENT TO',
    'OVERLAPS WITH',
    'SUBSET OF',
    'SUPERSET OF'
);

alter table chembl.target_relations add column target_id integer not null default -1;
update chembl.target_relations set target_id = replace(chembl.target_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.target_dictionary where chembl.target_relations.tid = chembl.target_dictionary.tid;
alter table chembl.target_relations alter column target_id drop default;

alter table chembl.target_relations add column related_target_id integer not null default -1;
update chembl.target_relations set related_target_id = replace(chembl.target_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.target_dictionary where chembl.target_relations.related_tid = chembl.target_dictionary.tid;
alter table chembl.target_relations alter column related_target_id drop default;

alter table chembl.target_relations alter column targrel_id type integer;
alter table chembl.target_relations alter column relationship type chembl.target_relationship_type using relationship::chembl.target_relationship_type;

--------------------------------------------------------------------------------

alter table chembl.target_components add column target_id integer not null default -1;
update chembl.target_components set target_id = replace(chembl.target_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.target_dictionary where chembl.target_components.tid = chembl.target_dictionary.tid;
alter table chembl.target_components alter column target_id drop default;

alter table chembl.target_components add column is_exact boolean not null default false;
update chembl.target_components set is_exact = true from chembl.target_dictionary where chembl.target_dictionary.tid = chembl.target_components.tid and chembl.target_dictionary.target_type = 'SINGLE PROTEIN';
alter table chembl.target_components alter column is_exact drop default;

alter table chembl.target_components add column is_related boolean not null default false;
update chembl.target_components set is_related = true from chembl.target_dictionary where chembl.target_dictionary.tid = chembl.target_components.tid and chembl.target_dictionary.target_type != 'NUCLEIC-ACID' and chembl.target_dictionary.target_type != 'SINGLE PROTEIN';
alter table chembl.target_components alter column is_related drop default;

alter table chembl.target_components alter column targcomp_id type integer;
alter table chembl.target_components alter column component_id type integer;
