sparql
create iri class iri-proc:call "http://bioinfo.iocb.cz/rdf/0.9/procedure-calls/ids/call#%d"
    (in id integer not null) option (bijection) .;


sparql
create iri class iri-proc:result "http://bioinfo.iocb.cz/rdf/0.9/procedure-calls/ids/result#%d"
    (in id integer not null) option (bijection).;
