alter table isdb.spectrum_bases add foreign key (id) references isdb.compound_bases(id) initially deferred;
