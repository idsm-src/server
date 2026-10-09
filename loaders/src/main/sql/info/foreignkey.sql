alter table info.queries add foreign key (target) references info.sparql_endpoints(iri) initially deferred;
alter table info.federated_queries add foreign key (target) references info.sparql_endpoints(iri) initially deferred;
alter table info.federated_query_targets add foreign key (query) references info.federated_queries(id) initially deferred;
alter table info.federated_query_targets add foreign key (target) references info.sparql_endpoints(iri) initially deferred;
