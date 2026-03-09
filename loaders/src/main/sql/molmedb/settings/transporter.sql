create index transporter_bases__substance_id on molmedb.transporter_bases(substance_id);
create index transporter_bases__protein_id on molmedb.transporter_bases(protein_id);
create index transporter_bases__membrane_id on molmedb.transporter_bases(membrane_id);
create index transporter_bases__method_id on molmedb.transporter_bases(method_id);
create index transporter_bases__publication_id on molmedb.transporter_bases(publication_id);
create index transporter_bases__model_publication_id on molmedb.transporter_bases(model_publication_id);
create index transporter_bases__category on molmedb.transporter_bases(category);
create index transporter_bases__km on molmedb.transporter_bases(km);
create index transporter_bases__km_accuracy on molmedb.transporter_bases(km_accuracy);
create index transporter_bases__ec50 on molmedb.transporter_bases(ec50);
create index transporter_bases__ec50_accuracy on molmedb.transporter_bases(ec50_accuracy);
create index transporter_bases__ki on molmedb.transporter_bases(ki);
create index transporter_bases__ki_accuracy on molmedb.transporter_bases(ki_accuracy);
create index transporter_bases__ic50 on molmedb.transporter_bases(ic50);
create index transporter_bases__ic50_accuracy on molmedb.transporter_bases(ic50_accuracy);
create index transporter_bases__temperature on molmedb.transporter_bases(temperature);
create index transporter_bases__ph on molmedb.transporter_bases(ph);
create index transporter_bases__charge on molmedb.transporter_bases(charge);
create index transporter_bases__comment on molmedb.transporter_bases(comment);
grant select on molmedb.transporter_bases to sparql;

--------------------------------------------------------------------------------

create index protein_bases__uniprot_id on molmedb.protein_bases(uniprot_id);
create index protein_bases__name on molmedb.protein_bases(name);
grant select on molmedb.protein_bases to sparql;
