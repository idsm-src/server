grant select on chebi.classes to sparql;

--------------------------------------------------------------------------------

create index class_parents__class on chebi.class_parents(class);
create index class_parents__parent on chebi.class_parents(parent);
grant select on chebi.class_parents to sparql;

--------------------------------------------------------------------------------

create index class_stars__star_id on chebi.class_stars(star_id);
grant select on chebi.class_stars to sparql;

--------------------------------------------------------------------------------

create index class_replacements__replacement on chebi.class_replacements(replacement);
grant select on chebi.class_replacements to sparql;

--------------------------------------------------------------------------------

create index class_obsolescence_reasons__reason_id on chebi.class_obsolescence_reasons(reason_id);
grant select on chebi.class_obsolescence_reasons to sparql;

--------------------------------------------------------------------------------

create index restrictions__class on chebi.restrictions(class);
create index restrictions__value_restriction on chebi.restrictions(value_restriction);
create index restrictions__property_unit_property_id on chebi.restrictions(property_unit, property_id);
grant select on chebi.restrictions to sparql;

--------------------------------------------------------------------------------

create index axioms__class on chebi.axioms(class);
create index axioms__property_unit_property_id on chebi.axioms(property_unit, property_id);
create index axioms__target on chebi.axioms(target);
create index axioms__type_id on chebi.axioms(type_id);
create index axioms__reference on chebi.axioms(reference);
create index axioms__source on chebi.axioms(source);
grant select on chebi.axioms to sparql;

--------------------------------------------------------------------------------

create index class_references__class on chebi.class_references(class);
create index class_references__reference on chebi.class_references(reference);
grant select on chebi.class_references to sparql;

--------------------------------------------------------------------------------

create index class_related_synonyms__class on chebi.class_related_synonyms(class);
create index class_related_synonyms__synonym on chebi.class_related_synonyms(synonym);
grant select on chebi.class_related_synonyms to sparql;

--------------------------------------------------------------------------------

create index class_exact_synonyms__class on chebi.class_exact_synonyms(class);
create index class_exact_synonyms__synonym on chebi.class_exact_synonyms(synonym);
grant select on chebi.class_exact_synonyms to sparql;

--------------------------------------------------------------------------------

create index class_formulas__class on chebi.class_formulas(class);
create index class_formulas__formula on chebi.class_formulas(formula);
grant select on chebi.class_formulas to sparql;

--------------------------------------------------------------------------------

create index class_masses__class on chebi.class_masses(class);
create index class_masses__mass on chebi.class_masses(mass);
grant select on chebi.class_masses to sparql;

--------------------------------------------------------------------------------

create index class_monoisotopic_masses__class on chebi.class_monoisotopic_masses(class);
create index class_monoisotopic_masses__mass on chebi.class_monoisotopic_masses(mass);
grant select on chebi.class_monoisotopic_masses to sparql;

--------------------------------------------------------------------------------

create index class_alternative_identifiers__class on chebi.class_alternative_identifiers(class);
create index class_alternative_identifiers__identifier on chebi.class_alternative_identifiers(identifier);
grant select on chebi.class_alternative_identifiers to sparql;

--------------------------------------------------------------------------------

create index class_labels__label on chebi.class_labels(label);
grant select on chebi.class_labels to sparql;

--------------------------------------------------------------------------------

create index class_identifiers__identifier on chebi.class_identifiers(identifier);
grant select on chebi.class_identifiers to sparql;

--------------------------------------------------------------------------------

create index class_namespaces__namespace on chebi.class_namespaces(namespace);
grant select on chebi.class_namespaces to sparql;

--------------------------------------------------------------------------------

create index class_charges__charge on chebi.class_charges(charge);
grant select on chebi.class_charges to sparql;

--------------------------------------------------------------------------------

create index class_smileses__smiles on chebi.class_smileses(smiles);
grant select on chebi.class_smileses to sparql;

--------------------------------------------------------------------------------

create index class_inchikeys__inchikey on chebi.class_inchikeys(inchikey);
grant select on chebi.class_inchikeys to sparql;

--------------------------------------------------------------------------------

create index class_inchis__inchi on chebi.class_inchis using hash(inchi);
grant select on chebi.class_inchis to sparql;

--------------------------------------------------------------------------------

create index class_wurcs_representations__wurcs on chebi.class_wurcs_representations(wurcs);
grant select on chebi.class_wurcs_representations to sparql;

--------------------------------------------------------------------------------

create index class_molfiles__molfile on chebi.class_molfiles using hash (molfile);
grant select on chebi.class_molfiles to sparql;

--------------------------------------------------------------------------------

create index class_definitions__definition on chebi.class_definitions(definition);
grant select on chebi.class_definitions to sparql;

--------------------------------------------------------------------------------

create index class_deprecated_flags__flag on chebi.class_deprecated_flags(flag);
grant select on chebi.class_deprecated_flags to sparql;
