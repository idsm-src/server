select sachem.add_index('pdb', 'pdb', 'compound_bases', 'id', 'molfile', 8, 8, 1000, 0);
grant select on table pdb.compound_bases to sparql;
