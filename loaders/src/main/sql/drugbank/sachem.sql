select sachem.add_index('drugbank', 'drugbank', 'compounds', 'id', 'molfile', 4, 4, 250, 0);
select sachem.sync_data('drugbank', false, true);
