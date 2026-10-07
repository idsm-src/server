alter table info.idsm_queries add foreign key (target) references info.sparql_endpoints(iri) initially deferred;
alter table info.idsm_federated_queries add foreign key (target) references info.sparql_endpoints(iri) initially deferred;
alter table info.idsm_federated_query_targets add foreign key (query) references info.idsm_federated_queries(id) initially deferred;
alter table info.idsm_federated_query_targets add foreign key (target) references info.sparql_endpoints(iri) initially deferred;
