select sachem.add_index('isdb', 'isdb', 'compound_bases', 'id', 'smiles', 16, 16, 100000, 0);
select sachem.sync_data('isdb');
