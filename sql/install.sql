-- run as postgres

create role rdf with login;
\password rdf
create database rdf with owner rdf;


create role "SPARQL" with login;
\password "SPARQL"
