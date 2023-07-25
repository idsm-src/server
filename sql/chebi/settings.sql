grant select on chebi.classes to sparql;

--------------------------------------------------------------------------------

create index parents__chebi on chebi.parents(chebi);
create index parents__parent on chebi.parents(parent);
grant select on chebi.parents to sparql;

--------------------------------------------------------------------------------

create index stars__star on chebi.stars(star);
grant select on chebi.stars to sparql;

--------------------------------------------------------------------------------

create index replacements__replacement on chebi.replacements(replacement);
grant select on chebi.replacements to sparql;

--------------------------------------------------------------------------------

create index obsolescence_reasons__reason on chebi.obsolescence_reasons(reason);
grant select on chebi.obsolescence_reasons to sparql;

--------------------------------------------------------------------------------

create index restrictions__chebi on chebi.restrictions(chebi);
create index restrictions__value_restriction on chebi.restrictions(value_restriction);
create index restrictions__property on chebi.restrictions(property_unit, property_id);
grant select on chebi.restrictions to sparql;

--------------------------------------------------------------------------------

create index axioms__chebi on chebi.axioms(chebi);
create index axioms__property on chebi.axioms(property_unit, property_id);
create index axioms__target on chebi.axioms(target);
create index axioms__type_id on chebi.axioms(type_id);
create index axioms__reference on chebi.axioms(reference);
create index axioms__source on chebi.axioms(source);
grant select on chebi.axioms to sparql;

--------------------------------------------------------------------------------

create index references__chebi on chebi.references(chebi);
create index references__reference on chebi.references(reference);
grant select on chebi.references to sparql;

--------------------------------------------------------------------------------

create index related_synonyms__chebi on chebi.related_synonyms(chebi);
create index related_synonyms__synonym on chebi.related_synonyms(synonym);
grant select on chebi.related_synonyms to sparql;

--------------------------------------------------------------------------------

create index exact_synonyms__chebi on chebi.exact_synonyms(chebi);
create index exact_synonyms__synonym on chebi.exact_synonyms(synonym);
grant select on chebi.exact_synonyms to sparql;

--------------------------------------------------------------------------------

create index formulas__chebi on chebi.formulas(chebi);
create index formulas__formula on chebi.formulas(formula);
grant select on chebi.formulas to sparql;

--------------------------------------------------------------------------------

create index masses__chebi on chebi.masses(chebi);
create index masses__mass on chebi.masses(mass);
grant select on chebi.masses to sparql;

--------------------------------------------------------------------------------

create index monoisotopic_masses__chebi on chebi.monoisotopic_masses(chebi);
create index monoisotopic_masses__mass on chebi.monoisotopic_masses(mass);
grant select on chebi.monoisotopic_masses to sparql;

--------------------------------------------------------------------------------

create index alternative_identifiers__chebi on chebi.alternative_identifiers(chebi);
create index alternative_identifiers__identifier on chebi.alternative_identifiers(identifier);
grant select on chebi.alternative_identifiers to sparql;

--------------------------------------------------------------------------------

create index labels__label on chebi.labels(label);
grant select on chebi.labels to sparql;

--------------------------------------------------------------------------------

create index identifiers__identifier on chebi.identifiers(identifier);
grant select on chebi.identifiers to sparql;

--------------------------------------------------------------------------------

create index namespaces__namespace on chebi.namespaces(namespace);
grant select on chebi.namespaces to sparql;

--------------------------------------------------------------------------------

create index charges__charge on chebi.charges(charge);
grant select on chebi.charges to sparql;

--------------------------------------------------------------------------------

create index smiles_codes__smiles on chebi.smiles_codes(smiles);
grant select on chebi.smiles_codes to sparql;

--------------------------------------------------------------------------------

create index inchikeys__inchikey on chebi.inchikeys(inchikey);
grant select on chebi.inchikeys to sparql;

--------------------------------------------------------------------------------

--create index inchies__inchi on chebi.inchies(inchi);
grant select on chebi.inchies to sparql;

--------------------------------------------------------------------------------


create index definitions__definition on chebi.definitions(definition);
grant select on chebi.definitions to sparql;

--------------------------------------------------------------------------------

create index deprecated_flags__flag on chebi.deprecated_flags(flag);
grant select on chebi.deprecated_flags to sparql;
