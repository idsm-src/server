-- check if there is something new

sparql
select distinct ?P from descriptor:compound where
{
    ?S ?P ?O .
    filter(?P != sio:has-unit)
    filter(?P != sio:has-value)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from descriptor:compound where
{
    ?S rdf:type ?T .
    filter(?T != sio:CHEMINF_000388)
    filter(?T != sio:CHEMINF_000382)
    filter(?T != sio:CHEMINF_000335)
    filter(?T != sio:CHEMINF_000337)
    filter(?T != sio:CHEMINF_000391)
    filter(?T != sio:CHEMINF_000395)
    filter(?T != sio:CHEMINF_000390)
    filter(?T != sio:CHEMINF_000370)
    filter(?T != sio:CHEMINF_000371)
    filter(?T != sio:CHEMINF_000338)
    filter(?T != sio:CHEMINF_000396)
    filter(?T != sio:CHEMINF_000372)
    filter(?T != sio:CHEMINF_000334)
    filter(?T != sio:CHEMINF_000392)
    filter(?T != sio:CHEMINF_000375)
    filter(?T != sio:CHEMINF_000379)
    filter(?T != sio:CHEMINF_000369)
    filter(?T != sio:CHEMINF_000336)
    filter(?T != sio:CHEMINF_000387)
    filter(?T != sio:CHEMINF_000373)
    filter(?T != sio:CHEMINF_000389)
    filter(?T != sio:CHEMINF_000374)
    filter(?T != sio:CHEMINF_000376)
};

sparql
select distinct ?T from descriptor:compound where
{
    ?S sio:has-unit ?O .
    ?S rdf:type ?T .
    filter(?T != sio:CHEMINF_000338)
    filter(?T != sio:CHEMINF_000334)
    filter(?T != sio:CHEMINF_000337)
    filter(?T != sio:CHEMINF_000392)
};

sparql
select distinct ?O from descriptor:compound where
{
    ?S sio:has-unit ?O .
    ?S rdf:type sio:CHEMINF_000338 .
    filter(?O != obo:UO_0000055)
};

sparql
select distinct ?O from descriptor:compound where
{
    ?S sio:has-unit ?O .
    ?S rdf:type sio:CHEMINF_000334 .
    filter(?O != obo:UO_0000055)
};

sparql
select distinct ?O from descriptor:compound where
{
    ?S sio:has-unit ?O .
    ?S rdf:type sio:CHEMINF_000337 .
    filter(?O != obo:UO_0000055)
};

sparql
select distinct ?O from descriptor:compound where
{
    ?S sio:has-unit ?O .
    ?S rdf:type sio:CHEMINF_000392 .
    filter(?O != obo:UO_0000324)
};
