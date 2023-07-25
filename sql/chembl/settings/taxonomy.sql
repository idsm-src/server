create view chembl_tmp.taxonomies as
  select tax_id, organism from chembl_tmp.component_sequences union select tax_id, organism from chembl_tmp.bio_component_sequences;

grant select on chembl_tmp.taxonomies to sparql;
