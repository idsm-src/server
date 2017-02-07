create index class_bases__label on class_bases(label);
grant select on class_bases to "SPARQL";

--------------------------------------------------------------------------------

create index class_subclasses__class on class_subclasses(class);
create index class_subclasses__subclass on class_subclasses(subclass);
grant select on class_subclasses to "SPARQL";

--------------------------------------------------------------------------------

create index property_bases__label on property_bases(label);
grant select on property_bases to "SPARQL";

--------------------------------------------------------------------------------

create index property_subproperties__property on property_subproperties(property);
create index property_subproperties__subproperty on property_subproperties(subproperty);
grant select on property_subproperties to "SPARQL";

--------------------------------------------------------------------------------

create index property_domains__property on property_domains(property);
create index property_domains__domain on property_domains(domain);
grant select on property_domains to "SPARQL";

--------------------------------------------------------------------------------

create index property_ranges__property on property_ranges(property);
create index property_ranges__range on property_ranges(range);
grant select on property_ranges to "SPARQL";
