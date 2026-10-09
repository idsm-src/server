alter table idsm.queries add foreign key (target) references idsm.sparql_endpoints(iri) initially deferred;
alter table idsm.federated_queries add foreign key (target) references idsm.sparql_endpoints(iri) initially deferred;
alter table idsm.federated_query_targets add foreign key (query) references idsm.federated_queries(id) initially deferred;
alter table idsm.federated_query_targets add foreign key (target) references idsm.sparql_endpoints(iri) initially deferred;
