select sachem.add_index('pdb', 'pdb', 'compounds', 'id', 'molfile', 8, 8, 1000, 0);
grant select on table pdb.compounds to sparql;
