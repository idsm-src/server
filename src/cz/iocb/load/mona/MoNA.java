package cz.iocb.load.mona;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Updater;



public class MoNA extends Updater
{
    @SuppressWarnings("serial")
    private static class SubmitterIntMap extends SqlMap<Submitter, Integer>
    {
        @Override
        public Submitter getKey(ResultSet result) throws SQLException
        {
            Submitter submitter = new Submitter();
            submitter.emailAddress = result.getString(1);
            submitter.firstName = result.getString(2);
            submitter.lastName = result.getString(3);
            submitter.institution = result.getString(4);
            return submitter;
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(5);
        }

        @Override
        public void set(PreparedStatement statement, Submitter key, Integer value) throws SQLException
        {
            statement.setString(1, key.emailAddress.isEmpty() ? null : key.emailAddress);
            statement.setString(2, key.firstName.isEmpty() ? null : key.firstName);
            statement.setString(3, key.lastName.isEmpty() ? null : key.lastName);
            statement.setString(4, key.institution.isEmpty() ? null : key.institution);
            statement.setInt(5, value);
        }
    }


    @SuppressWarnings("serial")
    private static class IntAnnotationPairIntMap extends SqlMap<Pair<Integer, Annotation>, Integer>
    {
        @Override
        public Pair<Integer, Annotation> getKey(ResultSet result) throws SQLException
        {
            Annotation annotation = new Annotation();
            annotation.value = result.getFloat(2);
            annotation.name = result.getString(3);

            return Pair.getPair(result.getInt(1), annotation);
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(4);
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, Annotation> key, Integer value) throws SQLException
        {
            statement.setInt(1, key.getOne());
            statement.setFloat(2, key.getTwo().value);
            statement.setString(3, key.getTwo().name);
            statement.setInt(4, value);
        }
    }


    private static TimeZone tz = TimeZone.getTimeZone("UTC");
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    static
    {
        df.setTimeZone(tz);
    }


    private static Map<String, ClassyFire> loadClassyFires(String path) throws FileNotFoundException, IOException
    {
        Map<String, ClassyFire> classyFires = new HashMap<String, ClassyFire>();

        try(BufferedReader reader = new BufferedReader(new FileReader(path)))
        {
            ClassyFire classyFire = null;
            String line = null;

            while((line = reader.readLine()) != null)
            {
                if(line.equals("[Term]"))
                    classyFire = null;
                else if(line.startsWith("id: "))
                    classyFire = new ClassyFire(Integer.parseInt(line.replaceFirst("^id: CHEMONTID:", "")));
                else if(line.startsWith("name: "))
                    classyFires.put(line.replaceFirst("^name: ", ""), classyFire);
                else if(line.matches("synonym: \".*\" RELATED ChEBI_TERM \\[CHEBI:.*\\]"))
                    classyFire.chebi.add(Integer.valueOf(line.replaceFirst(".*\\[CHEBI:(.*)\\]$", "$1")));
                else if(line.matches("synonym: \".*\" RELATED MeSH_TERM \\[MESH:.*\\]"))
                    classyFire.mesh.add(line.replaceFirst(".*\\[MESH:(.*)\\]$", "$1"));
            }
        }

        return classyFires;
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        StringIntMap keepCompounds = new StringIntMap();
        StringIntMap newCompounds = new StringIntMap();
        StringIntMap oldCompounds = new StringIntMap();

        IntStringMap keepCreatedDates = new IntStringMap();
        IntStringPairMap newCreatedDates = new IntStringPairMap();
        IntStringMap oldCreatedDates = new IntStringMap();

        IntStringMap keepCuratedDates = new IntStringMap();
        IntStringPairMap newCuratedDates = new IntStringPairMap();
        IntStringMap oldCuratedDates = new IntStringMap();

        IntStringMap keepUpdatedDates = new IntStringMap();
        IntStringPairMap newUpdatedDates = new IntStringPairMap();
        IntStringMap oldUpdatedDates = new IntStringMap();

        IntStringMap keepSpectra = new IntStringMap();
        IntStringPairMap newSpectra = new IntStringPairMap();
        IntStringMap oldSpectra = new IntStringMap();

        IntStringMap keepSplashes = new IntStringMap();
        IntStringPairMap newSplashes = new IntStringPairMap();
        IntStringMap oldSplashes = new IntStringMap();

        IntStringMap keepIonizationModes = new IntStringMap();
        IntStringPairMap newIonizationModes = new IntStringPairMap();
        IntStringMap oldIonizationModes = new IntStringMap();

        IntIntMap keepIonizationTypes = new IntIntMap();
        IntStringIntPairMap newIonizationTypes = new IntStringIntPairMap();
        IntIntMap oldIonizationTypes = new IntIntMap();

        IntIntMap keepLevels = new IntIntMap();
        IntStringIntPairMap newLevels = new IntStringIntPairMap();
        IntIntMap oldLevels = new IntIntMap();

        IntIntMap keepCompoundLibraries = new IntIntMap();
        IntStringIntPairMap newCompoundLibraries = new IntStringIntPairMap();
        IntIntMap oldCompoundLibraries = new IntIntMap();

        IntIntMap keepCompoundSubmitters = new IntIntMap();
        IntStringIntPairMap newCompoundSubmitters = new IntStringIntPairMap();
        IntIntMap oldCompoundSubmitters = new IntIntMap();

        IntStringMap keepLibraryLinks = new IntStringMap();
        IntStringPairMap newLibraryLinks = new IntStringPairMap();
        IntStringMap oldLibraryLinks = new IntStringMap();

        IntStringMap keepStructures = new IntStringMap();
        IntStringMap newStructures = new IntStringMap();
        IntStringMap oldStructures = new IntStringMap();

        IntStringSet keepNames = new IntStringSet();
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        IntPairSet keepClassyFires = new IntPairSet();
        IntPairSet newClassyFires = new IntPairSet();
        IntPairSet oldClassyFires = new IntPairSet();

        IntPairSet keepChebiClasses = new IntPairSet();
        IntPairSet newChebiClasses = new IntPairSet();
        IntPairSet oldChebiClasses = new IntPairSet();

        IntStringSet keepMeshClasses = new IntStringSet();
        IntStringSet newMeshClasses = new IntStringSet();
        IntStringSet oldMeshClasses = new IntStringSet();

        IntStringPairIntMap keepInchis = new IntStringPairIntMap();
        IntStringPairIntMap newInchis = new IntStringPairIntMap();
        IntStringPairIntMap oldInchis = new IntStringPairIntMap();

        IntStringSet keepInchiKeys = new IntStringSet();
        IntStringSet newInchiKeys = new IntStringSet();
        IntStringSet oldInchiKeys = new IntStringSet();

        IntStringSet keepFormulas = new IntStringSet();
        IntStringSet newFormulas = new IntStringSet();
        IntStringSet oldFormulas = new IntStringSet();

        IntStringSet keepSmiles = new IntStringSet();
        IntStringSet newSmiles = new IntStringSet();
        IntStringSet oldSmiles = new IntStringSet();

        IntFloatSet keepExactMasses = new IntFloatSet();
        IntFloatSet newExactMasses = new IntFloatSet();
        IntFloatSet oldExactMasses = new IntFloatSet();

        IntFloatSet keepMonoisotopicMasses = new IntFloatSet();
        IntFloatSet newMonoisotopicMasses = new IntFloatSet();
        IntFloatSet oldMonoisotopicMasses = new IntFloatSet();

        IntStringSet keepCasNumbers = new IntStringSet();
        IntStringSet newCasNumbers = new IntStringSet();
        IntStringSet oldCasNumbers = new IntStringSet();

        IntStringSet keepHmdbIdentifiers = new IntStringSet();
        IntStringSet newHmdbIdentifiers = new IntStringSet();
        IntStringSet oldHmdbIdentifiers = new IntStringSet();

        IntPairSet keepChebiIdentifiers = new IntPairSet();
        IntPairSet newChebiIdentifiers = new IntPairSet();
        IntPairSet oldChebiIdentifiers = new IntPairSet();

        IntStringSet keepChemspiderIdentifiers = new IntStringSet();
        IntStringSet newChemspiderIdentifiers = new IntStringSet();
        IntStringSet oldChemspiderIdentifiers = new IntStringSet();

        IntStringSet keepKeggIdentifiers = new IntStringSet();
        IntStringSet newKeggIdentifiers = new IntStringSet();
        IntStringSet oldKeggIdentifiers = new IntStringSet();

        IntStringSet keepKnapsackIdentifiers = new IntStringSet();
        IntStringSet newKnapsackIdentifiers = new IntStringSet();
        IntStringSet oldKnapsackIdentifiers = new IntStringSet();

        IntStringSet keepLipidBankIdentifiers = new IntStringSet();
        IntStringSet newLipidBankIdentifiers = new IntStringSet();
        IntStringSet oldLipidBankIdentifiers = new IntStringSet();

        IntStringSet keepLipidMapsIdentifiers = new IntStringSet();
        IntStringSet newLipidMapsIdentifiers = new IntStringSet();
        IntStringSet oldLipidMapsIdentifiers = new IntStringSet();

        IntPairSet keepPubchemCompoundIdentifiers = new IntPairSet();
        IntPairSet newPubchemCompoundIdentifiers = new IntPairSet();
        IntPairSet oldPubchemCompoundIdentifiers = new IntPairSet();

        IntPairSet keepPubchemSubstanceIdentifiers = new IntPairSet();
        IntPairSet newPubchemSubstanceIdentifiers = new IntPairSet();
        IntPairSet oldPubchemSubstanceIdentifiers = new IntPairSet();

        IntAnnotationPairIntMap keepAnnotations = new IntAnnotationPairIntMap();
        IntAnnotationPairIntMap newAnnotations = new IntAnnotationPairIntMap();
        IntAnnotationPairIntMap oldAnnotations = new IntAnnotationPairIntMap();

        IntStringSet keepTags = new IntStringSet();
        IntStringSet newTags = new IntStringSet();
        IntStringSet oldTags = new IntStringSet();

        IntFloatSet keepNormalizedEntropies = new IntFloatSet();
        IntFloatSet newNormalizedEntropies = new IntFloatSet();
        IntFloatSet oldNormalizedEntropies = new IntFloatSet();

        IntFloatSet keepSpectralEntropies = new IntFloatSet();
        IntFloatSet newSpectralEntropies = new IntFloatSet();
        IntFloatSet oldSpectralEntropies = new IntFloatSet();

        IntFloatIntPairSet keepRetentionTimes = new IntFloatIntPairSet();
        IntFloatIntPairSet newRetentionTimes = new IntFloatIntPairSet();
        IntFloatIntPairSet oldRetentionTimes = new IntFloatIntPairSet();

        IntFloatIntPairSet keepCollisionEnergies = new IntFloatIntPairSet();
        IntFloatIntPairSet newCollisionEnergies = new IntFloatIntPairSet();
        IntFloatIntPairSet oldCollisionEnergies = new IntFloatIntPairSet();

        IntFloatPairIntPairSet keepCollisionEnergyRamps = new IntFloatPairIntPairSet();
        IntFloatPairIntPairSet newCollisionEnergyRamps = new IntFloatPairIntPairSet();
        IntFloatPairIntPairSet oldCollisionEnergyRamps = new IntFloatPairIntPairSet();

        IntStringSet keepInstrumentTypes = new IntStringSet();
        IntStringSet newInstrumentTypes = new IntStringSet();
        IntStringSet oldInstrumentTypes = new IntStringSet();

        IntStringSet keepInstruments = new IntStringSet();
        IntStringSet newInstruments = new IntStringSet();
        IntStringSet oldInstruments = new IntStringSet();

        IntStringSet keepPrecursorTypes = new IntStringSet();
        IntStringSet newPrecursorTypes = new IntStringSet();
        IntStringSet oldPrecursorTypes = new IntStringSet();

        IntFloatSet keepPrecursorMZs = new IntFloatSet();
        IntFloatSet newPrecursorMZs = new IntFloatSet();
        IntFloatSet oldPrecursorMZs = new IntFloatSet();

        IntStringMap keepLibraryDescriptions = new IntStringMap();
        IntStringPairMap newLibraryDescriptions = new IntStringPairMap();
        IntStringMap oldLibraryDescriptions = new IntStringMap();

        StringIntMap keepLibraries = new StringIntMap();
        StringIntMap newLibraries = new StringIntMap();
        StringIntMap oldLibraries = new StringIntMap();

        SubmitterIntMap keepSubmitters = new SubmitterIntMap();
        SubmitterIntMap newSubmitters = new SubmitterIntMap();
        SubmitterIntMap oldSubmitters = new SubmitterIntMap();


        try
        {
            init();

            load("select accession,id from mona.compound_bases", oldCompounds);
            load("select id,created::varchar from mona.compound_bases where created is not null", oldCreatedDates);
            load("select id,curated::varchar from mona.compound_bases where created is not null", oldCuratedDates);
            load("select id,updated::varchar from mona.compound_bases where created is not null", oldUpdatedDates);
            load("select id,spectrum::varchar from mona.compound_bases where spectrum is not null", oldSpectra);
            load("select id,splash from mona.compound_bases where splash is not null", oldSplashes);
            load("select id,ionization_mode from mona.compound_bases where ionization_mode is not null",
                    oldIonizationModes);
            load("select id,ionization_type from mona.compound_bases where ionization_type is not null",
                    oldIonizationTypes);
            load("select id,level from mona.compound_bases where level is not null", oldLevels);
            load("select id,library from mona.compound_bases where library is not null", oldCompoundLibraries);
            load("select id,submitter from mona.compound_bases where submitter is not null", oldCompoundSubmitters);
            load("select id,link from mona.compound_bases where link is not null", oldLibraryLinks);
            load("select compound,structure from mona.compound_structures", oldStructures);
            load("select compound,name from mona.compound_names", oldNames);
            load("select compound,class from mona.compound_classyfires", oldClassyFires);
            load("select compound,chebi from mona.compound_chebi_classes", oldChebiClasses);
            load("select compound,mesh from mona.compound_mesh_classes", oldMeshClasses);
            load("select compound,inchi,id from mona.compound_inchis", oldInchis);
            load("select compound,inchikey from mona.compound_inchikeys", oldInchiKeys);
            load("select compound,formula from mona.compound_formulas", oldFormulas);
            load("select compound,smiles from mona.compound_smileses", oldSmiles);
            load("select compound,mass from mona.compound_exact_masses", oldExactMasses);
            load("select compound,mass from mona.compound_monoisotopic_masses", oldMonoisotopicMasses);
            load("select compound,cas from mona.compound_cas_numbers", oldCasNumbers);
            load("select compound,hmdb from mona.compound_hmdb_ids", oldHmdbIdentifiers);
            load("select compound,chebi from mona.compound_chebi_ids", oldChebiIdentifiers);
            load("select compound,chemspider from mona.compound_chemspider_ids", oldChemspiderIdentifiers);
            load("select compound,kegg from mona.compound_kegg_ids", oldKeggIdentifiers);
            load("select compound,knapsack from mona.compound_knapsack_ids", oldKnapsackIdentifiers);
            load("select compound,lipidbank from mona.compound_lipidbank_ids", oldLipidBankIdentifiers);
            load("select compound,lipidmaps from mona.compound_lipidmaps_ids", oldLipidMapsIdentifiers);
            load("select compound,cid from mona.compound_pubchem_compound_ids", oldPubchemCompoundIdentifiers);
            load("select compound,sid from mona.compound_pubchem_substance_ids", oldPubchemSubstanceIdentifiers);
            load("select compound,peak,value,id from mona.spectrum_annotations", oldAnnotations);
            load("select compound,tag from mona.spectrum_tags", oldTags);
            load("select compound,entropy from mona.spectrum_normalized_entropies", oldNormalizedEntropies);
            load("select compound,entropy from mona.spectrum_spectral_entropies", oldSpectralEntropies);
            load("select compound,time,unit from mona.spectrum_retention_times", oldRetentionTimes);
            load("select compound,energy,unit from mona.spectrum_collision_energies", oldCollisionEnergies);
            load("select compound,ramp_start,ramp_end,unit from mona.spectrum_collision_energy_ramps",
                    oldCollisionEnergyRamps);
            load("select compound,type from mona.spectrum_instrument_types", oldInstrumentTypes);
            load("select compound,instrument from mona.spectrum_instruments", oldInstruments);
            load("select compound,type from mona.spectrum_precursor_types", oldPrecursorTypes);
            load("select compound,mz from mona.spectrum_precursor_mzs", oldPrecursorMZs);
            load("select name,id from mona.library_bases", oldLibraries);
            load("select id,description from mona.library_bases where description is not null", oldLibraryDescriptions);
            load("select email,first_name,last_name,institution,id from mona.submitter_bases", oldSubmitters);

            int nextCompoundID = oldCompounds.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;
            int nextLibraryID = oldLibraries.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;
            int nextSubmitterID = oldSubmitters.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;
            int nextInchiID = oldInchis.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;
            int nextAnnotationID = oldAnnotations.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;


            Map<String, ClassyFire> classyFires = loadClassyFires(baseDirectory + "mona/ChemOnt_2_1.obo");

            BufferedReader in = new BufferedReader(new FileReader(baseDirectory + "mona/MoNA-export-All_Spectra.json"));
            JsonReader reader = new JsonReader(in);

            reader.beginArray();

            while(reader.hasNext())
            {
                Spectrum item = new Gson().fromJson(reader, Spectrum.class);

                if(item.compound.length != 1)
                    throw new IOException();


                Integer id = keepCompounds.get(item.id);

                if(id == null)
                {

                    id = newCompounds.get(item.id);

                    if(id == null)
                    {

                        if((id = oldCompounds.remove(item.id)) == null)
                            newCompounds.put(item.id, id = nextCompoundID++);
                        else
                            keepCompounds.put(item.id, id);
                    }
                }


                if(item.dateCreated != null)
                {
                    String date = df.format(new Date((item.dateCreated)));

                    if(date.equals(oldCreatedDates.remove(id)))
                    {
                        keepCreatedDates.put(id, date);
                    }
                    else
                    {
                        String keep = keepCreatedDates.get(id);

                        if(!date.equals(keep))
                        {
                            if(keep != null)
                                throw new IOException();

                            Pair<String, String> put = newCreatedDates.put(id, Pair.getPair(item.id, date));

                            if(put != null && !date.equals(put.getTwo()))
                                throw new IOException();
                        }
                    }
                }


                if(item.lastCurated != null)
                {
                    String date = df.format(new Date((item.lastCurated)));

                    if(date.equals(oldCuratedDates.remove(id)))
                    {
                        keepCuratedDates.put(id, date);
                    }
                    else
                    {
                        String keep = keepCuratedDates.get(id);

                        if(!date.equals(keep))
                        {
                            if(keep != null)
                                throw new IOException();

                            Pair<String, String> put = newCuratedDates.put(id, Pair.getPair(item.id, date));

                            if(put != null && !date.equals(put.getTwo()))
                                throw new IOException();
                        }
                    }
                }


                if(item.lastUpdated != null)
                {
                    String date = df.format(new Date((item.lastUpdated)));

                    if(date.equals(oldUpdatedDates.remove(id)))
                    {
                        keepUpdatedDates.put(id, date);
                    }
                    else
                    {
                        String keep = keepUpdatedDates.get(id);

                        if(!date.equals(keep))
                        {
                            if(keep != null)
                                throw new IOException();

                            Pair<String, String> put = newUpdatedDates.put(id, Pair.getPair(item.id, date));

                            if(put != null && !date.equals(put.getTwo()))
                                throw new IOException();
                        }
                    }
                }


                String spectrum = item.spectrum.replaceFirst("^Scan:#[0-9]+ m/z:Intensity ", "").trim();

                if(spectrumCompare(spectrum, oldSpectra.remove(id)))
                {
                    keepSpectra.put(id, spectrum);
                }
                else
                {
                    String keep = keepSpectra.get(id);

                    if(!spectrumCompare(spectrum, keep))
                    {
                        if(keep != null)
                            throw new IOException();

                        Pair<String, String> put = newSpectra.put(id, Pair.getPair(item.id, spectrum));

                        if(put != null && !spectrum.equals(put.getTwo()))
                            throw new IOException();
                    }
                }


                if(item.splash != null)
                {
                    String splash = item.splash.splash;

                    if(splash.equals(oldSplashes.remove(id)))
                    {
                        keepSplashes.put(id, splash);
                    }
                    else
                    {
                        String keep = keepSplashes.get(id);

                        if(!splash.equals(keep))
                        {
                            if(keep != null)
                                throw new IOException();

                            Pair<String, String> put = newSplashes.put(id, Pair.getPair(item.id, splash));

                            if(put != null && !splash.equals(put.getTwo()))
                                throw new IOException();
                        }
                    }
                }


                if(item.compound[0].molFile != null && !item.compound[0].molFile.isEmpty())
                {
                    String structure = item.compound[0].molFile;

                    if(structure.equals(oldStructures.remove(id)))
                    {
                        keepStructures.put(id, structure);
                    }
                    else
                    {
                        String keep = keepStructures.get(id);

                        if(!structure.equals(keep))
                        {
                            if(keep != null)
                                throw new IOException();

                            String put = newStructures.put(id, structure);

                            if(put != null && !structure.equals(put))
                                throw new IOException();
                        }
                    }
                }


                for(Name name : item.compound[0].names)
                {
                    if(name.name.isEmpty())
                        continue;

                    Pair<Integer, String> pair = Pair.getPair(id, name.name);

                    if(oldNames.remove(pair))
                        keepNames.add(pair);
                    else if(!keepNames.contains(pair))
                        newNames.add(pair);
                }


                for(Classification c : item.compound[0].classification)
                {
                    if(c.name.matches("ClassyFire Query ID|predicted lipidmaps|substituents"))
                        continue;

                    ClassyFire cf = classyFires.get(c.value);

                    if(cf == null)
                        throw new IOException();

                    for(Integer chebi : cf.chebi)
                    {
                        Pair<Integer, Integer> pair = Pair.getPair(id, chebi);

                        if(oldChebiClasses.remove(pair))
                            keepChebiClasses.add(pair);
                        else if(!keepChebiClasses.contains(pair))
                            newChebiClasses.add(pair);
                    }

                    for(String mesh : cf.mesh)
                    {
                        Pair<Integer, String> pair = Pair.getPair(id, mesh);

                        if(oldMeshClasses.remove(pair))
                            keepMeshClasses.add(pair);
                        else if(!keepMeshClasses.contains(pair))
                            newMeshClasses.add(pair);
                    }

                    Pair<Integer, Integer> pair = Pair.getPair(id, cf.id);

                    if(oldClassyFires.remove(pair))
                        keepClassyFires.add(pair);
                    else if(!keepClassyFires.contains(pair))
                        newClassyFires.add(pair);
                }


                for(MetaData a : item.compound[0].metaData)
                {
                    if("theoretical adduct".equals(a.category))
                        continue;

                    switch(a.name)
                    {
                        case "InChI":
                        {
                            if(!a.value.matches("InChI=1[^ ]*"))
                                break;

                            Pair<Integer, String> pair = Pair.getPair(id, a.value);
                            Integer inchiID = oldInchis.remove(pair);

                            if(inchiID != null)
                                keepInchis.put(pair, inchiID);
                            else if(!keepInchis.containsKey(pair))
                                newInchis.put(pair, nextInchiID++);

                            break;
                        }

                        case "InChIKey":
                        {
                            if(!a.value.matches("[A-Z]{14}-[A-Z]{10}-[A-Z]"))
                                break;

                            Pair<Integer, String> pair = Pair.getPair(id, a.value);

                            if(oldInchiKeys.remove(pair))
                                keepInchiKeys.add(pair);
                            else if(!keepInchiKeys.contains(pair))
                                newInchiKeys.add(pair);

                            break;
                        }

                        case "molecular formula":
                        {
                            if(a.value.contains(" "))
                                break;

                            Pair<Integer, String> pair = Pair.getPair(id, a.value);

                            if(oldFormulas.remove(pair))
                                keepFormulas.add(pair);
                            else if(!keepFormulas.contains(pair))
                                newFormulas.add(pair);

                            break;
                        }

                        case "SMILES":
                        case "smiles":
                        {
                            for(String smiles : a.value.split(", +"))
                            {
                                if(!smiles.contains(" "))
                                {
                                    Pair<Integer, String> pair = Pair.getPair(id, smiles);

                                    if(oldSmiles.remove(pair))
                                        keepSmiles.add(pair);
                                    else if(!keepSmiles.contains(pair))
                                        newSmiles.add(pair);
                                }
                            }

                            break;
                        }

                        case "total exact mass":
                        case "exact mass":
                        {
                            Pair<Integer, Float> pair = Pair.getPair(id, Float.valueOf(a.value));

                            if(oldExactMasses.remove(pair))
                                keepExactMasses.add(pair);
                            else if(!keepExactMasses.contains(pair))
                                newExactMasses.add(pair);
                        }
                            break;

                        case "monoisotopic mass":
                        {
                            Pair<Integer, Float> pair = Pair.getPair(id, Float.valueOf(a.value));

                            if(oldMonoisotopicMasses.remove(pair))
                                keepMonoisotopicMasses.add(pair);
                            else if(!keepMonoisotopicMasses.contains(pair))
                                newMonoisotopicMasses.add(pair);
                        }
                            break;

                        case "cas":
                        case "cas number":
                        {
                            if(a.value.equals("n/a") || a.value.equals("NA") || a.value.equals(""))
                                break;

                            for(String cas : a.value.replaceAll("\\([^()]+\\)", "").split("[ ,]+"))
                            {
                                if(!cas.matches("[0-9]+-[0-9]+-[0-9]+"))
                                    continue;

                                Pair<Integer, String> pair = Pair.getPair(id, cas);

                                if(oldCasNumbers.remove(pair))
                                    keepCasNumbers.add(pair);
                                else if(!keepCasNumbers.contains(pair))
                                    newCasNumbers.add(pair);
                            }

                            break;
                        }

                        case "hmdb":
                        {
                            for(String hmdb : a.value.split("[ ,]+"))
                            {
                                Pair<Integer, String> pair = Pair.getPair(id, hmdb);

                                if(oldHmdbIdentifiers.remove(pair))
                                    keepHmdbIdentifiers.add(pair);
                                else if(!keepHmdbIdentifiers.contains(pair))
                                    newHmdbIdentifiers.add(pair);
                            }

                            break;
                        }

                        case "chebi":
                        {
                            for(String chebi : a.value.split("[ ,]+"))
                            {
                                Pair<Integer, Integer> pair = Pair.getPair(id,
                                        Integer.valueOf(chebi.replaceFirst("^CHEBI:", "")));

                                if(oldChebiIdentifiers.remove(pair))
                                    keepChebiIdentifiers.add(pair);
                                else if(!keepChebiIdentifiers.contains(pair))
                                    newChebiIdentifiers.add(pair);
                            }

                            break;
                        }

                        case "chemspider":
                        {
                            for(String chemspider : a.value.split(" "))
                            {
                                Pair<Integer, String> pair = Pair.getPair(id, chemspider);

                                if(oldChemspiderIdentifiers.remove(pair))
                                    keepChemspiderIdentifiers.add(pair);
                                else if(!keepChemspiderIdentifiers.contains(pair))
                                    newChemspiderIdentifiers.add(pair);
                            }

                            break;
                        }

                        case "kegg":
                        {
                            for(String kegg : a.value.split(" "))
                            {
                                if(!kegg.matches("[A-Z][0-9]{5}"))
                                    continue;

                                Pair<Integer, String> pair = Pair.getPair(id, kegg);

                                if(oldKeggIdentifiers.remove(pair))
                                    keepKeggIdentifiers.add(pair);
                                else if(!keepKeggIdentifiers.contains(pair))
                                    newKeggIdentifiers.add(pair);
                            }

                            break;
                        }

                        case "knapsack":
                        {
                            for(String knapsack : a.value.trim().replaceAll("\\([^()]+\\)", "").split("[ ;]+"))
                            {
                                if(!knapsack.matches("C[0-9]{8}"))
                                    continue;

                                Pair<Integer, String> pair = Pair.getPair(id, knapsack);

                                if(oldKnapsackIdentifiers.remove(pair))
                                    keepKnapsackIdentifiers.add(pair);
                                else if(!keepKnapsackIdentifiers.contains(pair))
                                    newKnapsackIdentifiers.add(pair);
                            }

                            break;
                        }

                        case "lipidbank":
                        {
                            for(String lipidbank : a.value.split(" "))
                            {
                                if(!lipidbank.matches("[A-Z]{3}[0-9]{4,5}"))
                                    continue;

                                Pair<Integer, String> pair = Pair.getPair(id, lipidbank);

                                if(oldLipidBankIdentifiers.remove(pair))
                                    keepLipidBankIdentifiers.add(pair);
                                else if(!keepLipidBankIdentifiers.contains(pair))
                                    newLipidBankIdentifiers.add(pair);
                            }

                            break;
                        }

                        case "lipidmaps":
                        {
                            for(String lipidmaps : a.value.split(" "))
                            {
                                if(!lipidmaps.matches("LM[A-Z0-9]+"))
                                    continue;

                                Pair<Integer, String> pair = Pair.getPair(id, lipidmaps);

                                if(oldLipidMapsIdentifiers.remove(pair))
                                    keepLipidMapsIdentifiers.add(pair);
                                else if(!keepLipidMapsIdentifiers.contains(pair))
                                    newLipidMapsIdentifiers.add(pair);
                            }

                            break;
                        }

                        case "pubchem":
                        case "PubChem":
                        case "PubChem ID":
                        case "pubmed id":
                        case "pubchem cid":
                        {
                            if(a.value.equals("n/a") || a.value.equals("na") || a.value.equals("NA")
                                    || a.value.equals(""))
                                break;

                            for(String pubchem : a.value.trim().replaceAll("ID:? ", "ID:").split(" "))
                            {
                                if(pubchem.matches("SID:?[0-9]+"))
                                {
                                    Integer sid = Integer.valueOf(pubchem.replaceFirst("^SID:?", ""));
                                    Pair<Integer, Integer> pair = Pair.getPair(id, sid);

                                    if(oldPubchemSubstanceIdentifiers.remove(pair))
                                        keepPubchemSubstanceIdentifiers.add(pair);
                                    else if(!keepPubchemSubstanceIdentifiers.contains(pair))
                                        newPubchemSubstanceIdentifiers.add(pair);
                                }
                                else if(pubchem.matches("(CID:?)?[0-9]+(\\.0)?"))
                                {
                                    Integer cid = Integer
                                            .valueOf(pubchem.replaceFirst("^(CID:?)?([0-9]+)(\\.0)?$", "$2"));
                                    Pair<Integer, Integer> pair = Pair.getPair(id, cid);

                                    if(oldPubchemCompoundIdentifiers.remove(pair))
                                        keepPubchemCompoundIdentifiers.add(pair);
                                    else if(!keepPubchemCompoundIdentifiers.contains(pair))
                                        newPubchemCompoundIdentifiers.add(pair);
                                }
                            }

                            break;
                        }

                        case "pubchem sid":
                        {
                            for(String pubchem : a.value.split(" "))
                            {
                                if(pubchem.matches("CID:[0-9]+"))
                                {
                                    Integer cid = Integer.valueOf(pubchem.replaceFirst("^CID:", ""));
                                    Pair<Integer, Integer> pair = Pair.getPair(id, cid);

                                    if(oldPubchemCompoundIdentifiers.remove(pair))
                                        keepPubchemCompoundIdentifiers.add(pair);
                                    else if(!keepPubchemCompoundIdentifiers.contains(pair))
                                        newPubchemCompoundIdentifiers.add(pair);
                                }
                                else
                                {
                                    Integer sid = Integer.valueOf(pubchem);
                                    Pair<Integer, Integer> pair = Pair.getPair(id, sid);

                                    if(oldPubchemSubstanceIdentifiers.remove(pair))
                                        keepPubchemSubstanceIdentifiers.add(pair);
                                    else if(!keepPubchemSubstanceIdentifiers.contains(pair))
                                        newPubchemSubstanceIdentifiers.add(pair);
                                }
                            }

                            break;
                        }

                        case "kind":
                        case "compound class":
                            // skip
                            break;

                        default:
                            System.err.println("unknovn item: " + a.name + " (" + a.category + ")");
                            break;
                    }
                }


                for(Annotation annotation : item.annotations)
                {
                    Pair<Integer, Annotation> pair = Pair.getPair(id, annotation);

                    Integer annotationID = oldAnnotations.remove(pair);

                    if(annotationID != null)
                        keepAnnotations.put(pair, annotationID);
                    else if(!keepAnnotations.containsKey(pair))
                        newAnnotations.put(pair, nextAnnotationID++);
                }


                for(Tag tag : item.tags)
                {
                    Pair<Integer, String> pair = Pair.getPair(id, tag.text);

                    if(oldTags.remove(pair))
                        keepTags.add(pair);
                    else if(!keepTags.contains(pair))
                        newTags.add(pair);
                }


                for(MetaData a : item.metaData)
                {
                    switch(a.name)
                    {
                        case "ionization mode":
                        {
                            String mode;

                            if(a.value.matches("[Nn]egative"))
                                mode = "N";
                            else if(a.value.matches("[Pp]ositive|POSITIVE"))
                                mode = "P";
                            else
                                break;

                            if(mode.equals(oldIonizationModes.remove(id)))
                            {
                                keepIonizationModes.put(id, mode);
                            }
                            else
                            {
                                String keep = keepIonizationModes.get(id);

                                if(!mode.equals(keep))
                                {
                                    if(keep != null)
                                        throw new IOException();

                                    Pair<String, String> put = newIonizationModes.put(id, Pair.getPair(item.id, mode));

                                    if(put != null && !mode.equals(put.getTwo()))
                                        throw new IOException();
                                }
                            }

                            break;
                        }

                        case "ionization": // a parameter of the source element
                        {
                            Integer value = null;

                            // childs of MS:1000008
                            switch(a.value)
                            {
                                case "ESI":
                                case "Electrospray Ionization (ESI)":
                                    value = 1000073;
                                    break;

                                case "MALDI":
                                case "Matrix Assisted Laser Desorption Ionization (MALDI)":
                                    value = 1000075;
                                    break;

                                case "CI":
                                case "Chemical Ionization (CI)":
                                    value = 1000071;
                                    break;

                                case "EI":
                                case "Electron Impact (EI)":
                                    value = 1000389;
                                    break;

                                case "FAB":
                                    value = 1000074;
                                    break;

                                case "APCI":
                                    value = 1000070;
                                    break;

                                case "nano-ESI": //4x
                                    value = 1000398;
                                    break;

                                case "FI"://6x
                                    value = 1000258;
                                    break;

                                case "SSI (Sonic spray ionization)":
                                case "SIMS":
                                default:
                                    break;
                            }

                            if(value == null)
                                break;

                            if(value.equals(oldIonizationTypes.remove(id)))
                            {
                                keepIonizationTypes.put(id, value);
                            }
                            else
                            {
                                Integer keep = keepIonizationTypes.get(id);

                                if(!value.equals(keep))
                                {
                                    if(keep != null)
                                        throw new IOException();

                                    Pair<String, Integer> put = newIonizationTypes.put(id,
                                            Pair.getPair(item.id, value));

                                    if(put != null && !value.equals(put.getTwo()))
                                        throw new IOException();
                                }
                            }

                            break;
                        }

                        case "ms level":
                        {
                            if(a.value.matches("MS[1-5]-MS[1-5] Composite|MS"))
                                break;

                            Integer level = Integer.valueOf(a.value.replaceFirst("^MS", ""));

                            if(level.equals(oldLevels.remove(id)))
                            {
                                keepLevels.put(id, level);
                            }
                            else
                            {
                                Integer keep = keepLevels.get(id);

                                if(!level.equals(keep))
                                {
                                    if(keep != null)
                                        throw new IOException();

                                    Pair<String, Integer> put = newLevels.put(id, Pair.getPair(item.id, level));

                                    if(put != null && !level.equals(put.getTwo()))
                                        throw new IOException();
                                }
                            }

                            break;
                        }

                        case "normalized entropy":
                        {
                            Pair<Integer, Float> pair = Pair.getPair(id, Float.valueOf(a.value));

                            if(oldNormalizedEntropies.remove(pair))
                                keepNormalizedEntropies.add(pair);
                            else if(!keepNormalizedEntropies.contains(pair))
                                newNormalizedEntropies.add(pair);

                            break;
                        }

                        case "spectral entropy":
                        {
                            Pair<Integer, Float> pair = Pair.getPair(id, Float.valueOf(a.value));

                            if(oldSpectralEntropies.remove(pair))
                                keepSpectralEntropies.add(pair);
                            else if(!keepSpectralEntropies.contains(pair))
                                newSpectralEntropies.add(pair);

                            break;
                        }

                        case "retention time":
                        {
                            if(a.value.matches("N/A min|nan|-1|CCS:"))
                                break;

                            String time = a.value.replaceFirst(" \\((in paper|MSMS).*", "");
                            Pair<Float, Integer> value;

                            if(time.matches("[0-9]*(\\.[0-9]+)? ?min(utes)?")) // obo:UO_0000031
                                value = Pair.getPair(Float.valueOf(time.replaceFirst(" ?m.*", "")), 31);
                            else if(time.matches("[0-9]*(\\.[0-9]+)? +s(ec)?")) // obo:UO_0000010
                                value = Pair.getPair(Float.valueOf(time.replaceFirst(" .*", "")), 10);
                            else if(time.matches("[0-9.]+-[0-9.]+ min"))
                                break;//skip
                            else
                                value = Pair.getPair(Float.valueOf(time), 0); // null

                            Pair<Integer, Pair<Float, Integer>> pair = Pair.getPair(id, value);

                            if(oldRetentionTimes.remove(pair))
                                keepRetentionTimes.add(pair);
                            else if(!keepRetentionTimes.contains(pair))
                                newRetentionTimes.add(pair);

                            break;
                        }

                        case "collision energy":
                        {
                            if(a.value.equals("") || a.value.equals("--"))
                                break;

                            if(a.value.startsWith("Ramp") || a.value.startsWith("RAMP") || a.value.matches(".*[-–]>.*"))
                            {
                                if(a.value.equals("Ramp 22,9-34.3 eV") || a.value.equals("Ramp 17.2-25.8 eV 30 eV"))
                                    break;

                                String str = a.value.replaceFirst("^(Ramp|RAMP) ", "").replaceFirst(" *(eV|V|%) *$",
                                        "");
                                String[] vals = str.split("[-–]>|-", 2);
                                Pair<Float, Float> ramp = Pair.getPair(Float.valueOf(vals[0]), Float.valueOf(vals[1]));

                                Pair<Pair<Float, Float>, Integer> value;

                                if(a.value.matches(".*[0-9 ]eV *$"))
                                    value = Pair.getPair(ramp, 266);
                                else if(a.value.matches(".*[0-9 ]V *$"))
                                    value = Pair.getPair(ramp, 218);
                                else if(a.value.matches(".*% *$"))
                                    value = Pair.getPair(ramp, 190);
                                else
                                    throw new IOException();

                                Pair<Integer, Pair<Pair<Float, Float>, Integer>> pair = Pair.getPair(id, value);

                                if(oldCollisionEnergyRamps.remove(pair))
                                    keepCollisionEnergyRamps.add(pair);
                                else if(!keepCollisionEnergyRamps.contains(pair))
                                    newCollisionEnergyRamps.add(pair);
                            }
                            else
                            {
                                Pair<Float, Integer> value = null;

                                if(a.value.matches("-?[0-9.]+ ?e[Vv] ?( [FI]T-MS( II)?)?")) // obo:UO_0000266
                                    value = Pair.getPair(Float.valueOf(a.value.replaceFirst(" ?e[Vv].*", "")), 266);
                                else if(a.value.matches("-?[0-9.]+ ?V")) // obo:UO_0000218
                                    value = Pair.getPair(Float.valueOf(a.value.replaceFirst(" ?V", "")), 218);
                                else if(a.value.matches("[0-9]+ ?kV")) // obo:UO_0000248
                                    value = Pair.getPair(Float.valueOf(a.value.replaceFirst(" ?kV", "")), 248);
                                else if(a.value.matches("[0-9]+ +\\(nominal\\)"))
                                    value = Pair.getPair(Float.valueOf(a.value.replaceFirst(" .*", "")), 0);
                                else if(a.value.matches("CE[0-9]+"))
                                    value = Pair.getPair(Float.valueOf(a.value.replaceFirst("^CE", "")), 0);
                                else if(a.value.matches("-?[0-9.]+"))
                                    value = Pair.getPair(Float.valueOf(a.value), 0);
                                else if(a.value.matches("[0-9.]+ ?%( \\(nominal\\))?")) // obo:UO_0000190
                                    value = Pair.getPair(Float.valueOf(a.value.replaceFirst(" ?%.*", "")), 190);
                                else if(a.value.matches("[0-9.]+ *\\(?NCE\\)?")) // obo:UO_0000190
                                    value = Pair.getPair(Float.valueOf(a.value.replaceFirst(" *\\(?NCE\\)?", "")), 190);
                                else if(a.value.matches("NCE [0-9]+%")) // obo:UO_0000190
                                    value = Pair.getPair(Float.valueOf(a.value.replaceFirst("NCE (.*)%", "$1")), 190);
                                else
                                    break;

                                Pair<Integer, Pair<Float, Integer>> pair = Pair.getPair(id, value);

                                if(oldCollisionEnergies.remove(pair))
                                    keepCollisionEnergies.add(pair);
                                else if(!keepCollisionEnergies.contains(pair))
                                    newCollisionEnergies.add(pair);
                            }

                            break;
                        }

                        case "instrument type":
                        {
                            Pair<Integer, String> pair = Pair.getPair(id, a.value);

                            if(oldInstrumentTypes.remove(pair))
                                keepInstrumentTypes.add(pair);
                            else if(!keepInstrumentTypes.contains(pair))
                                newInstrumentTypes.add(pair);

                            break;
                        }

                        case "instrument":
                        {
                            Pair<Integer, String> pair = Pair.getPair(id, a.value);

                            if(oldInstruments.remove(pair))
                                keepInstruments.add(pair);
                            else if(!keepInstruments.contains(pair))
                                newInstruments.add(pair);

                            break;
                        }

                        case "precursor type":
                        {
                            Pair<Integer, String> pair = Pair.getPair(id, a.value);

                            if(oldPrecursorTypes.remove(pair))
                                keepPrecursorTypes.add(pair);
                            else if(!keepPrecursorTypes.contains(pair))
                                newPrecursorTypes.add(pair);

                            break;
                        }

                        case "precursor m/z":
                        {
                            Pair<Integer, Float> pair = Pair.getPair(id, Float.valueOf(a.value.replaceFirst(",", ".")));

                            if(oldPrecursorMZs.remove(pair))
                                keepPrecursorMZs.add(pair);
                            else if(!keepPrecursorMZs.contains(pair))
                                newPrecursorMZs.add(pair);

                            break;
                        }
                    }
                }


                if(item.library != null)
                {
                    Integer library = keepLibraries.get(item.library.library);

                    if(library == null)
                    {

                        library = newLibraries.get(item.library.library);

                        if(library == null)
                        {
                            if((library = oldLibraries.remove(item.library.library)) == null)
                                newLibraries.put(item.library.library, library = nextLibraryID++);
                            else
                                keepLibraries.put(item.library.library, library);
                        }
                    }


                    if(library.equals(oldCompoundLibraries.remove(id)))
                    {
                        keepCompoundLibraries.put(id, library);
                    }
                    else
                    {
                        Integer keep = keepCompoundLibraries.get(id);

                        if(!library.equals(keep))
                        {
                            if(keep != null)
                                throw new IOException();

                            Pair<String, Integer> put = newCompoundLibraries.put(id, Pair.getPair(item.id, library));

                            if(put != null && !library.equals(put.getTwo()))
                                throw new IOException();
                        }
                    }


                    String description = item.library.description;

                    if(description.equals(oldLibraryDescriptions.remove(library)))
                    {
                        keepLibraryDescriptions.put(library, description);
                    }
                    else
                    {
                        String keep = keepLibraryDescriptions.get(library);

                        if(!description.equals(keep))
                        {
                            if(keep != null)
                                throw new IOException();

                            Pair<String, String> put = newLibraryDescriptions.put(library,
                                    Pair.getPair(item.library.library, description));

                            if(put != null && !description.equals(put.getTwo()))
                                throw new IOException();
                        }
                    }


                    if(!item.library.link.isEmpty())
                    {
                        String link = item.library.link;

                        if(link.equals(oldLibraryLinks.remove(id)))
                        {
                            keepLibraryLinks.put(id, link);
                        }
                        else
                        {
                            String keep = keepLibraryLinks.get(id);

                            if(!link.equals(keep))
                            {
                                if(keep != null)
                                    throw new IOException();

                                Pair<String, String> put = newLibraryLinks.put(id, Pair.getPair(item.id, link));

                                if(put != null && !link.equals(put.getTwo()))
                                    throw new IOException();
                            }
                        }
                    }
                }


                if(item.submitter != null)
                {
                    Integer submitter = keepSubmitters.get(item.submitter);

                    if(submitter == null)
                    {
                        submitter = newSubmitters.get(item.submitter);

                        if(submitter == null)
                        {
                            if((submitter = oldSubmitters.remove(item.submitter)) == null)
                                newSubmitters.put(item.submitter, submitter = nextSubmitterID++);
                            else
                                keepSubmitters.put(item.submitter, submitter);
                        }
                    }


                    if(submitter.equals(oldCompoundSubmitters.remove(id)))
                    {
                        keepCompoundSubmitters.put(id, submitter);
                    }
                    else
                    {
                        Integer keep = keepCompoundSubmitters.get(id);

                        if(!submitter.equals(keep))
                        {
                            if(keep != null)
                                throw new IOException();

                            Pair<String, Integer> put = newCompoundSubmitters.put(id, Pair.getPair(item.id, submitter));

                            if(put != null && !submitter.equals(put.getTwo()))
                                throw new IOException();
                        }
                    }
                }
            }

            reader.endArray();


            store("delete from mona.compound_bases where accession=? and id=?", oldCompounds);
            store("insert into mona.compound_bases(accession,id) values(?,?)", newCompounds);

            store("update mona.compound_bases set created=null where id=? and created=?::date", oldCreatedDates);
            store("insert into mona.compound_bases(id,accession,created) values(?,?,?::date) "
                    + "on conflict(id) do update set created=EXCLUDED.created", newCreatedDates);

            store("update mona.compound_bases set curated=null where id=? and curated=?::date", oldCuratedDates);
            store("insert into mona.compound_bases(id,accession,curated) values(?,?,?::date) "
                    + "on conflict(id) do update set curated=EXCLUDED.curated", newCuratedDates);

            store("update mona.compound_bases set updated=null where id=? and updated=?::date", oldUpdatedDates);
            store("insert into mona.compound_bases(id,accession,updated) values(?,?,?::date) "
                    + "on conflict(id) do update set updated=EXCLUDED.updated", newUpdatedDates);

            store("update mona.compound_bases set spectrum=null where id=? and spectrum=?::pgms.spectrum", oldSpectra);
            store("insert into mona.compound_bases(id,accession,spectrum) values(?,?,?::pgms.spectrum) "
                    + "on conflict(id) do update set spectrum=EXCLUDED.spectrum", newSpectra);

            store("update mona.compound_bases set splash=null where id=? and splash=?", oldSplashes);
            store("insert into mona.compound_bases(id,accession,splash) values(?,?,?) "
                    + "on conflict(id) do update set splash=EXCLUDED.splash", newSplashes);

            store("update mona.compound_bases set ionization_mode=null where id=? and ionization_mode=?",
                    oldIonizationModes);
            store("insert into mona.compound_bases(id,accession,ionization_mode) values(?,?,?) "
                    + "on conflict(id) do update set ionization_mode=EXCLUDED.ionization_mode", newIonizationModes);

            store("update mona.compound_bases set ionization_type=null where id=? and ionization_type=?",
                    oldIonizationTypes);
            store("insert into mona.compound_bases(id,accession,ionization_type) values(?,?,?) "
                    + "on conflict(id) do update set ionization_type=EXCLUDED.ionization_type", newIonizationTypes);

            store("update mona.compound_bases set level=null where id=? and level=?", oldLevels);
            store("insert into mona.compound_bases(id,accession,level) values(?,?,?) "
                    + "on conflict(id) do update set level=EXCLUDED.level", newLevels);

            store("update mona.compound_bases set library=null where id=? and library=?", oldCompoundLibraries);
            store("insert into mona.compound_bases(id,accession,library) values(?,?,?) "
                    + "on conflict(id) do update set library=EXCLUDED.library", newCompoundLibraries);

            store("update mona.compound_bases set submitter=null where id=? and submitter=?", oldCompoundSubmitters);
            store("insert into mona.compound_bases(id,accession,submitter) values(?,?,?) "
                    + "on conflict(id) do update set submitter=EXCLUDED.submitter", newCompoundSubmitters);

            store("update mona.compound_bases set link=null where id=? and link=?", oldLibraryLinks);
            store("insert into mona.compound_bases(id,accession,link) values(?,?,?) "
                    + "on conflict(id) do update set link=EXCLUDED.link", newLibraryLinks);

            store("delete from mona.compound_structures where compound=? and structure=?", oldStructures);
            store("insert into mona.compound_structures(compound,structure) values(?,?) "
                    + "on conflict(compound) do update set structure=EXCLUDED.structure", newStructures);

            store("delete from mona.compound_names where compound=? and name=?", oldNames);
            store("insert into mona.compound_names(compound,name) values(?,?)", newNames);

            store("delete from mona.compound_classyfires where compound=? and class=?", oldClassyFires);
            store("insert into mona.compound_classyfires(compound,class) values(?,?)", newClassyFires);

            store("delete from mona.compound_chebi_classes where compound=? and chebi=?", oldChebiClasses);
            store("insert into mona.compound_chebi_classes(compound,chebi) values(?,?)", newChebiClasses);

            store("delete from mona.compound_mesh_classes where compound=? and mesh=?", oldMeshClasses);
            store("insert into mona.compound_mesh_classes(compound,mesh) values(?,?)", newMeshClasses);

            store("delete from mona.compound_inchis where compound=? and inchi=? and id=?", oldInchis);
            store("insert into mona.compound_inchis(compound,inchi,id) values(?,?,?)", newInchis);

            store("delete from mona.compound_inchikeys where compound=? and inchikey=?", oldInchiKeys);
            store("insert into mona.compound_inchikeys(compound,inchikey) values(?,?)", newInchiKeys);

            store("delete from mona.compound_formulas where compound=? and formula=?", oldFormulas);
            store("insert into mona.compound_formulas(compound,formula) values(?,?)", newFormulas);

            store("delete from mona.compound_smileses where compound=? and smiles=?", oldSmiles);
            store("insert into mona.compound_smileses(compound,smiles) values(?,?)", newSmiles);

            store("delete from mona.compound_exact_masses where compound=? and mass=?", oldExactMasses);
            store("insert into mona.compound_exact_masses(compound,mass) values(?,?)", newExactMasses);

            store("delete from mona.compound_monoisotopic_masses where compound=? and mass=?", oldMonoisotopicMasses);
            store("insert into mona.compound_monoisotopic_masses(compound,mass) values(?,?)", newMonoisotopicMasses);

            store("delete from mona.compound_cas_numbers where compound=? and cas=?", oldCasNumbers);
            store("insert into mona.compound_cas_numbers(compound,cas) values(?,?)", newCasNumbers);

            store("delete from mona.compound_hmdb_ids where compound=? and hmdb=?", oldHmdbIdentifiers);
            store("insert into mona.compound_hmdb_ids(compound,hmdb) values(?,?)", newHmdbIdentifiers);

            store("delete from mona.compound_chebi_ids where compound=? and chebi=?", oldChebiIdentifiers);
            store("insert into mona.compound_chebi_ids(compound,chebi) values(?,?)", newChebiIdentifiers);

            store("delete from mona.compound_chemspider_ids where compound=? and chemspider=?",
                    oldChemspiderIdentifiers);
            store("insert into mona.compound_chemspider_ids(compound,chemspider) values(?,?)",
                    newChemspiderIdentifiers);

            store("delete from mona.compound_kegg_ids where compound=? and kegg=?", oldKeggIdentifiers);
            store("insert into mona.compound_kegg_ids(compound,kegg) values(?,?)", newKeggIdentifiers);

            store("delete from mona.compound_knapsack_ids where compound=? and knapsack=?", oldKnapsackIdentifiers);
            store("insert into mona.compound_knapsack_ids(compound,knapsack) values(?,?)", newKnapsackIdentifiers);

            store("delete from mona.compound_lipidbank_ids where compound=? and lipidbank=?", oldLipidBankIdentifiers);
            store("insert into mona.compound_lipidbank_ids(compound,lipidbank) values(?,?)", newLipidBankIdentifiers);

            store("delete from mona.compound_lipidmaps_ids where compound=? and lipidmaps=?", oldLipidMapsIdentifiers);
            store("insert into mona.compound_lipidmaps_ids(compound,lipidmaps) values(?,?)", newLipidMapsIdentifiers);

            store("delete from mona.compound_pubchem_compound_ids where compound=? and cid=?",
                    oldPubchemCompoundIdentifiers);
            store("insert into mona.compound_pubchem_compound_ids(compound,cid) values(?,?)",
                    newPubchemCompoundIdentifiers);

            store("delete from mona.compound_pubchem_substance_ids where compound=? and sid=?",
                    oldPubchemSubstanceIdentifiers);
            store("insert into mona.compound_pubchem_substance_ids(compound,sid) values(?,?)",
                    newPubchemSubstanceIdentifiers);

            store("delete from mona.spectrum_annotations where compound=? and peak=? and value=? and id=?",
                    oldAnnotations);
            store("insert into mona.spectrum_annotations(compound,peak,value,id) values(?,?,?,?)", newAnnotations);

            store("delete from mona.spectrum_tags where compound=? and tag=?", oldTags);
            store("insert into mona.spectrum_tags(compound,tag) values(?,?)", newTags);


            store("delete from mona.spectrum_normalized_entropies where compound=? and entropy=?",
                    oldNormalizedEntropies);
            store("insert into mona.spectrum_normalized_entropies(compound,entropy) values(?,?)",
                    newNormalizedEntropies);

            store("delete from mona.spectrum_spectral_entropies where compound=? and entropy=?", oldSpectralEntropies);
            store("insert into mona.spectrum_spectral_entropies(compound,entropy) values(?,?)", newSpectralEntropies);

            store("delete from mona.spectrum_retention_times where compound=? and time=? and unit=?",
                    oldRetentionTimes);
            store("insert into mona.spectrum_retention_times(compound,time,unit) values(?,?,?)", newRetentionTimes);

            store("delete from mona.spectrum_collision_energies where compound=? and energy=? and unit=?",
                    oldCollisionEnergies);
            store("insert into mona.spectrum_collision_energies(compound,energy,unit) values(?,?,?)",
                    newCollisionEnergies);

            store("delete from mona.spectrum_collision_energy_ramps where compound=? and ramp_start=? and ramp_end=? and unit=?",
                    oldCollisionEnergyRamps);
            store("insert into mona.spectrum_collision_energy_ramps(compound,ramp_start,ramp_end,unit) values(?,?,?,?)",
                    newCollisionEnergyRamps);

            store("delete from mona.spectrum_instrument_types where compound=? and type=?", oldInstrumentTypes);
            store("insert into mona.spectrum_instrument_types(compound,type) values(?,?)", newInstrumentTypes);

            store("delete from mona.spectrum_instruments where compound=? and instrument=?", oldInstruments);
            store("insert into mona.spectrum_instruments(compound,instrument) values(?,?)", newInstruments);

            store("delete from mona.spectrum_precursor_types where compound=? and type=?", oldPrecursorTypes);
            store("insert into mona.spectrum_precursor_types(compound,type) values(?,?)", newPrecursorTypes);

            store("delete from mona.spectrum_precursor_mzs where compound=? and mz=?", oldPrecursorMZs);
            store("insert into mona.spectrum_precursor_mzs(compound,mz) values(?,?)", newPrecursorMZs);

            store("delete from mona.library_bases where name=? and id=?", oldLibraries);
            store("insert into mona.library_bases(name,id) values(?,?)", newLibraries);

            store("update mona.library_bases set description=null where id=? and description=?",
                    oldLibraryDescriptions);
            store("insert into mona.library_bases(id,name,description) values(?,?,?) "
                    + "on conflict(id) do update set description=EXCLUDED.description", newLibraryDescriptions);

            store("delete from mona.submitter_bases where email=? and first_name=? and last_name=? and institution=? and id=?",
                    oldSubmitters);
            store("insert into mona.submitter_bases(email,first_name,last_name,institution,id) values(?,?,?,?,?)",
                    newSubmitters);

            updateVersion();
            commit();
        }
        catch(Throwable e)
        {
            throw e;
        }
    }


    private static boolean spectrumCompare(String s1, String s2)
    {
        class Element implements Comparable<Element>
        {
            float mz;
            float intenzity;

            Element(String s)
            {
                mz = Float.parseFloat(s.replaceFirst(":.*", ""));
                intenzity = Float.parseFloat(s.replaceFirst(".*:", ""));
            }

            @Override
            public int compareTo(Element obj)
            {
                return mz == obj.mz ? Float.compare(intenzity, obj.intenzity) : Float.compare(mz, obj.mz);
            }

            @Override
            public boolean equals(Object obj)
            {
                return mz == ((Element) obj).mz && intenzity == ((Element) obj).intenzity;
            }

            @Override
            public String toString()
            {
                return mz + ":" + intenzity;
            }
        }


        if(s1 == null && s2 == null)
            return true;

        if(s1 == null || s2 == null)
            return false;

        List<Element> l1 = Arrays.stream(s1.split(" +")).map(Element::new).sorted().collect(Collectors.toList());
        List<Element> l2 = Arrays.stream(s2.split(" +")).map(Element::new).sorted().collect(Collectors.toList());

        return l1.equals(l2);
    }
}
