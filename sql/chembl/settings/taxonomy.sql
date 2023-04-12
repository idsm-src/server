create view chembl.taxonomies as
  select tax_id, organism from chembl.component_sequences union select tax_id, organism from chembl.bio_component_sequences;

grant select on chembl.taxonomies to sparql;
