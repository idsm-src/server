log_enable(2);

--============================================================================--

create table descriptor_substance_bases
(
    substance    integer not null,
    version      integer not null,
    primary key(substance)
);


insert into descriptor_substance_bases(substance, version)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID%d_Substance_Version', 0)[0] as substance,
    cast(version as integer)
from
(
    sparql select * from descriptor:substance where
    {
        ?S rdf:type obo:IAO_0000129;
           sio:has-value ?version
    }
) as tbl;


create bitmap index descriptor_substance_bases__version on descriptor_substance_bases(version);
grant select on descriptor_substance_bases to "SPARQL";
