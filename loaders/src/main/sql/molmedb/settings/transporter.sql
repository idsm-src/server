create index transporters__substance on molmedb.transporters(substance);
create index transporters__protein on molmedb.transporters(protein);
create index transporters__membrane on molmedb.transporters(membrane);
create index transporters__method on molmedb.transporters(method);
create index transporters__reference on molmedb.transporters(reference);
create index transporters__model_reference on molmedb.transporters(model_reference);
create index transporters__category on molmedb.transporters(category);
create index transporters__km on molmedb.transporters(km);
create index transporters__km_accuracy on molmedb.transporters(km_accuracy);
create index transporters__ec50 on molmedb.transporters(ec50);
create index transporters__ec50_accuracy on molmedb.transporters(ec50_accuracy);
create index transporters__ki on molmedb.transporters(ki);
create index transporters__ki_accuracy on molmedb.transporters(ki_accuracy);
create index transporters__ic50 on molmedb.transporters(ic50);
create index transporters__ic50_accuracy on molmedb.transporters(ic50_accuracy);
create index transporters__temperature on molmedb.transporters(temperature);
create index transporters__ph on molmedb.transporters(ph);
create index transporters__charge on molmedb.transporters(charge);
create index transporters__comment on molmedb.transporters(comment);
grant select on molmedb.transporters to sparql;

--------------------------------------------------------------------------------

create index proteins__uniprot_id on molmedb.proteins(uniprot_id);
create index proteins__name on molmedb.proteins(name);
grant select on molmedb.proteins to sparql;
