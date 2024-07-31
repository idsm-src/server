alter table chembl_tmp.activities alter column activity_id type integer;
alter table chembl_tmp.activities rename column activity_id to id;

alter table chembl_tmp.activities alter column assay_id type integer;
update chembl_tmp.activities set assay_id = replace(chembl_tmp.assays.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.assays where chembl_tmp.activities.assay_id = chembl_tmp.assays.assay_id;

alter table chembl_tmp.activities add column document_id integer not null default -1;
update chembl_tmp.activities set document_id = replace(chembl_tmp.docs.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.docs where chembl_tmp.activities.doc_id = chembl_tmp.docs.doc_id;
alter table chembl_tmp.activities alter column document_id drop default;

alter table chembl_tmp.activities add column molecule_id integer not null default -1;
update chembl_tmp.activities set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.activities.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.activities alter column molecule_id drop default;

alter table chembl_tmp.activities add column qudt_id integer;
update chembl_tmp.activities set qudt_id = (select resource_id from ontology.resources__reftable where iri = qudt_units);

alter table chembl_tmp.activities add column uo_unit_id integer;
update chembl_tmp.activities set uo_unit_id = replace(uo_units, 'UO_', '')::integer;

alter table chembl_tmp.activities add column bao_endpoint_id integer not null default -1;
update chembl_tmp.activities set bao_endpoint_id = substring(bao_endpoint from 5)::integer;
alter table chembl_tmp.activities alter column bao_endpoint_id drop default;

alter table chembl_tmp.activities drop constraint ck_potential_dup;
alter table chembl_tmp.activities alter column potential_duplicate type boolean using potential_duplicate::int::boolean;

alter table chembl_tmp.activities add column chembl_id varchar not null generated always as ('CHEMBL_ACT_' || id::varchar) stored;

alter table chembl_tmp.activities alter column value type float8;
alter table chembl_tmp.activities alter column standard_value type float8;
alter table chembl_tmp.activities alter column pchembl_value type float8;

alter table chembl_tmp.activities alter column standard_type set not null;
alter table chembl_tmp.activities alter column potential_duplicate set not null;
