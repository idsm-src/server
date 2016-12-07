log_enable(2);

--============================================================================--

create table gene_bases
(
    id           integer not null,
    title        varchar not null,
    description  varchar not null,
    primary key(id)
);


insert into gene_bases(id, title, description)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID%d', 0)[0] as id,
    title,
    description
from (
    sparql select ?S ?title ?description from pubchem:gene where
    {
        ?S dcterms:title ?title .
        ?S dcterms:description ?description .
    }
) as tbl;


create index gene_bases__title on gene_bases(title);
create index gene_bases__description on gene_bases(description);
grant select on gene_bases to "SPARQL";

--============================================================================--

create table gene_biosystems
(
    gene         integer not null,
    biosystem    integer not null,
    primary key(gene, biosystem)
);


insert into gene_biosystems(gene, biosystem)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID%d', 0)[0] as gene,
    sprintf_inverse(O, 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID%d', 0)[0] as biosystem
from (
    sparql select ?S ?O from pubchem:gene where
    {
        ?S obo:BFO_0000056 ?O
    }
) as tbl;


create index gene_biosystems__gene on gene_biosystems(gene);
create index gene_biosystems__biosystem on gene_biosystems(biosystem);
grant select on gene_biosystems to "SPARQL";

--============================================================================--

create table gene_alternatives
(
    __             smallint identity,
    gene           integer not null,
    alternative    nvarchar not null,
    primary key(__)
);


insert into gene_alternatives(gene, alternative)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID%d', 0)[0] as gene,
    alternative
from (
    sparql select ?S ?alternative from pubchem:gene where
    {
        ?S dcterms:alternative ?alternative
    }
) as tbl;


create index gene_alternatives__gene on gene_alternatives(gene);
create index gene_alternatives__alternative on gene_alternatives(alternative);
grant select on gene_alternatives to "SPARQL";

--============================================================================--

create table gene_references
(
    gene         integer not null,
    reference    integer not null,
    primary key(gene, reference)
);


insert into gene_references(gene, reference)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID%d', 0)[0] as gene,
    sprintf_inverse(D, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference
from (
    sparql select ?S ?D from pubchem:gene where
    {
        ?S cito:isDiscussedBy ?D
    }
) as tbl;


create index gene_references__gene on gene_references(gene);
create index gene_references__reference on gene_references(reference);
grant select on gene_references to "SPARQL";
