alter table chembl_tmp.drug_indication alter column drugind_id type integer;
alter table chembl_tmp.drug_indication rename column drugind_id to id;

alter table chembl_tmp.drug_indication add column molecule_id integer not null default -1;
update chembl_tmp.drug_indication set molecule_id = replace(chembl_tmp.molecule_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.molecule_dictionary where chembl_tmp.drug_indication.molregno = chembl_tmp.molecule_dictionary.molregno;
alter table chembl_tmp.drug_indication alter column molecule_id drop default;

alter table chembl_tmp.drug_indication add column efo_resource_unit smallint;
update chembl_tmp.drug_indication set efo_resource_unit = case when efo_id ~ '^GO:[0-9]{7}$' then '96'::smallint when efo_id ~ '^HP:[0-9]{7}$' then '66'::smallint when efo_id ~ '^DOID:[1-9][0-9]*$' then '97'::smallint when efo_id ~ '^EFO:[0-9]{7}$' then '92'::smallint when efo_id ~ '^Orphanet:[1-9][0-9]*$' then '93'::smallint when efo_id ~ '^MP:[0-9]{7}$' then '94'::smallint when efo_id ~ '^MONDO:[0-9]{7}$' then '98'::smallint end;

alter table chembl_tmp.drug_indication add column efo_resource_id integer;
update chembl_tmp.drug_indication set efo_resource_id = case when efo_id ~ '^((GO|HP|MP|EFO|MONDO):[0-9]{7}|(DOID|Orphanet):[1-9][0-9]*)$' then regexp_replace(efo_id, '.*:', '')::integer end;

alter table chembl_tmp.drug_indication add column chembl_id varchar not null generated always as ('CHEMBL_IND_' || id::varchar) stored;

alter table chembl_tmp.drug_indication alter column max_phase_for_ind type float4;

alter table chembl_tmp.drug_indication alter column max_phase_for_ind set not null;
