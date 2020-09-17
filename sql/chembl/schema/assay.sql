alter table chembl.assays add column id integer not null default -1;
update chembl.assays set id = replace(chembl_id, 'CHEMBL', '')::integer;
alter table chembl.assays alter column id drop default;

alter table chembl.assays add column cell_line_id integer;
update chembl.assays set cell_line_id = replace(chembl.cell_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.cell_dictionary where chembl.assays.cell_id = chembl.cell_dictionary.cell_id;

alter table chembl.assays add column document_id integer not null default -1;
update chembl.assays set document_id = replace(chembl.docs.chembl_id, 'CHEMBL', '')::integer from chembl.docs where chembl.assays.doc_id = chembl.docs.doc_id;
alter table chembl.assays alter column document_id drop default;

alter table chembl.assays add column target_id integer not null default -1;
update chembl.assays set target_id = replace(chembl.target_dictionary.chembl_id, 'CHEMBL', '')::integer from chembl.target_dictionary where chembl.assays.tid = chembl.target_dictionary.tid;
alter table chembl.assays alter column target_id drop default;

alter table chembl.assays add column bao_format_id integer not null default -1;
update chembl.assays set bao_format_id = substring(bao_format from 5)::integer;
alter table chembl.assays alter column bao_format_id drop default;

alter table chembl.assays add column pubchem_assay_id integer;
update chembl.assays set pubchem_assay_id = case when src_id = 7 then regexp_replace(src_assay_id, '_[0-9]+', '')::integer end;

alter table chembl.assays alter column assay_tax_id type integer;
alter table chembl.assays alter column confidence_score type integer;

alter table chembl.assays alter column assay_type set not null;
alter table chembl.assays alter column relationship_type set not null;
alter table chembl.assays alter column confidence_score set not null;

--------------------------------------------------------------------------------

alter table chembl.assay_type alter column assay_desc set not null;

--------------------------------------------------------------------------------

alter table chembl.relationship_type alter column relationship_desc set not null;
