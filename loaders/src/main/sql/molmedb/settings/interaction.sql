create index interactions__substance on molmedb.interactions(substance);
create index interactions__membrane on molmedb.interactions(membrane);
create index interactions__method on molmedb.interactions(method);
create index interactions__reference on molmedb.interactions(reference);
create index interactions__model_reference on molmedb.interactions(model_reference);
create index interactions__logk on molmedb.interactions(logk);
create index interactions__logk_accuracy on molmedb.interactions(logk_accuracy);
create index interactions__logperm on molmedb.interactions(logperm);
create index interactions__logperm_accuracy on molmedb.interactions(logperm_accuracy);
create index interactions__x_min on molmedb.interactions(x_min);
create index interactions__x_min_accuracy on molmedb.interactions(x_min_accuracy);
create index interactions__gpen on molmedb.interactions(gpen);
create index interactions__gpen_accuracy on molmedb.interactions(gpen_accuracy);
create index interactions__gwat on molmedb.interactions(gwat);
create index interactions__gwat_accuracy on molmedb.interactions(gwat_accuracy);
create index interactions__temperature on molmedb.interactions(temperature);
create index interactions__ph on molmedb.interactions(ph);
create index interactions__charge on molmedb.interactions(charge);
create index interactions__comment on molmedb.interactions(comment);
grant select on molmedb.interactions to sparql;

--------------------------------------------------------------------------------

create index fluorescent_interactions__substance on molmedb.fluorescent_interactions(substance);
create index fluorescent_interactions__membrane on molmedb.fluorescent_interactions(membrane);
create index fluorescent_interactions__method on molmedb.fluorescent_interactions(method);
create index fluorescent_interactions__reference on molmedb.fluorescent_interactions(reference);
create index fluorescent_interactions__model_reference on molmedb.fluorescent_interactions(model_reference);
create index fluorescent_interactions__theta on molmedb.fluorescent_interactions(theta);
create index fluorescent_interactions__theta_accuracy on molmedb.fluorescent_interactions(theta_accuracy);
create index fluorescent_interactions__abs_wl on molmedb.fluorescent_interactions(abs_wl);
create index fluorescent_interactions__abs_wl_accuracy on molmedb.fluorescent_interactions(abs_wl_accuracy);
create index fluorescent_interactions__fluo_wl on molmedb.fluorescent_interactions(fluo_wl);
create index fluorescent_interactions__fluo_wl_accuracy on molmedb.fluorescent_interactions(fluo_wl_accuracy);
create index fluorescent_interactions__qy on molmedb.fluorescent_interactions(qy);
create index fluorescent_interactions__qy_accuracy on molmedb.fluorescent_interactions(qy_accuracy);
create index fluorescent_interactions__lt on molmedb.fluorescent_interactions(lt);
create index fluorescent_interactions__lt_accuracy on molmedb.fluorescent_interactions(lt_accuracy);
create index fluorescent_interactions__temperature on molmedb.fluorescent_interactions(temperature);
create index fluorescent_interactions__ph on molmedb.fluorescent_interactions(ph);
create index fluorescent_interactions__charge on molmedb.fluorescent_interactions(charge);
create index fluorescent_interactions__comment on molmedb.fluorescent_interactions(comment);
grant select on molmedb.fluorescent_interactions to sparql;

--------------------------------------------------------------------------------

create index membranes__category on molmedb.membranes(category);
create index membranes__parent_category on molmedb.membranes(parent_category);
create index membranes__name on molmedb.membranes(name);
create index membranes__abbreviation on molmedb.membranes(abbreviation);
create index membranes__description on molmedb.membranes(description);
grant select on molmedb.membranes to sparql;

--------------------------------------------------------------------------------

create index membrane_parts__membrane on molmedb.membrane_parts(membrane);
create index membrane_parts__chebi on molmedb.membrane_parts(chebi);
grant select on molmedb.membrane_parts to sparql;

--------------------------------------------------------------------------------

create index methods__category on molmedb.methods(category);
create index methods__parent_category on molmedb.methods(parent_category);
create index methods__name on molmedb.methods(name);
create index methods__abbreviation on molmedb.methods(abbreviation);
create index methods__description on molmedb.methods(description);
grant select on molmedb.methods to sparql;
