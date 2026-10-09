create index spectra__splash on mona.spectra(splash);
create index spectra__level on mona.spectra(level);
create index spectra__ionization_mode on mona.spectra(ionization_mode);
create index spectra__ionization_type on mona.spectra(ionization_type);
create index spectra__library on mona.spectra(library);
create index spectra__submitter on mona.spectra(submitter);
create index spectra__link on mona.spectra(link);
grant select on mona.spectra to sparql;

--------------------------------------------------------------------------------

create index compound_structures__structure on mona.compound_structures using hash (structure);
grant select on mona.compound_structures to sparql;

--------------------------------------------------------------------------------

create index compound_names__compound on mona.compound_names(compound);
create index compound_names__name on mona.compound_names(name);
grant select on mona.compound_names to sparql;

--------------------------------------------------------------------------------

create index compound_classyfire_classes__compound on mona.compound_classyfire_classes(compound);
create index compound_classyfire_classes__class on mona.compound_classyfire_classes(class);
grant select on mona.compound_classyfire_classes to sparql;

--------------------------------------------------------------------------------

create index compound_chebi_classes__compound on mona.compound_chebi_classes(compound);
create index compound_chebi_classes__chebi on mona.compound_chebi_classes(chebi);
grant select on mona.compound_chebi_classes to sparql;

--------------------------------------------------------------------------------

create index compound_mesh_classes__compound on mona.compound_mesh_classes(compound);
create index compound_mesh_classes__mesh on mona.compound_mesh_classes(mesh);
grant select on mona.compound_mesh_classes to sparql;

--------------------------------------------------------------------------------

create index compound_inchis__compound on mona.compound_inchis(compound);
create index compound_inchis__inchi on mona.compound_inchis using hash (inchi);
grant select on mona.compound_inchis to sparql;

--------------------------------------------------------------------------------

create index compound_inchikeys__compound on mona.compound_inchikeys(compound);
create index compound_inchikeys__inchikey on mona.compound_inchikeys(inchikey);
grant select on mona.compound_inchikeys to sparql;

--------------------------------------------------------------------------------

create index compound_formulas__compound on mona.compound_formulas(compound);
create index compound_formulas__formula on mona.compound_formulas(formula);
grant select on mona.compound_formulas to sparql;

--------------------------------------------------------------------------------

create index compound_smileses__compound on mona.compound_smileses(compound);
create index compound_smileses__smiles on mona.compound_smileses(smiles);
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
create index compound_hmdb_ids__hmdb__prefixed on mona.compound_hmdb_ids((('HMDB' || hmdb)::varchar));
grant select on mona.compound_hmdb_ids to sparql;

--------------------------------------------------------------------------------

create index compound_chebi_ids__compound on mona.compound_chebi_ids(compound);
create index compound_chebi_ids__chebi on mona.compound_chebi_ids(chebi);
create index compound_chebi_ids__chebi__prefixed on mona.compound_chebi_ids((('CHEBI:' || chebi)::varchar));
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
create index compound_pubchem_compound_ids__cid__prefixed on mona.compound_pubchem_compound_ids((('CID' || cid)::varchar));
grant select on mona.compound_pubchem_compound_ids to sparql;

--------------------------------------------------------------------------------

create index compound_pubchem_substance_ids__compound on mona.compound_pubchem_substance_ids(compound);
create index compound_pubchem_substance_ids__sid on mona.compound_pubchem_substance_ids(sid);
create index compound_pubchem_substance_ids__sid__prefixed on mona.compound_pubchem_substance_ids((('SID' || sid)::varchar));
grant select on mona.compound_pubchem_substance_ids to sparql;

--------------------------------------------------------------------------------

create index spectrum_annotations__spectrum on mona.spectrum_annotations(spectrum);
create index spectrum_annotations__peak on mona.spectrum_annotations(peak);
create index spectrum_annotations__value on mona.spectrum_annotations(value);
grant select on mona.spectrum_annotations to sparql;

--------------------------------------------------------------------------------

create index spectrum_tags__spectrum on mona.spectrum_tags(spectrum);
create index spectrum_tags__tag on mona.spectrum_tags(tag);
grant select on mona.spectrum_tags to sparql;

--------------------------------------------------------------------------------

create index spectrum_normalized_entropies__spectrum on mona.spectrum_normalized_entropies(spectrum);
create index spectrum_normalized_entropies__entropy on mona.spectrum_normalized_entropies(entropy);
grant select on mona.spectrum_normalized_entropies to sparql;

--------------------------------------------------------------------------------

create index spectrum_spectral_entropies__spectrum on mona.spectrum_spectral_entropies(spectrum);
create index spectrum_spectral_entropies__entropy on mona.spectrum_spectral_entropies(entropy);
grant select on mona.spectrum_spectral_entropies to sparql;

--------------------------------------------------------------------------------

create index spectrum_retention_times__spectrum on mona.spectrum_retention_times(spectrum);
create index spectrum_retention_times__time on mona.spectrum_retention_times(time);
create index spectrum_retention_times__unit on mona.spectrum_retention_times(unit);
grant select on mona.spectrum_retention_times to sparql;

--------------------------------------------------------------------------------

create index spectrum_collision_energies__spectrum on mona.spectrum_collision_energies(spectrum);
create index spectrum_collision_energies__energy on mona.spectrum_collision_energies(energy);
create index spectrum_collision_energies__unit on mona.spectrum_collision_energies(unit);
grant select on mona.spectrum_collision_energies to sparql;

--------------------------------------------------------------------------------

create index spectrum_collision_energy_ramps__spectrum on mona.spectrum_collision_energy_ramps(spectrum);
create index spectrum_collision_energy_ramps__ramp_start on mona.spectrum_collision_energy_ramps(ramp_start);
create index spectrum_collision_energy_ramps__ramp_end on mona.spectrum_collision_energy_ramps(ramp_end);
create index spectrum_collision_energy_ramps__unit on mona.spectrum_collision_energy_ramps(unit);
grant select on mona.spectrum_collision_energy_ramps to sparql;

--------------------------------------------------------------------------------

create index spectrum_instrument_types__spectrum on mona.spectrum_instrument_types(spectrum);
create index spectrum_instrument_types__type on mona.spectrum_instrument_types(type);
grant select on mona.spectrum_instrument_types to sparql;

--------------------------------------------------------------------------------

create index spectrum_instruments__spectrum on mona.spectrum_instruments(spectrum);
create index spectrum_instruments__instrument on mona.spectrum_instruments(instrument);
grant select on mona.spectrum_instruments to sparql;

--------------------------------------------------------------------------------

create index spectrum_precursor_types__spectrum on mona.spectrum_precursor_types(spectrum);
create index spectrum_precursor_types__type on mona.spectrum_precursor_types(type);
grant select on mona.spectrum_precursor_types to sparql;

--------------------------------------------------------------------------------

create index spectrum_precursor_mzs__spectrum on mona.spectrum_precursor_mzs(spectrum);
create index spectrum_precursor_mzs__mz on mona.spectrum_precursor_mzs(mz);
grant select on mona.spectrum_precursor_mzs to sparql;

--------------------------------------------------------------------------------

create index libraries__description on mona.libraries(description);
grant select on mona.libraries to sparql;

--------------------------------------------------------------------------------

create index submitters__email on mona.submitters(email);
create index submitters__first_name on mona.submitters(first_name);
create index submitters__last_name on mona.submitters(last_name);
create index submitters__institution on mona.submitters(institution);
grant select on mona.submitters to sparql;
