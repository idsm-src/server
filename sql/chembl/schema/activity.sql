alter table chembl.activities alter column activity_id type integer;
alter table chembl.activities rename column activity_id to id;

alter table chembl.activities alter column assay_id type integer;
update chembl.activities set assay_id = replace(chembl.assays.chembl_id, 'CHEMBL', '')::integer from chembl.assays where chembl.activities.assay_id = chembl.assays.assay_id;

alter table chembl.activities add column document_id integer not null default -1;
update chembl.activities set document_id = replace(chembl.docs.chembl_id, 'CHEMBL', '')::integer from chembl.docs where chembl.activities.doc_id = chembl.docs.doc_id;
alter table chembl.activities alter column document_id drop default;

alter table chembl.activities add column molecule_id integer not null default -1;
update chembl.activities set molecule_id = replace(chembl.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.molecule_dictionary where chembl.activities.molregno = chembl.molecule_dictionary.molregno;
alter table chembl.activities alter column molecule_id drop default;

alter table chembl.activities add column qudt_id integer;
update chembl.activities set qudt_id = (select resource_id from ontology.resources__reftable where iri = qudt_units);

alter table chembl.activities add column uo_unit_id integer;
update chembl.activities set uo_unit_id = replace(uo_units, 'UO_', '')::integer;

alter table chembl.activities add column bao_endpoint_id integer not null default -1;
update chembl.activities set bao_endpoint_id = substring(bao_endpoint from 5)::integer;
alter table chembl.activities alter column bao_endpoint_id drop default;

alter table chembl.activities drop constraint ck_potential_dup;
alter table chembl.activities alter column potential_duplicate type boolean using potential_duplicate::int::boolean;

alter table chembl.activities add column chembl_id varchar not null generated always as ('CHEMBL_ACT_' || id::varchar) stored;

alter table chembl.activities alter column value type float8;
alter table chembl.activities alter column standard_value type float8;
alter table chembl.activities alter column pchembl_value type float8;

alter table chembl.activities alter column standard_type set not null;
alter table chembl.activities alter column potential_duplicate set not null;
