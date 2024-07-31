create index compound_bases__splash on mona.compound_bases(splash);
create index compound_bases__level on mona.compound_bases(level);
create index compound_bases__ionization_mode on mona.compound_bases(ionization_mode);
create index compound_bases__ionization_type on mona.compound_bases(ionization_type);
create index compound_bases__library on mona.compound_bases(library);
create index compound_bases__submitter on mona.compound_bases(submitter);
create index compound_bases__link on mona.compound_bases(link);
grant select on mona.compound_bases to sparql;

--------------------------------------------------------------------------------

create index compound_structures__structure on mona.compound_structures using hash (structure);
grant select on mona.compound_structures to sparql;

--------------------------------------------------------------------------------

create index compound_names__compound on mona.compound_names(compound);
create index compound_names__name on mona.compound_names(name);
grant select on mona.compound_names to sparql;

--------------------------------------------------------------------------------

create index compound_classyfires__compound on mona.compound_classyfires(compound);
create index compound_classyfires__class on mona.compound_classyfires(class);
grant select on mona.compound_classyfires to sparql;

--------------------------------------------------------------------------------

create index compound_chebi_classes__compound on mona.compound_chebi_classes(compound);
create index compound_chebi_classes__chebi on mona.compound_chebi_classes(chebi);
grant select on mona.compound_chebi_classes to sparql;

--------------------------------------------------------------------------------

create index compound_mesh_classes__compound on mona.compound_mesh_classes(compound);
create index compound_mesh_classes__mesh on mona.compound_mesh_classes(mesh);
grant select on mona.compound_mesh_classes to sparql;

--------------------------------------------------------------------------------

create index compound_inchi__compound on mona.compound_inchis(compound);
create index compound_inchi__inchi on mona.compound_inchis using hash (inchi);
grant select on mona.compound_inchis to sparql;

--------------------------------------------------------------------------------

create index compound_inchikey__compound on mona.compound_inchikeys(compound);
create index compound_inchikey__inchikey on mona.compound_inchikeys(inchikey);
grant select on mona.compound_inchikeys to sparql;

--------------------------------------------------------------------------------

create index compound_formulas__compound on mona.compound_formulas(compound);
create index compound_formulas__formula on mona.compound_formulas(formula);
grant select on mona.compound_formulas to sparql;

--------------------------------------------------------------------------------

create index compound_smiles__compound on mona.compound_smileses(compound);
create index compound_smiles__smiles on mona.compound_smileses(smiles);
grant select on mona.compound_smileses to sparql;

--------------------------------------------------------------------------------

create index compound_exact_masses__compound on mona.compound_exact_masses(compound);
create index compound_exact_masses__mass on mona.compound_exact_masses(mass);
grant select on mona.compound_exact_masses to sparql;

--------------------------------------------------------------------------------

create index compound_monoisotopic_masses__compound on mona.compound_monoisotopic_masses(compound);
create index compound_monoisotopic_masses__mass on mona.compound_monoisotopic_masses(mass);
grant select on mona.compound_monoisotopic_masses to sparql;

--------------------------------------------------------------------------------

create index compound_cas_numbers__compound on mona.compound_cas_numbers(compound);
create index compound_cas_numbers__cas on mona.compound_cas_numbers(cas);
grant select on mona.compound_cas_numbers to sparql;

--------------------------------------------------------------------------------

create index compound_hmdb_ids__compound on mona.compound_hmdb_ids(compound);
create index compound_hmdb_ids__hmdb on mona.compound_hmdb_ids(hmdb);
grant select on mona.compound_hmdb_ids to sparql;

--------------------------------------------------------------------------------

create index compound_chebi_ids__compound on mona.compound_chebi_ids(compound);
create index compound_chebi_ids__chebi on mona.compound_chebi_ids(chebi);
grant select on mona.compound_chebi_ids to sparql;

--------------------------------------------------------------------------------

create index compound_chemspider_ids__compound on mona.compound_chemspider_ids(compound);
create index compound_chemspider_ids__chemspider on mona.compound_chemspider_ids(chemspider);
grant select on mona.compound_chemspider_ids to sparql;

--------------------------------------------------------------------------------

create index compound_kegg_ids__compound on mona.compound_kegg_ids(compound);
create index compound_kegg_ids__kegg on mona.compound_kegg_ids(kegg);
grant select on mona.compound_kegg_ids to sparql;

--------------------------------------------------------------------------------

create index compound_knapsack_ids__compound on mona.compound_knapsack_ids(compound);
create index compound_knapsack_ids__knapsack on mona.compound_knapsack_ids(knapsack);
grant select on mona.compound_knapsack_ids to sparql;

--------------------------------------------------------------------------------

create index compound_lipidbank_ids__compound on mona.compound_lipidbank_ids(compound);
create index compound_lipidbank_ids__lipidbank on mona.compound_lipidbank_ids(lipidbank);
grant select on mona.compound_lipidbank_ids to sparql;

--------------------------------------------------------------------------------

create index compound_lipidmaps_ids__compound on mona.compound_lipidmaps_ids(compound);
create index compound_lipidmaps_ids__lipidmaps on mona.compound_lipidmaps_ids(lipidmaps);
grant select on mona.compound_lipidmaps_ids to sparql;

--------------------------------------------------------------------------------

create index compound_pubchem_compound_ids__compound on mona.compound_pubchem_compound_ids(compound);
create index compound_pubchem_compound_ids__cid on mona.compound_pubchem_compound_ids(cid);
grant select on mona.compound_pubchem_compound_ids to sparql;

--------------------------------------------------------------------------------

create index compound_pubchem_substance_ids__compound on mona.compound_pubchem_substance_ids(compound);
create index compound_pubchem_substance_ids__sid on mona.compound_pubchem_substance_ids(sid);
grant select on mona.compound_pubchem_substance_ids to sparql;

--------------------------------------------------------------------------------

create index spectrum_annotations__compound on mona.spectrum_annotations(compound);
create index spectrum_annotations__peak on mona.spectrum_annotations(peak);
create index spectrum_annotations__value on mona.spectrum_annotations(value);
grant select on mona.spectrum_annotations to sparql;

--------------------------------------------------------------------------------

create index spectrum_tags__compound on mona.spectrum_tags(compound);
create index spectrum_tags__tag on mona.spectrum_tags(tag);
grant select on mona.spectrum_tags to sparql;

--------------------------------------------------------------------------------

create index spectrum_normalized_entropies__compound on mona.spectrum_normalized_entropies(compound);
create index spectrum_normalized_entropies__entropy on mona.spectrum_normalized_entropies(entropy);
grant select on mona.spectrum_normalized_entropies to sparql;

--------------------------------------------------------------------------------

create index spectrum_spectral_entropies__compound on mona.spectrum_spectral_entropies(compound);
create index spectrum_spectral_entropies__entropy on mona.spectrum_spectral_entropies(entropy);
grant select on mona.spectrum_spectral_entropies to sparql;

--------------------------------------------------------------------------------

create index spectrum_retention_times__compound on mona.spectrum_retention_times(compound);
create index spectrum_retention_times__time on mona.spectrum_retention_times(time);
create index spectrum_retention_times__unit on mona.spectrum_retention_times(unit);
grant select on mona.spectrum_retention_times to sparql;

--------------------------------------------------------------------------------

create index spectrum_collision_energies__compound on mona.spectrum_collision_energies(compound);
create index spectrum_collision_energies__energy on mona.spectrum_collision_energies(energy);
create index spectrum_collision_energies__unit on mona.spectrum_collision_energies(unit);
grant select on mona.spectrum_collision_energies to sparql;

--------------------------------------------------------------------------------

create index spectrum_collision_energy_ramps__compound on mona.spectrum_collision_energy_ramps(compound);
create index spectrum_collision_energy_ramps__ramp_start on mona.spectrum_collision_energy_ramps(ramp_start);
create index spectrum_collision_energy_ramps__ramp_end on mona.spectrum_collision_energy_ramps(ramp_end);
create index spectrum_collision_energy_ramps__unit on mona.spectrum_collision_energy_ramps(unit);
grant select on mona.spectrum_collision_energy_ramps to sparql;

--------------------------------------------------------------------------------

create index spectrum_instrument_types__compound on mona.spectrum_instrument_types(compound);
create index spectrum_instrument_types__type on mona.spectrum_instrument_types(type);
grant select on mona.spectrum_instrument_types to sparql;

--------------------------------------------------------------------------------

create index spectrum_instruments__compound on mona.spectrum_instruments(compound);
create index spectrum_instruments__instrument on mona.spectrum_instruments(instrument);
grant select on mona.spectrum_instruments to sparql;

--------------------------------------------------------------------------------

create index spectrum_precursor_types__compound on mona.spectrum_precursor_types(compound);
create index spectrum_precursor_types__type on mona.spectrum_precursor_types(type);
grant select on mona.spectrum_precursor_types to sparql;

--------------------------------------------------------------------------------

create index spectrum_precursor_mzs__compound on mona.spectrum_precursor_mzs(compound);
create index spectrum_precursor_mzs__mz on mona.spectrum_precursor_mzs(mz);
grant select on mona.spectrum_precursor_mzs to sparql;

--------------------------------------------------------------------------------

create index library_bases__description on mona.library_bases(description);
grant select on mona.library_bases to sparql;

--------------------------------------------------------------------------------

create index submitter_bases__email on mona.submitter_bases(email);
create index submitter_bases__first_name on mona.submitter_bases(first_name);
create index submitter_bases__last_name on mona.submitter_bases(last_name);
create index submitter_bases__institution on mona.submitter_bases(institution);
grant select on mona.submitter_bases to sparql;
