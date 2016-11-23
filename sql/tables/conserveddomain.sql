log_enable(2);

--============================================================================--

create table conserveddomain_bases
(
    id          integer not null,
    title       nvarchar,
    abstract    long nvarchar,
    primary key(id)
);


insert into conserveddomain_bases(id, title, abstract)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID%d', 0)[0] as id,
    title,
    abstract
from (
    sparql select ?S ?title ?abstract from pubchem:conserveddomain where
    {
        ?S rdf:type obo:SO_0000417 .
        optional { ?S dcterms:title ?title }
        optional { ?S dcterms:abstract ?abstract }
    }
) as tbl;


grant select on conserveddomain_bases to "SPARQL";

--============================================================================--

create table conserveddomain_references
(
    domain       integer not null,
    reference    integer not null,
    primary key(domain, reference)
);


insert into conserveddomain_references(domain, reference)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID%d', 0)[0] as domain,
    sprintf_inverse(R, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference
from (
    sparql select ?S ?R from pubchem:conserveddomain where
    {
        ?S cito:isDiscussedBy ?R .
    }
) as tbl;


create index conserveddomain_references__domain on conserveddomain_references(domain);
create index conserveddomain_references__reference on conserveddomain_references(reference);
grant select on conserveddomain_references to "SPARQL";
