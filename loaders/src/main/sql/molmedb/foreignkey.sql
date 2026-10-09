alter table molmedb.interactions add foreign key (substance) references molmedb.substances(id) initially deferred;
alter table molmedb.interactions add foreign key (membrane) references molmedb.membranes(id) initially deferred;
alter table molmedb.interactions add foreign key (method) references molmedb.methods(id) initially deferred;
alter table molmedb.interactions add foreign key (reference) references molmedb.references(id) initially deferred;
alter table molmedb.interactions add foreign key (model_reference) references molmedb.references(id) initially deferred;
alter table molmedb.fluorescent_interactions add foreign key (substance) references molmedb.substances(id) initially deferred;
alter table molmedb.fluorescent_interactions add foreign key (membrane) references molmedb.membranes(id) initially deferred;
alter table molmedb.fluorescent_interactions add foreign key (method) references molmedb.methods(id) initially deferred;
alter table molmedb.fluorescent_interactions add foreign key (reference) references molmedb.references(id) initially deferred;
alter table molmedb.fluorescent_interactions add foreign key (model_reference) references molmedb.references(id) initially deferred;

alter table molmedb.membrane_parts add foreign key (membrane) references molmedb.membranes(id) initially deferred;

alter table molmedb.reference_substances add foreign key (reference) references molmedb.references(id) initially deferred;
alter table molmedb.reference_substances add foreign key (substance) references molmedb.substances(id) initially deferred;
alter table molmedb.reference_membranes add foreign key (reference) references molmedb.references(id) initially deferred;
alter table molmedb.reference_membranes add foreign key (membrane) references molmedb.membranes(id) initially deferred;
alter table molmedb.reference_methods add foreign key (reference) references molmedb.references(id) initially deferred;
alter table molmedb.reference_methods add foreign key (method) references molmedb.methods(id) initially deferred;
alter table molmedb.reference_proteins add foreign key (reference) references molmedb.references(id) initially deferred;
alter table molmedb.reference_proteins add foreign key (protein) references molmedb.proteins(id) initially deferred;

alter table molmedb.substances add foreign key (parent) references molmedb.substances(id) initially deferred;
alter table molmedb.substance_identifiers add foreign key (substance) references molmedb.substances(id) initially deferred;
alter table molmedb.substance_links add foreign key (substance) references molmedb.substances(id) initially deferred;
alter table molmedb.substance_obsolete_identifiers add foreign key (substance) references molmedb.substances(id) initially deferred;

alter table molmedb.transporters add foreign key (substance) references molmedb.substances(id) initially deferred;
alter table molmedb.transporters add foreign key (protein) references molmedb.proteins(id) initially deferred;
alter table molmedb.transporters add foreign key (membrane) references molmedb.membranes(id) initially deferred;
alter table molmedb.transporters add foreign key (method) references molmedb.methods(id) initially deferred;
alter table molmedb.transporters add foreign key (reference) references molmedb.references(id) initially deferred;
alter table molmedb.transporters add foreign key (model_reference) references molmedb.references(id) initially deferred;
