alter table molmedb.interaction_bases add foreign key (substance_id) references molmedb.substance_bases(id) initially deferred;
alter table molmedb.interaction_bases add foreign key (membrane_id) references molmedb.membrane_bases(id) initially deferred;
alter table molmedb.interaction_bases add foreign key (method_id) references molmedb.method_bases(id) initially deferred;
alter table molmedb.interaction_bases add foreign key (publication_id) references molmedb.reference_bases(id) initially deferred;
alter table molmedb.interaction_bases add foreign key (model_publication_id) references molmedb.reference_bases(id) initially deferred;
alter table molmedb.fluorescent_interaction_bases add foreign key (substance_id) references molmedb.substance_bases(id) initially deferred;
alter table molmedb.fluorescent_interaction_bases add foreign key (membrane_id) references molmedb.membrane_bases(id) initially deferred;
alter table molmedb.fluorescent_interaction_bases add foreign key (method_id) references molmedb.method_bases(id) initially deferred;
alter table molmedb.fluorescent_interaction_bases add foreign key (publication_id) references molmedb.reference_bases(id) initially deferred;
alter table molmedb.fluorescent_interaction_bases add foreign key (model_publication_id) references molmedb.reference_bases(id) initially deferred;

alter table molmedb.membrane_parts add foreign key (membrane_id) references molmedb.membrane_bases(id) initially deferred;

alter table molmedb.reference_substances add foreign key (reference_id) references molmedb.reference_bases(id) initially deferred;
alter table molmedb.reference_substances add foreign key (substance_id) references molmedb.substance_bases(id) initially deferred;
alter table molmedb.reference_membranes add foreign key (reference_id) references molmedb.reference_bases(id) initially deferred;
alter table molmedb.reference_membranes add foreign key (membrane_id) references molmedb.membrane_bases(id) initially deferred;
alter table molmedb.reference_methods add foreign key (reference_id) references molmedb.reference_bases(id) initially deferred;
alter table molmedb.reference_methods add foreign key (method_id) references molmedb.method_bases(id) initially deferred;
alter table molmedb.reference_proteins add foreign key (reference_id) references molmedb.reference_bases(id) initially deferred;
alter table molmedb.reference_proteins add foreign key (protein_id) references molmedb.protein_bases(id) initially deferred;

alter table molmedb.substance_bases add foreign key (parent_id) references molmedb.substance_bases(id) initially deferred;
alter table molmedb.substance_identifiers add foreign key (substance_id) references molmedb.substance_bases(id) initially deferred;
alter table molmedb.substance_links add foreign key (substance_id) references molmedb.substance_bases(id) initially deferred;
alter table molmedb.obsoleted_substances add foreign key (substance_id) references molmedb.substance_bases(id) initially deferred;

alter table molmedb.transporter_bases add foreign key (substance_id) references molmedb.substance_bases(id) initially deferred;
alter table molmedb.transporter_bases add foreign key (protein_id) references molmedb.protein_bases(id) initially deferred;
alter table molmedb.transporter_bases add foreign key (membrane_id) references molmedb.membrane_bases(id) initially deferred;
alter table molmedb.transporter_bases add foreign key (method_id) references molmedb.method_bases(id) initially deferred;
alter table molmedb.transporter_bases add foreign key (publication_id) references molmedb.reference_bases(id) initially deferred;
alter table molmedb.transporter_bases add foreign key (model_publication_id) references molmedb.reference_bases(id) initially deferred;
