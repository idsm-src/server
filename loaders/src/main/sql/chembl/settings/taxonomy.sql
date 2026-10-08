create index taxonomy_labels__taxonomy on chembl.taxonomy_labels(taxonomy);
create index taxonomy_labels__type on chembl.taxonomy_labels(type);
create index taxonomy_labels__label on chembl.taxonomy_labels(label);
grant select on chembl.taxonomy_labels to sparql;
