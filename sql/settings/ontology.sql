create index class_bases__label on class_bases(label);
grant select on class_bases to "SPARQL";

--------------------------------------------------------------------------------

create index class_superclasses__class on class_superclasses(class);
create index class_superclasses__superclass on class_superclasses(superclass);
grant select on class_superclasses to "SPARQL";

--------------------------------------------------------------------------------

create index property_bases__label on property_bases(label);
grant select on property_bases to "SPARQL";

--------------------------------------------------------------------------------

create index property_superproperties__property on property_superproperties(property);
create index property_superproperties__superproperty on property_superproperties(superproperty);
grant select on property_superproperties to "SPARQL";

--------------------------------------------------------------------------------

create index property_domains__property on property_domains(property);
create index property_domains__domain on property_domains(domain);
grant select on property_domains to "SPARQL";

--------------------------------------------------------------------------------

create index property_ranges__property on property_ranges(property);
create index property_ranges__range on property_ranges(range);
grant select on property_ranges to "SPARQL";
