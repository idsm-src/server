log_enable(2);

--============================================================================--

create table reference_types__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


insert into reference_types__reftable(id, iri) values (0, 'http://purl.org/spar/fabio/ReviewArticle');
insert into reference_types__reftable(id, iri) values (1, 'http://purl.org/spar/fabio/JournalArticle');
grant select on reference_types__reftable to "SPARQL";

--------------------------------------------------------------------------------

create table reference_bases
(
    id          integer not null,
    type        smallint not null,
    dcdate      datetime,
    title       nvarchar,
    citation    nvarchar,
    primary key(id)
);


insert into reference_bases(id, type, dcdate, title, citation)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as id,
    rt.id as type,
    dcdate,
    title,
    citation
from (
    sparql select ?S (str(str(?type)) as ?type) ?title ?dcdate ?citation from pubchem:reference where
    {
        ?S rdf:type ?type .
        optional { ?S dcterms:title ?title }
        optional { ?S dcterms:date ?dcdate }
        optional { ?S dcterms:bibliographicCitation ?citation filter(strlen(?citation) < 2048) }
    }
) as tbl
inner join reference_types__reftable as rt on rt.iri=tbl.type;


create bitmap index reference_bases__type on reference_bases(type);
create index reference_bases__dcdate on reference_bases(dcdate);
grant select on reference_bases to "SPARQL";

--------------------------------------------------------------------------------

create table reference_citations_long
(
    reference    integer not null,
    citation     long nvarchar not null,
    primary key(reference)
);


insert into reference_citations_long(reference, citation)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference,
    cast(citation as nvarchar)
from (
    sparql select ?S ?citation from pubchem:reference where
    {
        ?S dcterms:bibliographicCitation ?citation
        filter(strlen(?citation) >= 2048)
    }
) as tbl;


grant select on reference_citations_long to "SPARQL";

--============================================================================--

create table reference_discusses_mesh
(
    reference    integer not null,
    statement    integer not null,
    primary key(reference, statement)
);


insert into reference_discusses_mesh(reference, statement)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference,
    sprintf_inverse(O, 'http://id.nlm.nih.gov/mesh/M%d', 0)[0] as statement
from (
    sparql select ?S ?O from pubchem:reference where
    {
        ?S cito:discusses ?O
        filter(strstarts(str(?O), 'http://id.nlm.nih.gov/mesh/M'))
    }
) as tbl;


create index reference_discusses_mesh__reference on reference_discusses_mesh(reference);
create index reference_discusses_mesh__statement on reference_discusses_mesh(statement);
grant select on reference_discusses_mesh to "SPARQL";

--------------------------------------------------------------------------------

create table reference_discusses_cmesh
(
    reference    integer not null,
    statement    integer not null,
    primary key(reference, statement)
);


insert into reference_discusses_cmesh(reference, statement)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference,
    sprintf_inverse(O, 'http://id.nlm.nih.gov/mesh/C%d', 0)[0] as statement
from (
    sparql select ?S ?O from pubchem:reference where
    {
        ?S cito:discusses ?O
        filter(strstarts(str(?O), 'http://id.nlm.nih.gov/mesh/C'))
    }
) as tbl;


create index reference_discusses_cmesh__reference on reference_discusses_cmesh(reference);
create index reference_discusses_cmesh__statement on reference_discusses_cmesh(statement);
grant select on reference_discusses_cmesh to "SPARQL";

--============================================================================--

create table reference_subject_descriptors
(
    reference     integer not null,
    descriptor    integer not null,
    primary key(reference, descriptor)
);


insert into reference_subject_descriptors(reference, descriptor)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference,
    sprintf_inverse(O, 'http://id.nlm.nih.gov/mesh/D%d', 0)[0] as descriptor
from (
    sparql select ?S ?O from pubchem:reference where
    {
        ?S fabio:hasSubjectTerm ?O
    }
) as tbl
where sprintf_inverse(O, 'http://id.nlm.nih.gov/mesh/D%d', 0) is not null;


create index reference_subject_descriptors__reference on reference_subject_descriptors(reference);
create index reference_subject_descriptors__descriptor on reference_subject_descriptors(descriptor);
grant select on reference_subject_descriptors to "SPARQL";

--------------------------------------------------------------------------------

create table reference_subject_descriptor_qualifiers
(
    reference     integer not null,
    descriptor    integer not null,
    qualifier     integer not null,
    primary key(reference, descriptor, qualifier)
);


insert into reference_subject_descriptor_qualifiers(reference, descriptor, qualifier)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID%d', 0)[0] as reference,
    sprintf_inverse(O, 'http://id.nlm.nih.gov/mesh/D%dQ%d', 0)[0] as descriptor,
    sprintf_inverse(O, 'http://id.nlm.nih.gov/mesh/D%dQ%d', 0)[1] as qualifier
from (
    sparql select ?S ?O from pubchem:reference where
    {
        ?S fabio:hasSubjectTerm ?O
    }
) as tbl
where sprintf_inverse(O, 'http://id.nlm.nih.gov/mesh/D%d', 0) is null;


create index reference_subject_descriptor_qualifiers__reference on reference_subject_descriptor_qualifiers(reference);
create index reference_subject_descriptor_qualifiers__descriptor_qualifier on reference_subject_descriptor_qualifiers(descriptor, qualifier);
grant select on reference_subject_descriptor_qualifiers to "SPARQL";
