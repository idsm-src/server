\lo_import isdb_neg.mgf

insert into isdb.compound_bases(accession, exact_mass, formula, smiles, inchi)
  select * from pgms.mgf_to_recordset(:LASTOID) as (name varchar, exactmass real, molecular_formula varchar, smiles varchar, inchi varchar);

insert into isdb.spectrum_bases(id, ionmode, pepmass, spectrum)
  select b.id, 'N', d.pepmass, d.spectrum from (select * from pgms.mgf_to_recordset(:LASTOID) as (name varchar, pepmass real, spectrum pgms.spectrum)) d, isdb.compound_bases b where b.accession = d.name;


\lo_import isdb_pos.mgf

insert into isdb.compound_bases(accession, exact_mass, formula, smiles, inchi)
  select * from pgms.mgf_to_recordset(:LASTOID) as (name varchar, exactmass real, molecular_formula varchar, smiles varchar, inchi varchar)
  on conflict do nothing;

insert into isdb.spectrum_bases(id, ionmode, pepmass, spectrum)
  select b.id, 'P', d.pepmass, d.spectrum from (select * from  pgms.mgf_to_recordset(:LASTOID) as (name varchar, pepmass real, spectrum pgms.spectrum)) d, isdb.compound_bases b where b.accession = d.name;
