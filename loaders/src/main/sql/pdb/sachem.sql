select sachem.add_index('pdb', 'pdb', 'compounds', 'id', 'molfile', 8, 8, 1000, 0);
select sachem.sync_data('pdb', false, true);
