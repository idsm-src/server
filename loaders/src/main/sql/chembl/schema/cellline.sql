alter table chembl_tmp.cell_dictionary add column id integer not null default -1;
update chembl_tmp.cell_dictionary set id = replace(chembl_id, 'CHEMBL', '')::integer;
alter table chembl_tmp.cell_dictionary alter column id drop default;

alter table chembl_tmp.cell_dictionary add column clo_resource_id integer;
update chembl_tmp.cell_dictionary set clo_resource_id = replace(clo_id, 'CLO_', '')::integer where clo_id like 'CLO\_%';

alter table chembl_tmp.cell_dictionary add column efo_resource_id integer;
update chembl_tmp.cell_dictionary set efo_resource_id = replace(efo_id, 'EFO_', '')::integer;

alter table chembl_tmp.cell_dictionary alter column cell_source_tax_id type integer;

alter table chembl_tmp.cell_dictionary alter column cell_description set not null;
alter table chembl_tmp.cell_dictionary alter column chembl_id set not null;
