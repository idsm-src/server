alter table chembl_tmp.assays add column id integer not null default -1;
update chembl_tmp.assays set id = replace(chembl_id, 'CHEMBL', '')::integer;
alter table chembl_tmp.assays alter column id drop default;

alter table chembl_tmp.assays add column cell_line_id integer;
update chembl_tmp.assays set cell_line_id = replace(chembl_tmp.cell_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.cell_dictionary where chembl_tmp.assays.cell_id = chembl_tmp.cell_dictionary.cell_id;

alter table chembl_tmp.assays add column document_id integer not null default -1;
update chembl_tmp.assays set document_id = replace(chembl_tmp.docs.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.docs where chembl_tmp.assays.doc_id = chembl_tmp.docs.doc_id;
alter table chembl_tmp.assays alter column document_id drop default;

alter table chembl_tmp.assays add column target_id integer not null default -1;
update chembl_tmp.assays set target_id = replace(chembl_tmp.target_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl_tmp.target_dictionary where chembl_tmp.assays.tid = chembl_tmp.target_dictionary.tid;
alter table chembl_tmp.assays alter column target_id drop default;

alter table chembl_tmp.assays add column bao_format_id integer not null default -1;
update chembl_tmp.assays set bao_format_id = substring(bao_format from 5)::integer;
alter table chembl_tmp.assays alter column bao_format_id drop default;

alter table chembl_tmp.assays add column pubchem_assay_id integer;
update chembl_tmp.assays set pubchem_assay_id = case when src_id = 7 then regexp_replace(src_assay_id, '_[0-9]+', '')::integer end;

alter table chembl_tmp.assays alter column assay_tax_id type integer;
alter table chembl_tmp.assays alter column confidence_score type integer;

alter table chembl_tmp.assays alter column assay_type set not null;
alter table chembl_tmp.assays alter column relationship_type set not null;
alter table chembl_tmp.assays alter column confidence_score set not null;

--------------------------------------------------------------------------------

alter table chembl_tmp.assay_type alter column assay_desc set not null;

--------------------------------------------------------------------------------

alter table chembl_tmp.relationship_type alter column relationship_desc set not null;
