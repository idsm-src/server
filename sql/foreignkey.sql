-- compound
alter table compound_relations add foreign key (relation) references compound_relations__reftable(id);
alter table compound_roles add foreign key (roleid) references compound_roles__reftable(id);
alter table compound_types add foreign key (unit) references compound_type_units__reftable(id);

-- concept
alter table concept_bases add foreign key (scheme) references concept_bases(id);
alter table concept_bases add foreign key (broader) references concept_bases(id);

-- source
alter table source_subjects add foreign key (source) references source_bases(id);
alter table source_subjects add foreign key (subject) references source_subjects__reftable(id);
alter table source_alternatives add foreign key (source) references source_bases(id);

-- substance
alter table substance_bases add foreign key (source) references source_bases(id);
alter table substance_types add foreign key (source) references source_bases(id);
alter table substance_chembl_matches add foreign key (substance) references substance_bases(id);
alter table substance_schembl_matches add foreign key (substance) references substance_bases(id);
alter table substance_references add foreign key (source) references source_bases(id);
alter table substance_pdblinks add foreign key (substance) references substance_bases(id);

-- endpoint
alter table endpoint_bases add foreign key (outcome) references endpoint_outcomes__reftable(id);
alter table endpoint_bases add foreign key (substance) references substance_bases(id);
alter table endpoint_bases add foreign key (bioassay, measuregroup) references measuregroup_bases(bioassay, measuregroup);
alter table endpoint_measurements add foreign key (substance, bioassay, measuregroup) references endpoint_bases(substance, bioassay, measuregroup);
alter table endpoint_references add foreign key (substance, bioassay, measuregroup) references endpoint_bases(substance, bioassay, measuregroup);

-- measuregroup
alter table measuregroup_proteins add foreign key (bioassay, measuregroup) references measuregroup_bases(bioassay, measuregroup);
alter table measuregroup_genes add foreign key (bioassay, measuregroup) references measuregroup_bases(bioassay, measuregroup);

-- inchikey
alter table inchikey_subjects add foreign key (inchikey) references inchikey_bases(id);
alter table inchikey_compounds add foreign key (inchikey) references inchikey_bases(id);
