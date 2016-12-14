-- check if there is something new

sparql
select distinct ?P from descriptor:substance where
{
    ?S ?P ?O .
    filter(?P != sio:has-value)
    filter(?P != rdf:type)
};

sparql
select distinct ?T from descriptor:substance where
{
    ?S rdf:type ?T .
    filter(?T != obo:IAO_0000129)
};
