log_enable(2);

--============================================================================--

create table protein_bases
(
    id          integer identity,
    name        varchar unique not null,
    organism    integer,
    title       nvarchar,
    primary key(id)
);


insert into protein_bases(name, organism, title)
select
    sprintf_inverse(tbl.S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/%s', 0)[0] as name,
    case
        when tbl.O is not null then sprintf_inverse(tbl.O, 'http://identifiers.org/taxonomy/%d', 0)[0]
        else null
    end as organism,
    title
from (
    sparql select ?S ?O ?title from pubchem:protein where
    {
        {select distinct ?S from pubchem:protein where { ?S [] [] }}
        optional { ?S dcterms:title ?title }
        optional { ?S bp:organism ?O }
    }
) as tbl;


insert into protein_bases(name)
select distinct
    P as name
from (
    sparql select (str(replace(str(?P), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?P) from pubchem:protein where
    {
        ?S vocab:hasSimilarProtein ?P
    }
) as tbl
left  join protein_bases as rt on rt.name = tbl.P where rt.id is null;


-- workaround: add missing proteins
insert into protein_bases(name)
select distinct
    P as name
from (
    sparql select (str(replace(str(?P), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?P) from pubchem:measuregroup where
    {
        ?S obo:BFO_0000057 ?P .
        filter(strstarts(str(?P), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"))
        filter(?S != measuregroup:AID493040)
    }
) as tbl
left join protein_bases as rt on rt.name = tbl.P where rt.id is null;


create index protein_bases__organism on protein_bases(organism);
create index protein_bases__title on protein_bases(title);
grant select on protein_bases to "SPARQL";

--============================================================================--

create table protein_references
(
    protein      integer not null,
    reference    integer not null,
    primary key(protein, reference)
);


insert into protein_references(protein, reference)
select
    rt.id as protein,
    sprintf_inverse(R, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference
from (
    sparql select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?R from pubchem:protein where
    {
        ?S cito:isDiscussedBy ?R .
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_references__protein on protein_references(protein);
create index protein_references__reference on protein_references(reference);
grant select on protein_references to "SPARQL";

--============================================================================--

create table protein_pdblinks
(
    protein    integer not null,
    pdblink    char(4) not null,
    primary key(protein, pdblink)
);


insert into protein_pdblinks(protein, pdblink)
select
    rt.id as protein,
    sprintf_inverse(L, 'http://rdf.wwpdb.org/pdb/%U', 0)[0] as pdblink
from (
    sparql
    select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?L from pubchem:protein where
    {
        ?S pdbo:link_to_pdb ?L
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_pdblinks__protein on protein_pdblinks(protein);
create index protein_pdblinks__pdblink on protein_pdblinks(pdblink);
grant select on protein_pdblinks to "SPARQL";

--============================================================================--

create table protein_similarproteins
(
    protein    integer not null,
    similar    integer not null,
    primary key(protein, similar)
);


insert into protein_similarproteins(protein, similar)
select
    rt1.id as protein,
    rt2.id as similar
from (
    sparql
    select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S)
           (str(replace(str(?O), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?O) from pubchem:protein where
    {
        ?S vocab:hasSimilarProtein ?O
    }
) as tbl
inner join protein_bases as rt1 on rt1.name = tbl.S
inner join protein_bases as rt2 on rt2.name = tbl.O;


create index protein_similarproteins__protein on protein_similarproteins(protein);
create index protein_similarproteins__similar on protein_similarproteins(similar);
grant select on protein_similarproteins to "SPARQL";

--============================================================================--

create table protein_genes
(
    protein    integer not null,
    gene       integer not null,
    primary key(protein)
);


insert into protein_genes(protein, gene)
select
    rt.id as protein,
    sprintf_inverse(G, 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID%d', 0)[0] as gene
from (
    sparql
    select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?G from pubchem:protein where
    {
        ?S vocab:encodedBy ?G
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_genes__gene on protein_genes(gene);
grant select on protein_genes to "SPARQL";

--============================================================================--

create table protein_closematches
(
    __         integer identity,
    protein    integer not null,
    match      varchar not null,
    primary key(__)
);


insert into protein_closematches(protein, match)
select
    rt.id as protein,
    sprintf_inverse(M, 'http://purl.uniprot.org/uniprot/%U', 0)[0] as match
from (
    sparql
    select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?M from pubchem:protein where
    {
        ?S skos:closeMatch ?M
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_closematches__protein on protein_closematches(protein);
create index protein_closematches__match on protein_closematches(match);
grant select on protein_closematches to "SPARQL";

--============================================================================--

create table protein_conserveddomains
(
    protein    integer not null,
    domain     integer not null,
    primary key(protein)
);


insert into protein_conserveddomains(protein, domain)
select
    rt.id as protein,
    sprintf_inverse(D, 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID%d', 0)[0] as domain
from (
    sparql select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?D from pubchem:protein where
    {
        ?S obo:BFO_0000110 ?D .
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_conserveddomains__domain on protein_conserveddomains(domain);
grant select on protein_conserveddomains to "SPARQL";

--============================================================================--

create table protein_continuantparts
(
    protein    integer not null,
    part       integer not null,
    primary key(protein, part)
);


insert into protein_continuantparts(protein, part)
select
    rt1.id as protein,
    rt2.id as part
from (
    sparql select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S)
                  (str(replace(str(?P), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?P) from pubchem:protein where
    {
        ?S obo:BFO_0000178 ?P .
    }
) as tbl
inner join protein_bases as rt1 on rt1.name = tbl.S
left  join protein_bases as rt2 on rt2.name = tbl.P;


create index protein_continuantparts__protein on protein_continuantparts(protein);
create index protein_continuantparts__part on protein_continuantparts(part);
grant select on protein_continuantparts to "SPARQL";

--============================================================================--

create table protein_participates_goes
(
    protein          integer not null,
    participation    integer not null,
    primary key(protein, participation)
);


insert into protein_participates_goes(protein, participation)
select
    rt.id as protein,
    sprintf_inverse(O, 'http://purl.obolibrary.org/obo/GO_%d', 0)[0] as participation
from (
    sparql select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?O from pubchem:protein where
    {
        ?S obo:BFO_0000056 ?O .
        filter(strstarts(str(?O), "http://purl.obolibrary.org/obo/GO_"))
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_participates_goes__protein on protein_participates_goes(protein);
create index protein_participates_goes__participation on protein_participates_goes(participation);
grant select on protein_participates_goes to "SPARQL";

--============================================================================--

create table protein_participates_biosystems
(
    protein          integer not null,
    biosystem    integer not null,
    primary key(protein, biosystem)
);


insert into protein_participates_biosystems(protein, biosystem)
select
    rt.id as protein,
    sprintf_inverse(O, 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID%d', 0)[0] as biosystem
from (
    sparql select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?O from pubchem:protein where
    {
        ?S obo:BFO_0000056 ?O .
        filter(strstarts(str(?O), "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"))
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_participates_biosystems__protein on protein_participates_biosystems(protein);
create index protein_participates_biosystems__biosystem on protein_participates_biosystems(biosystem);
grant select on protein_participates_biosystems to "SPARQL";

--============================================================================--

create table protein_functions
(
    protein       integer not null,
    gofunction    integer not null,
    primary key(protein, gofunction)
);


insert into protein_functions(protein, gofunction)
select
    rt.id as protein,
    sprintf_inverse(O, 'http://purl.obolibrary.org/obo/GO_%d', 0)[0] as gofunction
from (
    sparql select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?O from pubchem:protein where
    {
        ?S obo:BFO_0000160 ?O .
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_functions__protein on protein_functions(protein);
create index protein_functions__gofunction on protein_functions(gofunction);
grant select on protein_functions to "SPARQL";

--============================================================================--

create table protein_locations
(
    protein     integer not null,
    location    integer not null,
    primary key(protein, location)
);


insert into protein_locations(protein, location)
select
    rt.id as protein,
    sprintf_inverse(O, 'http://purl.obolibrary.org/obo/GO_%d', 0)[0] as location
from (
    sparql select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?O from pubchem:protein where
    {
        ?S obo:BFO_0000171 ?O .
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_locations__protein on protein_locations(protein);
create index protein_locations__location on protein_locations(location);
grant select on protein_locations to "SPARQL";

--============================================================================--

create table protein_types
(
    protein    integer not null,
    type       integer not null,
    primary key(protein, type)
);


insert into protein_types(protein, type)
select
    rt.id as protein,
    sprintf_inverse(T, 'http://purl.obolibrary.org/obo/PR_%d', 0)[0] as type
from (
    sparql select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?T from pubchem:protein where
    {
        ?S rdf:type ?T .
        filter(strstarts(str(?T), "http://purl.obolibrary.org/obo/PR_"))
    }
) as tbl
inner join protein_bases as rt on rt.name = tbl.S;


create index protein_types__protein on protein_types(protein);
create index protein_types__type on protein_types(type);
grant select on protein_types to "SPARQL";

--============================================================================--

create table protein_complexes
(
    protein    integer not null,
    primary key(protein)
);


--insert into protein_complexes(protein)
--select
--    rt.id as protein
--from (
--    sparql select (str(replace(str(?S), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "")) as ?S) ?T from pubchem:protein where
--    {
--        ?S rdf:type obo:GO_0043234 .
--    }
--) as tbl
--inner join protein_bases as rt on rt.name = tbl.S;

insert into protein_complexes(protein)
select id from protein_bases where name like 'GI%GI%';


grant select on protein_complexes to "SPARQL";
