create index graphs__classes on void.graphs(classes);
create index graphs__properties on void.graphs(properties);
create index graphs__triples on void.graphs(triples);
create index graphs__subjects on void.graphs(subjects);
create index graphs__objects on void.graphs(objects);
create index graphs__iri_objects on void.graphs(iri_objects);
create index graphs__literal_objects on void.graphs(literal_objects);
grant select on void.graphs to sparql;

--------------------------------------------------------------------------------

create index class_partitions__graph on void.class_partitions(graph);
create index class_partitions__class on void.class_partitions(class_unit, class_id);
create index class_partitions__classes on void.class_partitions(classes);
create index class_partitions__properties on void.class_partitions(properties);
create index class_partitions__triples on void.class_partitions(triples);
create index class_partitions__subjects on void.class_partitions(subjects);
create index class_partitions__objects on void.class_partitions(objects);
create index class_partitions__iri_objects on void.class_partitions(iri_objects);
create index class_partitions__literal_objects on void.class_partitions(literal_objects);
grant select on void.class_partitions to sparql;

--------------------------------------------------------------------------------

create index property_partitions__graph on void.property_partitions(graph);
create index property_partitions__property on void.property_partitions(property_unit, property_id);
create index property_partitions__triples on void.property_partitions(triples);
create index property_partitions__subjects on void.property_partitions(subjects);
create index property_partitions__objects on void.property_partitions(objects);
create index property_partitions__iri_objects on void.property_partitions(iri_objects);
create index property_partitions__literal_objects on void.property_partitions(literal_objects);
grant select on void.property_partitions to sparql;

--------------------------------------------------------------------------------

create index class_property_partitions__graph on void.class_property_partitions(graph);
create index class_property_partitions__class on void.class_property_partitions(class_unit, class_id);
create index class_property_partitions__property on void.class_property_partitions(property_unit, property_id);
create index class_property_partitions__triples on void.class_property_partitions(triples);
create index class_property_partitions__subjects on void.class_property_partitions(subjects);
create index class_property_partitions__objects on void.class_property_partitions(objects);
create index class_property_partitions__iri_objects on void.class_property_partitions(iri_objects);
create index class_property_partitions__literal_objects on void.class_property_partitions(literal_objects);
grant select on void.class_property_partitions to sparql;

--------------------------------------------------------------------------------

create index linksets__property_graph on void.linksets(property_graph);
create index linksets__property on void.linksets(property_unit, property_id);
create index linksets__subject_graph on void.linksets(subject_graph);
create index linksets__subject on void.linksets(subject_unit, subject_id);
create index linksets__object_graph on void.linksets(object_graph);
create index linksets__object on void.linksets(object_unit, object_id);
create index linksets__triples on void.linksets(triples);
create index linksets__subjects on void.linksets(subjects);
create index linksets__objects on void.linksets(objects);
grant select on void.linksets to sparql;

--------------------------------------------------------------------------------

create index literal_linksets__property_graph on void.literal_linksets(property_graph);
create index literal_linksets__property on void.literal_linksets(property_unit, property_id);
create index literal_linksets__subject_graph on void.literal_linksets(subject_graph);
create index literal_linksets__subject on void.literal_linksets(subject_unit, subject_id);
create index literal_linksets__datatype on void.literal_linksets(datatype_unit, datatype_id);
create index literal_linksets__triples on void.literal_linksets(triples);
create index literal_linksets__subjects on void.literal_linksets(subjects);
create index literal_linksets__objects on void.literal_linksets(objects);
grant select on void.literal_linksets to sparql;
