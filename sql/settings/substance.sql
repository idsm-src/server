insert into substance_bases(id)
select distinct substance from endpoint_outcomes as t where not exists (select id from substance_bases where id = substance);

insert into substance_bases(id)
select distinct substance from substance_types as t where not exists (select id from substance_bases where id = substance);

insert into substance_bases(id)
select distinct substance from substance_matches as t where not exists (select id from substance_bases where id = substance);

insert into substance_bases(id)
select distinct substance from substance_references as t where not exists (select id from substance_bases where id = substance);

insert into substance_bases(id)
select distinct substance from substance_pdblinks as t where not exists (select id from substance_bases where id = substance);

insert into substance_bases(id)
select distinct substance from substance_synonyms as t where not exists (select id from substance_bases where id = substance);

insert into substance_bases(id)
select distinct substance from descriptor_substance_bases as t where not exists (select id from substance_bases where id = substance);

create index substance_bases__source on substance_bases(source);
create index substance_bases__available on substance_bases(available);
create index substance_bases__modified on substance_bases(modified);
create index substance_bases__compound on substance_bases(compound);
grant select on substance_bases to "SPARQL";

--------------------------------------------------------------------------------

create index substance_types__substance on substance_types(substance);
create index substance_types__chebi on substance_types(chebi);
grant select on substance_types to "SPARQL";

--------------------------------------------------------------------------------

create index substance_matches__substance on substance_matches(substance);
create index substance_matches__match on substance_matches(match);
grant select on substance_matches to "SPARQL";

--------------------------------------------------------------------------------

create index substance_references__substance on substance_references(substance);
create index substance_references__reference on substance_references(reference);
grant select on substance_references to "SPARQL";

--------------------------------------------------------------------------------

create index substance_pdblinks__substance on substance_pdblinks(substance);
create index substance_pdblinks__pdblink on substance_pdblinks(pdblink);
grant select on substance_pdblinks to "SPARQL";

--------------------------------------------------------------------------------

create index substance_synonyms__substance on substance_synonyms(substance);
create index substance_synonyms__synonym on substance_synonyms(synonym);
grant select on substance_synonyms to "SPARQL";
