create index interaction_bases__substance_id on molmedb.interaction_bases(substance_id);
create index interaction_bases__membrane_id on molmedb.interaction_bases(membrane_id);
create index interaction_bases__method_id on molmedb.interaction_bases(method_id);
create index interaction_bases__publication_id on molmedb.interaction_bases(publication_id);
create index interaction_bases__model_publication_id on molmedb.interaction_bases(model_publication_id);
create index interaction_bases__logk on molmedb.interaction_bases(logk);
create index interaction_bases__logk_accuracy on molmedb.interaction_bases(logk_accuracy);
create index interaction_bases__logperm on molmedb.interaction_bases(logperm);
create index interaction_bases__logperm_accuracy on molmedb.interaction_bases(logperm_accuracy);
create index interaction_bases__x_min on molmedb.interaction_bases(x_min);
create index interaction_bases__x_min_accuracy on molmedb.interaction_bases(x_min_accuracy);
create index interaction_bases__gpen on molmedb.interaction_bases(gpen);
create index interaction_bases__gpen_accuracy on molmedb.interaction_bases(gpen_accuracy);
create index interaction_bases__gwat on molmedb.interaction_bases(gwat);
create index interaction_bases__gwat_accuracy on molmedb.interaction_bases(gwat_accuracy);
create index interaction_bases__temperature on molmedb.interaction_bases(temperature);
create index interaction_bases__ph on molmedb.interaction_bases(ph);
create index interaction_bases__charge on molmedb.interaction_bases(charge);
create index interaction_bases__comment on molmedb.interaction_bases(comment);
grant select on molmedb.interaction_bases to sparql;

--------------------------------------------------------------------------------

create index fluorescent_interaction_bases__substance_id on molmedb.fluorescent_interaction_bases(substance_id);
create index fluorescent_interaction_bases__membrane_id on molmedb.fluorescent_interaction_bases(membrane_id);
create index fluorescent_interaction_bases__method_id on molmedb.fluorescent_interaction_bases(method_id);
create index fluorescent_interaction_bases__publication_id on molmedb.fluorescent_interaction_bases(publication_id);
create index fluorescent_interaction_bases__model_publication_id on molmedb.fluorescent_interaction_bases(model_publication_id);
create index fluorescent_interaction_bases__theta on molmedb.fluorescent_interaction_bases(theta);
create index fluorescent_interaction_bases__theta_accuracy on molmedb.fluorescent_interaction_bases(theta_accuracy);
create index fluorescent_interaction_bases__abs_wl on molmedb.fluorescent_interaction_bases(abs_wl);
create index fluorescent_interaction_bases__abs_wl_accuracy on molmedb.fluorescent_interaction_bases(abs_wl_accuracy);
create index fluorescent_interaction_bases__fluo_wl on molmedb.fluorescent_interaction_bases(fluo_wl);
create index fluorescent_interaction_bases__fluo_wl_accuracy on molmedb.fluorescent_interaction_bases(fluo_wl_accuracy);
create index fluorescent_interaction_bases__qy on molmedb.fluorescent_interaction_bases(qy);
create index fluorescent_interaction_bases__qy_accuracy on molmedb.fluorescent_interaction_bases(qy_accuracy);
create index fluorescent_interaction_bases__lt on molmedb.fluorescent_interaction_bases(lt);
create index fluorescent_interaction_bases__lt_accuracy on molmedb.fluorescent_interaction_bases(lt_accuracy);
create index fluorescent_interaction_bases__temperature on molmedb.fluorescent_interaction_bases(temperature);
create index fluorescent_interaction_bases__ph on molmedb.fluorescent_interaction_bases(ph);
create index fluorescent_interaction_bases__charge on molmedb.fluorescent_interaction_bases(charge);
create index fluorescent_interaction_bases__comment on molmedb.fluorescent_interaction_bases(comment);
grant select on molmedb.fluorescent_interaction_bases to sparql;

--------------------------------------------------------------------------------

create index membrane_bases__category on molmedb.membrane_bases(category);
create index membrane_bases__parent_category on molmedb.membrane_bases(parent_category);
create index membrane_bases__name on molmedb.membrane_bases(name);
create index membrane_bases__abbreviation on molmedb.membrane_bases(abbreviation);
create index membrane_bases__description on molmedb.membrane_bases(description);
grant select on molmedb.membrane_bases to sparql;

--------------------------------------------------------------------------------

create index membrane_parts__membrane_id on molmedb.membrane_parts(membrane_id);
create index membrane_parts__chebi_id on molmedb.membrane_parts(chebi_id);
grant select on molmedb.membrane_parts to sparql;

--------------------------------------------------------------------------------

create index method_bases__category on molmedb.method_bases(category);
create index method_bases__parent_category on molmedb.method_bases(parent_category);
create index method_bases__name on molmedb.method_bases(name);
create index method_bases__abbreviation on molmedb.method_bases(abbreviation);
create index method_bases__description on molmedb.method_bases(description);
grant select on molmedb.method_bases to sparql;
