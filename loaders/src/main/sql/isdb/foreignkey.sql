alter table isdb.spectra add foreign key (compound) references isdb.compounds(id) initially deferred;
